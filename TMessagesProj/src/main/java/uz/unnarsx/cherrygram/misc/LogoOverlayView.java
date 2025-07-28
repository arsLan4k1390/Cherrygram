/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.misc;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.P)
public class LogoOverlayView extends FrameLayout {

    private ImageView logoView;
    private Rect notchRect;

    public LogoOverlayView(Context context) {
        super(context);
        init();
    }

    public LogoOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        logoView = new ImageView(getContext());
        logoView.setImageResource(!Theme.isCurrentThemeDay() ? R.drawable.cg_logo_notch_white : R.drawable.cg_logo_notch);
        logoView.setVisibility(View.INVISIBLE);
        addView(logoView);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        DisplayCutout cutout = insets.getDisplayCutout();
        if (cutout != null) {
            List<Rect> rects = cutout.getBoundingRects();
            if (!rects.isEmpty()) {
                notchRect = rects.get(0);

                float cutoutWidth = notchRect.width();
                float cutoutHeight = notchRect.height();
                float size = Math.min(cutoutWidth, cutoutHeight) * 0.9f;

                float centerX = notchRect.left + cutoutWidth / 2f;
                float localX = centerX - size / 2f;

                float localY = notchRect.top + cutoutHeight / 2f - size / 2f + AndroidUtilities.dpf2(2);

                LayoutParams lp = new LayoutParams((int) size, (int) size);
                logoView.setLayoutParams(lp);
                logoView.setX(localX);
                logoView.setY(localY);
                logoView.setVisibility(View.VISIBLE);
            }
        }
        return super.onApplyWindowInsets(insets);
    }

}

