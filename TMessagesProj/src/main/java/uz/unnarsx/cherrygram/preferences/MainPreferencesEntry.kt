package uz.unnarsx.cherrygram.preferences

import android.app.assist.AssistContent
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Browser
import org.telegram.messenger.BuildVars
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.CherrygramPreferencesNavigator
import uz.unnarsx.cherrygram.preferences.ktx.category
import uz.unnarsx.cherrygram.preferences.ktx.textDetail
import uz.unnarsx.cherrygram.preferences.ktx.textIcon
import uz.unnarsx.cherrygram.preferences.ktx.tgKitScreen
import uz.unnarsx.extras.CherrygramExtras
import uz.unnarsx.tgkit.preference.types.TGKitTextIconRow


class MainPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("CGP_AdvancedSettings", R.string.CGP_AdvancedSettings)) {
        category(LocaleController.getString("CGP_Header_Categories", R.string.CGP_Header_Categories)) {
            textIcon {
                title = LocaleController.getString("AP_Header_Appearance", R.string.AP_Header_Appearance)
                icon = R.drawable.palette_outline_28
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createAppearance())
                }
            }
            textIcon {
                title = LocaleController.getString("СP_Header_Chats", R.string.CP_Header_Chats)
                icon = R.drawable.messages_outline_28
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createChats())
                }
            }
            textIcon {
                title = LocaleController.getString("CP_Category_Camera", R.string.CP_Category_Camera)
                icon = R.drawable.camera_outline_28
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CameraPrefenrecesEntry())
                }
            }
            textIcon {
                title = LocaleController.getString("SP_Category_Security", R.string.SP_Category_Security)
                icon = R.drawable.lock_outline_28
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createSecurity())
                }
            }
            textIcon {
                title = LocaleController.getString("CGP_Updates_Category", R.string.CGP_Updates_Category)
                icon = R.mipmap.outline_send
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createUpdates())
                }
            }
            textIcon {
                title = LocaleController.getString("DP_Donate", R.string.DP_Donate)
                icon = R.drawable.money_send_outline_28
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createDonate())
                }
            }

            category(LocaleController.getString("AS_Header_About", R.string.CGP_Header_About)) {
                textDetail {
                    title = "Cherrygram v" + CherrygramExtras.CG_VERSION + " [" + BuildVars.BUILD_VERSION_STRING + "]"
                    detail = LocaleController.getString("CGP_About_Desc", R.string.CGP_About_Desc)
                }
                textIcon {
                    title = LocaleController.getString("CGP_ToChannel", R.string.CGP_ToChannel)
                    icon = R.drawable.advertising_outline_28
                    value = "@Cherry_gram"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToChannel(it)
                    }
                }
                textIcon {
                    title = LocaleController.getString("CGP_ToChat", R.string.CGP_ToChat)
                    icon = R.drawable.chats_outline_28
                    value = "@CherrygramSupport"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToChat(it)
                    }
                }
                textIcon {
                    title = LocaleController.getString("CGP_Source", R.string.CGP_Source)
                    icon = R.mipmap.outline_source_white_28
                    value = "Github"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToGithub(it)
                    }
                }
                textIcon {
                    title = LocaleController.getString("CGP_Crowdin", R.string.CGP_Crowdin)
                    icon = R.drawable.hieroglyph_character_outline_28
                    value = "Crowdin"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToCrowdin(it)
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