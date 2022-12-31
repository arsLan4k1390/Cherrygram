package uz.unnarsx.cherrygram.extras

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.ColorInt
import org.telegram.messenger.*
import uz.unnarsx.cherrygram.CherrygramConfig
import java.io.File
import java.util.*

object CherrygramExtras {

    var CG_VERSION = "7.4.1"
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

    fun createForwardDateAndTime(date: Long): String {
        var dateAndTime = date
        try {
            dateAndTime *= 1000
            val rightNow = Calendar.getInstance()
            rightNow.timeInMillis = dateAndTime
            return String.format("%1\$s | %2\$s", LocaleController.getInstance().formatterYear.format(Date(dateAndTime)),
                LocaleController.getInstance().formatterDay.format(Date(dateAndTime))
            )
        } catch (e: Exception) {
            FileLog.e(e)
        }
        return "LOC_ERR"
    }

    private val cherrygramLogo = File(ApplicationLoader.applicationContext.getExternalFilesDir(null), "Telegram/Stickers/cherrygram.webm")
    fun downloadCherrygramLogo(context: Context) {
        if (!cherrygramLogo.exists() && CherrygramConfig.blockStickers) {
            try {
                val request = DownloadManager.Request(Uri.parse("https://github.com/arsLan4k1390/Cherrygram/raw/main/cherrygram.webm"))
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                request.setTitle("Cherrygram Logo")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalFilesDir(context, "Telegram/Stickers/", "cherrygram.webm")
                val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                manager.enqueue(request)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
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