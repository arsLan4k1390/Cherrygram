/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

object FirebaseCrashlyticsHelper {

    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    fun logAsNonFatal(e: Throwable) {
        crashlytics.recordException(e)
    }

}