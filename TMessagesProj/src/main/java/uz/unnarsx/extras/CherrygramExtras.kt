package uz.unnarsx.extras

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import uz.unnarsx.cherrygram.CherrygramConfig.drawerBlur
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.ColorInt
import org.telegram.messenger.SharedConfig
import org.telegram.tgnet.TLRPC
import org.telegram.messenger.FileLoader
import org.telegram.messenger.Utilities
import java.io.DataInputStream
import java.io.FileInputStream
import java.lang.Exception



object CherrygramExtras {

    var CG_VERSION = "6.5.1"
    var CG_AUTHOR = "Updates: t.me/cherry_gram"
    @JvmField
    var currentAccountBitmap: BitmapDrawable? = null

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
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

    @JvmStatic
    fun setAccountBitmap(user: TLRPC.User) {
        if (user.photo != null) {
            try {
                val photo = FileLoader.getPathToAttach(user.photo.photo_big, true)
                val photoData = ByteArray(photo.length().toInt())
                val photoIn: FileInputStream
                photoIn = FileInputStream(photo)
                DataInputStream(photoIn).readFully(photoData)
                photoIn.close()
                var bg = BitmapFactory.decodeByteArray(photoData, 0, photoData.size)
                if (drawerBlur) bg = Utilities.blurWallpaper(bg)
                currentAccountBitmap = BitmapDrawable(Resources.getSystem(), bg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun darkenBitmap(bm: Bitmap): Bitmap {
        val canvas = Canvas(bm)
        val p = Paint(Color.RED)
        val filter: ColorFilter = LightingColorFilter(-0x808081, 0x00000000) // darken
        p.colorFilter = filter
        canvas.drawBitmap(bm, Matrix(), p)
        return bm
    }

    fun wrapEmoticon(base: String?): String {
        return if (base == null) {
            "\uD83D\uDCC1"
        } else if (base.isEmpty()) {
            //Log.d("CG-Test", Arrays.toString(base.getBytes(StandardCharsets.UTF_16BE)));
            "\uD83D\uDDC2"
        } else {
            //Log.d("CG-Test", Arrays.toString(base.getBytes(StandardCharsets.UTF_16BE)));
            base
        }
    }
}