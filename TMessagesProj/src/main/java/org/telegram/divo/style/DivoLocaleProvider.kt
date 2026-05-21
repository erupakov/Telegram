package org.telegram.divo.style

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import org.telegram.messenger.LocaleController
import org.telegram.messenger.NotificationCenter
import java.util.Locale

/**
 * Оборачивает Compose-дерево в контекст с правильной локалью.
 *
 * Слушает [NotificationCenter.reloadInterface] — именно это событие Telegram
 * публикует после смены языка в LanguageSelectActivity. При получении события
 * пересоздаёт локализованный контекст, что заставляет Compose перечитать
 * stringResource() с нужной локалью.
 *
 * Контекст оборачивается через [ContextWrapper], чтобы сохранить
 * ViewModelStoreOwner, LifecycleOwner и другие интерфейсы Activity.
 */
@Composable
fun DivoLocaleProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current

    // Текущая локаль Telegram (или системная как fallback)
    var currentLocale by remember {
        mutableStateOf(
            LocaleController.getInstance()?.getCurrentLocale() ?: Locale.getDefault()
        )
    }

    // Подписываемся на событие смены языка в Telegram
    DisposableEffect(Unit) {
        val observer = NotificationCenter.NotificationCenterDelegate { id, _, _ ->
            if (id == NotificationCenter.reloadInterface) {
                val newLocale = LocaleController.getInstance()?.getCurrentLocale() ?: Locale.getDefault()
                currentLocale = newLocale
            }
        }
        NotificationCenter.getGlobalInstance().addObserver(observer, NotificationCenter.reloadInterface)
        onDispose {
            NotificationCenter.getGlobalInstance().removeObserver(observer, NotificationCenter.reloadInterface)
        }
    }

    // ContextWrapper подменяет только getResources(), сохраняя
    // ViewModelStoreOwner, LifecycleOwner и прочие интерфейсы Activity.
    val localizedContext = remember(currentLocale, context) {
        val config = Configuration(context.resources.configuration).apply {
            setLocale(currentLocale)
        }
        val localizedResources = context.createConfigurationContext(config).resources
        object : ContextWrapper(context) {
            override fun getResources(): Resources = localizedResources
        }
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        content()
    }
}

/**
 * Extension-обёртка: вызывает setContent с DivoLocaleProvider внутри.
 * Используйте вместо обычного setContent во всех Divo-фрагментах,
 * чтобы stringResource() автоматически подхватывал язык из настроек Telegram.
 */
fun androidx.compose.ui.platform.ComposeView.setDivoContent(
    content: @Composable () -> Unit
) {
    setContent {
        DivoLocaleProvider {
            content()
        }
    }
}
