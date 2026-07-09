/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.helpers.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.util.component1
import androidx.core.util.component2
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.MessageObject
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import androidx.core.graphics.scale
import org.telegram.messenger.UserConfig
import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper
import uz.unnarsx.cherrygram.core.CherrygramLogger

object StickersManager {

    fun getStickerFileFromImage(originalPath: String): File {
        val dotIndex = originalPath.lastIndexOf('.')
        val basePath = if (dotIndex != -1) originalPath.take(dotIndex) else originalPath
        val webpFile = File("$basePath.webp")

        try {
            val MAX_SIZE = 2560
            var bmp = BitmapFactory.decodeFile(originalPath)
            if (bmp != null) {
                val width = bmp.width
                val height = bmp.height

                if (width > MAX_SIZE || height > MAX_SIZE) {
                    val scale = minOf(MAX_SIZE.toFloat() / width, MAX_SIZE.toFloat() / height)
                    val newWidth = (width * scale).roundToInt()
                    val newHeight = (height * scale).roundToInt()
                    bmp = bmp.scale(newWidth, newHeight)
                }

                val (rotate, flip) = AndroidUtilities.getImageOrientation(originalPath)

                if (rotate != 0 || flip != 0) {
                    val matrix = Matrix()
                    if (flip == 1) {
                        matrix.postScale(-1f, 1f)
                        matrix.postTranslate(bmp.width.toFloat(), 0f)
                    }
                    if (rotate != 0) {
                        matrix.postRotate(rotate.toFloat())
                    }
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                }

                FileOutputStream(webpFile).use { out ->
                    bmp.compress(Bitmap.CompressFormat.WEBP, 50, out)
                }
            }
        } catch (e: Exception) {
            CherrygramLogger.e(e)
        }

        return webpFile
    }

    fun addMessageToClipboardAsSticker(selectedObject: MessageObject, callback: Runnable?) {
        val path = ChatsHelper.getInstance(UserConfig.selectedAccount).getPathToMessage(selectedObject)
        if (path.isNullOrEmpty()) return

        Thread {
            try {
                val image = BitmapFactory.decodeFile(path) ?: return@Thread

                val dotIndex = path.lastIndexOf('.')
                val basePath = if (dotIndex != -1) path.take(dotIndex) else path
                val file = File("$basePath.webp")

                FileOutputStream(file).use { stream ->
                    image.compress(Bitmap.CompressFormat.WEBP, 50, stream)
                }

                AndroidUtilities.runOnUIThread {
                    ChatsHelper.addFileToClipboard(file, callback)
                }
            } catch (e: Exception) {
                CherrygramLogger.e(e)
            }
        }.start()
    }

}