package uz.unnarsx.cherrygram.preferences

import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.ota.OTA
import uz.unnarsx.cherrygram.preferences.ktx.*
import uz.unnarsx.tgkit.preference.types.TGKitTextIconRow

class UpdatesPreferenceEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("CG_Updates_Category", R.string.CGP_Updates_Category)) {
        category(LocaleController.getString("CG_Updates_Category", R.string.CGP_Updates_Category)) {
            switch {
                title = LocaleController.getString("CG_Auto_OTA", R.string.CG_Auto_OTA)
                summary = LocaleController.getString("CG_Auto_OTA_summary", R.string.CG_Auto_OTA_summary)

                contract({
                    return@contract CherrygramConfig.autoOTA
                }) {
                    CherrygramConfig.autoOTA = it
                }
            }
            textIcon {
                title = LocaleController.getString("CG_Ota", R.string.CG_OTA)
                listener = TGKitTextIconRow.TGTIListener { OTA.download(bf.parentActivity, true) }
            }
        }
    }
}