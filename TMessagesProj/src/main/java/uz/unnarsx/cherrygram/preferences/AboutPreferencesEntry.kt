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
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
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
                detail = LaunchActivity.instance.lastCheckUpdateTime

                listener = TGKitTextDetailRow.TGTDListener {
                    if (CherrygramCoreConfig.isPlayStoreBuild()) {
                        CherrygramCoreConfig.lastUpdateCheckTime = System.currentTimeMillis()
                        detail = LaunchActivity.instance.lastCheckUpdateTime

                        Browser.openUrl(bf.context, Constants.UPDATE_APP_URL)
                    } else if (CherrygramCoreConfig.isStandalonePremiumBuild()) {
                        // Fuckoff :)
                    } else {
                        LaunchActivity.instance.showCgUpdaterSettings(bf.context, bf)
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
                value = "@${Constants.CG_CHANNEL_USERNAME}"

                listener = TGKitTextIconRow.TGTIListener {
                    bf.messagesController.openByUserName(Constants.CG_CHANNEL_USERNAME, bf, 1);
                }
            }
            textIcon {
                icon = R.drawable.msg_discuss_solar
                title = getString(R.string.CGP_ToChat)
                value = "@${Constants.CG_CHAT_USERNAME}"

                listener = TGKitTextIconRow.TGTIListener {
                    bf.messagesController.openByUserName(Constants.CG_CHAT_USERNAME, bf, 1);
                }
            }
            textIcon {
                isAvailable = !CherrygramCoreConfig.isStandalonePremiumBuild()
                icon = R.drawable.github_logo_white
                title = getString(R.string.CGP_Source)

                value = if (CherrygramCoreConfig.isStandaloneBetaBuild() || CherrygramCoreConfig.isDevBuild()) {
                    "GitHub"
                } else {
                    "commit " + BuildConfig.GIT_COMMIT_HASH.substring(0, 8)
                }

                listener = TGKitTextIconRow.TGTIListener {
                    if (CherrygramCoreConfig.isStandaloneBetaBuild() || CherrygramCoreConfig.isDevBuild()) {
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