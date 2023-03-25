package uz.unnarsx.cherrygram.preferences

import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.BulletinFactory
import uz.unnarsx.cherrygram.tgkit.preference.TGKitSettings
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitListPreference
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitSettingsCellRow
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitSwitchPreference
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitTextDetailRow

interface BasePreferencesEntry {
    fun getProcessedPrefs(bf: BaseFragment): TGKitSettings {
        return getPreferences(bf).also {
            it.categories.forEach { c ->
                val lastIndex = c.preferences.lastIndex
                c.preferences.forEachIndexed { index, pref ->
                    val isNotLast = index != lastIndex
                    when (pref) {
                        is TGKitListPreference -> {
                            pref.divider = isNotLast
                        }
                        is TGKitSettingsCellRow -> {
                            pref.divider = isNotLast
                        }
                        is TGKitTextDetailRow -> {
                            pref.divider = isNotLast
                        }
                        is TGKitSwitchPreference -> {
                            pref.divider = isNotLast
                        }
                    }
                }
            }
        }
    }

    fun getPreferences(bf: BaseFragment): TGKitSettings

    fun createRestartBulletin(bf: BaseFragment) {
        BulletinFactory.of(bf).createRestartBulletin(
            R.raw.chats_infotip,
            LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
            LocaleController.getString("BotUnblock", R.string.BotUnblock)
        ) {
        }.show()
    }
}