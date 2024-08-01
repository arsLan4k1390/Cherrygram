package uz.unnarsx.cherrygram.core

import uz.unnarsx.cherrygram.CherrygramConfig

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGFeatureHooks {

    @JvmStatic
    fun setFlashLight(b: Boolean) {
        // ...
        CherrygramConfig.whiteBackground = b
    }

    @JvmStatic
    fun switchNoAuthor(b: Boolean) {
        // ...
        CherrygramConfig.noAuthorship = b
    }

    @JvmStatic
    fun switchGifSpoilers(b: Boolean) {
        // ...
        CherrygramConfig.gifSpoilers = b
    }

}