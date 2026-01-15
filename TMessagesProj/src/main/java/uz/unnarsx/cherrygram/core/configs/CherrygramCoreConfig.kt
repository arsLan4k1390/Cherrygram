/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import android.os.Build
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.KotlinFragmentsManager
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import uz.unnarsx.cherrygram.core.helpers.FirebaseRemoteConfigHelper
import uz.unnarsx.cherrygram.donates.DonatesManager
import uz.unnarsx.cherrygram.misc.Constants
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.float
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.long
import uz.unnarsx.cherrygram.preferences.string
import androidx.core.content.edit

object CherrygramCoreConfig: CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Default
) {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        preferences.edit {
            putBoolean(key, value)
        }
    }

    fun putStringForUserPrefs(key: String, value: String) {
        val preferences = MessagesController.getMainSettings(UserConfig.selectedAccount)
        preferences.edit {
            putString(key, value)
        }
    }

    /** General start */
    var noRounding by sharedPreferences.boolean("CP_NoRounding", false)
    var systemEmoji by sharedPreferences.boolean("AP_SystemEmoji", false)
    var systemFonts by sharedPreferences.boolean("AP_SystemFonts", true)

    const val EDGE_MODE_ENABLE = 0
    const val EDGE_MODE_DISABLE = 1
    const val EDGE_MODE_AUTO = 2
    var edgeToEdgeMode by sharedPreferences.int("CP_EdgeToEdge", EDGE_MODE_AUTO)

    const val TABLET_MODE_ENABLE = 0
    const val TABLET_MODE_DISABLE = 1
    const val TABLET_MODE_AUTO = 2
    var tabletMode by sharedPreferences.int("AP_Tablet_Mode", TABLET_MODE_AUTO)

    var oldNotificationIcon by sharedPreferences.boolean("AP_Old_Notification_Icon", false)
    var residentNotification by sharedPreferences.boolean("CG_ResidentNotification", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM && !ApplicationLoader.checkPlayServices())
    /** General finish */

    /** Animations and Premium Features start */
    var hideStories by sharedPreferences.boolean("CP_HideStories", false)
    var archiveStoriesFromUsers by sharedPreferences.boolean("CP_ArchiveStoriesFromUsers", false)
    var archiveStoriesFromChannels by sharedPreferences.boolean("CP_ArchiveStoriesFromChannels", false)
    var customWallpapers by sharedPreferences.boolean("CP_CustomWallpapers", true)
    var disableAnimatedAvatars by sharedPreferences.boolean("CP_DisableAnimAvatars", false)
    var disableReactionsOverlay by sharedPreferences.boolean("CP_DisableReactionsOverlay", false)
    var disableReactionAnim by sharedPreferences.boolean("CP_DisableReactionAnim", false)
    var disablePremStickAnim by sharedPreferences.boolean("CP_DisablePremStickAnim", false)
    var disablePremStickAutoPlay by sharedPreferences.boolean("CP_DisablePremStickAutoPlay", false)
    var hideSendAsChannel by sharedPreferences.boolean("CP_HideSendAsChannel", false)
    /** Animations and Premium Features finish */

    /** OTA start */
    var installBetas by sharedPreferences.boolean("CG_Install_Beta_Ver", isStandaloneBetaBuild())
    var autoOTA by sharedPreferences.boolean("CG_Check_Auto_OTA", isStandaloneStableBuild() || isStandaloneBetaBuild() || isDevBuild())
    var lastUpdateCheckTime by sharedPreferences.long("CG_LastUpdateCheckTime", 0)
    var updateScheduleTimestamp by sharedPreferences.long("CG_UpdateScheduleTimestamp", 0)
    var forceFound by sharedPreferences.boolean("CG_ForceFound", false)

    var updatesNewUI by sharedPreferences.boolean("CG_UpdatesNewUI", true)
    var updateVersionName by sharedPreferences.string("CG_UpdateVersionName", "idk")
    var updateSize by sharedPreferences.string("CG_UpdateSize", "0")
    var updateIsDownloading by sharedPreferences.boolean("CG_UpdateIsDownloading", false)
    var updateDownloadingProgress by sharedPreferences.float("CG_NewUpdateDownloadingProgress", 0f)
    var updateAvailable by sharedPreferences.boolean("CG_UpdateAvailable", false)
    /** OTA finish */

    /** Misc start */
    var cgBrandedScreenshots by sharedPreferences.boolean("DP_BrandedScreenshots", false)
    var sleepTimer by sharedPreferences.boolean("CG_Sleep_Timer", false)
    var showNotifications by sharedPreferences.boolean("CG_ShowNotifications", true)
    var allowSafeStars by sharedPreferences.boolean("CG_AllowSafeStarsUI", true)
    /** Misc finish */

    /** Cherrygram build types start */
    @JvmStatic
    fun isStandaloneStableBuild(): Boolean {
        return ApplicationLoader.isStandaloneBuild() && !isDevBuild() && !isStandalonePremiumBuild() && !isStandaloneBetaBuild()
    }

    @JvmStatic
    fun isStandaloneBetaBuild(): Boolean {
        return false
    }

    @JvmStatic
    fun isDevBuild(): Boolean {
        return false
    }

    @JvmStatic
    fun isStandalonePremiumBuild(): Boolean {
        return false
    }

    @JvmStatic
    fun isPlayStoreBuild(): Boolean {
        return !ApplicationLoader.isStandaloneBuild()
    }
    /** Cherrygram build types finish */

    /** Misc start */
    var lastDonatesCheckTime by sharedPreferences.long("CG_LastDonatesCheckTime", 0)
    /** Misc finish*/

    /** Migration start */
    private fun migratePreferences() {
        if (CherrygramAppearanceConfig.showIDDC_old >= CherrygramAppearanceConfig.ID_DC) {
            CherrygramAppearanceConfig.showIDDC_old = 1
            CherrygramAppearanceConfig.showIDDC = true
        }
    }
    /** Migration finish */

    fun init() {
        launch {
            if (ApplicationLoader.checkPlayServices()) {
                FirebaseApp.initializeApp(ApplicationLoader.applicationContext)
                FirebaseRemoteConfigHelper.initRemoteConfig()
            }

            DonatesManager.startAutoRefresh(ApplicationLoader.applicationContext, force = false, fromIntegrityChecker = false)

            if (KotlinFragmentsManager.vreg42r2r2r1r3q1rq3(getString(R.string.CG_FollowChannelInfo))
                || KotlinFragmentsManager.vreg42r2r2r1r3q1rq3(getString(R.string.CG_FollowChannelTitle))
                || KotlinFragmentsManager.vreg42r2r2r1r3q1rq3(Constants.CG_APKS_CHANNEL_URL)
                || KotlinFragmentsManager.vreg42r2r2r1r3q1rq3(Constants.CG_APKS_CHANNEL_USERNAME)
                || KotlinFragmentsManager.vreg42r2r2r1r3q1rq3(Constants.CG_AUTHOR)
                || KotlinFragmentsManager.vreg42r2r2r1r3q1rq3(Constants.CG_CHANNEL_URL)
                || KotlinFragmentsManager.vreg42r2r2r1r3q1rq3(Constants.CG_CHAT_URL)
                || KotlinFragmentsManager.vreg42r2r2r1r3q1rq3(Constants.CG_CHANNEL_USERNAME)
                || KotlinFragmentsManager.vreg42r2r2r1r3q1rq3(Constants.CG_CHAT_USERNAME)
            ) {
                KotlinFragmentsManager.nfweioufwehr117()
            }

            migratePreferences()
        }
    }

}
