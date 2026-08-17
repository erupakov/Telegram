package org.telegram.divo.dal.utils

import android.content.DialogInterface
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.tl.TL_account
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme

object DivoDeleteAccountHelper {

    @JvmStatic
    fun showDeleteProfileDialog(fragment: BaseFragment) {
        val activity = fragment.parentActivity ?: return
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(LocaleController.getString("DeleteProfileTitle", R.string.DeleteProfileTitle))
        builder.setMessage(LocaleController.getString("DeleteProfileDescription", R.string.DeleteProfileDescription))
        builder.setPositiveButton(LocaleController.getString("DeleteProfileConfirm", R.string.DeleteProfileConfirm)) { _, _ ->
            deleteProfile(fragment)
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
        val alertDialog = builder.create()
        fragment.showDialog(alertDialog)
        val button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE) as? TextView
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_text_RedBold))
        }
    }

    @JvmStatic
    fun deleteProfile(fragment: BaseFragment) {
        val activity = fragment.parentActivity ?: return
        val progressDialog = AlertDialog(activity, AlertDialog.ALERT_TYPE_SPINNER)
        progressDialog.setCanCancel(false)
        progressDialog.show()

        val currentAccount = fragment.currentAccount

        CoroutineScope(Dispatchers.IO).launch {
            val result = DivoApi.userRepository.deleteAccount()
            withContext(Dispatchers.Main) {
                if (result is DivoResult.Success) {
                    val req = TL_account.deleteAccount()
                    req.reason = "Divo Profile Deleted"
                    ConnectionsManager.getInstance(currentAccount).sendRequest(req) { response, error ->
                        AndroidUtilities.runOnUIThread {
                            try {
                                progressDialog.dismiss()
                            } catch (e: Exception) {
                                FileLog.e(e)
                            }
                            if (response is TLRPC.TL_boolTrue) {
                                MessagesController.getInstance(currentAccount).performLogout(0)
                            } else if (error == null || error.code != -1000) {
                                var errorText = LocaleController.getString(R.string.ErrorOccurred)
                                if (error != null) {
                                    errorText += "\n" + error.text
                                }
                                val errorBuilder = AlertDialog.Builder(activity)
                                errorBuilder.setTitle(LocaleController.getString(R.string.AppName))
                                errorBuilder.setMessage(errorText)
                                errorBuilder.setPositiveButton(LocaleController.getString(R.string.OK), null)
                                fragment.showDialog(errorBuilder.create())
                            }
                        }
                    }
                } else {
                    try {
                        progressDialog.dismiss()
                    } catch (e: Exception) {
                        FileLog.e(e)
                    }
                    val errorBuilder = AlertDialog.Builder(activity)
                    errorBuilder.setTitle(LocaleController.getString(R.string.AppName))
                    errorBuilder.setMessage(result.getErrorMessage())
                    errorBuilder.setPositiveButton(LocaleController.getString(R.string.OK), null)
                    fragment.showDialog(errorBuilder.create())
                }
            }
        }
    }
}
