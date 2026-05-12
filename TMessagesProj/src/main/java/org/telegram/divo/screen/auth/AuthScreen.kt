package org.telegram.divo.screen.auth

import android.content.Context
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.exoplayer2.util.Log
import kotlinx.coroutines.launch
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.UserConfig

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onAuthClicked: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            launch {
                when (effect) {
                    is AuthViewEffect.ShowError -> {
                        Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                    }
                    is AuthViewEffect.LoginSuccess -> {
                        onAuthClicked()
                    }
                }
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LottieProgressIndicator(modifier = Modifier.size(32.dp))
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            Text(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp),
                text = "Welcome to DIVO".uppercase(),
                fontSize = 32.sp,
                style = AppTheme.typography.helveticaNeueLtCom
            )
            val currentAccount = UserConfig.selectedAccount
            Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 8.dp)) {
                UIButtonNew(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Login (Model)",
                    onClick = {
                        // ==========================================
                        // 2. Пользователь всё сделал. Ставим флаг в TRUE
                        val prefs = context.getSharedPreferences("divo_auth", Context.MODE_PRIVATE)
                        val userId = UserConfig.getInstance(currentAccount).clientUserId
                        prefs.edit { putBoolean("auth_completed_$userId", true) }
                        // ==========================================

                        // Переходим на главный экран
                        viewModel.setIntent(
                            AuthViewIntent.Login(
                                email = "elenared720@gmail.com",
                                password = "Qwerty#123",
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                UIButtonNew(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Login (Agency)",
                    onClick = {
                        // ==========================================
                        // 2. Пользователь всё сделал. Ставим флаг в TRUE
                        val prefs = context.getSharedPreferences("divo_auth", Context.MODE_PRIVATE)
                        val userId = UserConfig.getInstance(currentAccount).clientUserId
                        prefs.edit { putBoolean("auth_completed_$userId", true) }
                        // ==========================================

                        // Переходим на главный экран
                        viewModel.setIntent(
                            AuthViewIntent.Login(
                                email = "chiva_gp2022@icloud.com", //elenared720@gmail.com
                                password = "Qwerty#123",
                            )
                        )
                    }
                )
            }
        }
    }
}