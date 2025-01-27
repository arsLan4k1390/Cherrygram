/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.string

object CherrygramExperimentalConfig: CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Main.immediate
) {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /** General start **/
    const val ANIMATION_SPRING = 0
    const val ANIMATION_CLASSIC = 1
    var springAnimation by sharedPreferences.int("EP_SpringAnimation", ANIMATION_SPRING)

    var actionbarCrossfade by sharedPreferences.boolean("EP_ActionbarCrossfade", true)
    fun toggleActionbarCrossfade() {
        actionbarCrossfade = !actionbarCrossfade
        putBoolean("EP_ActionbarCrossfade", actionbarCrossfade)
    }

    var residentNotification by sharedPreferences.boolean("CG_ResidentNotification", !ApplicationLoader.checkPlayServices())
    fun toggleResidentNotification() {
        residentNotification = !residentNotification
        putBoolean("CG_ResidentNotification", residentNotification)
    }

    var customChatForSavedMessages by sharedPreferences.boolean("CP_CustomChatForSavedMessages", false)
    fun toggleCustomChatForSavedMessages() {
        customChatForSavedMessages = !customChatForSavedMessages
        putBoolean("CP_CustomChatForSavedMessages", customChatForSavedMessages)
    }
    /** General finish **/

    /** Network start **/
    const val BOOST_NONE = 0
    const val BOOST_AVERAGE = 1
    const val BOOST_EXTREME = 2
    var downloadSpeedBoost by sharedPreferences.int("EP_DownloadSpeedBoost", BOOST_NONE)

    var uploadSpeedBoost by sharedPreferences.boolean("EP_UploadSpeedBoost", false)
    fun toggleUploadSpeedBoost() {
        uploadSpeedBoost = !uploadSpeedBoost
        putBoolean("EP_UploadSpeedBoost", uploadSpeedBoost)
    }

    var slowNetworkMode by sharedPreferences.boolean("EP_SlowNetworkMode", false)
    fun toggleSlowNetworkMode() {
        slowNetworkMode = !slowNetworkMode
        putBoolean("EP_SlowNetworkMode", slowNetworkMode)
    }
    /** Network finish **/

}