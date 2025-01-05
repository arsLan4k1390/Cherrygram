/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.icons.icon_replaces

import android.util.SparseIntArray
import androidx.core.util.containsKey

abstract class BaseIconReplace {
    abstract val replaces: SparseIntArray

    fun wrap(id: Int): Int {
        if (replaces.containsKey(id)) return replaces[id]
        return id
    }
}