/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences

import android.os.Build
import android.os.Environment
import android.widget.Toast
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.DialogObject
import org.telegram.messenger.FileLog
import org.telegram.messenger.FingerprintController
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.UsersSelectActivity
import uz.unnarsx.cherrygram.chats.helpers.ChatsPasswordHelper
import uz.unnarsx.cherrygram.core.CGBiometricPrompt
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.contract
import uz.unnarsx.cherrygram.preferences.tgkit.preference.switch
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow
import java.io.File

class PrivacyAndSecurityPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(getString(R.string.SP_Category_PrivacyAndSecurity)) {
        category(getString(R.string.SP_Header_Privacy)) {
            switch {
                title = getString(R.string.SP_NoProxyPromo)

                contract({
                    return@contract CherrygramPrivacyConfig.hideProxySponsor
                }) {
                    CherrygramPrivacyConfig.hideProxySponsor = it
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
            switch {
                title = getString(R.string.SP_GoogleAnalytics)
                description = getString(R.string.SP_GoogleAnalytics_Desc)

                contract({
                    return@contract CherrygramPrivacyConfig.googleAnalytics
                }) {
                    CherrygramPrivacyConfig.googleAnalytics = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }

            textIcon {
                title = getString(R.string.SP_CleanOld)

                listener = TGKitTextIconRow.TGTIListener {
                    val file = File(Environment.getExternalStorageDirectory(), "Telegram")
                    file.deleteRecursively()
                    Toast.makeText(bf.parentActivity, getString(R.string.SP_RemovedS), Toast.LENGTH_SHORT).show()
                }
            }
        }

        category(getString(R.string.CP_Header_Chats)) {
            switch {
                title = getString(R.string.SP_HideArchive)
                description = getString(R.string.SP_HideArchive_Desc)

                contract({
                    return@contract CherrygramPrivacyConfig.hideArchiveFromChatsList
                }) {
                    CherrygramPrivacyConfig.hideArchiveFromChatsList = it
                    if (!CherrygramAppearanceConfig.archivedChatsDrawerButton) CherrygramAppearanceConfig.archivedChatsDrawerButton = true
                }
            }
            switch {
                isAvailable = Build.VERSION.SDK_INT >= 23 && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()

                title = getString(R.string.SP_AskPinForArchive)
                description = getString(R.string.SP_AskPinForArchive_Desc)

                contract({
                    return@contract CherrygramPrivacyConfig.askBiometricsToOpenArchive
                }) {
                    CGBiometricPrompt.prompt(bf.parentActivity) {
                        CherrygramPrivacyConfig.askBiometricsToOpenArchive = it
                        bf.parentLayout.rebuildAllFragmentViews(true, true)
                    }
                }
            }
            switch {
                isAvailable = Build.VERSION.SDK_INT >= 23 && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()

                title = getString(R.string.SP_AskPinForChats)
                description = getString(R.string.SP_AskPinForChats_Desc)
                divider = false

                contract({
                    return@contract CherrygramPrivacyConfig.askBiometricsToOpenChat
                }) {
                    CGBiometricPrompt.prompt(bf.parentActivity) {
                        CherrygramPrivacyConfig.askBiometricsToOpenChat = it
                        bf.parentLayout.rebuildAllFragmentViews(true, true)
                    }
                }
            }
            textIcon {
                isAvailable = CherrygramPrivacyConfig.askBiometricsToOpenChat && Build.VERSION.SDK_INT >= 23 && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()

                icon = R.drawable.msg_discussion
                title = getString(R.string.SP_LockedChats)
                value = ChatsPasswordHelper.getLockedChatsCount().toString()
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    CGBiometricPrompt.prompt(bf.parentActivity) {
                        AndroidUtilities.runOnUIThread({
                            val activity: UsersSelectActivity = getUsersSelectActivity()
                            activity.setDelegate { ids: ArrayList<Long>, flags: Int ->
                                val chatIds = ids.toSet()
                                val lockedChats = ChatsPasswordHelper.getArrayList(ChatsPasswordHelper.PASSCODE_ARRAY).toMutableSet()

                                if (CherrygramCoreConfig.isDevBuild()) FileLog.d("old locked chats array: $lockedChats")
                                lockedChats.clear()

                                if (chatIds.isNotEmpty()) {
                                    chatIds.forEach { id ->
                                        if (DialogObject.isUserDialog(id) || DialogObject.isChatDialog(id)) {
                                            lockedChats.add(id.toString())
                                        }
                                    }
                                }

                                ChatsPasswordHelper.saveArrayList(ArrayList(lockedChats), ChatsPasswordHelper.PASSCODE_ARRAY)
                                if (CherrygramCoreConfig.isDevBuild()) FileLog.d("new locked chats array: $lockedChats")

                                value = ChatsPasswordHelper.getLockedChatsCount().toString()
                            }
                            bf.presentFragment(activity)
                        }, 300)
                    }
                }
            }
            /*textIcon {
                isAvailable = CherrygramCoreConfig.isDevBuild() && CherrygramPrivacyConfig.askBiometricsToOpenChat && Build.VERSION.SDK_INT >= 23 && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()

                icon = R.drawable.msg_clear
                title = getString(R.string.SP_LockedChats)
                value = ChatsPasswordHelper.getLockedChatsCount().toString()
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    val arr = ChatsPasswordHelper.getArrayList(ChatsPasswordHelper.Passcode_Array)!!
                    arr.clear()
                    ChatsPasswordHelper.saveArrayList(arr, ChatsPasswordHelper.Passcode_Array)

                    value = ChatsPasswordHelper.getLockedChatsCount().toString()
                    bf.parentLayout.rebuildAllFragmentViews(true, true)
                }
            }*/
            switch {
                isAvailable = Build.VERSION.SDK_INT >= 23 && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()

                title = getString(R.string.SP_AskPinBeforeDelete)
                description = getString(R.string.SP_AskPinBeforeDelete_Desc)

                contract({
                    return@contract CherrygramPrivacyConfig.askPasscodeBeforeDelete
                }) {
                    CGBiometricPrompt.prompt(bf.parentActivity) {
                        CherrygramPrivacyConfig.askPasscodeBeforeDelete = it
                        bf.parentLayout.rebuildAllFragmentViews(true, true)
                    }
                }
            }
            switch {
                isAvailable = Build.VERSION.SDK_INT >= 23 && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()

                title = getString(R.string.SP_AllowUseSystemPasscode)
                description = getString(R.string.SP_AllowUseSystemPasscode_Desc)

                contract({
                    return@contract CherrygramPrivacyConfig.allowSystemPasscode
                }) {
                    CherrygramPrivacyConfig.allowSystemPasscode = it
                }
            }
            textIcon {
                isAvailable = Build.VERSION.SDK_INT >= 23 && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()

                title = getString(R.string.SP_TestFingerprint)
                icon = R.drawable.fingerprint

                listener = TGKitTextIconRow.TGTIListener {
                    CGBiometricPrompt.prompt(bf.parentActivity) { AppRestartHelper.createDebugSuccessBulletin(bf) }
                }
            }
        }

        category(getString(R.string.SP_Category_Account)) {
            textIcon {
                title = getString(R.string.SP_DeleteAccount)
                icon = R.drawable.msg_delete
                listener = TGKitTextIconRow.TGTIListener {
                    DeleteAccountDialog.showDeleteAccountDialog(bf)
                }
            }
        }

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("privacy_preferences_screen")
    }

    private fun getUsersSelectActivity(): UsersSelectActivity {
        val chatsList = ArrayList<Long>()
        for (chatId in ChatsPasswordHelper.getArrayList(ChatsPasswordHelper.PASSCODE_ARRAY)) {

            val user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(chatId!!.toLong())
            val chat = MessagesController.getInstance(UserConfig.selectedAccount).getChat(-chatId!!.toLong())

            if (user != null) {
                chatsList.add(user.id)
            } else if (chat != null) {
                chatsList.add(-chat.id)
            }

        }

        val activity = UsersSelectActivity(true, chatsList, 0)
        activity.asLockedChats()
        return activity
    }

}