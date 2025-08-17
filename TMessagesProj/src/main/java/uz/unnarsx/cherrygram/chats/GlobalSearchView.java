/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Components.BlurredFrameLayout;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.SnowflakesEffect;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;

// Dear Nagram / Nagram X / Octogram and related fork developers:
// Please respect this work and do not copy or reuse this feature in your forks.
// It required a significant amount of time and effort to implement,
// and it is provided exclusively for my users, who also support this project financially.

@SuppressLint("ViewConstructor")
public class GlobalSearchView extends BlurredFrameLayout {

    private TextView searchTextView;
    private BlurredFrameLayout searchFrame;

    private SnowflakesEffect snowflakesEffect;

    /*public GlobalSearchView(Context context) {
        super(context, null);
    }*/

    public GlobalSearchView(Context context, SizeNotifierFrameLayout sizeNotifierFrameLayout) {
        super(context, sizeNotifierFrameLayout);

        setWillNotDraw(false);

        searchFrame = new BlurredFrameLayout(context, sizeNotifierFrameLayout);
        searchFrame.isTopView = false;
        searchTextView = new TextView(context);
        searchTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        searchTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append("..").setSpan(new ColoredImageSpan(ContextCompat.getDrawable(context, R.drawable.ic_ab_search)), 0, 1, 0);
        spannableStringBuilder.setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(4)), 1, 2, 0);
        spannableStringBuilder.append(getString(R.string.Search));

        searchTextView.setText(spannableStringBuilder);
        searchTextView.setGravity(Gravity.CENTER);
        searchTextView.setSingleLine(true);

        searchFrame.addView(searchTextView);
        addView(searchFrame, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 38, 0, dp(5), 0, dp(5), 0));

        updateColors();
    }

    public void updateColors() {
        int backgroundColor;
        int textColor = Theme.getColor(Theme.key_dialogTextGray3);

        if (!Theme.isCurrentThemeDay()) {
            backgroundColor = ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_graySection), (int) (255 * 0.35f));

            if (Theme.getActiveTheme().isMonet() || Theme.getActiveTheme().isAmoled()) {
                backgroundColor = ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_actionBarTabLine), 0x2F);
                textColor = Theme.getColor(Theme.key_actionBarTabActiveText);
            }

        } else {
            if (isWhiteOrNearWhite(Theme.getColor(Theme.key_actionBarDefault))) {
                backgroundColor = Theme.getColor(Theme.key_graySection);
            } else {
                backgroundColor = ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_actionBarTabLine), 0x2F);
                textColor = Theme.getColor(Theme.key_actionBarTabActiveText);
            }
        }
        searchTextView.setTextColor(textColor);

        Drawable background = Theme.createSimpleSelectorRoundRectDrawable(
                dp(10),
                backgroundColor,
                ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_listSelector), (int) (255 * 0.3f))
        );
        searchFrame.setBackground(background);

        if (snowflakesEffect != null) {
            snowflakesEffect.updateColors();
        }
    }

    private boolean isWhiteOrNearWhite(@ColorInt int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        if (r < 240 || g < 240 || b < 240) {
            return false;
        }

        float[] hsl = new float[3];
        ColorUtils.RGBToHSL(r, g, b, hsl);

        float saturation = hsl[1];
        float lightness = hsl[2];

        return saturation <= 0.1f && lightness >= 0.9f;
    }

    public BlurredFrameLayout getSearchFrame() {
        return searchFrame;
    }

    public TextView getSearchTextView() {
        return searchTextView;
    }

    public static void saveFoldersExistence() {
        MessagesController messagesController = MessagesController.getInstance(UserConfig.selectedAccount);
        messagesController.getMainSettings()
                .edit()
                .putBoolean("user_has_folders_for_search", messagesController.getDialogFilters() != null && messagesController.getDialogFilters().size() > 1)
                .apply();
    }

    public boolean getFoldersExistence() {
        return MessagesController.getMainSettings(UserConfig.selectedAccount)
                .getBoolean("user_has_folders_for_search", CherrygramAppearanceConfig.INSTANCE.getIosSearchPanel());
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        checkSnowflake(canvas);
    }

    private void checkSnowflake(Canvas canvas) {
        if (!CherrygramAppearanceConfig.INSTANCE.getDrawSnowInActionBar()) return;
        if (snowflakesEffect == null) {
            snowflakesEffect = new SnowflakesEffect(0);
        }
        snowflakesEffect.onDraw(this, canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (snowflakesEffect != null) snowflakesEffect = null;
    }

}
