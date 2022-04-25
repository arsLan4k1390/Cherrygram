package uz.unnarsx.cherrygram.preferences

import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.CherrygramConfig
//import uz.unnarsx.cherrygram.OTA
import uz.unnarsx.cherrygram.preferences.ktx.*
import uz.unnarsx.tgkit.preference.types.TGKitTextIconRow

//class UpdatesPreferenceEntry : BasePreferencesEntry {
//    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("UP_Updates_Category", R.string.UP_Updates_Category)) {
//        category(LocaleController.getString("UP_Updates_Category", R.string.UP_Updates_Category)) {
//            switch {
//                title = LocaleController.getString("CG_Auto_Ota", R.string.AP_Auto_Ota)
//                summary = LocaleController.getString("CG_Auto_Ota_summary", R.string.AP_Auto_Ota_summary)
//
//                contract({
//                    return@contract CherrygramConfig.autoOta
//                }) {
//                    CherrygramConfig.autoOta = it
//                }
//            }
//            textIcon {
//                title = LocaleController.getString("UP_Ota", R.string.UP_Ota)
//                listener = TGKitTextIconRow.TGTIListener { OTA.download(bf.parentActivity, true) }
//            }
//        }
//    }
//}