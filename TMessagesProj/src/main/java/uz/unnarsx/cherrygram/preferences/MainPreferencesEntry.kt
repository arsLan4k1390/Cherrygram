/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences

import android.content.Intent
import org.telegram.messenger.*
import org.telegram.messenger.LocaleController.getString
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.misc.CherrygramExtras
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.core.helpers.backup.BackupHelper
import uz.unnarsx.cherrygram.preferences.tgkit.CherrygramPreferencesNavigator
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow

class MainPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(getString(R.string.CGP_AdvancedSettings)) {
        category(getString(R.string.CGP_Header_Categories)) {
            textIcon {
                title = getString(R.string.AP_Header_General)
                icon = R.drawable.msg_settings_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createGeneral())
                }
            }
            textIcon {
                title = getString(R.string.AP_Header_Appearance)
                icon = R.drawable.msg_theme_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createAppearance())
                }
            }
            textIcon {
                title = getString(R.string.CP_Header_Chats)
                icon = R.drawable.msg_msgbubble3_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createChats())
                }
            }
            textIcon {
                title = getString(R.string.CP_Category_Camera)
                icon = R.drawable.camera_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CameraPreferencesEntry())
                }
            }
            textIcon {
                title = getString(R.string.EP_Category_Experimental)
                icon = R.drawable.msg_fave_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(ExperimentalPreferencesEntry())
                }
            }
            textIcon {
                title = getString(R.string.SP_Category_PrivacyAndSecurity)
                icon = R.drawable.msg_secret_solar
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createPrivacyAndSecurity())
                }
                //divider = true
            }
        }

        category(getString(R.string.LocalOther)) {
            textIcon {
                isAvailable = ApplicationLoader.isStandaloneBuild()

                icon = R.drawable.heart_angle_solar
                title = getString(R.string.DP_Donate)

                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createDonate())
                }
            }
            textIcon {
                isAvailable = CherrygramCoreConfig.isPlayStoreBuild()

                icon = R.drawable.heart_angle_solar
                title = getString(R.string.DP_RateUs)

                listener = TGKitTextIconRow.TGTIListener {
                    CherrygramExtras.requestReviewFlow(bf, bf.context, bf.parentActivity)
                }
            }
            textIcon {
                title = getString(R.string.CG_ExportSettings)
                icon = R.drawable.msg_instant_link_solar
                listener = TGKitTextIconRow.TGTIListener {
                    BackupHelper.backupSettings(bf, bf.context)
                }
            }
            textIcon {
                title = getString(R.string.CG_ImportSettings)
                icon = R.drawable.msg_photo_settings_solar
                listener = TGKitTextIconRow.TGTIListener {
                    BackupHelper.importSettings(bf)
                }
            }
            textIcon {
                title = getString(R.string.CG_Restart)
                icon = R.drawable.msg_retry_solar
                listener = TGKitTextIconRow.TGTIListener {
                    AppRestartHelper.triggerRebirth(bf.context, Intent(bf.context, LaunchActivity::class.java))
                }
            }
        }

        category(getString(R.string.CGP_Header_About)) {
            textIcon {
                title = getString(R.string.CGP_Header_About_Desc)
                icon = R.drawable.msg_info_solar

                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createAbout())
                }
            }
        }

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("main_preferences_screen")
    }
}