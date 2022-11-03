package uz.unnarsx.extras

import androidx.annotation.ColorInt
import org.telegram.messenger.SharedConfig

object CherrygramExtras {

    var CG_VERSION = "7.3.1"
    var CG_AUTHOR = "Updates: @CherrygramAPKs"

    fun getDCGeo(dcId: Int): String? {
        return when (dcId) {
            1, 3 -> "USA (Miami)"
            2, 4 -> "NLD (Amsterdam)"
            5 -> "SGP (Singapore)"
            else -> "UNK (Unknown)"
        }
    }

    fun getDCName(dc: Int): String? {
        return when (dc) {
            1 -> "Pluto"
            2 -> "Venus"
            3 -> "Aurora"
            4 -> "Vesta"
            5 -> "Flora"
            else -> "Unknown"
        }
    }

    @JvmStatic
    @get:ColorInt
    val lightStatusbarColor: Int
        get() = if (SharedConfig.noStatusBar) {
            0x00000000
        } else {
            0x0f000000
        }

    @JvmStatic
    @get:ColorInt
    val darkStatusbarColor: Int
        get() = if (SharedConfig.noStatusBar) {
            0x00000000
        } else {
            0x33000000
        }
}