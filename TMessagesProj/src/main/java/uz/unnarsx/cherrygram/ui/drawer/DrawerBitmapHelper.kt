package uz.unnarsx.cherrygram.ui.drawer

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.telegram.messenger.FileLoader
import org.telegram.messenger.ImageLocation
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.messenger.Utilities
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.CherrygramConfig
import java.io.DataInputStream
import java.io.FileInputStream
import java.io.FileNotFoundException
import kotlin.math.roundToInt

class DrawerBitmapHelper: CoroutineScope by MainScope() {

    companion object {

        @JvmField
        var currentAccountBitmap: BitmapDrawable? = null
        var size: TLRPC.PhotoSize? = null

        @JvmStatic
        fun setAccountBitmap(user: TLRPC.User) {
            if (user.photo != null) {
                try {
                    val photo = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(user.photo.photo_big, true)
                    photo.mkdirs()
                    FileInputStream(photo).use { photoIn ->
                        DataInputStream(photoIn).use { data ->
                            val photoData = ByteArray(photo.length().toInt())
                            data.readFully(photoData)
                            var bg = BitmapFactory.decodeByteArray(photoData, 0, photoData.size)
                            if (CherrygramConfig.drawerBlur) bg = blurDrawerWallpaper(bg)
                            currentAccountBitmap = BitmapDrawable(Resources.getSystem(), bg)
                        }
                    }
                } catch (e: FileNotFoundException) {
                    try {
                        val userFull = MessagesController.getInstance(UserConfig.selectedAccount).getUserFull(user.id)

                        try {
                            size = FileLoader.getClosestPhotoSizeWithSize(userFull.profile_photo.sizes, Int.MAX_VALUE)
                        } catch (ignored: Exception) {
                            if (userFull.fallback_photo != null) {
                                size = FileLoader.getClosestPhotoSizeWithSize(userFull.fallback_photo.sizes, Int.MAX_VALUE)
                            }
                        }

                        FileLoader.getInstance(UserConfig.selectedAccount).loadFile(
                            ImageLocation.getForPhoto(size, userFull.profile_photo),
                            0,
                            "jpg",
                            FileLoader.PRIORITY_LOW,
                            1
                        )
                    } catch (ignored: Exception) {}

                    e.printStackTrace()
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