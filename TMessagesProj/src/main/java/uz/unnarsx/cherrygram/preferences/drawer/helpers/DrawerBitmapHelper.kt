/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.drawer.helpers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LightingColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap

object DrawerBitmapHelper {

    private fun darkenBitmap(bm: Bitmap?): Bitmap {
        val canvas = Canvas(bm!!)
        val p = Paint(Color.RED)
        val filter: ColorFilter = LightingColorFilter(-0x808081, 0x00000000)
        p.colorFilter = filter
        canvas.drawBitmap(bm, Matrix(), p)
        return bm
    }

    fun createDrawerWallpaper(src: Bitmap?): Bitmap? {
        if (src == null) {
            return null
        }

        var b: Bitmap = if (src.height > src.width) {
            createBitmap((512f * src.width / src.height).roundToInt(), 512)
        } else {
            createBitmap(512, (512f * src.height / src.width).roundToInt())
        }
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        val rect = Rect(0, 0, b.width, b.height)
        Canvas(b).drawBitmap(src, null, rect, paint)

        if (CherrygramAppearanceConfig.drawerDarken) b = darkenBitmap(b)

        return b
    }

}