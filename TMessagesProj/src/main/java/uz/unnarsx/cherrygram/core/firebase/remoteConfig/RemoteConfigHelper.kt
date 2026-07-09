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
            2 * HOUR

    private const val FILE_NAME = "remote_config.json"
    private val REMOTE_URLS = listOf(
        "https://gitlab.com/arsLan4k1390/Cherrygram-IDS/-/raw/main/remote_config.json?inline=false",
        "https://raw.githubusercontent.com/arsLan4k1390/Cherrygram-IDS/main/remote_config.json",
        "https://cdn.jsdelivr.net/gh/arsLan4k1390/Cherrygram-IDS@main/remote_config.json"
    )

    @Volatile
    private var config = JSONObject()

    private val defaults = mapOf<String, Any>(
        RemoteConfigConstants.show_ads_screen_in_settings to true,
        RemoteConfigConstants.show_ads_in_play_store_builds to true,
        RemoteConfigConstants.videomessages_resolution to 512,

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

        RemoteConfigConstants.show_proxy_in_settings to false,
        RemoteConfigConstants.proxy_link_in_settings to "https://t.me/proxy?server=45.67.131.84&port=8443&secret=ee0123456789abcdef0123456789abcdef7777772e676f6f676c652e636f6d",
    )

    suspend fun fetchAndActivate(): Boolean {
        return try {
            startAutoRefresh(ApplicationLoader.applicationContext)
            applyConfig()
            true
        } catch (e: Exception) {
            CherrygramLogger.e(e)
            false
        }
    }

    private suspend fun startAutoRefresh(context: Context) = coroutineScope {
        val lastUpdate = CherrygramFirebaseConfig.lastRemoteConfigCheckTime
        val now = System.currentTimeMillis()

        if (now - lastUpdate > REFRESH_INTERVAL) {
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

    fun applyConfig() {
        checkAdsScreenInSettings()
        checkAdsInPlayStoreBuilds()
        checkVideoMessagesResolution()

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

        checkProxyInSettings()
    }

    private fun checkAdsScreenInSettings() {
        val oldShowAdsScreenInSettings = CherrygramFirebaseConfig.showAdsScreenInSettings
        val newShowAdsScreenInSettings = getBoolean(RemoteConfigConstants.show_ads_screen_in_settings)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_ads_screen_in_settings} value = $newShowAdsScreenInSettings" }
        if (oldShowAdsScreenInSettings != newShowAdsScreenInSettings) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_ads_screen_in_settings} changed $oldShowAdsScreenInSettings -> $newShowAdsScreenInSettings" }
        }

        CherrygramFirebaseConfig.showAdsScreenInSettings = newShowAdsScreenInSettings
    }

    private fun checkAdsInPlayStoreBuilds() {
        val oldShowAdsInPlayStoreBuilds = CherrygramFirebaseConfig.showAdsInPlayStoreBuilds
        val newShowAdsInPlayStoreBuilds = getBoolean(RemoteConfigConstants.show_ads_in_play_store_builds)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_ads_in_play_store_builds} value = $newShowAdsInPlayStoreBuilds" }
        if (oldShowAdsInPlayStoreBuilds != newShowAdsInPlayStoreBuilds) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_ads_in_play_store_builds} changed $oldShowAdsInPlayStoreBuilds -> $newShowAdsInPlayStoreBuilds" }
        }

        CherrygramFirebaseConfig.showAdsInPlayStoreBuilds = newShowAdsInPlayStoreBuilds
    }

    private fun checkVideoMessagesResolution() {
        val oldResolutionValue = CherrygramCameraConfig.videoMessagesResolution
        val newResolutionValue = getInt(RemoteConfigConstants.videomessages_resolution)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.videomessages_resolution} value = $newResolutionValue" }
        if (oldResolutionValue != newResolutionValue) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.videomessages_resolution} changed $oldResolutionValue -> $newResolutionValue" }
        }

        CherrygramCameraConfig.videoMessagesResolution = newResolutionValue
    }

    private fun checkHumoCardNumber() {
        val oldHumoCardNumber = CherrygramFirebaseConfig.humoCardNumber
        val newHumoCardNumber = getString(RemoteConfigConstants.humo_card_number)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.humo_card_number} value = $newHumoCardNumber" }
        if (oldHumoCardNumber != newHumoCardNumber) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.humo_card_number} changed $oldHumoCardNumber -> $newHumoCardNumber" }
        }

        CherrygramFirebaseConfig.humoCardNumber = newHumoCardNumber
    }

    private fun checkTBankCardNumber() {
        val oldTBankCardNumber = CherrygramFirebaseConfig.tbankCardNumber
        val newTBankCardNumber = getString(RemoteConfigConstants.tbank_card_number)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.tbank_card_number} value = $newTBankCardNumber" }
        if (oldTBankCardNumber != newTBankCardNumber) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.tbank_card_number} changed $oldTBankCardNumber -> $newTBankCardNumber" }
        }

        CherrygramFirebaseConfig.tbankCardNumber = newTBankCardNumber
    }

    private fun checkSafeStars() {
        val oldSafeStars = CherrygramFirebaseConfig.allowSafeStars
        val newSafeStars = getBoolean(RemoteConfigConstants.allow_use_safestars)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.allow_use_safestars} value = $newSafeStars" }
        if (oldSafeStars != newSafeStars) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.allow_use_safestars} changed $oldSafeStars -> $newSafeStars" }
        }

        CherrygramFirebaseConfig.allowSafeStars = newSafeStars
    }

    private fun checkSafeStarsURL() {
        val oldSafeStarsURL = CherrygramFirebaseConfig.safe_stars_URL
        val newSafeStarsURL = getString(RemoteConfigConstants.safe_stars_URL)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.safe_stars_URL} value = $newSafeStarsURL" }
        if (oldSafeStarsURL != newSafeStarsURL) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.safe_stars_URL} changed $oldSafeStarsURL -> $newSafeStarsURL" }
        }

        CherrygramFirebaseConfig.safe_stars_URL = newSafeStarsURL
    }

    private fun checkSafeStarsURL_RU() {
        val oldSafeStarsURL_RU = CherrygramFirebaseConfig.safe_stars_URL_RU
        val newSafeStarsURL_RU = getString(RemoteConfigConstants.safe_stars_URL_RU)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.safe_stars_URL_RU} value = $newSafeStarsURL_RU" }
        if (oldSafeStarsURL_RU != newSafeStarsURL_RU) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.safe_stars_URL_RU} changed $oldSafeStarsURL_RU -> $newSafeStarsURL_RU" }
        }

        CherrygramFirebaseConfig.safe_stars_URL_RU = newSafeStarsURL_RU
    }

    private fun checkSafeSurf() {
        val oldSafeSurf = CherrygramFirebaseConfig.allowSafeSurf
        val newSafeSurf = getBoolean(RemoteConfigConstants.allow_use_safesurf)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.allow_use_safesurf} value = $newSafeSurf" }
        if (oldSafeSurf != newSafeSurf) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.allow_use_safesurf} changed $oldSafeSurf -> $newSafeSurf" }
        }

        CherrygramFirebaseConfig.allowSafeSurf = newSafeSurf
    }

    private fun checkSafeSurfURL() {
        val oldSafeSurfURL = CherrygramFirebaseConfig.safe_surf_URL
        val newSafeSurfURL = getString(RemoteConfigConstants.safe_surf_URL)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.safe_surf_URL} value = $newSafeSurfURL" }
        if (oldSafeSurfURL != newSafeSurfURL) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.safe_surf_URL} changed $oldSafeSurfURL -> $newSafeSurfURL" }
        }

        CherrygramFirebaseConfig.safe_surf_URL = newSafeSurfURL
    }

    private fun checkSafePay() {
        val oldSafePay = CherrygramFirebaseConfig.allowSafePay
        val newSafePay = getBoolean(RemoteConfigConstants.allow_use_safepay)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.allow_use_safepay} value = $newSafePay" }
        if (oldSafePay != newSafePay) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.allow_use_safepay} changed $oldSafePay -> $newSafePay" }
        }

        CherrygramFirebaseConfig.allowSafePay = newSafePay
    }

    private fun checkSafePayURL() {
        val oldSafePayURL = CherrygramFirebaseConfig.safe_pay_URL
        val newSafePayURL = getString(RemoteConfigConstants.safe_pay_URL)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.safe_pay_URL} value = $newSafePayURL" }
        if (oldSafePayURL != newSafePayURL) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.safe_pay_URL} changed $oldSafePayURL -> $newSafePayURL" }
        }

        CherrygramFirebaseConfig.safe_pay_URL = newSafePayURL
    }

    private fun checkRecordOOMasNonFatal() {
        val oldRecordOOMasNonFatal = CherrygramFirebaseConfig.recordOOMasNonFatal
        val newRecordOOMasNonFatal = getBoolean(RemoteConfigConstants.record_oom_as_nf)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.record_oom_as_nf} value = $newRecordOOMasNonFatal" }
        if (oldRecordOOMasNonFatal != newRecordOOMasNonFatal) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.record_oom_as_nf} changed $oldRecordOOMasNonFatal -> $newRecordOOMasNonFatal" }
        }

        CherrygramFirebaseConfig.recordOOMasNonFatal = newRecordOOMasNonFatal
//        Crashlytics.updateOOMReserve()
    }

    private fun checkProxyInSettings() {
        val oldProxyURL = CherrygramFirebaseConfig.proxyURL
        val newProxyURL = getString(RemoteConfigConstants.proxy_link_in_settings)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.proxy_link_in_settings} value = $newProxyURL" }
        if (oldProxyURL != newProxyURL) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.proxy_link_in_settings} changed $oldProxyURL -> $newProxyURL" }
        }

        CherrygramFirebaseConfig.proxyURL = newProxyURL


        val oldProxyInSettings = CherrygramFirebaseConfig.showProxyInSettings
        val newProxyInSettings = getBoolean(RemoteConfigConstants.show_proxy_in_settings)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_proxy_in_settings} value = $newProxyInSettings" }
        if (oldProxyInSettings != newProxyInSettings) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_proxy_in_settings} changed $oldProxyInSettings -> $newProxyInSettings" }
        }

        CherrygramFirebaseConfig.showProxyInSettings = newProxyInSettings
    }

}