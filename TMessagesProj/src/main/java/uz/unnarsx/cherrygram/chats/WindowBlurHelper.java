/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats;

import android.app.Activity;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

// Dear Nagram / Nagram X / Octogram and related fork developers:
// Please respect this work and do not copy or reuse this feature in your forks.
// It required a significant amount of time and effort to implement,
// and it is provided exclusively for my users, who also support this project financially.

@RequiresApi(Build.VERSION_CODES.S)
public class WindowBlurHelper {

    public void setWindowBlur(
            Activity activity,
            boolean enable,
            boolean hideStatusBar,
            float windowBlurRadius
    ) {
        if (activity == null) return;

        Window window = activity.getWindow();

        hideStatusBar(window, hideStatusBar);

        View root = window.getDecorView();
        if (enable) {
            root.setRenderEffect(
                    RenderEffect.createBlurEffect(windowBlurRadius, windowBlurRadius, Shader.TileMode.DECAL)
            );
        } else {
            root.setRenderEffect(null);
        }
    }

    public void hideStatusBar(Window window, boolean hide) {
        if (hide) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

}
