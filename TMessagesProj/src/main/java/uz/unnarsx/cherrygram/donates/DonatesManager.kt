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
import android.util.Base64
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.telegram.messenger.AccountInstance
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.AlertDialog
import uz.unnarsx.cherrygram.Extra
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

object DonatesManager {

    private val REFRESH_INTERVAL = if (CherrygramCoreConfig.isDevBuild()) 60 * 60 * 1000L else 6 * 60 * 60 * 1000L // 6 hours

    suspend fun startAutoRefresh(context: Context, force: Boolean, fromIntegrityChecker: Boolean) = coroutineScope {
        val lastUpdateTime = CherrygramCoreConfig.lastDonatesCheckTime
        val currentTime = System.currentTimeMillis()

        if (fromIntegrityChecker || force || currentTime - lastUpdateTime > REFRESH_INTERVAL) {
            var progressDialog: AlertDialog? = null

            if (force) {
                AndroidUtilities.runOnUIThread {
                    try {
                        progressDialog = AlertDialog(context, AlertDialog.ALERT_TYPE_SPINNER)
                        progressDialog.show()
                    } catch (e: Exception) {
                        FileLog.e(e)
                    }
                }
            }

            try {
                val donateJob = async { updateDonateList(context) }
                val marketplaceJob = async { updateDonateListMarketplace(context) }
                val blockedJob = async { updateBlockedList(context) }
                val colorsJob = async { updateBadgeColors(context) }
                val tonRateJob = async { updateTonUsdtRate(context) }

                awaitAll(donateJob, marketplaceJob, blockedJob, colorsJob, tonRateJob)

                if (!force) showToast("Loaded remote donate list")
            } catch (e: Exception) {
                FileLog.e(e)
                showToast("Error loading donate list, using local cache")

                if (!fromIntegrityChecker) {
                    loadLocalDonateList(context)
                    loadLocalDonateListMarketplace(context)
                    loadLocalBlockedList(context)
                    loadLocalBadgeColors(context, FILE_NAME_BADGE_COLORS)
                }
            } finally {
                CherrygramCoreConfig.lastDonatesCheckTime = System.currentTimeMillis()
                if (force) {
                    AndroidUtilities.runOnUIThread {
                        try {
                            progressDialog?.dismiss()
                            NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.cgDonatesLoaded)
                        } catch (e: Exception) {
                            FileLog.e(e)
                        }
                    }
                }
            }
        } else {
            loadLocalDonateList(context)
            loadLocalDonateListMarketplace(context)
            loadLocalBlockedList(context)
            loadLocalBadgeColors(context, FILE_NAME_BADGE_COLORS)
            showToast("Loaded local donate list")
        }
    }

    private fun showToast(text: String) {
        if (CherrygramCoreConfig.isDevBuild() || CherrygramDebugConfig.showRPCErrors) {
            AndroidUtilities.runOnUIThread {
                Toast.makeText(ApplicationLoader.applicationContext, text, Toast.LENGTH_SHORT).show()
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

                    if (file.exists() && !canAccess(file)) {
                        file.delete()
                    }

                    if (!file.exists()) {
                        file.createNewFile()
                        file.setReadable(true, true)
                        file.setWritable(true, true)
                    }

                    val tempFile = File(context.filesDir, "$fileName.tmp")
                    OutputStreamWriter(tempFile.outputStream()).use { writer ->
                        tempUserIds.forEach { id -> writer.write("$id\n") }
                    }
                    if (!tempFile.renameTo(file)) {
                        file.delete()
                        tempFile.renameTo(file)
                    }

                    FileIntegrityUtils.updateFileHash(context, file)

                    synchronized(targetSet) {
                        targetSet.clear()
                        targetSet.addAll(tempUserIds)
                    }
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

                val isValid = FileIntegrityUtils.verifyFileIntegrity(context, file)
                if (!isValid) return@withContext

                val tempUserIds = mutableSetOf<Long>()
                InputStreamReader(file.inputStream()).use { reader ->
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
    private val FILE_NAME = decodeBase64Array(Extra.FILE_NAME_HASH)
    private val GITLAB_RAW_URL = decodeBase64Array(Extra.GITLAB_RAW_URL_HASH)

    private val verifiedUserIds = mutableSetOf<Long>()

    private suspend fun updateDonateList(context: Context) =
        updateList(context, GITLAB_RAW_URL, FILE_NAME, verifiedUserIds, ::loadLocalDonateList)

    private suspend fun loadLocalDonateList(context: Context) =
        loadLocalList(context, FILE_NAME, verifiedUserIds)

    fun checkAllDonatedAccounts(): Boolean {
        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val userConfig = AccountInstance.getInstance(i).userConfig
            val currentUser = userConfig?.currentUser
            val userId = currentUser?.id ?: 0L

            if (userConfig != null && userConfig.isClientActivated && userId != 0L) {
                if (didUserDonate(userId)) {
                    if (CherrygramCoreConfig.isDevBuild()) println("Account's ID is in the list: $userId")
                    return true
                } else {
                    if (CherrygramCoreConfig.isDevBuild()) println("Account's ID is not in the list: $userId")
                }
            }
        }
        return false
    }

    fun didUserDonate(userId: Long): Boolean {
        synchronized(verifiedUserIds) {
            return verifiedUserIds.contains(userId) || didUserDonateForMarketplace(userId)
        }
    }

    fun didUserDonate2(userId: Long): Boolean {
        synchronized(verifiedUserIds) {
            return verifiedUserIds.contains(userId)
        }
    }

    fun didUserDonateForFeature() : Boolean {
        return checkAllDonatedAccounts() || checkAllDonatedAccountsForMarketplace()
    }
    /** Donates finish */

    /** Stars start */
    private val FILE_NAME_MARKETPLACE = decodeBase64Array(Extra.FILE_NAME_MARKETPLACE_HASH)
    private val GITLAB_RAW_URL_MARKETPLACE = decodeBase64Array(Extra.GITLAB_RAW_URL_MARKETPLACE_HASH)

    private val verifiedUserIdsMarketplace = mutableSetOf<Long>()

    private suspend fun updateDonateListMarketplace(context: Context) =
        updateList(context, GITLAB_RAW_URL_MARKETPLACE, FILE_NAME_MARKETPLACE, verifiedUserIdsMarketplace, ::loadLocalDonateListMarketplace)

    private suspend fun loadLocalDonateListMarketplace(context: Context) =
        loadLocalList(context, FILE_NAME_MARKETPLACE, verifiedUserIdsMarketplace)

    fun checkAllDonatedAccountsForMarketplace(): Boolean {
        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val userConfig = AccountInstance.getInstance(i).userConfig
            val currentUser = userConfig?.currentUser
            val userId = currentUser?.id ?: 0L

            if (userConfig != null && userConfig.isClientActivated && userId != 0L) {
                if (didUserDonateForMarketplace(userId)) {
                    if (CherrygramCoreConfig.isDevBuild()) println("Account's ID is in the list: $userId")
                    CherrygramCoreConfig.showNotifications = true
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
    private val FILE_NAME_BLOCKED = decodeBase64Array(Extra.FILE_NAME_BLOCKED_HASH)
    private val GITLAB_RAW_URL_BLOCKED = decodeBase64Array(Extra.GITLAB_RAW_URL_BLOCKED_HASH)

    private val blockedUserIds = mutableSetOf<Long>()

    private suspend fun updateBlockedList(context: Context) =
        updateList(context, GITLAB_RAW_URL_BLOCKED, FILE_NAME_BLOCKED, blockedUserIds, ::loadLocalBlockedList)

    private suspend fun loadLocalBlockedList(context: Context) =
        loadLocalList(context, FILE_NAME_BLOCKED, blockedUserIds)

    fun isUserBlocked(userId: Long): Boolean {
        synchronized(blockedUserIds) {
            return blockedUserIds.contains(userId)
        }
    }
    /** Blocked finish */

    /** Badge colors start */
    private val FILE_NAME_BADGE_COLORS = decodeBase64Array(Extra.FILE_NAME_BADGE_COLORS_HASH)
    private val GITLAB_RAW_URL_BADGE_COLORS = decodeBase64Array(Extra.GITLAB_RAW_URL_BADGE_COLORS_HASH)

    private suspend fun updateBadgeColors(context: Context) =
        updateListColors(context, GITLAB_RAW_URL_BADGE_COLORS, FILE_NAME_BADGE_COLORS)

    private suspend fun updateListColors(context: Context, urlString: String, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val tempMap = mutableMapOf<Long, BadgeHelper.UserColor>()
                InputStreamReader(connection.inputStream).buffered().useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.split(",").map { it.trim() }
                        if (parts.size < 4) return@forEach
                        val userId = parts[0].toLongOrNull() ?: return@forEach
                        val light = parts[1].let {
                            if (it.startsWith("#")) BadgeHelper.convertColor(it, parts[3].toIntOrNull() ?: 255)
                            else it.toIntOrNull() ?: return@forEach
                        }
                        val dark = parts[2].let {
                            if (it.startsWith("#")) BadgeHelper.convertColor(it, parts[3].toIntOrNull() ?: 255)
                            else it.toIntOrNull() ?: return@forEach
                        }
                        val alpha = parts[3].toIntOrNull() ?: 255
                        tempMap[userId] = BadgeHelper.UserColor(light, dark, alpha)
                    }
                }

                if (tempMap.isNotEmpty()) {
                    val file = File(context.filesDir, fileName)

                    if (file.exists() && !canAccess(file)) {
                        file.delete()
                    }

                    if (!file.exists()) {
                        file.createNewFile()
                        file.setReadable(true, true)
                        file.setWritable(true, true)
                    }

                    val tempFile = File(context.filesDir, "$fileName.tmp")
                    OutputStreamWriter(tempFile.outputStream()).use { writer ->
                        tempMap.forEach { (id, uc) ->
                            writer.write("$id,${uc.lightColor},${uc.darkColor},${uc.alpha}\n")
                        }
                    }
                    if (!tempFile.renameTo(file)) {
                        file.delete()
                        tempFile.renameTo(file)
                    }

                    FileIntegrityUtils.updateFileHash(context, file)

                    BadgeHelper.updateBadgeColorsMap(tempMap)
                } else {
                    loadLocalBadgeColors(context, fileName)
                }
            } catch (e: Exception) {
                FileLog.e(e)
                loadLocalBadgeColors(context, fileName)
            }
        }
    }

    private suspend fun loadLocalBadgeColors(context: Context, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, fileName)

                val isValid = FileIntegrityUtils.verifyFileIntegrity(context, file)
                if (!isValid) return@withContext

                val tempMap = mutableMapOf<Long, BadgeHelper.UserColor>()
                InputStreamReader(file.inputStream()).buffered().useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.split(",").map { it.trim() }
                        if (parts.size < 4) return@forEach
                        val userId = parts[0].toLongOrNull() ?: return@forEach
                        val light = parts[1].toIntOrNull() ?: return@forEach
                        val dark = parts[2].toIntOrNull() ?: return@forEach
                        val alpha = parts[3].toIntOrNull() ?: 255
                        tempMap[userId] = BadgeHelper.UserColor(light, dark, alpha)
                    }
                }

                if (tempMap.isNotEmpty()) {
                    BadgeHelper.updateBadgeColorsMap(tempMap)
                }
            } catch (e: Exception) {
                FileLog.e(e)
            }
        }
    }
    /** Badge colors finish */

    /** USDT to TON converter start */
    private val FILE_NAME_TON_RATE = decodeBase64Array(Extra.FILE_NAME_TON_RATE_HASH)
    private val TON_RATE_URL = decodeBase64Array(Extra.TON_RATE_URL_HASH)

    @Volatile
    private var tonRateFinal = 0.0

    fun loadLocalTonUsdtRateSync(context: Context) {
        try {
            val file = File(context.filesDir, FILE_NAME_TON_RATE)
            if (!file.exists()) return

            val jsonString = file.readText()
            val json = JSONObject(jsonString)
            val ton = json.getJSONObject("ton")
            val rawRate = ton.getDouble("usdt")
            tonRateFinal = rawRate
        } catch (e: Exception) {
            FileLog.e(e)
        }
    }

    suspend fun updateTonUsdtRate(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(TON_RATE_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                val ton = json.getJSONObject("ton")
                val rawRate = ton.getDouble("usdt")

                tonRateFinal = rawRate

                val file = File(context.filesDir, FILE_NAME_TON_RATE)
                if (file.exists() && !canAccess(file)) file.delete()
                if (!file.exists()) file.createNewFile()
                file.setReadable(true, true)
                file.setWritable(true, true)
                file.writeText(jsonString)
                FileIntegrityUtils.updateFileHash(context, file)

            } catch (e: Exception) {
                FileLog.e(e)
                loadLocalTonUsdtRateSync(context)
            }
        }
    }

    fun getTonUsdtRate(context: Context): Double {
        if (tonRateFinal == 0.0) loadLocalTonUsdtRateSync(context)
        return tonRateFinal
    }

    fun getTonAmountForUsd(context: Context, usdPrice: Double, marketplace: Boolean): Double {
        val rate = getTonUsdtRate(context) * 0.80
        val amount = usdPrice / rate
        return max(if (marketplace) 4.0 else 2.0, roundTo(amount, 2))
    }

    fun roundTo(value: Double, decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return round(value * factor) / factor
    }
    /** USDT to TON converter end */

    fun donatesCounter(type: Int) : Int {
        return when (type) {
            0 -> verifiedUserIds.size
            1 -> verifiedUserIdsMarketplace.size
            else -> verifiedUserIds.size + verifiedUserIdsMarketplace.size
        }
    }

    private fun canAccess(file: File): Boolean {
        val can = file.canRead() && file.canWrite()
        if (!can) CherrygramCoreConfig.showNotifications = false
        return can
    }

    fun decodeBase64Array(parts: Array<String>): String {
        val joined = parts.joinToString("")
        val decodedBytes = Base64.decode(joined, Base64.DEFAULT)
        return String(decodedBytes)
    }

}