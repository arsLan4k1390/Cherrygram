/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.donates

import android.content.Context
import org.telegram.messenger.FileLog
import java.io.File
import java.security.MessageDigest
import androidx.core.content.edit
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig

object FileIntegrityUtils {

    private const val PREF_NAME = "file_hashes"

    fun saveFileHash(context: Context, fileName: String, hash: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                putString("${fileName}_hash", hash)
            }
    }

    fun getFileHash(context: Context, fileName: String): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString("${fileName}_hash", null)
    }

    private var isIntegrityRefreshRunning = false

    suspend fun verifyFileIntegrity(context: Context, file: File): Boolean {
        return try {
            if (!file.exists()) {
                if (!isIntegrityRefreshRunning) {
                    isIntegrityRefreshRunning = true
                    try {
                        DonatesManager.startAutoRefresh(context, force = true, fromIntegrityChecker = true)
                    } finally {
                        isIntegrityRefreshRunning = false
                    }
                }
                return false
            }

            if (!file.canRead() || !file.canWrite()) {
                CherrygramCoreConfig.showNotifications = false

                if (!isIntegrityRefreshRunning) {
                    isIntegrityRefreshRunning = true
                    try {
                        DonatesManager.startAutoRefresh(context, force = true, fromIntegrityChecker = true)
                    } finally {
                        isIntegrityRefreshRunning = false
                    }
                }
                return false
            }

            val currentHash = computeFileHash(file)
            val savedHash = getFileHash(context, file.name)

            if (savedHash == null || savedHash != currentHash) {
                if (!isIntegrityRefreshRunning) {
                    isIntegrityRefreshRunning = true
                    try {
                        DonatesManager.startAutoRefresh(context, force = true, fromIntegrityChecker = true)
                    } finally {
                        isIntegrityRefreshRunning = false
                    }
                }
                return false
            }

            true
        } catch (e: Exception) {
            FileLog.e(e)
            false
        }
    }

    fun updateFileHash(context: Context, file: File) {
        if (!file.exists() || !file.canRead()) return
        try {
            val hash = computeFileHash(file)
            saveFileHash(context, file.name, hash)
        } catch (e: Exception) {
            FileLog.e(e)
        }
    }

    private fun computeFileHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

}

