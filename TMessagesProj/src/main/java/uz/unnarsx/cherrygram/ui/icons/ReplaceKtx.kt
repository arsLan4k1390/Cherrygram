package uz.unnarsx.cherrygram.ui.icons

import android.util.SparseIntArray

fun newSparseInt(vararg intPairs: Pair<Int, Int>) = SparseIntArray().apply {
    intPairs.forEach {
        this.put(it.first, it.second)
    }
}