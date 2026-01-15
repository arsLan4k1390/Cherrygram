/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricPrompt
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.DialogObject
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.Components.RecyclerListView
import org.telegram.ui.LaunchActivity
import org.telegram.ui.UsersSelectActivity
import uz.unnarsx.cherrygram.core.CGBiometricPrompt
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.contract
import uz.unnarsx.cherrygram.preferences.tgkit.preference.hint
import uz.unnarsx.cherrygram.preferences.tgkit.preference.switch
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow

class PrivacyAndSecurityPreferencesEntry : BasePreferencesEntry {

    private var listView: RecyclerListView? = null

    override fun setListView(rv: RecyclerListView) {
        listView = rv
    }

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
                title = getString(R.string.SP_DeleteAccount)
                icon = R.drawable.msg_delete

                listener = TGKitTextIconRow.TGTIListener {
                    if (bf.chatsPasswordHelper.checkBiometricAvailable()) {
                        CGBiometricPrompt.prompt(bf.parentActivity) {
                            DeleteAccountDialog.showDeleteAccountDialog(bf)
                        }
                    } else {
                        DeleteAccountDialog.showDeleteAccountDialog(bf)
                    }
                }
            }
        }

        category(getString(R.string.CP_Header_Chats)) {
            switch {
                isAvailable = (CherrygramCoreConfig.isStandalonePremiumBuild() || CherrygramCoreConfig.isDevBuild()) && (bf.userConfig.clientUserId == 6578415824L || bf.userConfig.clientUserId == 282287840L)
                title = "Скрыть архивированные истории"
                description = "Скрывает раздел архивированных историй в профиле"

                contract({
                    return@contract CherrygramPrivacyConfig.hideArchivedStories
                }) {
                    CherrygramPrivacyConfig.hideArchivedStories = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
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
            textIcon {
                isAvailable = bf.chatsPasswordHelper.checkBiometricAvailable()

                title = getString(R.string.SP_AskBioToOpenChats)
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    CGBiometricPrompt.prompt(bf.parentActivity) {
                        showPasscodeItemsSelector(bf)
                    }
                }
            }
            if (bf.chatsPasswordHelper.checkBiometricAvailable()) hint(getString(R.string.SP_AskBioToOpenChats_Desc))
            textIcon {
                isAvailable = CherrygramPrivacyConfig.askBiometricsToOpenChat && bf.chatsPasswordHelper.checkBiometricAvailable()

                icon = R.drawable.msg_discussion
                title = getString(R.string.SP_LockedChats)
                value = bf.chatsPasswordHelper.getLockedChatsCount().toString()
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    CGBiometricPrompt.prompt(bf.parentActivity) {
                        AndroidUtilities.runOnUIThread({
                            val activity: UsersSelectActivity = getUsersSelectActivity(bf)
                            activity.setDelegate { ids: ArrayList<Long>, _: Int ->
                                val chatIds = ids.toSet()
                                val lockedChats = bf.chatsPasswordHelper.getArrayList(bf.chatsPasswordHelper.getPasscodeArray()).toMutableSet()

                                if (CherrygramCoreConfig.isDevBuild()) FileLog.d("old locked chats array: $lockedChats")
                                lockedChats.clear()

                                if (chatIds.isNotEmpty()) {
                                    chatIds.forEach { id ->
                                        if (DialogObject.isUserDialog(id) || DialogObject.isChatDialog(id)) {
                                            lockedChats.add(id.toString())
                                        }
                                    }
                                }

                                bf.chatsPasswordHelper.saveArrayList(ArrayList(lockedChats), bf.chatsPasswordHelper.getPasscodeArray())
                                if (CherrygramCoreConfig.isDevBuild()) FileLog.d("new locked chats array: $lockedChats")

                                value = bf.chatsPasswordHelper.getLockedChatsCount().toString()
                            }
                            bf.presentFragment(activity)
                        }, 300)
                    }
                }
            }
            switch {
                isAvailable = bf.chatsPasswordHelper.checkBiometricAvailable()

                title = getString(R.string.SP_AskPinBeforeDelete)
                description = getString(R.string.SP_AskPinBeforeDelete_Desc)

                contract({
                    return@contract CherrygramPrivacyConfig.askPasscodeBeforeDelete
                }) {
                    CGBiometricPrompt.prompt(bf.parentActivity) {
                        CherrygramPrivacyConfig.askPasscodeBeforeDelete = it
                        listView?.post {
                            listView!!.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
            switch {
                isAvailable = bf.chatsPasswordHelper.checkBiometricAvailable()

                title = getString(R.string.SP_AllowUseSystemPasscode)
                description = getString(R.string.SP_AllowUseSystemPasscode_Desc)

                contract({
                    return@contract CherrygramPrivacyConfig.allowSystemPasscode
                }) {
                    CherrygramPrivacyConfig.allowSystemPasscode = it
                }
            }
            textIcon {
                title = getString(R.string.SP_TestFingerprint)
                icon = R.drawable.fingerprint

                listener = TGKitTextIconRow.TGTIListener {
                    CGBiometricPrompt.fixFingerprint(bf.parentActivity, object : CGBiometricPrompt.CGBiometricListener {

                        override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                            handle()
                        }

                        override fun onFailed() {
//                            showError(0)
                        }

                        override fun onError(error: Int, msg: CharSequence) {
                            showError(error)
                        }

                        fun handle() {
                            CGBiometricPrompt.cancelPendingAuthentications()
                            CGBiometricPrompt.reloadFingerprintState()
                            if (CGBiometricPrompt.hasFingerprintCached()) {
                                AndroidUtilities.runOnUIThread({
                                    BulletinFactory.of(bf).createSimpleBulletin(
                                        R.raw.chats_infotip,
                                        getString(R.string.SP_BiometricUnavailable_Test_Fixed),
                                        getString(R.string.CG_RestartToApply),
                                        getString(R.string.OK)
                                    ) {
                                        AppRestartHelper.triggerRebirth(bf.context, Intent(bf.context, LaunchActivity::class.java))
                                    }.show()
//                                    bf.parentLayout.rebuildAllFragmentViews(true, true)
                                }, 300)
                            } else {
                                showError(0)
                            }
                        }

                        fun showError(error: Int) {
                            BulletinFactory.of(bf).createSimpleBulletin(
                                R.raw.chats_infotip,
                                getString(R.string.CG_AppCrashed) + if (error == 0) "" else " (e$error)",
                                getString(R.string.SP_BiometricUnavailable_Test_Wrong_Desc),
                                getString(R.string.Settings)
                            ) {
                                openFingerprintSettings(bf.context)
                            }.show()
                        }

                        fun openFingerprintSettings(context: Context) {
                            val fallbackIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)

                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    val fingerprintIntent = Intent(Settings.ACTION_FINGERPRINT_ENROLL).apply {
                                        setPackage("com.android.settings")
                                    }

                                    if (fingerprintIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(fingerprintIntent)
                                        return
                                    }
                                }
                                context.startActivity(fallbackIntent)
                            } catch (e: SecurityException) {
                                FileLog.e(e)
                                context.startActivity(fallbackIntent)
                            } catch (e: Exception) {
                                FileLog.e(e)
                            }
                        }

                    })

                }
            }
            hint(getString(R.string.SP_TestFingerprint_Desc))
        }

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("privacy_preferences_screen")
    }

    private fun getUsersSelectActivity(bf: BaseFragment): UsersSelectActivity {
        val chatsList = ArrayList<Long>()
        for (chatId in bf.chatsPasswordHelper.getArrayList(bf.chatsPasswordHelper.getPasscodeArray())) {

            val user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(chatId.toLong())
            val chat = MessagesController.getInstance(UserConfig.selectedAccount).getChat(-chatId.toLong())

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

    private fun showPasscodeItemsSelector(fragment: BaseFragment) {
        val menuItems = listOf(
            MenuItemConfig(
                getString(R.string.FilterChats),
                0,
                { CherrygramPrivacyConfig.askBiometricsToOpenChat },
                { CherrygramPrivacyConfig.askBiometricsToOpenChat = !CherrygramPrivacyConfig.askBiometricsToOpenChat },
                true
            ),
            MenuItemConfig(
                getString(R.string.SecretChat),
                0,
                { CherrygramPrivacyConfig.askBiometricsToOpenEncrypted },
                { CherrygramPrivacyConfig.askBiometricsToOpenEncrypted = !CherrygramPrivacyConfig.askBiometricsToOpenEncrypted },
                true
            ),
            MenuItemConfig(
                getString(R.string.ArchivedChats), //ArchiveSearchFilter
                0,
                { CherrygramPrivacyConfig.askBiometricsToOpenArchive },
                { CherrygramPrivacyConfig.askBiometricsToOpenArchive = !CherrygramPrivacyConfig.askBiometricsToOpenArchive },
                false
            ),
        )

        val prefTitle = ArrayList<String>()
        val prefIcon = ArrayList<Int>()
        val prefCheck = ArrayList<Boolean>()
        val prefDivider = ArrayList<Boolean>()
        val clickListener = ArrayList<Runnable>()

        for (item in menuItems) {
            prefTitle.add(item.titleRes)
            prefIcon.add(item.iconRes)
            prefCheck.add(item.isChecked())
            prefDivider.add(item.divider)
            clickListener.add(Runnable { item.toggle() })
        }

        PopupHelper.showSwitchAlert(
            getString(R.string.SelectChats),
            fragment,
            prefTitle,
            prefIcon,
            prefCheck,
            null,
            null,
            prefDivider,
            clickListener,
            null
        )
    }

    data class MenuItemConfig(
        val titleRes: String,
        val iconRes: Int,
        val isChecked: () -> Boolean,
        val toggle: () -> Unit,
        val divider: Boolean = false
    )

}