package uz.unnarsx.cherrygram.core.crashlytics

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import androidx.core.content.edit
import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2

object FirebaseCrashlyticsHelper {

    fun logAsNonFatal(e: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    fun logToCrashlytics(context: Context, userId: Long) {
//        if (true) return

        val prefs = context.getSharedPreferences("security", Context.MODE_PRIVATE)

        val key = "tamper_count_$userId"
        val count = prefs.getInt(key, 0) + 1
        prefs.edit { putInt(key, count) }

        val crashlytics = FirebaseCrashlytics.getInstance()

        crashlytics.setUserId(userId.toString())
        crashlytics.setCustomKey("tamper_count", count)
        crashlytics.setCustomKey("userId", userId.toString())
        crashlytics.setCustomKey("publicName", "@" + ChatsHelper2.getActiveUsername(userId))

        crashlytics.recordException(
            ClassNotFoundException("Check this content with $userId!")
        )
    }

}