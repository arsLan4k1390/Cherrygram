package uz.unnarsx.cherrygram.core.icons

import android.util.SparseIntArray

fun newSparseInt(vararg intPairs: Pair<Int, Int>) = SparseIntArray().apply {
    intPairs.forEach {
        this.put(it.first, it.second)
    }
}