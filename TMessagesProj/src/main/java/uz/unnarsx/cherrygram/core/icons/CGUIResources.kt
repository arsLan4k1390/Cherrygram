/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.icons

import android.annotation.SuppressLint
import android.content.res.*
import android.graphics.drawable.Drawable
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
//import com.google.android.exoplayer2.util.Log
import uz.unnarsx.cherrygram.core.icons.icon_replaces.BaseIconReplace

@Suppress("DEPRECATION")
class CGUIResources(private val wrapped: Resources) : Resources(wrapped.assets, wrapped.displayMetrics, wrapped.configuration) {
    private var activeReplacement: BaseIconReplace = CherrygramAppearanceConfig.getCurrentIconPack()
    fun reloadReplacements() {
        activeReplacement = CherrygramAppearanceConfig.getCurrentIconPack()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("UseCompatLoadingForDrawables")
    @Throws(NotFoundException::class)
    override fun getDrawable(id: Int): Drawable? {
//        logAnId("getDrawable/legacy", id)
        return wrapped.getDrawable(activeReplacement.wrap(id))
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Throws(NotFoundException::class)
    override fun getDrawable(id: Int, theme: Theme?): Drawable? {
//        logAnId("getDrawable", id)
        return wrapped.getDrawable(activeReplacement.wrap(id), theme)
    }

    @Deprecated("Deprecated in Java")
    @Throws(NotFoundException::class)
    override fun getDrawableForDensity(id: Int, density: Int): Drawable? {
//        logAnId("getDrawableForDensity/legacy", id)
        return wrapped.getDrawableForDensity(activeReplacement.wrap(id), density)
    }

    override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? {
//        logAnId("getDrawableForDensity", id)
        return wrapped.getDrawableForDensity(activeReplacement.wrap(id), density, theme)
    }

//    private fun logAnId(str: String, id: Int) {
//        Log.d("CGUIResources", "[$str] >> id: $id {name: ${getResourceName(id)}}")
//    }
}