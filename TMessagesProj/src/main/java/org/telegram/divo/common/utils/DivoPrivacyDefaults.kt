package org.telegram.divo.common.utils

import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ContactsController
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.tl.TL_account

/**
 * Divo hides the "Profile photos" privacy setting, so a user who once set it to "Nobody" could
 * never undo it. Reset it to "Everybody" once per account.
 */
object DivoPrivacyDefaults {

    private const val KEY_PROFILE_PHOTO_RESET = "divo_profile_photo_privacy_reset"

    @JvmStatic
    fun ensureDefaults(account: Int) {
        val userConfig = UserConfig.getInstance(account)
        if (!userConfig.isClientActivated) return
        val prefs = MessagesController.getMainSettings(account)
        if (prefs.getBoolean(KEY_PROFILE_PHOTO_RESET, false)) return

        val req = TL_account.setPrivacy().apply {
            key = TLRPC.TL_inputPrivacyKeyProfilePhoto()
            rules.add(TLRPC.TL_inputPrivacyValueAllowAll())
        }
        ConnectionsManager.getInstance(account).sendRequest(req) { response, _ ->
            if (response !is TL_account.privacyRules) return@sendRequest
            AndroidUtilities.runOnUIThread {
                val mc = MessagesController.getInstance(account)
                mc.putUsers(response.users, false)
                mc.putChats(response.chats, false)
                ContactsController.getInstance(account).setPrivacyRules(response.rules, ContactsController.PRIVACY_RULES_TYPE_PHOTO)
                // Only remember success, so a failed request is retried on the next start
                prefs.edit().putBoolean(KEY_PROFILE_PHOTO_RESET, true).apply()
            }
        }
    }
}
