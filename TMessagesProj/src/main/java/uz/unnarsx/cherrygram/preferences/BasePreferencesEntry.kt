package uz.unnarsx.cherrygram.preferences

import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.ui.tgkit.preference.TGKitSettings
import uz.unnarsx.cherrygram.ui.tgkit.preference.types.TGKitListPreference
import uz.unnarsx.cherrygram.ui.tgkit.preference.types.TGKitSettingsCellRow
import uz.unnarsx.cherrygram.ui.tgkit.preference.types.TGKitSwitchPreference
import uz.unnarsx.cherrygram.ui.tgkit.preference.types.TGKitTextDetailRow

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

}