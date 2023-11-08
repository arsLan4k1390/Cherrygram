package uz.unnarsx.cherrygram.preferences

import org.telegram.messenger.*
import org.telegram.messenger.browser.Browser
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.extras.CherrygramExtras
import uz.unnarsx.cherrygram.ui.tgkit.CherrygramPreferencesNavigator
import uz.unnarsx.cherrygram.ui.tgkit.preference.category
import uz.unnarsx.cherrygram.ui.tgkit.preference.textDetail
import uz.unnarsx.cherrygram.ui.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.ui.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.ui.tgkit.preference.types.TGKitTextDetailRow
import uz.unnarsx.cherrygram.ui.tgkit.preference.types.TGKitTextIconRow
import uz.unnarsx.cherrygram.updater.UpdaterBottomSheet

class MainPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("CGP_AdvancedSettings", R.string.CGP_AdvancedSettings)) {
        category(LocaleController.getString("CGP_Header_Categories", R.string.CGP_Header_Categories)) {
            textIcon {
                title = LocaleController.getString("AP_Header_General", R.string.AP_Header_General)
                icon = R.drawable.msg_settings
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createGeneral())
                }
            }
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
                    it.presentFragment(CameraPreferencesEntry())
                }
            }
            textIcon {
                title = LocaleController.getString("EP_Category_Experimental", R.string.EP_Category_Experimental)
                icon = R.drawable.msg_fave
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(ExperimentalPreferencesEntry())
                }
            }
            textIcon {
                title = LocaleController.getString("SP_Category_PrivacyAndSecurity", R.string.SP_Category_PrivacyAndSecurity)
                icon = R.drawable.msg_secret
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createPrivacyAndSecurity())
                }
            }
            textIcon {
                title = LocaleController.getString("DP_Donate", R.string.DP_Donate)
                icon = R.drawable.heart_angle_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createDonate())
                }
            }

            category(LocaleController.getString("AS_Header_About", R.string.CGP_Header_About)) {
                textDetail {
                    //title = LocaleController.getString("CG_AppName", R.string.CG_AppName) + " | " + CherrygramExtras.CG_VERSION + " " + "("+ CherrygramExtras.getAbiCode() + ")"
                    title = LocaleController.getString("CG_AppName", R.string.CG_AppName) + " " + CherrygramExtras.CG_VERSION + " | " + "Telegram v" + BuildVars.BUILD_VERSION_STRING + " " + "(" + BuildVars.BUILD_VERSION + ")"
                    detail = LocaleController.getString("UP_TapToCheckUpdates", R.string.UP_TapToCheckUpdates)
                    listener = TGKitTextDetailRow.TGTDListener {
                        UpdaterBottomSheet(bf.parentActivity, bf, false, null).show()
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
                    val commitInfo = String.format("%s commit", BuildConfig.GIT_COMMIT_HASH)
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
            }
        }
    }

}