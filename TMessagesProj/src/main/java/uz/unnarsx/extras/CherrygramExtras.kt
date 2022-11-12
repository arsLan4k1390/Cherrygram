package uz.unnarsx.extras

import android.os.Build
import androidx.annotation.ColorInt
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.SharedConfig
import uz.unnarsx.cherrygram.CherrygramConfig

object CherrygramExtras {

    var CG_VERSION = "7.3.4"
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

    fun getAbiCode(): String {
        val pInfo = ApplicationLoader.applicationContext.packageManager.getPackageInfo(
            ApplicationLoader.applicationContext.packageName,
            0
        )
        var abi = ""
        when (pInfo.versionCode % 10) {
            1, 3 -> abi = "arm-v7a"
            2, 4 -> abi = "x86"
            5, 7 -> abi = "arm64-v8a"
            6, 8 -> abi = "x86_64"
            0, 9 -> abi = (if (!CherrygramConfig.isDirectApp()) BuildConfig.BUILD_TYPE else "universal") + " " + Build.SUPPORTED_ABIS[0]
        }
        return abi
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