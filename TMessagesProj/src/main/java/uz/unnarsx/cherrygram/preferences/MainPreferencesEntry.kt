package uz.unnarsx.cherrygram.preferences

import android.content.Intent
import org.telegram.messenger.*
import org.telegram.messenger.browser.Browser
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.tgkit.CherrygramPreferencesNavigator
import uz.unnarsx.cherrygram.updater.UpdaterBottomSheet
import uz.unnarsx.cherrygram.tgkit.preference.category
import uz.unnarsx.cherrygram.tgkit.preference.textDetail
import uz.unnarsx.cherrygram.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.crashlytics.Crashlytics
import uz.unnarsx.cherrygram.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitTextDetailRow
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitTextIconRow


class MainPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("CGP_AdvancedSettings", R.string.CGP_AdvancedSettings)) {
        category(LocaleController.getString("CGP_Header_Categories", R.string.CGP_Header_Categories)) {
            textIcon {
                title = LocaleController.getString("AP_Header_Appearance", R.string.AP_Header_Appearance)
                icon = R.drawable.msg_theme
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createAppearance())
                }
            }
            textIcon {
                title = LocaleController.getString("СP_Header_Chats", R.string.CP_Header_Chats)
                icon = R.drawable.msg_msgbubble3
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createChats())
                }
            }
            textIcon {
                title = LocaleController.getString("CP_Category_Camera", R.string.CP_Category_Camera)
                icon = R.drawable.camera_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CameraPrefenrecesEntry())
                }
            }
            textIcon {
                title = LocaleController.getString("EP_Category_Experimental", R.string.EP_Category_Experimental)
                icon = R.drawable.msg_fave
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(ExperimentalPrefenrecesEntry())
                }
            }
            textIcon {
                title = LocaleController.getString("SP_Category_Security", R.string.SP_Category_Security)
                icon = R.drawable.msg_secret
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createSecurity())
                }
            }
            textIcon {
                title = LocaleController.getString("DP_Donate", R.string.DP_Donate)
                icon = R.drawable.card_send_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createDonate())
                }
            }

            category(LocaleController.getString("AS_Header_About", R.string.CGP_Header_About)) {
                textDetail {
                    //title = LocaleController.getString("CG_AppName", R.string.CG_AppName) + " | " + BuildConfig.VERSION_NAME_CHERRY + " " + "("+ CherrygramExtras.getAbiCode() + ")"
                    title = LocaleController.getString("CG_AppName", R.string.CG_AppName) + " " + BuildConfig.VERSION_NAME_CHERRY + " | " + "Telegram v" + BuildVars.BUILD_VERSION_STRING + " " + "(" + BuildVars.BUILD_VERSION + ")"
                    detail = LocaleController.getString("UP_TapToCheckUpdates", R.string.UP_TapToCheckUpdates)
                    listener = TGKitTextDetailRow.TGTDListener {
                        UpdaterBottomSheet(bf.parentActivity, false).show()
                    }
                }
                textIcon {
                    title = LocaleController.getString("CGP_ToChannel", R.string.CGP_ToChannel)
                    icon = R.drawable.msg_channel
                    value = "@Cherry_gram"
                    listener = TGKitTextIconRow.TGTIListener {
                        Browser.openUrl(bf.parentActivity, "https://t.me/Cherry_gram")
                    }
                }
                textIcon {
                    title = LocaleController.getString("CGP_ToChat", R.string.CGP_ToChat)
                    icon = R.drawable.msg_discuss
                    value = "@CherrygramSupport"
                    listener = TGKitTextIconRow.TGTIListener {
                        Browser.openUrl(bf.parentActivity, "https://t.me/CherrygramSupport")
                    }
                }
                textIcon {
                    val commitInfo = java.lang.String.format("%s commit", BuildConfig.GIT_COMMIT_HASH)
                    title = LocaleController.getString("CGP_Source", R.string.CGP_Source)
                    icon = R.mipmap.outline_source_white_28
                    value = commitInfo
                    listener = TGKitTextIconRow.TGTIListener {
                        Browser.openUrl(bf.parentActivity, "https://github.com/arsLan4k1390/Cherrygram")
                    }
                }
                textIcon {
                    title = LocaleController.getString("CGP_Crowdin", R.string.CGP_Crowdin)
                    icon = R.drawable.msg_translate
                    value = "Crowdin"
                    listener = TGKitTextIconRow.TGTIListener {
                        Browser.openUrl(bf.parentActivity, "https://crowdin.com/project/cherrygram")
                    }
                }
                /*textIcon {
                    title = LocaleController.getString("UP_Category_Updates", R.string.UP_Category_Updates)
                    icon = R.drawable.sync_outline_28
                    listener = TGKitTextIconRow.TGTIListener {
                        UpdaterBottomSheet(bf.parentActivity, false).show()
                    }
                }*/
                textIcon {
                    title = LocaleController.getString("CG_CopyReportDetails", R.string.CG_CopyReportDetails)
                    icon = R.drawable.bug_solar
                    listener = TGKitTextIconRow.TGTIListener {
                        AndroidUtilities.addToClipboard(Crashlytics.getReportMessage().toString() + "\n\n#bug")
                        BulletinFactory.of(bf).createErrorBulletin(
                            LocaleController.getString("CG_ReportDetailsCopied", R.string.CG_ReportDetailsCopied)
                        ).show()
                    }
                }
                textIcon {
                    title = LocaleController.getString("CG_Restart", R.string.CG_Restart)
                    icon = R.drawable.msg_retry
                    listener = TGKitTextIconRow.TGTIListener {
                        AppRestartHelper.triggerRebirth(
                            ApplicationLoader.applicationContext,
                            Intent(ApplicationLoader.applicationContext, LaunchActivity::class.java)
                        )
                    }
                }
            }
        }
    }

}