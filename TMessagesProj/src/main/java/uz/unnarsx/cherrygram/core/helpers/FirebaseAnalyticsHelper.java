package uz.unnarsx.cherrygram.core.helpers;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalyticsHelper {

    private static FirebaseAnalytics firebaseAnalytics;

    public static void start(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static void trackEvent(String event, Bundle bundle) {
        firebaseAnalytics.logEvent(event, bundle);
    }

    public static void trackEvent1(String event) {
        Bundle bundle = new Bundle();
        FirebaseAnalyticsHelper.trackEvent(event, bundle);
    }

}
