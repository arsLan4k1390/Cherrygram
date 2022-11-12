package uz.unnarsx.cherrygram.preferences

import android.app.assist.AssistContent
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Browser
import org.telegram.messenger.*
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.CherrygramPreferencesNavigator
import uz.unnarsx.cherrygram.ota.UpdaterBottomSheet
import uz.unnarsx.cherrygram.preferences.ktx.category
import uz.unnarsx.cherrygram.preferences.ktx.textDetail
import uz.unnarsx.cherrygram.preferences.ktx.textIcon
import uz.unnarsx.cherrygram.preferences.ktx.tgKitScreen
import uz.unnarsx.extras.CherrygramExtras
import uz.unnarsx.extras.Crashlytics
import uz.unnarsx.tgkit.preference.types.TGKitTextDetailRow
import uz.unnarsx.tgkit.preference.types.TGKitTextIconRow


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
                icon = R.drawable.camera
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
                icon = R.drawable.card_send
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
                        goToChannel(it)
                    }
                }
                textIcon {
                    title = LocaleController.getString("CGP_ToChat", R.string.CGP_ToChat)
                    icon = R.drawable.msg_discuss
                    value = "@CherrygramSupport"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToChat(it)
                    }
                }
                textIcon {
                    val commitInfo = java.lang.String.format("%s commit", BuildConfig.GIT_COMMIT_HASH)
                    title = LocaleController.getString("CGP_Source", R.string.CGP_Source)
                    icon = R.mipmap.outline_source_white_28
                    value = commitInfo
                    listener = TGKitTextIconRow.TGTIListener {
                        goToGithub(it)
                    }
                }
                textIcon {
                    title = LocaleController.getString("CGP_Crowdin", R.string.CGP_Crowdin)
                    icon = R.drawable.msg_translate
                    value = "Crowdin"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToCrowdin(it)
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
                    icon = R.drawable.bug
                    listener = TGKitTextIconRow.TGTIListener {
                        AndroidUtilities.addToClipboard(Crashlytics.getReportMessage().toString() + "\n\n#bug")
                        BulletinFactory.of(bf).createErrorBulletin(
                            LocaleController.getString("CG_ReportDetailsCopied", R.string.CG_ReportDetailsCopied)
                        ).show()
                    }
                }
            }
        }
    }

    companion object {
        private fun goToChannel(bf: BaseFragment) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Cherry_gram"))
            val componentName = ComponentName(bf.parentActivity.packageName, LaunchActivity::class.java.name)
            intent.component = componentName
            intent.putExtra(Browser.EXTRA_CREATE_NEW_TAB, true)
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, bf.parentActivity.packageName)
            bf.parentActivity.startActivity(intent)
        }

        private fun goToChat(bf: BaseFragment) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/CherrygramSupport"))
            val componentName = ComponentName(bf.parentActivity.packageName, LaunchActivity::class.java.name)
            intent.component = componentName
            intent.putExtra(Browser.EXTRA_CREATE_NEW_TAB, true)
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, bf.parentActivity.packageName)
            bf.parentActivity.startActivity(intent)
        }

        private fun goToGithub(bf: BaseFragment) {
            val openURL = Intent(android.content.Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://github.com/arsLan4k1390/Cherrygram")
            bf.parentActivity.startActivity(openURL)
        }

        private fun goToCrowdin(bf: BaseFragment) {
            val openURL = Intent(android.content.Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://crowdin.com/project/cherrygram")
            bf.parentActivity.startActivity(openURL)
        }

        fun onProvideAssistContent(outContent: AssistContent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                outContent.webUri = Uri.parse(
                    String.format(
                        "https://t.me/Cherry_gram"
                    )
                )
            }
        }

    }
}