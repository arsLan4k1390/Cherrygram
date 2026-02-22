/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGFeatureHooks {

    fun switchNoAuthor(b: Boolean) {
        CherrygramChatsConfig.noAuthorship = b
    }

    fun switchNoCaptions(b: Boolean) {
        CherrygramChatsConfig.noCaptions = b
    }

}