package uz.unnarsx.cherrygram.preferences

import android.os.Bundle
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.BuildVars
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.messenger.browser.Browser
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ChatActivity
import org.telegram.ui.Components.BulletinFactory
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.Extra
import uz.unnarsx.cherrygram.core.crashlytics.Crashlytics
import uz.unnarsx.cherrygram.misc.Constants
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper
import uz.unnarsx.cherrygram.preferences.tgkit.CherrygramPreferencesNavigator
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textDetail
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextDetailRow
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow
import uz.unnarsx.cherrygram.core.updater.UpdaterUtils

class AboutPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(getString(R.string.CGP_Header_About)) {
        category(getString(R.string.Info)) {
            textDetail {
                title = CGResourcesHelper.getAppName() + " " + Constants.CG_VERSION + " | " + "Telegram v" + BuildVars.BUILD_VERSION_STRING
                detail = getString(R.string.CGP_About_Desc)

                listener = TGKitTextDetailRow.TGTDListener {
                    Browser.openUrl(bf.parentActivity, "https://github.com/arsLan4k1390/Cherrygram#readme")
                }
            }

            textDetail {
                icon = R.drawable.sync_outline_28
                title = getString(R.string.UP_Category_Updates)
                detail = UpdaterUtils.getLastCheckUpdateTime()

                listener = TGKitTextDetailRow.TGTDListener {
                    if (CherrygramCoreConfig.isPlayStoreBuild()) {
                        CherrygramCoreConfig.lastUpdateCheckTime = System.currentTimeMillis()
                        detail = UpdaterUtils.getLastCheckUpdateTime()

                        Browser.openUrl(bf.context, Extra.PLAYSTORE_APP_URL)
                    } else if (CherrygramCoreConfig.isPremiumBuild()) {
                        // Fuckoff :)
                    } else {
//                        UpdaterBottomSheet.showAlert(bf.context, bf, false, null)
                    }
                }
            }

            textIcon {
                icon = R.drawable.bug_solar
                title = getString(R.string.CG_CopyReportDetails)

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard(Crashlytics.getReportMessage().toString() + "\n\n#bug")
                    BulletinFactory.of(bf).createErrorBulletin(getString(R.string.CG_ReportDetailsCopied)).show()
                }
            }
            textIcon {
                icon = R.drawable.test_tube_solar
                title = "Debug // WIP"

                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createDebug())
                }
            }
        }

        category(getString(R.string.CGP_Links)) {
            textIcon {
                icon = R.drawable.msg_channel_solar
                title = getString(R.string.CGP_ToChannel)
                value = "@Cherry_gram"

                listener = TGKitTextIconRow.TGTIListener {
                    Browser.openUrl(bf.parentActivity, "https://t.me/Cherry_gram")
                }
            }
            textIcon {
                icon = R.drawable.msg_discuss_solar
                title = getString(R.string.CGP_ToChat)
                value = "@CherrygramSupport"

                listener = TGKitTextIconRow.TGTIListener {
                    Browser.openUrl(bf.parentActivity, "https://t.me/CherrygramSupport")
                }
            }
            textIcon {
                isAvailable = !CherrygramCoreConfig.isPremiumBuild()
                icon = R.drawable.github_logo_white
                title = getString(R.string.CGP_Source)

                value = if (CherrygramCoreConfig.isBetaBuild() || CherrygramCoreConfig.isDevBuild()) {
                    "GitHub"
                } else {
                    "commit " + BuildConfig.GIT_COMMIT_HASH.substring(0, 8)
                }

                listener = TGKitTextIconRow.TGTIListener {
                    if (CherrygramCoreConfig.isBetaBuild() || CherrygramCoreConfig.isDevBuild()) {
                        Browser.openUrl(bf.parentActivity, "https://github.com/arsLan4k1390/Cherrygram/")
                    } else {
                        Browser.openUrl(bf.parentActivity, "https://github.com/arsLan4k1390/Cherrygram/commit/" + BuildConfig.GIT_COMMIT_HASH)
                    }
                }
            }
            textIcon {
                icon = R.drawable.msg_translate_solar
                title = getString(R.string.CGP_Crowdin)
                value = "Crowdin"

                listener = TGKitTextIconRow.TGTIListener {
                    Browser.openUrl(bf.parentActivity, "https://crowdin.com/project/cherrygram")
                }
            }

            if (CherrygramCoreConfig.isPlayStoreBuild()) {
                textIcon {
                    icon = R.drawable.msg2_policy
                    title = getString(R.string.PrivacyPolicy)

                    listener = TGKitTextIconRow.TGTIListener {
                        Browser.openUrl(bf.parentActivity, "https://arslan4k1390.github.io/cherrygram/privacy")
                    }
                }
            }
            /*textIcon {
                icon = R.drawable.heart_angle_solar
                title = getString(R.string.DP_Donate)

                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createDonate())
                }
            }*/
        }

    }
}