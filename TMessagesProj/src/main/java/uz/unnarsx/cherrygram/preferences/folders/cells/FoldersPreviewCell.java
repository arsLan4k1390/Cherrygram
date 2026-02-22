/*

 This is the source code of exteraGram for Android.

 We do not and cannot prevent the use of our code,
 but be respectful and credit the original author.

 Copyright @immat0x1, 2023

*/

package uz.unnarsx.cherrygram.preferences.folders.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Easings;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Objects;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.preferences.folders.helpers.FolderIconHelper;

@SuppressLint("ViewConstructor")
public class FoldersPreviewCell extends FrameLayout {

    private final FrameLayout preview;

    private final RectF rect = new RectF();
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float hideAllChatsProgress;
    private float iconProgress = 0f, titleProgress = 0f;
    private float counterProgress = 0f;
    private String allChatsTabName;
    private String allChatsTabIcon;

    private ValueAnimator animator;

    private final String[][] filters = new String[][]{
            {getString(R.string.FilterAllChats), "\uD83D\uDCAC"},
            {getString(R.string.FilterGroups), "\uD83D\uDC65"},
            {getString(R.string.FilterBots), "\uD83E\uDD16"},
            {getString(R.string.FilterChannels), "\uD83D\uDCE2"},
            {getString(R.string.FilterNameNonMuted), "\uD83D\uDD14"},
            {getString(R.string.FilterContacts), "\uD83C\uDFE0"},
            {getString(R.string.FilterNonContacts), "\uD83C\uDFAD"},
    };

    public FoldersPreviewCell(Context context) {
        super(context);
        setWillNotDraw(false);

        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_switchTrack), 0x3F));
        outlinePaint.setStrokeWidth(Math.max(2, dp(1f)));

        preview = new FrameLayout(context) {
            @SuppressLint("DrawAllocation")
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int color = Theme.getColor(Theme.key_switchTrack);
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                float w = getMeasuredWidth();
                float h = getMeasuredHeight();

                float frameStartX = dp(10);
                float contentInset = dp(5);

                float chipHeight = dp(32);
                float paddingAround = dp(5);

                float centerY = h / 2;

                float frameTop = centerY - (chipHeight / 2) - paddingAround;
                float frameBottom = centerY + (chipHeight / 2) + paddingAround;
                float frameLeft = frameStartX;
                float frameRight = w - frameStartX;

                float startX = frameLeft + contentInset;

                rect.set(frameLeft, frameTop, frameRight, frameBottom);
                Theme.dialogs_onlineCirclePaint.setColor(Color.argb(20, r, g, b));
                canvas.drawRoundRect(rect, dp(50), dp(50), Theme.dialogs_onlineCirclePaint);

                float stroke = outlinePaint.getStrokeWidth() / 2;
                rect.set(frameLeft + stroke, frameTop + stroke, frameRight - stroke, frameBottom - stroke);
                canvas.drawRoundRect(rect, dp(50), dp(50), outlinePaint);

                canvas.save();

                Path frameClip = new Path();
                frameClip.addRoundRect(
                        frameLeft + stroke,
                        frameTop + stroke,
                        frameRight - stroke,
                        frameBottom - stroke,
                        dp(50),
                        dp(50),
                        Path.Direction.CW
                );
                canvas.clipPath(frameClip);

                if (CherrygramAppearanceConfig.INSTANCE.getTabStyleStroke()) {
                    outlinePaint2.setStyle(Paint.Style.STROKE);
                    outlinePaint2.setStrokeWidth(Math.max(5, AndroidUtilities.dp(0.5f)));
                    outlinePaint2.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
                }

                float startY = h - dp(4) - dpf2(4.5f) - stroke;

                @SuppressLint("DrawAllocation") Path tab = new Path();
                tab.addRect(0, startY + dp(4), getMeasuredWidth(), startY + dp(10), Path.Direction.CCW);
                canvas.clipPath(tab, Region.Op.DIFFERENCE);

                textPaint.setTypeface(AndroidUtilities.bold());

                for (int i = 0; i < filters.length; i++) {
                    textPaint.setTextSize(dp(15));

                    String name = i == 0 ? allChatsTabName : filters[i][0];

                    if (i == 0) {
                        textPaint.setColor(ColorUtils.blendARGB(0x00, Theme.getColor(Theme.key_windowBackgroundWhiteValueText), hideAllChatsProgress));
                        textPaint.setTextScaleX(hideAllChatsProgress * titleProgress);

                        int chipBgColor = ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhiteValueText), 0x2F);
                        Theme.dialogs_onlineCirclePaint.setColor(ColorUtils.blendARGB(0x00, chipBgColor, hideAllChatsProgress));
                    } else {
                        textPaint.setColor(ColorUtils.blendARGB(0x00, color, titleProgress));
                        textPaint.setTextScaleX(titleProgress);
                    }

                    Drawable icon = ContextCompat.getDrawable(context, FolderIconHelper.getTabIcon(i == 0 ? allChatsTabIcon : filters[i][1])).mutate();
                    icon.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(0x00, i == 0 ? textPaint.getColor() : color, iconProgress), PorterDuff.Mode.MULTIPLY));

                    float sw = textPaint.measureText(name) + dp(30 + 4) * iconProgress + (i == 0 ? dpf2(24) * counterProgress : 1) + 14 * (1 - iconProgress) * titleProgress - dp(4) * iconProgress * (1 - titleProgress) * counterProgress;
                    float itemGap = dp(10);

                    if (i == 0) {
                        float chipTop = centerY - chipHeight / 2;
                        float chipBottom = centerY + chipHeight / 2;
                        float chipRight = startX + sw + dpf2(4) * (1 - titleProgress) * (1 - counterProgress) + dpf2(22);
                        float cornerRadius = chipHeight / 2;

                        canvas.drawRoundRect(startX, chipTop, chipRight, chipBottom, cornerRadius, cornerRadius, Theme.dialogs_onlineCirclePaint);

                        if (CherrygramAppearanceConfig.INSTANCE.getTabStyleStroke()) {
                            canvas.drawRoundRect(startX, chipTop, chipRight, chipBottom, cornerRadius, cornerRadius, outlinePaint2);
                        }

                        float iconOffset = startX + dpf2(6) * (1 - titleProgress) * (1 - counterProgress) + dpf2(11);
                        int iconSizeHalf = dp(13);
                        icon.setBounds((int) (iconOffset), (int) (centerY - iconSizeHalf), (int) (dpf2(26) * iconProgress * hideAllChatsProgress + iconOffset), (int) (centerY + iconSizeHalf));

                        Paint.FontMetrics fm = textPaint.getFontMetrics();
                        float textY = centerY - (fm.ascent + fm.descent) / 2;
                        canvas.drawText(name, startX + dp(30 * iconProgress) + dpf2(10) + 7f * (1 - iconProgress) * titleProgress, textY, textPaint);

                        textPaint.setTextSize(dp(14 * hideAllChatsProgress * counterProgress));
                        textPaint.setTextScaleX(counterProgress);

                        float counterX = startX + sw - dpf2(15.5f) + dpf2(12) - dp(1) * (1 - titleProgress);
                        float circleCenterX = counterX + dpf2(4);

                        Path path = new Path();
                        textPaint.getTextPath("7", 0, 1, 0, 0, path);

                        RectF bounds = new RectF();
                        path.computeBounds(bounds, true);

                        float offsetX = circleCenterX - bounds.centerX();
                        float offsetY = centerY - bounds.centerY();

                        path.offset(offsetX, offsetY);

                        canvas.save();
                        canvas.clipPath(path, Region.Op.DIFFERENCE);

                        textPaint.setColor(ColorUtils.blendARGB(0x00, Theme.getColor(Theme.key_windowBackgroundWhiteValueText), counterProgress * hideAllChatsProgress));
                        canvas.drawCircle(circleCenterX, centerY, dp(10 * counterProgress * hideAllChatsProgress), textPaint);

                        startX += itemGap + sw + dpf2(22);
                    } else {
                        icon.setBounds((int) startX, (int) (centerY - dp(13)), (int) startX + dp(26 * iconProgress), (int) (centerY + dp(13)));
                        Paint.FontMetrics fm = textPaint.getFontMetrics();
                        canvas.drawText(name, startX + dp(30) * iconProgress, centerY - (fm.ascent + fm.descent) / 2, textPaint);
                        startX += itemGap + sw + dpf2(5);
                    }
                    icon.draw(canvas);
                }
                canvas.restore();
            }
        };
        preview.setWillNotDraw(false);
        addView(preview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.CENTER, 15, 15, 15, 15));
        updateTabIcons(false);
        updateTabTitle(false);
        updateAllChatsTabName(false);
        updateTabCounter(false);
    }

    public void updateAllChatsTabName(boolean animate) {
        if (Objects.equals(allChatsTabName, getAllChatsTabName()) && animate)
            return;
        if (animate) {
            animator = ValueAnimator.ofFloat(1f, 0f).setDuration(250);
            animator.setInterpolator(Easings.easeInOutQuad);
            animator.addUpdateListener(animation -> {
                hideAllChatsProgress = (Float) animation.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    allChatsTabName = getAllChatsTabName();
                    allChatsTabIcon = getAllChatsTabIcon();
                    animator.setFloatValues(0f, 1f);
                    animator.removeAllListeners();
                    animator.start();
                }
            });
            animator.start();
        } else {
            allChatsTabName = getAllChatsTabName();
            allChatsTabIcon = getAllChatsTabIcon();
            hideAllChatsProgress = 1f;
            invalidate();
        }
    }

    public void updateTabTitle(boolean animate) {
        float to = CherrygramAppearanceConfig.INSTANCE.getTabMode() != 2 ? 1 : 0;
        if (to == titleProgress && animate)
            return;
        if (animate) {
            animator = ValueAnimator.ofFloat(titleProgress, to).setDuration(250);
            animator.setInterpolator(Easings.easeInOutQuad);
            animator.addUpdateListener(animation -> {
                titleProgress = (Float) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();
        } else {
            titleProgress = to;
            invalidate();
        }
    }

    public void updateTabIcons(boolean animate) {
        float to = CherrygramAppearanceConfig.INSTANCE.getTabMode() != 1 ? 1 : 0;
        if (to == iconProgress && animate)
            return;
        if (animate) {
            animator = ValueAnimator.ofFloat(iconProgress, to).setDuration(250);
            animator.setInterpolator(Easings.easeInOutQuad);
            animator.addUpdateListener(animation -> {
                iconProgress = (Float) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();
        } else {
            iconProgress = to;
            invalidate();
        }
    }

    public void updateTabCounter(boolean animate) {
        float to = !CherrygramAppearanceConfig.INSTANCE.getTabsNoUnread() ? 1 : 0;
        if (to == counterProgress && animate)
            return;
        if (animate) {
            animator = ValueAnimator.ofFloat(counterProgress, to).setDuration(250);
            animator.setInterpolator(Easings.easeInOutQuad);
            animator.addUpdateListener(animation -> {
                counterProgress = (Float) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();
        } else {
            counterProgress = to;
            invalidate();
        }
    }

    private String getAllChatsTabName() {
        return CherrygramAppearanceConfig.INSTANCE.getTabsHideAllChats() ? filters[6][0] : filters[0][0];
    }

    private String getAllChatsTabIcon() {
        return CherrygramAppearanceConfig.INSTANCE.getTabsHideAllChats() ? filters[6][1] : filters[0][1];
    }

    @Override
    public void invalidate() {
        super.invalidate();
        preview.invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(LocaleController.isRTL ? 0 : dp(21), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? dp(21) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int customHeightSpec = MeasureSpec.makeMeasureSpec(dp(80), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, customHeightSpec);
    }

}
