/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.helpers.network

import android.content.Context
import android.content.res.AssetManager
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.MessageObject
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object StickersManager {

    private const val FILE_NAME = "blocker_stickers_list.txt"
    private const val GITLAB_RAW_URL = "https://gitlab.com/arsLan4k1390/Cherrygram-IDS/-/raw/main/stickers.txt?inline=false"
    private val REFRESH_INTERVAL = if (CherrygramCoreConfig.isDevBuild()) 60 * 60 * 1000L else 24 * 60 * 60 * 1000L // 24 hours

    private val blockedStickersIds = mutableSetOf<Long>()

    suspend fun startAutoRefresh(context: Context) {
        val lastUpdateTime = CherrygramChatsConfig.lastStickersCheckTime
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastUpdateTime > REFRESH_INTERVAL) {
            updaterStickersList(context)
            if (CherrygramCoreConfig.isDevBuild() || CherrygramDebugConfig.showRPCErrors) {
                AndroidUtilities.runOnUIThread {
                    Toast.makeText(ApplicationLoader.applicationContext, "Loaded remote stickers list", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            loadLocalStickersList(context)
            if (CherrygramCoreConfig.isDevBuild() || CherrygramDebugConfig.showRPCErrors) {
                AndroidUtilities.runOnUIThread {
                    Toast.makeText(ApplicationLoader.applicationContext, "Loaded local stickers list", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun updaterStickersList(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(GITLAB_RAW_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val reader = InputStreamReader(connection.inputStream)
                val tempStickerSetIDs = mutableSetOf<Long>()
                val file = File(context.filesDir, FILE_NAME)
                val writer = OutputStreamWriter(file.outputStream())

                reader.buffered().useLines { lines ->
                    lines.forEach { line ->
                        line.trim().toLongOrNull()?.let { id ->
                            tempStickerSetIDs.add(id)
                            writer.write("$id\n")
                        }
                    }
                }

                writer.close()

                synchronized(blockedStickersIds) {
                    blockedStickersIds.clear()
                    blockedStickersIds.addAll(tempStickerSetIDs)
                }

                CherrygramChatsConfig.lastStickersCheckTime = System.currentTimeMillis()

            } catch (e: Exception) {
                FileLog.e(e)
            }
        }
    }

    private suspend fun loadLocalStickersList(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, FILE_NAME)
                if (!file.exists()) return@withContext

                val reader = InputStreamReader(context.openFileInput(FILE_NAME))
                val tempStickerSetIDs = mutableSetOf<Long>()

                reader.buffered().useLines { lines ->
                    lines.forEach { line ->
                        line.trim().toLongOrNull()?.let { id ->
                            tempStickerSetIDs.add(id)
                        }
                    }
                }

                synchronized(blockedStickersIds) {
                    blockedStickersIds.clear()
                    blockedStickersIds.addAll(tempStickerSetIDs)
                }

            } catch (e: Exception) {
                FileLog.e(e)
            }
        }
    }

    private fun isStickerSetBlockedRemote(stickerSetID: Long): Boolean {
        synchronized(blockedStickersIds) {
            return blockedStickersIds.contains(stickerSetID)
        }
    }

    // Locally stored IDs
    private val iDs = arrayOf(683462835916767409L, 1510769529645432834L, 8106175868352593928L, 5835129661968875533L,
        5149354467191160831L, 5091996690789957635L, 7131267980628852734L, 7131267980628852733L, 3346563080237613068L,
        6055278067666911223L, 5062008833983905790L, 1169953291908415506L, 6055278067666911216L, 4331929539736240157L,
        5091996690789957649L, 9087292238668496936L, 6417088260173987842L, 8728063708061761539L, 4238900539514945542L,
        4008340909736329215, 3560771065137332232, 5062008833983905791L, 3442978400170409980L, 4366402085420269571L,
        7346771112912486399L, 1307729623638867964L, 8088313558921641982L
    )

    private fun isLocalSetId(document: TLRPC.Document): Boolean = iDs.any { setID: Long ->
        setID == MessageObject.getStickerSetId(document)
    }

    fun isStickerSetToBlock(document: TLRPC.Document): Boolean {
        return isStickerSetBlockedRemote(document.id) || isLocalSetId(document)
    }

    //Get sticker from assets
    fun copyStickerFromAssets() {
        try {
            val outFile = File(ApplicationLoader.applicationContext.getExternalFilesDir(null), "stickers/cherrygram.webm")
            if (outFile.exists()) return
            outFile.parentFile?.mkdirs()
            val am: AssetManager = ApplicationLoader.applicationContext.assets
            val `in` = am.open("cherrygram.webm")
            val out: OutputStream = FileOutputStream(outFile)
            copyFile(`in`, out)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

}