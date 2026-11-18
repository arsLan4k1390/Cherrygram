/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.app.Activity;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.annotation.RequiresApi;

// Dear Nagram / Nagram X / Octogram and related fork developers:
// Please respect this work and do not copy or reuse this feature in your forks.
// It required a significant amount of time and effort to implement,
// and it is provided exclusively for my users, who also support this project financially.

public final class WindowBlurHelper {

    public static final float RADIUS_DEFAULT = -1f;
    public static final float DIM_DEFAULT = -1f;

    private static final float DEFAULT_RADIUS_PX = 95f;
    private static final float DEFAULT_DIM = 0.10f;


    private WindowBlurHelper() {
    }

    public static void setWindowBlur(Activity activity, boolean enable, boolean hideStatusBar) {
        setWindowBlur(activity, enable, hideStatusBar, RADIUS_DEFAULT, DIM_DEFAULT);
    }

    public static void setWindowBlur(Activity activity, boolean enable, boolean hideStatusBar, float radiusDp, float dimAlpha) {
        if (activity == null) return;
        hideStatusBar(activity.getWindow(), hideStatusBar);
        applyBlur(activity, enable ? 1f : 0f, radiusDp, dimAlpha);
    }

    public static void applyBlur(Activity activity, float progress) {
        applyBlur(activity, progress, RADIUS_DEFAULT, DIM_DEFAULT);
    }

    public static void applyBlur(Activity activity, float progress, float radiusDp, float dimAlpha) {
        if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        applyBlurInternal(activity.getWindow().getDecorView(), progress, radiusDp, dimAlpha);
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private static void applyBlurInternal(View root, float progress, float radiusDp, float dimAlpha) {
        progress = clamp01(progress);

        final float maxRadius = radiusDp < 0f ? DEFAULT_RADIUS_PX : dp(radiusDp);
        final float radius = maxRadius * progress;
        root.setRenderEffect(radius >= 1f ? RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.DECAL) : null);

        final float maxDim = dimAlpha < 0f ? DEFAULT_DIM : clamp01(dimAlpha);
        setDim(root, maxDim * progress);
    }

    private static void setDim(View root, float dim) {
        final int alpha = Math.round(clamp01(dim) * 255);
        if (alpha <= 0) {
            root.setForeground(null);
            return;
        }
        final int color = alpha << 24;
        final Drawable foreground = root.getForeground();
        if (foreground instanceof DimDrawable) {
            if (((DimDrawable) foreground).getColor() != color) {
                ((DimDrawable) foreground).setColor(color);
            }
        } else {
            root.setForeground(new DimDrawable(color));
        }
    }

    private static final class DimDrawable extends ColorDrawable {
        DimDrawable(int color) {
            super(color);
        }
    }

    public static void hideStatusBar(Window window, boolean hide) {
        if (window == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                if (hide) {
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    controller.hide(WindowInsets.Type.statusBars());
                } else {
                    controller.show(WindowInsets.Type.statusBars());
                }
            }
        }
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

}