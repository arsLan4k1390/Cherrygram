package uz.unnarsx.cherrygram.core.helpers;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig;

public class FirebaseAnalyticsHelper {

    private static FirebaseAnalytics firebaseAnalytics;

    public static void start(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static void trackEventWithEmptyBundle(String eventName) {
        if (CherrygramPrivacyConfig.INSTANCE.getGoogleAnalytics() && ApplicationLoader.checkPlayServices()) {
            trackEvent(eventName, Bundle.EMPTY);
        }
    }

    public static void trackEvent(String eventName, Bundle bundle) {
        if (firebaseAnalytics == null) {
            return;
        }

        /*bundle.remove("debug_event");
        bundle.remove("firebase_event_origin");
        bundle.remove("firebase_screen_class");
        bundle.remove("firebase_screen_id");
        bundle.remove("ga_session_id");
        bundle.remove("ga_session_number");*/

        firebaseAnalytics.logEvent(eventName, bundle);

        if (CherrygramCoreConfig.INSTANCE.isDevBuild() && CherrygramDebugConfig.INSTANCE.getShowRPCErrors()) {
            AndroidUtilities.runOnUIThread(() -> Toast.makeText(ApplicationLoader.applicationContext, eventName, Toast.LENGTH_SHORT).show(), 3000);
        }
        if (CherrygramCoreConfig.INSTANCE.isDevBuild()) FileLog.e("отслежен ивент: " + eventName + " " + bundle);
    }

}
