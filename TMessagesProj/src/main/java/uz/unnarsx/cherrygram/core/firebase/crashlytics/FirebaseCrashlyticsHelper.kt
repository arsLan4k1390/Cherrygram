/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.firebase.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import androidx.core.content.edit
import org.telegram.messenger.ApplicationLoader
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper

object FirebaseCrashlyticsHelper {

    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    fun updateUser(user: TLRPC.User?) {
        if (user == null) {
            crashlytics.setUserId("")

            crashlytics.setCustomKey("userId", "")
            crashlytics.setCustomKey("publicName", "")

            return
        }

        crashlytics.setUserId(user.id.toString())

        crashlytics.setCustomKey("userId", user.id.toString())
        crashlytics.setCustomKey(
            "publicName",
            "@${ChatsHelper2.getActiveUsername(user.id)}"
        )
        crashlytics.setCustomKey("buildDate", CGResourcesHelper.getBuildDate())
    }

    fun logAsNonFatal(e: Throwable) {
        crashlytics.recordException(e)
    }

}