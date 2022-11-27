package uz.unnarsx.cherrygram.preferences

import android.app.Activity
import android.content.SharedPreferences
import android.os.Environment
import android.widget.Toast
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.preferences.ktx.*
import uz.unnarsx.tgkit.preference.types.TGKitSliderPreference
import uz.unnarsx.tgkit.preference.types.TGKitTextIconRow
import java.io.File

class SecurityPreferencesEntry : BasePreferencesEntry {
    val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AS_Category_Security", R.string.SP_Category_Security)) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(CherrygramConfig.listener)
        category(LocaleController.getString("SP_Header_Privacy", R.string.SP_Header_Privacy)) {
            switch {
                title = LocaleController.getString("AS_NoProxyPromo", R.string.SP_NoProxyPromo)

                contract({
                    return@contract CherrygramConfig.hideProxySponsor
                }) {
                    CherrygramConfig.hideProxySponsor = it
                    bf.parentActivity.recreate()
                }
            }

            textIcon {
                title = LocaleController.getString("SP_CleanOld", R.string.SP_CleanOld)

                listener = TGKitTextIconRow.TGTIListener {
                    val file = File(Environment.getExternalStorageDirectory(), "Telegram");
                    file.deleteRecursively()
                    Toast.makeText(bf.parentActivity, LocaleController.getString("SP_RemovedS", R.string.SP_RemovedS), Toast.LENGTH_SHORT).show()
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