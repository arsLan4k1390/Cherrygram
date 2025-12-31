/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences

import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.RecyclerListView
import uz.unnarsx.cherrygram.preferences.tgkit.preference.TGKitSettings
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitListPreference
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSettingsCellRow
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSwitchPreference
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextDetailRow

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

    fun setListView(rv: RecyclerListView)

}