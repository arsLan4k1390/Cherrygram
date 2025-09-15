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
import org.telegram.messenger.AccountInstance
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.UserConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object DonatesManager {

    private val REFRESH_INTERVAL = if (CherrygramCoreConfig.isDevBuild()) 60 * 60 * 1000L else 6 * 60 * 60 * 1000L // 6 hours

    suspend fun startAutoRefresh(context: Context) {
        val lastUpdateTime = CherrygramCoreConfig.lastDonatesCheckTime
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastUpdateTime > REFRESH_INTERVAL) {
            updateDonateList(context)
            updateDonateListMarketplace(context)
            updateBlockedList(context)
            if (CherrygramCoreConfig.isDevBuild() || CherrygramDebugConfig.showRPCErrors) {
                AndroidUtilities.runOnUIThread {
                    Toast.makeText(ApplicationLoader.applicationContext, "Loaded remote donate list", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            loadLocalDonateList(context)
            loadLocalDonateListMarketplace(context)
            loadLocalBlockedList(context)
            if (CherrygramCoreConfig.isDevBuild() || CherrygramDebugConfig.showRPCErrors) {
                AndroidUtilities.runOnUIThread {
                    Toast.makeText(ApplicationLoader.applicationContext, "Loaded local donate list", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun updateList(
        context: Context,
        urlString: String,
        fileName: String,
        targetSet: MutableSet<Long>,
        fallback: suspend (Context) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val reader = InputStreamReader(connection.inputStream)
                val tempUserIds = mutableSetOf<Long>()

                reader.buffered().useLines { lines ->
                    lines.forEach { line ->
                        line.trim().toLongOrNull()?.let { id ->
                            tempUserIds.add(id)
                        }
                    }
                }

                if (tempUserIds.isNotEmpty()) {
                    val file = File(context.filesDir, fileName)
                    OutputStreamWriter(file.outputStream()).use { writer ->
                        tempUserIds.forEach { id -> writer.write("$id\n") }
                    }

                    synchronized(targetSet) {
                        targetSet.clear()
                        targetSet.addAll(tempUserIds)
                    }

                    CherrygramCoreConfig.lastDonatesCheckTime = System.currentTimeMillis()
                } else {
                    fallback(context)
                }
            } catch (e: Exception) {
                FileLog.e(e)
                fallback(context)
            }
        }
    }

    private suspend fun loadLocalList(
        context: Context,
        fileName: String,
        targetSet: MutableSet<Long>
    ) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, fileName)
                if (!file.exists()) return@withContext

                val tempUserIds = mutableSetOf<Long>()
                InputStreamReader(context.openFileInput(fileName)).use { reader ->
                    reader.buffered().useLines { lines ->
                        lines.forEach { line ->
                            line.trim().toLongOrNull()?.let { id ->
                                tempUserIds.add(id)
                            }
                        }
                    }
                }

                synchronized(targetSet) {
                    targetSet.clear()
                    targetSet.addAll(tempUserIds)
                }
            } catch (e: Exception) {
                FileLog.e(e)
            }
        }
    }

    /** Donates start */
    private const val FILE_NAME = "donated_users_list.txt"
    private const val GITLAB_RAW_URL = "https://gitlab.com/arsLan4k1390/Cherrygram-IDS/-/raw/main/donates.txt?inline=false"
//    private const val GITHUB_RAW_URL = "https://raw.githubusercontent.com/arsLan4k1390/Cherrygram/main/donates.txt"

    private val verifiedUserIds = mutableSetOf<Long>()

    private suspend fun updateDonateList(context: Context) =
        updateList(context, GITLAB_RAW_URL, FILE_NAME, verifiedUserIds, ::loadLocalDonateList)

    private suspend fun loadLocalDonateList(context: Context) =
        loadLocalList(context, FILE_NAME, verifiedUserIds)

    fun didUserDonate(userId: Long): Boolean {
        synchronized(verifiedUserIds) {
            return verifiedUserIds.contains(userId) || didUserDonateForMarketplace(userId)
        }
    }
    /** Donates finish */

    /** Stars start */
    private const val FILE_NAME_MARKETPLACE = "donated_users_list_marketplace.txt"
    private const val GITLAB_RAW_URL_MARKETPLACE = "https://gitlab.com/arsLan4k1390/Cherrygram-IDS/-/raw/main/donates_marketplace.txt?inline=false"

    private val verifiedUserIdsMarketplace = mutableSetOf<Long>()

    private suspend fun updateDonateListMarketplace(context: Context) =
        updateList(context, GITLAB_RAW_URL_MARKETPLACE, FILE_NAME_MARKETPLACE, verifiedUserIdsMarketplace, ::loadLocalDonateListMarketplace)

    private suspend fun loadLocalDonateListMarketplace(context: Context) =
        loadLocalList(context, FILE_NAME_MARKETPLACE, verifiedUserIdsMarketplace)

    fun checkAllDonatedAccountsForMarketplace(): Boolean {
        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val userConfig = AccountInstance.getInstance(i).userConfig
            val userId = userConfig?.currentUser?.id ?: 0L
            if (userConfig != null && userConfig.isClientActivated && userId != 0L) {
                if (didUserDonateForMarketplace(userId)) {
                    if (CherrygramCoreConfig.isDevBuild()) println("Account's ID is in the list: $userId")
                    return true
                } else {
                    if (CherrygramCoreConfig.isDevBuild()) println("Account's ID is not in the list: $userId")
                }
            }
        }
        return false
    }

    fun didUserDonateForMarketplace(userId: Long): Boolean {
        synchronized(verifiedUserIdsMarketplace) {
            return verifiedUserIdsMarketplace.contains(userId)
        }
    }
    /** Stars finish */

    /** Blocked start*/
    private const val FILE_NAME_BLOCKED = "vip_users_list.txt"
    private const val GITLAB_RAW_URL_BLOCKED = "https://gitlab.com/arsLan4k1390/Cherrygram-IDS/-/raw/main/vip_users.txt?inline=false"

    private val blockedUserIds = mutableSetOf<Long>()

    private suspend fun updateBlockedList(context: Context) =
        updateList(context, GITLAB_RAW_URL_BLOCKED, FILE_NAME_BLOCKED, blockedUserIds, ::loadLocalBlockedList)

    private suspend fun loadLocalBlockedList(context: Context) =
        loadLocalList(context, FILE_NAME_BLOCKED, blockedUserIds)

    fun isUsesBlocked(userId: Long): Boolean {
        synchronized(blockedUserIds) {
            return blockedUserIds.contains(userId)
        }
    }
    /** Blocked finish */

}