package uz.unnarsx.cherrygram.preferences.drawer

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.preferences.ktx.*
import uz.unnarsx.cherrygram.preferences.BasePreferencesEntry
import uz.unnarsx.cherrygram.tgkit.CherrygramPreferencesNavigator
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitTextDetailRow
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitTextIconRow

class DrawerIconsPreferencesEntry : BasePreferencesEntry {
    val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AP_DrawerButtonsCategory", R.string.AP_DrawerButtonsCategory)) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(CherrygramConfig.listener)
        category(LocaleController.getString("AP_DrawerButtonsCategory", R.string.AP_DrawerButtonsCategory)) {
            switch {
                title = LocaleController.getString("NewGroup", R.string.NewGroup)
                contract({
                    return@contract CherrygramConfig.createGroupDrawerButton
                }) {
                    CherrygramConfig.createGroupDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("NewSecretChat", R.string.NewSecretChat)

                contract({
                    return@contract CherrygramConfig.secretChatDrawerButton
                }) {
                    CherrygramConfig.secretChatDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("NewChannel", R.string.NewChannel)

                contract({
                    return@contract CherrygramConfig.createChannelDrawerButton
                }) {
                    CherrygramConfig.createChannelDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("Contacts", R.string.Contacts)

                contract({
                    return@contract CherrygramConfig.contactsDrawerButton
                }) {
                    CherrygramConfig.contactsDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("Calls", R.string.Calls)

                contract({
                    return@contract CherrygramConfig.callsDrawerButton
                }) {
                    CherrygramConfig.callsDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("SavedMessages", R.string.SavedMessages)

                contract({
                    return@contract CherrygramConfig.savedMessagesDrawerButton
                }) {
                    CherrygramConfig.savedMessagesDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("ArchivedChats", R.string.ArchivedChats)

                contract({
                    return@contract CherrygramConfig.archivedChatsDrawerButton
                }) {
                    CherrygramConfig.archivedChatsDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("PeopleNearby", R.string.PeopleNearby)

                contract({
                    return@contract CherrygramConfig.peopleNearbyDrawerButton
                }) {
                    CherrygramConfig.peopleNearbyDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("AuthAnotherClient", R.string.AuthAnotherClient)

                contract({
                    return@contract CherrygramConfig.scanQRDrawerButton
                }) {
                    CherrygramConfig.scanQRDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("CGP_AdvancedSettings", R.string.CGP_AdvancedSettings)

                contract({
                    return@contract CherrygramConfig.cGPreferencesDrawerButton
                }) {
                    CherrygramConfig.cGPreferencesDrawerButton = it
                }
            }

        }

        category(LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply)) {
            textIcon {
                title = LocaleController.getString("CG_Restart", R.string.CG_Restart)
                icon = R.drawable.msg_retry
                listener = TGKitTextIconRow.TGTIListener {
                    AppRestartHelper.triggerRebirth(
                        ApplicationLoader.applicationContext,
                        Intent(ApplicationLoader.applicationContext, LaunchActivity::class.java)
                    )
                }
            }
        }

    }
}
