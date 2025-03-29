/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.drawer.helpers

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.messenger.FileLoader
import org.telegram.messenger.ImageLocation
import org.telegram.messenger.MessagesController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.messenger.Utilities
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import kotlin.math.roundToInt

object DrawerBitmapHelper : CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Main.immediate
) {

    var currentAccountBitmap: BitmapDrawable? = null

    fun setAccountBitmap(user: TLRPC.User) {
        val userFull = MessagesController.getInstance(UserConfig.selectedAccount).getUserFull(user.id)
        try {
            var photo: File? = null

            if (user?.photo != null && user.photo.photo_big != null) {
                photo = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(user.photo.photo_big, true)
            } else if (userFull?.profile_photo != null) {
                photo = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(userFull.profile_photo, true)
            } else if (userFull?.fallback_photo != null) {
                photo = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(userFull.fallback_photo, true)
            }

            val photoData = photo?.length()?.let { ByteArray(it.toInt()) }
            val photoIn = FileInputStream(photo)
            DataInputStream(photoIn).readFully(photoData)
            photoIn.close()
            var bg = photoData?.let { BitmapFactory.decodeByteArray(photoData, 0, it.size) }
            if (CherrygramAppearanceConfig.drawerBlur) bg = blurDrawerWallpaper(bg)
            currentAccountBitmap = BitmapDrawable(Resources.getSystem(), bg)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()

            val size: TLRPC.PhotoSize?

            if (userFull?.profile_photo != null && userFull.profile_photo.sizes.isNotEmpty()) {
                size = FileLoader.getClosestPhotoSizeWithSize(userFull.profile_photo.sizes, Int.MAX_VALUE)
                if (size == null) return
                FileLoader.getInstance(UserConfig.selectedAccount).loadFile(
                    ImageLocation.getForPhoto(size, userFull.profile_photo),
                    0,
                    "jpg",
                    FileLoader.PRIORITY_HIGH,
                    1
                )
            } else if (userFull?.fallback_photo != null && userFull.fallback_photo.sizes.isNotEmpty()) {
                size = FileLoader.getClosestPhotoSizeWithSize(userFull.fallback_photo.sizes, Int.MAX_VALUE)
                if (size == null) return
                FileLoader.getInstance(UserConfig.selectedAccount).loadFile(
                    ImageLocation.getForPhoto(size, userFull.fallback_photo),
                    0,
                    "jpg",
                    FileLoader.PRIORITY_HIGH,
                    1
                )
            }
            launch {
                delay(3000)
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.cg_updateDrawerAvatar)
            }
        }
    }

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

        if (src.width <= 640 && src.height <= 640) {
            return Utilities.stackBlurBitmap(src, CherrygramAppearanceConfig.drawerBlurIntensity).let { src }
        }

        val b: Bitmap = if (src.height > src.width) {
            Bitmap.createBitmap((640f * src.width / src.height).roundToInt(), 640, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(640, (640f * src.height / src.width).roundToInt(), Bitmap.Config.ARGB_8888)
        }
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        val rect = Rect(0, 0, b.width, b.height)
        Canvas(b).drawBitmap(src, null, rect, paint)
        Utilities.stackBlurBitmap(b, CherrygramAppearanceConfig.drawerBlurIntensity)
        return b
    }
}