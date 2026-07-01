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
import androidx.annotation.Keep
import androidx.core.content.edit
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import uz.unnarsx.cherrygram.core.helpers.FirebaseRemoteConfigHelper
import uz.unnarsx.cherrygram.donates.DonatesManager
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.long
import uz.unnarsx.cherrygram.preferences.string

@Keep
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
    /** Animations start */
    const val ANIMATION_SPRING = 0
    const val ANIMATION_CLASSIC = 1
    var springAnimation by sharedPreferences.int("CG_SpringAnimation", ANIMATION_SPRING)

    var actionbarCrossfade by sharedPreferences.boolean("CG_ActionbarCrossfade", true)
    var predictiveBack by sharedPreferences.boolean("CG_PredictiveBack", false)
    /** Animations finish */

    /** Notifications start */
    var silenceNonContacts by sharedPreferences.boolean("CP_SilenceNonContacts", false)
    var oldNotificationIcon by sharedPreferences.boolean("AP_Old_Notification_Icon", false)
    var residentNotification by sharedPreferences.boolean("CG_ResidentNotification", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM && !ApplicationLoader.checkPlayServices())
    /** Notifications finish */

    /** Stories start */
    var hideStories by sharedPreferences.boolean("CP_HideStories", false)
    var archiveStoriesFromUsers by sharedPreferences.boolean("CP_ArchiveStoriesFromUsers", false)
    var archiveStoriesFromChannels by sharedPreferences.boolean("CP_ArchiveStoriesFromChannels", false)
    /** Stories finish */

    /** Miscellaneous start */
    var noRounding by sharedPreferences.boolean("CP_NoRounding1", true)
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
    /** Miscellaneous finish */

    /** Network start */
    const val BOOST_NONE = 0
    const val BOOST_AVERAGE = 1
    const val BOOST_EXTREME = 2
    var downloadSpeedBoost by sharedPreferences.int("EP_DownloadSpeedBoost", BOOST_NONE)

    var uploadSpeedBoost by sharedPreferences.boolean("EP_UploadSpeedBoost", false)
    var slowNetworkMode by sharedPreferences.boolean("EP_SlowNetworkMode", false)
    /** Network finish */

    /** OTA start */
    var installBetas by sharedPreferences.boolean("CG_Install_Beta_Ver", isStandaloneBetaBuild())
    var autoOTA by sharedPreferences.boolean("CG_Check_Auto_OTA", isStandaloneStableBuild() || isStandaloneBetaBuild() || isDevBuild())
    var forceFound by sharedPreferences.boolean("CG_ForceFound", false)
    /** OTA finish */

    /** Misc start */
    var cgBrandedScreenshots by sharedPreferences.boolean("DP_BrandedScreenshots", false)
    var humoCardNumber by sharedPreferences.string("CP_Humo_Card_Number", "9860100128256904")
    var tbankCardNumber by sharedPreferences.string("CP_TBank_Card_Number", "9860100128256904")
    var sleepTimer by sharedPreferences.boolean("CG_Sleep_Timer", false)

    var showAdsScreenInSettings by sharedPreferences.boolean("CG_ShowAdsScreenInSettings", true)

    var allowSafeStars by sharedPreferences.boolean("CG_AllowSafeStarsUI1", true)
    var safe_stars_URL by sharedPreferences.string("CP_SafeStarsURL", "https://safe-stars.com/?partner=cherrygram")
    var safe_stars_URL_RU by sharedPreferences.string("CP_SafeStarsURL_RU", "https://safe-stars.com/ru/?partner=cherrygram")
    var allowSafeSurf by sharedPreferences.boolean("CG_AllowSafeSurfUI", true)
    var safe_surf_URL by sharedPreferences.string("CP_SafeSurfURL", "https://t.me/safe_surfbot?start=cherry")

    var showProxyInSettings by sharedPreferences.boolean("CG_ShowProxyInSettings", false)
    var proxyURL by sharedPreferences.string("CP_ProxyURL", "https://t.me/proxy?server=78.17.39.137&port=443&secret=ee7ae12ad5e1268d51eccdc0d1acb595ea74656c6567612e6d65")
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
                FirebaseRemoteConfigHelper.init()
                val success = FirebaseRemoteConfigHelper.fetchAndActivate()
                if (success) {
                    FirebaseRemoteConfigHelper.applyConfig()
                }
            }

            DonatesManager.startAutoRefresh(ApplicationLoader.applicationContext, force = false, fromIntegrityChecker = false)

            migratePreferences()

            if (allowSafeStars) {
                val messagesController = MessagesController.getInstance(UserConfig.selectedAccount)

                val exceptions = messagesController.getWebBrowserExceptionsList(false)

                delay(10000)
                val inAppBrowserEnabled = messagesController.isWebBrowserInAppEnabled

                val hasSafeStarsPro = exceptions.any { it.domain.equals("safestars.pro", ignoreCase = true) }
                if (!hasSafeStarsPro) {
                    messagesController.addWebBrowserException("safestars.pro", inAppBrowserEnabled)
                }

                delay(15000)

                val hasSafeStarsCom = exceptions.any { it.domain.equals("safe-stars.com", ignoreCase = true) }
                if (!hasSafeStarsCom) {
                    messagesController.addWebBrowserException("safe-stars.com", inAppBrowserEnabled)
                }
            }

        }
    }

}
