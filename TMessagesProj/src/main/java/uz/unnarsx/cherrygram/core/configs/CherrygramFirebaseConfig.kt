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
import androidx.annotation.Keep
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import uz.unnarsx.cherrygram.core.firebase.remoteConfig.RemoteConfigHelper
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.long
import uz.unnarsx.cherrygram.preferences.string

@Keep
object CherrygramFirebaseConfig: CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Default
) {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    /** Card numbers start */
    var humoCardNumber by sharedPreferences.string("CP_Humo_Card_Number", "9860100128256904")
    var tbankCardNumber by sharedPreferences.string("CP_TBank_Card_Number", "9860100128256904")
    /** Card numbers finish */

    /** SafeStars start */
    var allowSafeStars by sharedPreferences.boolean("CG_AllowSafeStarsUI1", true)
    var safe_stars_URL by sharedPreferences.string("CP_SafeStarsURL", "https://safe-stars.com/?partner=cherrygram")
    var safe_stars_URL_RU by sharedPreferences.string("CP_SafeStarsURL_RU", "https://safe-stars.com/ru/?partner=cherrygram")
    /** SafeStars finish */

    /** SafeSurf start */
    var allowSafeSurf by sharedPreferences.boolean("CG_AllowSafeSurfUI", true)
    var safe_surf_URL by sharedPreferences.string("CP_SafeSurfURL", "https://t.me/safe_surfbot?start=cherry")
    /** SafeSurf finish */

    /** SafePay start */
    var allowSafePay by sharedPreferences.boolean("CG_AllowSafePayUI", true)
    var safe_pay_URL by sharedPreferences.string("CP_SafePayURL", "https://t.me/safepaycard_bot?start=cherry")
    /** SafePay finish */

    /** AdsGram start */
    var showAdsRandomly by sharedPreferences.boolean("CG_ShowAdsRandomly", false)
    var showAdsScreenInSettings by sharedPreferences.boolean("CG_ShowAdsScreenInSettings", true)
    var showAdsInPlayStoreBuilds by sharedPreferences.boolean("CG_ShowAdsInPlayStoreBuilds", true)
    var alwaysShowAdsGramInChats by sharedPreferences.boolean("CG_AlwaysShowAdsGramInChats", false)
    /** AdsGram finish */

    /** Misc start */
    var useBrailleSpoiler by sharedPreferences.boolean("CG_UseBrailleSpoiler", true)

    var lastRemoteConfigCheckTime by sharedPreferences.long("CG_LastRemoteConfigCheckTime", 0)

    var showProxyInSettings by sharedPreferences.boolean("CG_ShowProxyInSettings", false)
    var proxyURL by sharedPreferences.string("CP_ProxyURL", "https://t.me/proxy?server=78.17.39.137&port=443&secret=ee7ae12ad5e1268d51eccdc0d1acb595ea74656c6567612e6d65")
    /** Misc finish */

    /** Deleted Gifts start */
    var showDeletedGifts by sharedPreferences.boolean("CG_ShowDeletedGifts", true)
    var deletedGiftsConfigURL by sharedPreferences.string("CG_DeletedGiftsConfigURL", "https://gitlab.com/arsLan4k1390/Cherrygram-IDS/-/raw/main/gift_list.json?inline=false")
    var deletedGiftsStickerPackName by sharedPreferences.string("CG_DeletedGiftsStickerPackLink", "Cherrygram_HiddenGifts")
    var deletedGiftsStickerPackOffset by sharedPreferences.int("CG_DeletedGiftsStickerpackOffset", 0)
    /** Deleted Gifts finish */

    fun init() {
        launch {
            if (ApplicationLoader.checkPlayServices()) {
                FirebaseApp.initializeApp(ApplicationLoader.applicationContext)
                Firebase.analytics.setUserProperty("cherrygram_version", CGResourcesHelper.getCherryVersion())
                Firebase.analytics.setUserProperty("code_version", CGResourcesHelper.getCodeVersion())
                Firebase.analytics.setUserProperty("source_code_version", CGResourcesHelper.getSourceCodeVersion())
                /*FirebaseRemoteConfigHelper_deprecated.init()
                val success = FirebaseRemoteConfigHelper_deprecated.fetchAndActivate()
                if (success) {
                    FirebaseRemoteConfigHelper_deprecated.applyConfig()
                }*/
            }
            RemoteConfigHelper.fetchAndActivate()

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
