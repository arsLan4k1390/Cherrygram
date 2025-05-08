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
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object DonatesManager {

    private const val FILE_NAME = "donated_users_list.txt"
    private const val GITLAB_RAW_URL = "https://gitlab.com/arsLan4k1390/Cherrygram-IDS/-/raw/main/donates.txt?inline=false"
    private const val GITHUB_RAW_URL = "https://raw.githubusercontent.com/arsLan4k1390/Cherrygram/main/donates.txt"
    private val REFRESH_INTERVAL = if (CherrygramCoreConfig.isDevBuild()) 60 * 60 * 1000L else 6 * 60 * 60 * 1000L // 6 hours

    private val verifiedUserIds = mutableSetOf<Long>()

    suspend fun startAutoRefresh(context: Context) {
        val lastUpdateTime = CherrygramCoreConfig.lastDonatesCheckTime
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastUpdateTime > REFRESH_INTERVAL) {
            updateDonateList(context)
            if (CherrygramCoreConfig.isDevBuild() || CherrygramDebugConfig.showRPCErrors) {
                AndroidUtilities.runOnUIThread {
                    Toast.makeText(ApplicationLoader.applicationContext, "Loaded remote donate list", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            loadLocalDonateList(context)
            if (CherrygramCoreConfig.isDevBuild() || CherrygramDebugConfig.showRPCErrors) {
                AndroidUtilities.runOnUIThread {
                    Toast.makeText(ApplicationLoader.applicationContext, "Loaded local donate list", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun didUserDonate(userId: Long): Boolean {
        synchronized(verifiedUserIds) {
            return verifiedUserIds.contains(userId)
        }
    }

    private suspend fun updateDonateList(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(GITLAB_RAW_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val reader = InputStreamReader(connection.inputStream)
                val tempUserIds = mutableSetOf<Long>()
                val file = File(context.filesDir, FILE_NAME)
                val writer = OutputStreamWriter(file.outputStream())

                reader.buffered().useLines { lines ->
                    lines.forEach { line ->
                        line.trim().toLongOrNull()?.let { id ->
                            tempUserIds.add(id)
                            writer.write("$id\n")
                        }
                    }
                }

                writer.close()

                synchronized(verifiedUserIds) {
                    verifiedUserIds.clear()
                    verifiedUserIds.addAll(tempUserIds)
                }

                CherrygramCoreConfig.lastDonatesCheckTime = System.currentTimeMillis()

            } catch (e: Exception) {
                FileLog.e(e)
            }
        }
    }

    private suspend fun loadLocalDonateList(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, FILE_NAME)
                if (!file.exists()) return@withContext

                val reader = InputStreamReader(context.openFileInput(FILE_NAME))
                val tempUserIds = mutableSetOf<Long>()

                reader.buffered().useLines { lines ->
                    lines.forEach { line ->
                        line.trim().toLongOrNull()?.let { id ->
                            tempUserIds.add(id)
                        }
                    }
                }

                synchronized(verifiedUserIds) {
                    verifiedUserIds.clear()
                    verifiedUserIds.addAll(tempUserIds)
                }

            } catch (e: Exception) {
                FileLog.e(e)
            }
        }
    }

}