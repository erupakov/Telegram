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
    private var currentGoogleFirstName: String? by mutableStateOf(null)
    private var currentGoogleLastName: String? by mutableStateOf(null)
    private var currentGooglePhotoUrl: String? by mutableStateOf(null)

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
                    googleFirstName = currentGoogleFirstName,
                    googleLastName = currentGoogleLastName,
                    googlePhotoUrl = currentGooglePhotoUrl,
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
            currentGoogleFirstName = params.getString("googleFirstName")
            currentGoogleLastName = params.getString("googleLastName")
            currentGooglePhotoUrl = params.getString("googlePhotoUrl")
        }
    }

    override fun saveStateParams(bundle: Bundle) {
        super.saveStateParams(bundle)
        bundle.putString("role_phoneFormated", currentPhone)
        bundle.putString("role_phoneHash", currentPhoneHash)
        bundle.putString("role_firebaseUid", currentFirebaseUid)
        bundle.putString("role_googleEmail", currentGoogleEmail)
        bundle.putString("role_googleFirstName", currentGoogleFirstName)
        bundle.putString("role_googleLastName", currentGoogleLastName)
        bundle.putString("role_googlePhotoUrl", currentGooglePhotoUrl)
    }

    override fun restoreStateParams(bundle: Bundle) {
        super.restoreStateParams(bundle)
        currentPhone = bundle.getString("role_phoneFormated", "")
        currentPhoneHash = bundle.getString("role_phoneHash", "")
        currentFirebaseUid = bundle.getString("role_firebaseUid")
        currentGoogleEmail = bundle.getString("role_googleEmail")
        currentGoogleFirstName = bundle.getString("role_googleFirstName")
        currentGoogleLastName = bundle.getString("role_googleLastName")
        currentGooglePhotoUrl = bundle.getString("role_googlePhotoUrl")
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

