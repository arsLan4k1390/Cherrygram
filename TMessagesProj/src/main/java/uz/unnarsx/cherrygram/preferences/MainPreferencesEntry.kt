package uz.unnarsx.cherrygram.preferences

import android.content.Intent
import org.telegram.messenger.*
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.extras.CherrygramExtras
import uz.unnarsx.cherrygram.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.helpers.BackupHelper
import uz.unnarsx.cherrygram.ui.tgkit.CherrygramPreferencesNavigator
import uz.unnarsx.cherrygram.ui.tgkit.preference.category
import uz.unnarsx.cherrygram.ui.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.ui.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.ui.tgkit.preference.types.TGKitTextIconRow

class MainPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("CGP_AdvancedSettings", R.string.CGP_AdvancedSettings)) {
        category(LocaleController.getString("CGP_Header_Categories", R.string.CGP_Header_Categories)) {
            textIcon {
                title = LocaleController.getString("AP_Header_General", R.string.AP_Header_General)
                icon = R.drawable.msg_settings_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createGeneral())
                }
            }
            textIcon {
                title = LocaleController.getString("AP_Header_Appearance", R.string.AP_Header_Appearance)
                icon = R.drawable.msg_theme_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createAppearance())
                }
            }
            textIcon {
                title = LocaleController.getString("СP_Header_Chats", R.string.CP_Header_Chats)
                icon = R.drawable.msg_msgbubble3_solar
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
                icon = R.drawable.msg_fave_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(ExperimentalPreferencesEntry())
                }
            }
            textIcon {
                title = LocaleController.getString("SP_Category_PrivacyAndSecurity", R.string.SP_Category_PrivacyAndSecurity)
                icon = R.drawable.msg_secret_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createPrivacyAndSecurity())
                }
                //divider = true
            }
        }

        category(LocaleController.getString("LocalOther", R.string.LocalOther)) {
            if (!CherrygramConfig.isPlayStoreBuild()) {
                textIcon {
                    icon = R.drawable.heart_angle_solar
                    title = LocaleController.getString("DP_Donate", R.string.DP_Donate)

                    listener = TGKitTextIconRow.TGTIListener {
                        it.presentFragment(CherrygramPreferencesNavigator.createDonate())
                    }
                }
            } else {
                textIcon {
                    icon = R.drawable.heart_angle_solar
                    title = LocaleController.getString("DP_RateUs", R.string.DP_RateUs)

                    listener = TGKitTextIconRow.TGTIListener {
                        CherrygramExtras.requestReviewFlow(bf, bf.context, bf.parentActivity)
                    }
                }
            }
            textIcon {
                title = LocaleController.getString("CG_ExportSettings", R.string.CG_ExportSettings)
                icon = R.drawable.msg_instant_link_solar
                listener = TGKitTextIconRow.TGTIListener {
                    BackupHelper.backupSettings(bf, bf.context)
                }
            }
            textIcon {
                title = LocaleController.getString("CG_ImportSettings", R.string.CG_ImportSettings)
                icon = R.drawable.msg_photo_settings_solar
                listener = TGKitTextIconRow.TGTIListener {
                    BackupHelper.importSettings(bf)
                }
            }
            textIcon {
                title = LocaleController.getString("CG_Restart", R.string.CG_Restart)
                icon = R.drawable.msg_retry_solar
                listener = TGKitTextIconRow.TGTIListener {
                    AppRestartHelper.triggerRebirth(bf.context, Intent(bf.context, LaunchActivity::class.java))
                }
            }
        }

        category(LocaleController.getString("AS_Header_About", R.string.CGP_Header_About)) {
            textIcon {
                title = LocaleController.getString("CGP_Header_About_Desc", R.string.CGP_Header_About_Desc)
                icon = R.drawable.msg_info_solar

                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createAbout())
                }
            }
        }
    }

}