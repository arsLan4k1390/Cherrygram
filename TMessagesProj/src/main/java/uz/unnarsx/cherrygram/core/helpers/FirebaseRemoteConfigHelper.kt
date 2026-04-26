/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.helpers

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig
import uz.unnarsx.cherrygram.misc.Constants

object FirebaseRemoteConfigHelper {

    private val remoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    private val defaults = mapOf(
        Constants.Videomessages_Resolution to 512L,
        Constants.allow_use_safestars to true,
        Constants.allow_use_safesurf to true,
        Constants.humo_card_number to "9860600408892476",
        Constants.tbank_card_number to "9860350143344678"
    )

    fun init() {
        remoteConfig.setDefaultsAsync(defaults)
    }

    suspend fun fetchAndActivate(): Boolean {
        val interval = if (CherrygramCoreConfig.isDevBuild()) 10800L else 21600L // 3/6 hours

        return try {
            remoteConfig.fetch(interval).await()
            remoteConfig.activate().await()
            true
        } catch (e: Exception) {
            FileLog.e(e)
            false
        }
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        if (!ApplicationLoader.checkPlayServices()) return default

        return try {
            remoteConfig.getBoolean(key)
        } catch (e: Exception) {
            FileLog.e(e)
            default
        }
    }

    fun getInt(key: String, default: Int = 0): Int {
        if (!ApplicationLoader.checkPlayServices()) return default

        return try {
            remoteConfig.getLong(key).toInt()
        } catch (e: Exception) {
            FileLog.e(e)
            default
        }
    }

    fun getString(key: String, default: String = ""): String {
        if (!ApplicationLoader.checkPlayServices()) return default

        return try {
            remoteConfig.getString(key)
        } catch (e: Exception) {
            FileLog.e(e)
            default
        }
    }

    fun applyConfig() {
        checkVideoMessagesResolution()
        checkSafeStars()
        checkSafeSurf()
        checkHumoCardNumber()
        checkTBankCardNumber()
    }

    private fun checkVideoMessagesResolution() {
        val oldResolutionValue = CherrygramCameraConfig.videoMessagesResolution
        val newResolutionValue = getInt(Constants.Videomessages_Resolution, 512)

        FileLog.d("RemoteConfig: ${Constants.Videomessages_Resolution} value = $newResolutionValue")
        if (oldResolutionValue != newResolutionValue) {
            FileLog.d("RemoteConfig: ${Constants.Videomessages_Resolution} changed $oldResolutionValue -> $newResolutionValue")
        }

        CherrygramCameraConfig.videoMessagesResolution = newResolutionValue
    }

    private fun checkHumoCardNumber() {
        val oldHumoCardNumber = CherrygramCoreConfig.humoCardNumber
        val newHumoCardNumber = getString(Constants.humo_card_number)

        FileLog.d("RemoteConfig: ${Constants.humo_card_number} value = $newHumoCardNumber")
        if (oldHumoCardNumber != newHumoCardNumber) {
            FileLog.d("RemoteConfig: ${Constants.humo_card_number} changed $oldHumoCardNumber -> $newHumoCardNumber")
        }

        CherrygramCoreConfig.humoCardNumber = newHumoCardNumber
    }

    private fun checkTBankCardNumber() {
        val oldTBankCardNumber = CherrygramCoreConfig.tbankCardNumber
        val newTBankCardNumber = getString(Constants.tbank_card_number)

        FileLog.d("RemoteConfig: ${Constants.tbank_card_number} value = $newTBankCardNumber")
        if (oldTBankCardNumber != newTBankCardNumber) {
            FileLog.d("RemoteConfig: ${Constants.tbank_card_number} changed $oldTBankCardNumber -> $newTBankCardNumber")
        }

        CherrygramCoreConfig.tbankCardNumber = newTBankCardNumber
    }

    private fun checkSafeStars() {
        val oldSafeStars = CherrygramCoreConfig.allowSafeStars
        val newSafeStars = getBoolean(Constants.allow_use_safestars, true)

        FileLog.d("RemoteConfig: ${Constants.allow_use_safestars} value = $newSafeStars")
        if (oldSafeStars != newSafeStars) {
            FileLog.d("RemoteConfig: ${Constants.allow_use_safestars} changed $oldSafeStars -> $newSafeStars")
        }

        CherrygramCoreConfig.allowSafeStars = newSafeStars
    }

    private fun checkSafeSurf() {
        val oldSafeSurf = CherrygramCoreConfig.allowSafeSurf
        val newSafeSurf = getBoolean(Constants.allow_use_safesurf, true)

        FileLog.d("RemoteConfig: ${Constants.allow_use_safesurf} value = $newSafeSurf")
        if (oldSafeSurf != newSafeSurf) {
            FileLog.d("RemoteConfig: ${Constants.allow_use_safesurf} changed $oldSafeSurf -> $newSafeSurf")
        }

        CherrygramCoreConfig.allowSafeSurf = newSafeSurf
    }

}