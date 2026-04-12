package uz.unnarsx.cherrygram.core.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

object FirebaseCrashlyticsHelper {

    fun logAsNonFatal(e: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }

}