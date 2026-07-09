/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.firebase.remoteConfig

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.core.CherrygramLogger
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramFirebaseConfig
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper

object FirebaseRemoteConfigHelper_deprecated {

    private val remoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    private val defaults = mapOf(
        RemoteConfigConstants.show_ads_screen_in_settings to true,
        RemoteConfigConstants.show_ads_in_play_store_builds to true,
        RemoteConfigConstants.videomessages_resolution to 512L,

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
        val newShowAdsScreenInSettings = getBoolean(RemoteConfigConstants.show_ads_screen_in_settings, true)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_ads_screen_in_settings} value = $newShowAdsScreenInSettings" }
        if (oldShowAdsScreenInSettings != newShowAdsScreenInSettings) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_ads_screen_in_settings} changed $oldShowAdsScreenInSettings -> $newShowAdsScreenInSettings" }
        }

        CherrygramFirebaseConfig.showAdsScreenInSettings = newShowAdsScreenInSettings
    }

    private fun checkAdsInPlayStoreBuilds() {
        val oldShowAdsInPlayStoreBuilds = CherrygramFirebaseConfig.showAdsInPlayStoreBuilds
        val newShowAdsInPlayStoreBuilds = getBoolean(RemoteConfigConstants.show_ads_in_play_store_builds, true)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_ads_in_play_store_builds} value = $newShowAdsInPlayStoreBuilds" }
        if (oldShowAdsInPlayStoreBuilds != newShowAdsInPlayStoreBuilds) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_ads_in_play_store_builds} changed $oldShowAdsInPlayStoreBuilds -> $newShowAdsInPlayStoreBuilds" }
        }

        CherrygramFirebaseConfig.showAdsInPlayStoreBuilds = newShowAdsInPlayStoreBuilds
    }

    private fun checkVideoMessagesResolution() {
        val oldResolutionValue = CherrygramCameraConfig.videoMessagesResolution
        val newResolutionValue = getInt(RemoteConfigConstants.videomessages_resolution, 512)

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
        val newSafeStars = getBoolean(RemoteConfigConstants.allow_use_safestars, true)

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
        val newSafeSurf = getBoolean(RemoteConfigConstants.allow_use_safesurf, true)

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
        val newSafePay = getBoolean(RemoteConfigConstants.allow_use_safepay, true)

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
        val newRecordOOMasNonFatal = getBoolean(RemoteConfigConstants.record_oom_as_nf, true)

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
        val newProxyInSettings = getBoolean(RemoteConfigConstants.show_proxy_in_settings, true)

        CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_proxy_in_settings} value = $newProxyInSettings" }
        if (oldProxyInSettings != newProxyInSettings) {
            CherrygramLogger.d { "RemoteConfig: ${RemoteConfigConstants.show_proxy_in_settings} changed $oldProxyInSettings -> $newProxyInSettings" }
        }

        CherrygramFirebaseConfig.showProxyInSettings = newProxyInSettings
    }

}