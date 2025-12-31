/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.tgkit.preference

import androidx.core.util.Pair
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitListPreference
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSliderPreference
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSwitchPreference
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextDetailRow
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextHintRow
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow

fun tgKitScreen(name: String, block: TGKitScreen.() -> Unit) = TGKitSettings(name, mutableListOf<TGKitCategory>().apply(block))

fun TGKitScreen.category(name: String?, block: TGKitPreferences.() -> Unit) = add(
        TGKitCategory(name, mutableListOf<TGKitPreference>().apply(block))
)

fun TGKitScreen.category(name: String?, isAvailable: Boolean, block: TGKitPreferences.() -> Unit) = add(
    TGKitCategory(name, isAvailable, mutableListOf<TGKitPreference>().apply(block))
)

fun TGKitPreferences.list(block: TGKitListPreference.() -> Unit) = add(TGKitListPreference().apply(block))
fun TGKitPreferences.switch(block: TGKitSwitchPreference.() -> Unit) = add(TGKitSwitchPreference().apply(block))
fun TGKitPreferences.slider(block: TGKitSliderPreference.() -> Unit) = add(TGKitSliderPreference().apply(block))
fun TGKitPreferences.textIcon(block: TGKitTextIconRow.() -> Unit) = add(TGKitTextIconRow().apply(block))
fun TGKitPreferences.textDetail(block: TGKitTextDetailRow.() -> Unit) = add(TGKitTextDetailRow().apply(block))
fun TGKitPreferences.hint(text: CharSequence) = add(TGKitTextHintRow().also { it.title = text })

fun TGKitSwitchPreference.contract(getValue: () -> Boolean, setValue: (Boolean) -> Unit) {
    contract = object : TGKitSwitchPreference.TGSPContract {
        override fun getPreferenceValue() = getValue()
        override fun toggleValue() = setValue(!getValue())
    }
}

fun TGKitListPreference.contract(getOptions: () -> List<Pair<Int, String>>, getValue: () -> String, setValue: (Int) -> Unit) {
    contract = object : TGKitListPreference.TGTLContract {
        override fun setValue(id: Int) {
            setValue(id)
        }

        override fun hasIcons(): Boolean {
            return false
        }

        override fun getOptionsIcons(): MutableList<Triple<Int, String, Int>> {
            return mutableListOf()
        }

        override fun getValue(): String {
            return getValue()
        }

        override fun getOptions(): List<Pair<Int, String>> {
            return getOptions()
        }
    }
}

typealias TGKitScreen = MutableList<TGKitCategory>
typealias TGKitPreferences = MutableList<TGKitPreference>