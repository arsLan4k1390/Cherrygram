package uz.unnarsx.extras

import androidx.annotation.ColorInt
import org.telegram.messenger.SharedConfig

object CherrygramExtras {

    var CG_VERSION = "6.7.9"
    var CG_AUTHOR = "Updates: t.me/cherry_gram"

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