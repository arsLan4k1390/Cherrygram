package uz.unnarsx.cherrygram.preferences.drawer

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import kotlinx.coroutines.*
import org.telegram.messenger.FileLoader
import org.telegram.messenger.UserConfig
import org.telegram.messenger.Utilities
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.CherrygramConfig
import java.io.DataInputStream
import java.io.FileInputStream
import kotlin.math.roundToInt

class DrawerBitmapHelper: CoroutineScope by MainScope() {

    companion object {

        @JvmField
        var currentAccountBitmap: BitmapDrawable? = null

        @JvmStatic
        fun setAccountBitmap(user: TLRPC.User) {
            if (user.photo != null) {
                MainScope().launch {
                    val photo = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(user.photo.photo_big, true)
                    try {
                        withContext(Dispatchers.IO) {
                            FileInputStream(photo).use { photoIn ->
                                DataInputStream(photoIn).use { data ->
                                    val photoData = ByteArray(photo.length().toInt())
                                    data.readFully(photoData)
                                    var bg = BitmapFactory.decodeByteArray(photoData, 0, photoData.size)
                                    if (CherrygramConfig.drawerBlur) bg = blurDrawerWallpaper(bg)
                                    currentAccountBitmap = BitmapDrawable(Resources.getSystem(), bg)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        @JvmStatic
        fun darkenBitmap(bm: Bitmap?): Bitmap {
            val canvas = Canvas(bm!!)
            val p = Paint(Color.RED)
            val filter: ColorFilter = LightingColorFilter(-0x808081, 0x00000000)
            p.colorFilter = filter
            canvas.drawBitmap(bm, Matrix(), p)
            return bm
        }

        private fun blurDrawerWallpaper(src: Bitmap?): Bitmap? {
            if (src == null) {
                return null
            }
            val b: Bitmap = if (src.height > src.width) {
                Bitmap.createBitmap((640f * src.width / src.height).roundToInt(), 640, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(640, (640f * src.height / src.width).roundToInt(), Bitmap.Config.ARGB_8888)
            }
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            val rect = Rect(0, 0, b.width, b.height)
            Canvas(b).drawBitmap(src, null, rect, paint)
            Utilities.stackBlurBitmap(b, CherrygramConfig.drawerBlurIntensity)
            return b
        }
    }
}