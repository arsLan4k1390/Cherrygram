package uz.unnarsx.cherrygram.core

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGFeatureHooks {

    @JvmStatic
    fun setFlashLight(b: Boolean) {
        // ...
        CherrygramCameraConfig.whiteBackground = b
    }

    @JvmStatic
    fun switchNoAuthor(b: Boolean) {
        // ...
        CherrygramChatsConfig.noAuthorship = b
    }

    @JvmStatic
    fun switchGifSpoilers(b: Boolean) {
        // ...
        CherrygramChatsConfig.gifSpoilers = b
    }

}