package uz.unnarsx.cherrygram.vkui

import android.util.SparseIntArray

fun newSparseInt(vararg intPairs: Pair<Int, Int>) = SparseIntArray().apply {
    intPairs.forEach {
        this.put(it.first, it.second)
    }
}