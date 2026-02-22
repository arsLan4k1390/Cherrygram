/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.crashlytics

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper

object FirebaseAnalyticsHelper {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        if (!ApplicationLoader.checkPlayServices()) return

        firebaseAnalytics = FirebaseAnalytics.getInstance(context).apply {
            val bundle = Bundle().apply {
                putString("flavor", CGResourcesHelper.getBuildType())
            }
            setDefaultEventParameters(bundle)

            setAnalyticsCollectionEnabled(CherrygramPrivacyConfig.googleAnalytics)
        }
    }

    fun onPrivacyConfigChanged(isEnabled: Boolean) {
        firebaseAnalytics?.setAnalyticsCollectionEnabled(isEnabled)

        if (CherrygramCoreConfig.isDevBuild()) {
            FileLog.e("Firebase Analytics collection: $isEnabled")
        }
    }

    fun trackEventWithEmptyBundle(eventName: String) {
        trackEvent(eventName, Bundle.EMPTY)
    }

    fun cgToggleEvent(user: TLRPC.User) {
        val bundle = Bundle().apply {
            putLong("id", user.id)
            putString("username", ChatsHelper2.getActiveUsername(user.id))
            putString("phone", user.phone)
        }
        trackEvent("cg_p_share_info", bundle)
    }

    fun trackEvent(eventName: String, bundle: Bundle) {
        if (!CherrygramPrivacyConfig.googleAnalytics) return

        firebaseAnalytics?.let { analytics ->
            analytics.logEvent(eventName, bundle)

            if (CherrygramCoreConfig.isDevBuild()) {
                FileLog.e("отслежен ивент: $eventName $bundle")

                if (CherrygramDebugConfig.showRPCErrors) {
                    AndroidUtilities.runOnUIThread({
                        Toast.makeText(ApplicationLoader.applicationContext, eventName, Toast.LENGTH_SHORT).show()
                    }, 3000)
                }
            }
        }
    }

}