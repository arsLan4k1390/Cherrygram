/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.firebase.remoteConfig

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.core.CherrygramLogger
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramExperimentalConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramFirebaseConfig
import uz.unnarsx.cherrygram.donates.SSLUtils.openSecureConnection
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object RemoteConfigHelper {

    private const val MINUTE = 60 * 1000L
    private const val HOUR = 60 * MINUTE

    private val REFRESH_INTERVAL =
        if (CherrygramCoreConfig.isDevBuild())
            30 * MINUTE
        else
            1 * HOUR

    private const val FILE_NAME = "remote_config.json"
    private val REMOTE_URLS = listOf(
        "https://gitlab.com/arsLan4k1390/Cherrygram-IDS/-/raw/main/remote_config.json?inline=false",
        "https://raw.githubusercontent.com/arsLan4k1390/Cherrygram-IDS/main/remote_config.json",
        "https://cdn.jsdelivr.net/gh/arsLan4k1390/Cherrygram-IDS@main/remote_config.json"
    )

    @Volatile
    private var config = JSONObject()

    private val defaults = mapOf<String, Any>(
        RemoteConfigConstants.show_ads_randomly to false,
        RemoteConfigConstants.show_ads_screen_in_settings to true,
        RemoteConfigConstants.show_ads_in_play_store_builds to true,
        RemoteConfigConstants.always_show_adsgram_in_chats to true,

        RemoteConfigConstants.videomessages_resolution to 512,
        RemoteConfigConstants.use_braille_spoiler to true,

        RemoteConfigConstants.humo_card_number to "9860100128256904",
        RemoteConfigConstants.tbank_card_number to "9860100128256904",

        RemoteConfigConstants.allow_use_safestars to true,
        RemoteConfigConstants.safe_stars_URL to "https://safe-stars.com/?partner=cherrygram",
        RemoteConfigConstants.safe_stars_URL_RU to "https://safe-stars.com/ru/?partner=cherrygram",
        RemoteConfigConstants.allow_use_safesurf to true,
        RemoteConfigConstants.safe_surf_URL to "https://t.me/safe_surfbot?start=cherry",
        RemoteConfigConstants.allow_use_safepay to true,
        RemoteConfigConstants.safe_pay_URL to "https://t.me/safepaycard_bot?start=cherry",

        RemoteConfigConstants.record_oom_as_nf to true,
        RemoteConfigConstants.use_cg_oom_handler to true,

        RemoteConfigConstants.show_proxy_in_settings to false,
        RemoteConfigConstants.proxy_link_in_settings to "https://t.me/proxy?server=45.67.131.84&port=8443&secret=ee0123456789abcdef0123456789abcdef7777772e676f6f676c652e636f6d",

        RemoteConfigConstants.show_deleted_gifts to true,
        RemoteConfigConstants.deleted_gifts_config_url to "https://gitlab.com/arsLan4k1390/Cherrygram-IDS/-/raw/main/gift_list.json?inline=false",
        RemoteConfigConstants.deleted_gifts_stickerpack_name to "Cherrygram_HiddenGifts",
        RemoteConfigConstants.deleted_gifts_stickerpack_offset to 0
    )

    suspend fun fetchAndActivate(force: Boolean = false): Boolean {
        return try {
            startAutoRefresh(ApplicationLoader.applicationContext, force)
            applyConfig()
            true
        } catch (e: Exception) {
            CherrygramLogger.e(e)
            false
        }
    }

    private suspend fun startAutoRefresh(context: Context, force: Boolean) = coroutineScope {
        val lastUpdate = CherrygramFirebaseConfig.lastRemoteConfigCheckTime
        val now = System.currentTimeMillis()

        if (force || now - lastUpdate > REFRESH_INTERVAL) {
            try {
                downloadRemoteConfig(context)
                showToast("Loading remote config")
            } catch (e: Exception) {
                CherrygramLogger.e(e, true)
                loadLocalConfig(context)
                showToast("Loading cached remote config")
            } finally {
                CherrygramFirebaseConfig.lastRemoteConfigCheckTime = now
            }
        } else {
            loadLocalConfig(context)
            showToast("Loading cached remote config")
        }
    }

    private suspend fun downloadRemoteConfig(context: Context) =
        withContext(Dispatchers.IO) {
            var lastException: Exception? = null

            for (url in REMOTE_URLS) {
                try {
                    downloadFromUrl(context, url)
                    CherrygramLogger.d { "Remote config loaded successfully from: $url" }
                    return@withContext
                } catch (e: Exception) {
                    CherrygramLogger.w { "Failed to load remote config from: $url" }
                    CherrygramLogger.e(e)
                    lastException = e
                }
            }

            throw lastException ?: IOException("No remote config source available")
        }

    private fun downloadFromUrl(context: Context, url: String) {

        val connection = openSecureConnection(URL(url))
            ?: throw IOException("Unable to open connection: $url")

        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = true

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode} (${connection.responseMessage})")
            }

            val text = connection.inputStream.bufferedReader().use {
                it.readText()
            }

            if (text.isBlank()) {
                throw IOException("Remote config is empty")
            }

            // Проверяем, что ответ является валидным JSON
            val json = JSONObject(text)

            if (json.length() == 0) {
                throw IOException("Remote config JSON is empty")
            }

            val file = File(context.filesDir, FILE_NAME)
            val tempFile = File(context.filesDir, "$FILE_NAME.tmp")

            tempFile.writeText(text)

            if (!tempFile.renameTo(file)) {
                file.delete()

                if (!tempFile.renameTo(file)) {
                    throw IOException("Unable to replace remote config file")
                }
            }

            config = json
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun loadLocalConfig(context: Context) =
        withContext(Dispatchers.IO) {

            val file = File(context.filesDir, FILE_NAME)

            if (!file.exists()) {
                CherrygramLogger.w { "Remote config cache doesn't exist" }
                config = JSONObject()
                return@withContext
            }

            try {
                val text = file.readText()

                if (text.isBlank()) {
                    throw IOException("Cached remote config is empty")
                }

                config = JSONObject(text)

                CherrygramLogger.d { "Loaded cached remote config" }
                showToast("Loaded cached remote config")
            } catch (e: Exception) {
                CherrygramLogger.e(e)
                config = JSONObject()
            }
        }

    private fun showToast(text: String) {
        if (CherrygramCoreConfig.isDevBuild() || CherrygramDebugConfig.showRPCErrors) {
            AndroidUtilities.runOnUIThread {
                Toast.makeText(
                    ApplicationLoader.applicationContext,
                    text,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun getBoolean(key: String): Boolean {
        return when {
            config.has(key) -> config.optBoolean(key)
            else -> defaults[key] as? Boolean ?: false
        }
    }

    fun getInt(key: String): Int {
        return when {
            config.has(key) -> config.optInt(key)
            else -> (defaults[key] as? Number)?.toInt() ?: 0
        }
    }

    fun getLong(key: String): Long {
        return when {
            config.has(key) -> config.optLong(key)
            else -> (defaults[key] as? Number)?.toLong() ?: 0L
        }
    }

    fun getDouble(key: String): Double {
        return when {
            config.has(key) -> config.optDouble(key)
            else -> (defaults[key] as? Number)?.toDouble() ?: .0
        }
    }

    fun getString(key: String): String {
        return when {
            config.has(key) -> config.optString(key)
            else -> defaults[key] as? String ?: ""
        }
    }

    private inline fun <T> checkAndUpdate(
        key: String,
        getValue: (String) -> T,
        current: () -> T,
        update: (T) -> Unit
    ) {
        val oldValue = current()
        val newValue = getValue(key)

        CherrygramLogger.d { "RemoteConfig: $key value = $newValue" }
        if (oldValue != newValue) {
            CherrygramLogger.d { "RemoteConfig: $key changed $oldValue -> $newValue" }
        }

        update(newValue)
    }

    fun applyConfig() {
        checkShowAdsRandomly()
        checkAdsScreenInSettings()
        checkAdsInPlayStoreBuilds()
        checkAlwaysShowAdsGramInChats()

        checkBrailleSpoiler()
        checkVideoMessagesResolution()
        checkMinCherryVersion()

        checkHumoCardNumber()
        checkTBankCardNumber()

        checkSafeStars()
        checkSafeSurf()
        checkSafeStarsURL()
        checkSafeStarsURL_RU()
        checkSafeSurfURL()
        checkSafePay()
        checkSafePayURL()

        checkRecordOOMasNonFatal()
        checkUseCGOOMHandler()

        checkProxyInSettings()

        checkDeletedGifts()
    }

    private fun checkShowAdsRandomly() = checkAndUpdate(
        RemoteConfigConstants.show_ads_randomly,
        ::getBoolean,
        { CherrygramFirebaseConfig.showAdsRandomly },
        { CherrygramFirebaseConfig.showAdsRandomly = it }
    )

    private fun checkAdsScreenInSettings() = checkAndUpdate(
        RemoteConfigConstants.show_ads_screen_in_settings,
        ::getBoolean,
        { CherrygramFirebaseConfig.showAdsScreenInSettings },
        { CherrygramFirebaseConfig.showAdsScreenInSettings = it }
    )

    private fun checkAdsInPlayStoreBuilds() = checkAndUpdate(
        RemoteConfigConstants.show_ads_in_play_store_builds,
        ::getBoolean,
        { CherrygramFirebaseConfig.showAdsInPlayStoreBuilds },
        { CherrygramFirebaseConfig.showAdsInPlayStoreBuilds = it }
    )

    private fun checkAlwaysShowAdsGramInChats() = checkAndUpdate(
        RemoteConfigConstants.always_show_adsgram_in_chats,
        ::getBoolean,
        { CherrygramFirebaseConfig.alwaysShowAdsGramInChats },
        { CherrygramFirebaseConfig.alwaysShowAdsGramInChats = it }
    )

    private fun checkVideoMessagesResolution() = checkAndUpdate(
        RemoteConfigConstants.videomessages_resolution,
        ::getInt,
        { CherrygramCameraConfig.videoMessagesResolution },
        { CherrygramCameraConfig.videoMessagesResolution = it }
    )

    private fun checkBrailleSpoiler() = checkAndUpdate(
        RemoteConfigConstants.always_show_adsgram_in_chats,
        ::getBoolean,
        { CherrygramFirebaseConfig.useBrailleSpoiler },
        { CherrygramFirebaseConfig.useBrailleSpoiler = it }
    )

    private fun checkMinCherryVersion() = checkAndUpdate(
        RemoteConfigConstants.min_cherry_version,
        ::getString,
        { CherrygramCoreConfig.minCherryVersion },
        { CherrygramCoreConfig.minCherryVersion = it }
    )

    private fun checkHumoCardNumber() = checkAndUpdate(
        RemoteConfigConstants.humo_card_number,
        ::getString,
        { CherrygramFirebaseConfig.humoCardNumber },
        { CherrygramFirebaseConfig.humoCardNumber = it }
    )

    private fun checkTBankCardNumber() = checkAndUpdate(
        RemoteConfigConstants.tbank_card_number,
        ::getString,
        { CherrygramFirebaseConfig.tbankCardNumber },
        { CherrygramFirebaseConfig.tbankCardNumber = it }
    )

    private fun checkSafeStars() = checkAndUpdate(
        RemoteConfigConstants.allow_use_safestars,
        ::getBoolean,
        { CherrygramFirebaseConfig.allowSafeStars },
        { CherrygramFirebaseConfig.allowSafeStars = it }
    )

    private fun checkSafeStarsURL() = checkAndUpdate(
        RemoteConfigConstants.safe_stars_URL,
        ::getString,
        { CherrygramFirebaseConfig.safe_stars_URL },
        { CherrygramFirebaseConfig.safe_stars_URL = it }
    )

    private fun checkSafeStarsURL_RU() = checkAndUpdate(
        RemoteConfigConstants.safe_stars_URL_RU,
        ::getString,
        { CherrygramFirebaseConfig.safe_stars_URL_RU },
        { CherrygramFirebaseConfig.safe_stars_URL_RU = it }
    )

    private fun checkSafeSurf() = checkAndUpdate(
        RemoteConfigConstants.allow_use_safesurf,
        ::getBoolean,
        { CherrygramFirebaseConfig.allowSafeSurf },
        { CherrygramFirebaseConfig.allowSafeSurf = it }
    )

    private fun checkSafeSurfURL() = checkAndUpdate(
        RemoteConfigConstants.safe_surf_URL,
        ::getString,
        { CherrygramFirebaseConfig.safe_surf_URL },
        { CherrygramFirebaseConfig.safe_surf_URL = it }
    )

    private fun checkSafePay() = checkAndUpdate(
        RemoteConfigConstants.allow_use_safepay,
        ::getBoolean,
        { CherrygramFirebaseConfig.allowSafePay },
        { CherrygramFirebaseConfig.allowSafePay = it }
    )

    private fun checkSafePayURL() = checkAndUpdate(
        RemoteConfigConstants.safe_pay_URL,
        ::getString,
        { CherrygramFirebaseConfig.safe_pay_URL },
        { CherrygramFirebaseConfig.safe_pay_URL = it }
    )

    private fun checkRecordOOMasNonFatal() {
        checkAndUpdate(
            RemoteConfigConstants.record_oom_as_nf,
            ::getBoolean,
            { CherrygramExperimentalConfig.recordOOMasNonFatal },
            { CherrygramExperimentalConfig.recordOOMasNonFatal = it }
        )
//        Crashlytics.updateOOMReserve()
    }

    private fun checkUseCGOOMHandler() = checkAndUpdate(
        RemoteConfigConstants.use_cg_oom_handler,
        ::getBoolean,
        { CherrygramExperimentalConfig.use_CG_OOMHandler },
        { CherrygramExperimentalConfig.use_CG_OOMHandler = it }
    )

    private fun checkProxyInSettings() {
        checkAndUpdate(
            RemoteConfigConstants.proxy_link_in_settings,
            ::getString,
            { CherrygramFirebaseConfig.proxyURL },
            { CherrygramFirebaseConfig.proxyURL = it }
        )
        checkAndUpdate(
            RemoteConfigConstants.show_proxy_in_settings,
            ::getBoolean,
            { CherrygramFirebaseConfig.showProxyInSettings },
            { CherrygramFirebaseConfig.showProxyInSettings = it }
        )
    }

    private fun checkDeletedGifts() {
        checkAndUpdate(
            RemoteConfigConstants.show_deleted_gifts,
            ::getBoolean,
            { CherrygramFirebaseConfig.showDeletedGifts },
            { CherrygramFirebaseConfig.showDeletedGifts = it }
        )
        checkAndUpdate(
            RemoteConfigConstants.deleted_gifts_config_url,
            ::getString,
            { CherrygramFirebaseConfig.deletedGiftsConfigURL },
            { CherrygramFirebaseConfig.deletedGiftsConfigURL = it }
        )
        checkAndUpdate(
            RemoteConfigConstants.deleted_gifts_stickerpack_name,
            ::getString,
            { CherrygramFirebaseConfig.deletedGiftsStickerPackName },
            { CherrygramFirebaseConfig.deletedGiftsStickerPackName = it }
        )
        checkAndUpdate(
            RemoteConfigConstants.deleted_gifts_stickerpack_offset,
            ::getInt,
            { CherrygramFirebaseConfig.deletedGiftsStickerPackOffset },
            { CherrygramFirebaseConfig.deletedGiftsStickerPackOffset = it }
        )
    }

}