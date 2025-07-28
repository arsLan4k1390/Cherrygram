/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.tgkit.preference.types

import android.app.Activity
import androidx.core.util.Pair
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.AlertsCreator
import uz.unnarsx.cherrygram.preferences.tgkit.preference.TGKitPreference

class TGKitListPreference : TGKitPreference() {
    var divider = false
    var contract: TGTLContract? = null

    override fun getType(): TGPType {
        return TGPType.LIST
    }

    fun callActionHueta(bf: BaseFragment, pr: Activity, ti: TempInterface) {
        var selected = 0
        val titleArray = mutableListOf<String>()
        val idArray = mutableListOf<Int>()

        if (contract!!.hasIcons()) {
            contract!!.getOptionsIcons().forEachIndexed { index, triple ->
                titleArray.add(triple.second)
                idArray.add(triple.first)

                if (contract!!.getValue() == triple.second) selected = index
            }
        } else {
            contract!!.getOptions().forEachIndexed { index, pair ->
                titleArray.add(pair.second)
                idArray.add(pair.first)

                if (contract!!.getValue() == pair.second) selected = index
            }
        }

        val d = AlertsCreator.createSingleChoiceDialog(pr, titleArray.toTypedArray(),
            title.toString(), selected) { _, sel ->
            contract!!.setValue(idArray[sel])
            ti.update()
        }

        bf.visibleDialog = d

        d.show()
    }

    interface TGTLContract {
        fun setValue(id: Int)
        fun getValue(): String
        fun getOptions(): List<Pair<Int, String>>
        fun getOptionsIcons(): List<Triple<Int, String, Int?>>
        fun hasIcons(): Boolean
    }

    interface TempInterface {
        fun update()
    }
}