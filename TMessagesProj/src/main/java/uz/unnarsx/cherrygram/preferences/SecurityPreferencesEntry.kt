package uz.unnarsx.cherrygram.preferences

import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.preferences.ktx.*
import uz.unnarsx.tgkit.preference.types.TGKitTextIconRow

class SecurityPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AS_Category_Security", R.string.SP_Category_Security)) {

        category(LocaleController.getString("SP_Header_Privacy", R.string.SP_Header_Privacy)) {
            switch {
                title = LocaleController.getString("AS_NoProxyPromo", R.string.SP_NoProxyPromo)

                contract({
                    return@contract CherrygramConfig.hideProxySponsor
                }) {
                    CherrygramConfig.hideProxySponsor = it
                }
            }

            switch {
                title = LocaleController.getString("SP_Kaboom", R.string.SP_Kaboom)
                summary = LocaleController.getString("SP_Kaboom_Desc", R.string.SP_Kaboom_Desc)

                contract({
                    return@contract CherrygramConfig.kaboom
                }) {
                    CherrygramConfig.kaboom = it
                }
            }
        }

        category(LocaleController.getString("SP_Category_Account", R.string.SP_Category_Account)) {
            textIcon {
                title = LocaleController.getString("SP_DeleteAccount", R.string.SP_DeleteAccount)
                icon = R.drawable.msg_delete
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(AccountSettingsActivity())
                }
            }
        }
    }
}