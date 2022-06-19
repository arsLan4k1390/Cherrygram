package uz.unnarsx.cherrygram.preferences

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import org.telegram.messenger.BuildVars
import org.telegram.messenger.LocaleController
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.CherrygramPreferencesNavigator
import uz.unnarsx.cherrygram.preferences.ktx.category
import uz.unnarsx.cherrygram.preferences.ktx.textDetail
import uz.unnarsx.cherrygram.preferences.ktx.textIcon
import uz.unnarsx.cherrygram.preferences.ktx.tgKitScreen
import uz.unnarsx.extras.CherrygramExtras
import uz.unnarsx.tgkit.preference.types.TGKitTextIconRow

import android.os.Build

import android.app.assist.AssistContent
import org.telegram.messenger.R


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
                icon = R.drawable.menu_chats
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createChats())
                }
            }

            textIcon {
                title = LocaleController.getString("SP_Category_Security", R.string.SP_Category_Security)
                icon = R.drawable.menu_secret
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createSecurity())
                }
            }
            textIcon {
                title = LocaleController.getString("EP_Category_Experimental", R.string.EP_Category_Experimental)
                icon = R.drawable.favorite_outline_28
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(ExperimentalPrefenrecesEntry())
                }
            }
            textIcon {
                title = LocaleController.getString("CGP_Updates_Category", R.string.CGP_Updates_Category)
                icon = R.drawable.outline_send
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CherrygramPreferencesNavigator.createUpdates())
                }
            }
            textIcon {
                title = LocaleController.getString("DP_Donate", R.string.DP_Donate)
                icon = R.drawable.money_circle_outline_28
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
                    value = "@Cherry_gram"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToChannel(it)
                    }
                }
                textIcon {
                    title = LocaleController.getString("CGP_ToChat", R.string.CGP_ToChat)
                    value = "@cherry_gram_support"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToChat(it)
                    }
                }
                textIcon {
                    title = LocaleController.getString("CGP_Source", R.string.CGP_Source)
                    value = "Github"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToGithub(it)
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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/cherry_gram_support"))
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