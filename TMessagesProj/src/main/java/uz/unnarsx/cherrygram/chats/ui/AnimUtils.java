/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.ui;

import android.animation.ValueAnimator;
import android.os.Build;

import org.telegram.messenger.FileLog;

import java.lang.reflect.Method;

import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig;
import uz.unnarsx.cherrygram.core.firebase.crashlytics.FirebaseCrashlyticsHelper;

public class AnimUtils {

    private static void setDurationScale(float scale) {
        try {
            Method method = ValueAnimator.class.getMethod("setDurationScale", float.class);
            method.setAccessible(true);
            method.invoke(null, scale);
        } catch (Exception e) {
            CherrygramLogger.e(e);
            FirebaseCrashlyticsHelper.INSTANCE.logAsNonFatal(e);
        }
    }

    private static float getDurationScale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ValueAnimator.getDurationScale();
        }

        try {
            Method method = ValueAnimator.class.getMethod("getDurationScale");
            method.setAccessible(true);
            Object result = method.invoke(null);
            if (result instanceof Float) {
                return (Float) result;
            }
        } catch (Exception ignored) {}

        return 1.0f;
    }

    private static boolean shouldFixAnimation() {
        return CherrygramMessagesConfig.INSTANCE.getFixMsgMenuAnimation() && getDurationScale() > 0.5F;
    }

    public static long getAnimDuration(long defaultDuration) {
        if (shouldFixAnimation()) {
            return defaultDuration / 2;
        }
        return defaultDuration;
    }

}
