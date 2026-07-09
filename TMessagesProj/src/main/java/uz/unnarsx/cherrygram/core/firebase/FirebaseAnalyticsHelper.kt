/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.firebase

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.core.CherrygramLogger
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper
import uz.unnarsx.cherrygram.preferences.CameraPreferencesEntry

object FirebaseAnalyticsHelper {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context).apply {
            val bundle = Bundle().apply {
                putString("flavor", CGResourcesHelper.getBuildType())
                putString("cg_version", CGResourcesHelper.getCherryVersion())
                putString("camera", CameraPreferencesEntry.getCameraName())
                putString("dualCamera", CherrygramCameraConfig.useDualCamera.toString())
            }
            setDefaultEventParameters(bundle)

            setAnalyticsCollectionEnabled(/*CherrygramPrivacyConfig.googleAnalytics*/ true)
        }
    }

    fun onPrivacyConfigChanged(isEnabled: Boolean) {
        firebaseAnalytics?.setAnalyticsCollectionEnabled(isEnabled)

        CherrygramLogger.w {"Firebase Analytics collection: $isEnabled" }
    }

    fun trackEventWithEmptyBundle(eventName: String) {
        trackEvent(eventName, Bundle.EMPTY)
    }

    fun trackEvent(eventName: String, bundle: Bundle) {
        if (!CherrygramPrivacyConfig.googleAnalytics) return

        firebaseAnalytics?.let { analytics ->
            analytics.logEvent(eventName, bundle)

            if (CherrygramCoreConfig.isDevBuild()) {
                CherrygramLogger.i { "отслежен ивент: $eventName $bundle" }

                if (CherrygramDebugConfig.showRPCErrors) {
                    AndroidUtilities.runOnUIThread({
                        Toast.makeText(ApplicationLoader.applicationContext, eventName, Toast.LENGTH_SHORT).show()
                    }, 3000)
                }
            }
        }
    }

}