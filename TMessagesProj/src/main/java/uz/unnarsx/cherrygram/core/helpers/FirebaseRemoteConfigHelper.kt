/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.helpers

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.core.CherrygramLogger
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig
import uz.unnarsx.cherrygram.misc.Constants

object FirebaseRemoteConfigHelper {

    private val remoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    private val defaults = mapOf(
        Constants.show_ads_screen_in_settings to true,
        Constants.videomessages_resolution to 512L,

        Constants.humo_card_number to "9860100128256904",
        Constants.tbank_card_number to "9860100128256904",

        Constants.allow_use_safestars to true,
        Constants.allow_use_safesurf to true,
        Constants.safe_stars_URL to "https://safe-stars.com/?partner=cherrygram",
        Constants.safe_stars_URL_RU to "https://safe-stars.com/ru/?partner=cherrygram",
        Constants.safe_surf_URL to "https://t.me/safe_surfbot?start=cherry",

        Constants.show_proxy_in_settings to false,
        Constants.proxy_link_in_settings to "https://t.me/proxy?server=45.67.131.84&port=8443&secret=ee0123456789abcdef0123456789abcdef7777772e676f6f676c652e636f6d",
    )

    fun init() {
        Firebase.analytics.setUserProperty("cherrygram_version", CGResourcesHelper.getCherryVersion())
        Firebase.analytics.setUserProperty("code_version", CGResourcesHelper.getCodeVersion())
        Firebase.analytics.setUserProperty("source_code_version", CGResourcesHelper.getSourceCodeVersion())

        remoteConfig.setDefaultsAsync(defaults)
    }

    suspend fun fetchAndActivate(): Boolean {
        val interval = if (CherrygramCoreConfig.isDevBuild()) 10800L else 21600L // 3/6 hours

        return try {
            remoteConfig.fetch(interval).await()
            remoteConfig.activate().await()
            true
        } catch (e: Exception) {
            CherrygramLogger.e(e)
            false
        }
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        if (!ApplicationLoader.checkPlayServices()) return default

        return try {
            remoteConfig.getBoolean(key)
        } catch (e: Exception) {
            CherrygramLogger.e(e)
            default
        }
    }

    fun getInt(key: String, default: Int = 0): Int {
        if (!ApplicationLoader.checkPlayServices()) return default

        return try {
            remoteConfig.getLong(key).toInt()
        } catch (e: Exception) {
            CherrygramLogger.e(e)
            default
        }
    }

    fun getString(key: String, default: String = ""): String {
        if (!ApplicationLoader.checkPlayServices()) return default

        return try {
            remoteConfig.getString(key)
        } catch (e: Exception) {
            CherrygramLogger.e(e)
            default
        }
    }

    fun applyConfig() {
        checkAdsScreenInSettings()
        checkVideoMessagesResolution()

        checkHumoCardNumber()
        checkTBankCardNumber()

        checkSafeStars()
        checkSafeSurf()
        checkSafeStarsURL()
        checkSafeStarsURL_RU()
        checkSafeSurfURL()

        checkProxyInSettings()
    }

    private fun checkAdsScreenInSettings() {
        val oldShowAdsScreenInSettings = CherrygramCoreConfig.showAdsScreenInSettings
        val newShowAdsScreenInSettings = getBoolean(Constants.show_ads_screen_in_settings, true)

        CherrygramLogger.d { "RemoteConfig: ${Constants.show_ads_screen_in_settings} value = $newShowAdsScreenInSettings" }
        if (oldShowAdsScreenInSettings != newShowAdsScreenInSettings) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.show_ads_screen_in_settings} changed $oldShowAdsScreenInSettings -> $newShowAdsScreenInSettings" }
        }

        CherrygramCoreConfig.showAdsScreenInSettings = newShowAdsScreenInSettings
    }

    private fun checkVideoMessagesResolution() {
        val oldResolutionValue = CherrygramCameraConfig.videoMessagesResolution
        val newResolutionValue = getInt(Constants.videomessages_resolution, 512)

        CherrygramLogger.d { "RemoteConfig: ${Constants.videomessages_resolution} value = $newResolutionValue" }
        if (oldResolutionValue != newResolutionValue) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.videomessages_resolution} changed $oldResolutionValue -> $newResolutionValue" }
        }

        CherrygramCameraConfig.videoMessagesResolution = newResolutionValue
    }

    private fun checkHumoCardNumber() {
        val oldHumoCardNumber = CherrygramCoreConfig.humoCardNumber
        val newHumoCardNumber = getString(Constants.humo_card_number)

        CherrygramLogger.d { "RemoteConfig: ${Constants.humo_card_number} value = $newHumoCardNumber" }
        if (oldHumoCardNumber != newHumoCardNumber) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.humo_card_number} changed $oldHumoCardNumber -> $newHumoCardNumber" }
        }

        CherrygramCoreConfig.humoCardNumber = newHumoCardNumber
    }

    private fun checkTBankCardNumber() {
        val oldTBankCardNumber = CherrygramCoreConfig.tbankCardNumber
        val newTBankCardNumber = getString(Constants.tbank_card_number)

        CherrygramLogger.d { "RemoteConfig: ${Constants.tbank_card_number} value = $newTBankCardNumber" }
        if (oldTBankCardNumber != newTBankCardNumber) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.tbank_card_number} changed $oldTBankCardNumber -> $newTBankCardNumber" }
        }

        CherrygramCoreConfig.tbankCardNumber = newTBankCardNumber
    }

    private fun checkSafeStars() {
        val oldSafeStars = CherrygramCoreConfig.allowSafeStars
        val newSafeStars = getBoolean(Constants.allow_use_safestars, true)

        CherrygramLogger.d { "RemoteConfig: ${Constants.allow_use_safestars} value = $newSafeStars" }
        if (oldSafeStars != newSafeStars) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.allow_use_safestars} changed $oldSafeStars -> $newSafeStars" }
        }

        CherrygramCoreConfig.allowSafeStars = newSafeStars
    }

    private fun checkSafeStarsURL() {
        val oldSafeStarsURL = CherrygramCoreConfig.safe_stars_URL
        val newSafeStarsURL = getString(Constants.safe_stars_URL)

        CherrygramLogger.d { "RemoteConfig: ${Constants.safe_stars_URL} value = $newSafeStarsURL" }
        if (oldSafeStarsURL != newSafeStarsURL) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.safe_stars_URL} changed $oldSafeStarsURL -> $newSafeStarsURL" }
        }

        CherrygramCoreConfig.safe_stars_URL = newSafeStarsURL
    }

    private fun checkSafeStarsURL_RU() {
        val oldSafeStarsURL_RU = CherrygramCoreConfig.safe_stars_URL_RU
        val newSafeStarsURL_RU = getString(Constants.safe_stars_URL_RU)

        CherrygramLogger.d { "RemoteConfig: ${Constants.safe_stars_URL_RU} value = $newSafeStarsURL_RU" }
        if (oldSafeStarsURL_RU != newSafeStarsURL_RU) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.safe_stars_URL_RU} changed $oldSafeStarsURL_RU -> $newSafeStarsURL_RU" }
        }

        CherrygramCoreConfig.safe_stars_URL_RU = newSafeStarsURL_RU
    }

    private fun checkSafeSurf() {
        val oldSafeSurf = CherrygramCoreConfig.allowSafeSurf
        val newSafeSurf = getBoolean(Constants.allow_use_safesurf, true)

        CherrygramLogger.d { "RemoteConfig: ${Constants.allow_use_safesurf} value = $newSafeSurf" }
        if (oldSafeSurf != newSafeSurf) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.allow_use_safesurf} changed $oldSafeSurf -> $newSafeSurf" }
        }

        CherrygramCoreConfig.allowSafeSurf = newSafeSurf
    }

    private fun checkSafeSurfURL() {
        val oldSafeSurfURL = CherrygramCoreConfig.safe_surf_URL
        val newSafeSurfURL = getString(Constants.safe_surf_URL)

        CherrygramLogger.d { "RemoteConfig: ${Constants.safe_surf_URL} value = $newSafeSurfURL" }
        if (oldSafeSurfURL != newSafeSurfURL) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.safe_surf_URL} changed $oldSafeSurfURL -> $newSafeSurfURL" }
        }

        CherrygramCoreConfig.safe_surf_URL = newSafeSurfURL
    }

    private fun checkProxyInSettings() {
        val oldProxyURL = CherrygramCoreConfig.proxyURL
        val newProxyURL = getString(Constants.proxy_link_in_settings)

        CherrygramLogger.d { "RemoteConfig: ${Constants.proxy_link_in_settings} value = $newProxyURL" }
        if (oldProxyURL != newProxyURL) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.proxy_link_in_settings} changed $oldProxyURL -> $newProxyURL" }
        }

        CherrygramCoreConfig.proxyURL = newProxyURL


        val oldProxyInSettings = CherrygramCoreConfig.showProxyInSettings
        val newProxyInSettings = getBoolean(Constants.show_proxy_in_settings, true)

        CherrygramLogger.d { "RemoteConfig: ${Constants.show_proxy_in_settings} value = $newProxyInSettings" }
        if (oldProxyInSettings != newProxyInSettings) {
            CherrygramLogger.d { "RemoteConfig: ${Constants.show_proxy_in_settings} changed $oldProxyInSettings -> $newProxyInSettings" }
        }

        CherrygramCoreConfig.showProxyInSettings = newProxyInSettings
    }

}