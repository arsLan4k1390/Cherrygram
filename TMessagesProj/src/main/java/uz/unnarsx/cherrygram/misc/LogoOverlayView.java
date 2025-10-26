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
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.telegram.messenger.NotchInfoUtils;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

@RequiresApi(api = Build.VERSION_CODES.P)
public class LogoOverlayView extends FrameLayout {

    private ImageView logoView;

    @Nullable
    public NotchInfoUtils.NotchInfo notchInfo;

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
        notchInfo = NotchInfoUtils.getInfo(getContext());

        if (notchInfo != null) { // TODO hide if cutout is not in center
            float width = 0.8f * notchInfo.bounds.width();
            float height = 0.8f * notchInfo.bounds.height();

            float localX = notchInfo.bounds.centerX() - width / 2f;
            final float cy = notchInfo.bounds.bottom - notchInfo.bounds.height() / 2f;

            LayoutParams lp = new LayoutParams((int) width, (int) height);
            logoView.setLayoutParams(lp);
            logoView.setX(localX);
            logoView.setY(cy - (width / 2));
            logoView.setVisibility(View.VISIBLE);
        }
        return super.onApplyWindowInsets(insets);
    }

}

