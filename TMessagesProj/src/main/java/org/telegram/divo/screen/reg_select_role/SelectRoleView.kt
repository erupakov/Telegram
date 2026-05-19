package org.telegram.divo.screen.reg_select_role

import android.content.Context
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.NavController
import org.telegram.ui.Components.SlideView

class RoleSelectionView(context: Context) : SlideView(context) {

    private var rolesNavController: NavController? = null
    private var currentPhone: String? = null
    private var currentPhoneHash: String? = null

    var onBack: (() -> Unit)? = null
    var onFinish: (() -> Unit)? = null

    init {
        orientation = VERTICAL

        val composeView = ComposeView(context).apply {
            // Чтобы ComposeView корректно уничтожался вместе со SlideView
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                RoleNavGraph(
                    onNavControllerReady = { navController ->
                        this@RoleSelectionView.rolesNavController = navController
                    },
                    onBackToPhone = { onBack?.invoke() },
                    onFinishedFlow = { onFinish?.invoke() }
                )
            }
        }

        // Добавляем ComposeView в SlideView, чтобы он занял весь экран
        addView(composeView, org.telegram.ui.Components.LayoutHelper.createLinear(
            org.telegram.ui.Components.LayoutHelper.MATCH_PARENT,
            org.telegram.ui.Components.LayoutHelper.MATCH_PARENT
        ))
    }

    // Telegram дергает этот метод при передаче данных с предыдущего экрана (например, после ввода кода)
    override fun setParams(params: Bundle?, restore: Boolean) {
        super.setParams(params, restore)
        if (params != null) {
            currentPhone = params.getString("phoneFormated")
            currentPhoneHash = params.getString("phoneHash")
        }
    }

    override fun getHeaderName(): String {
        return "Registration"
    }

    override fun needBackButton(): Boolean {
        return false
    }

    override fun onBackPressed(force: Boolean): Boolean {
        val nav = rolesNavController
        // Если внутри NavHost есть куда возвращаться — возвращаемся и блокируем закрытие SlideView
        if (nav != null && nav.previousBackStackEntry != null) {
            nav.popBackStack()
            return false // false значит "событие обработано внутри, закрывать экран не надо"
        }

        // Иначе позволяем Telegram-у вернуться на экран ввода номера
        return super.onBackPressed(force)
    }



    // Метод, который будет дергать Telegram API для регистрации
//    private fun submitRegistration(data: SignUpData) {
//        // Здесь ты собираешь запрос так же, как это было в LoginActivityRegisterModelView
//        // Например:
//        /*
//        val req = TLRPC.TL_auth_signUp().apply {
//            phone_code_hash = currentPhoneHash
//            phone_number = currentPhone
//            first_name = data.firstName
//            last_name = data.lastName
//
//            // Если нужно добавить твою модель данных
//            // model_info = ...
//        }
//
//        loginActivity.needShowProgress(0)
//        ConnectionsManager.getInstance(loginActivity.currentAccount).sendRequest(...) {
//            // Обработка ответа как в старом коде
//        }
//        */
//    }
}

//data class SignUpData(
//    val role: Role,
//    val firstName: String,
//    val lastName: String,
//    val age: Int,
//    val gender: Int,
//)
