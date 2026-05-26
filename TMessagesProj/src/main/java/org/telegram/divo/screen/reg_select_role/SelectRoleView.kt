package org.telegram.divo.screen.reg_select_role

import android.content.Context
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.telegram.divo.style.setDivoContent
import org.telegram.ui.Components.SlideView

class RoleSelectionView(context: Context, private val account: Int) : SlideView(context) {

    private var rolesNavController: NavController? = null
    private var currentPhone by mutableStateOf("")
    private var currentPhoneHash by mutableStateOf("")
    private var currentFirebaseUid: String? by mutableStateOf(null)
    private var currentGoogleEmail: String? by mutableStateOf(null)

    var onBack: (() -> Unit)? = null
    var onFinish: ((org.telegram.tgnet.TLRPC.TL_auth_authorization) -> Unit)? = null

    init {
        orientation = VERTICAL

        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setDivoContent {
                RoleNavGraph(
                    currentAccount = account,
                    phoneHash = currentPhoneHash,
                    phoneNumber = currentPhone,
                    firebaseUid = currentFirebaseUid,
                    googleEmail = currentGoogleEmail,
                    onNavControllerReady = { navController ->
                        this@RoleSelectionView.rolesNavController = navController
                    },
                    onBackToPhone = { onBack?.invoke() },
                    onFinishedFlow = { data -> onFinish?.invoke(data) }
                )
            }
        }

        addView(composeView, org.telegram.ui.Components.LayoutHelper.createLinear(
            org.telegram.ui.Components.LayoutHelper.MATCH_PARENT,
            org.telegram.ui.Components.LayoutHelper.MATCH_PARENT
        ))
    }

    override fun setParams(params: Bundle?, restore: Boolean) {
        super.setParams(params, restore)
        if (params != null) {
            currentPhone = params.getString("phoneFormated", "")
            currentPhoneHash = params.getString("phoneHash", "")
            currentFirebaseUid = params.getString("firebaseUid")
            currentGoogleEmail = params.getString("googleEmail")
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

        if (nav != null && nav.previousBackStackEntry != null) {
            nav.popBackStack()
            return false
        }

        return super.onBackPressed(force)
    }
}

