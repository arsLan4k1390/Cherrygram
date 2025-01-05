/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.drawer.cells;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

public class ThemeDrawerCell extends FrameLayout {

    private final float STROKE_RADIUS = AndroidUtilities.dp(8);
    private final float INNER_RADIUS = AndroidUtilities.dp(6);
    private final float INNER_RECT_SPACE = AndroidUtilities.dp(4);
    private final RLottieImageView lottieImageView;
    private final Paint outlineBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final EventThemeDrawable themeDrawable = new EventThemeDrawable();
    private final RectF rectF = new RectF();
    private final Path clipPath = new Path();

    private ValueAnimator strokeAlphaAnimator;
    private float selectionProgress;
    private float changeThemeProgress = 1f;
    private int[] icons;
    int eventId = 0;
    Runnable animationCancelRunnable;


    private class EventThemeDrawable {

        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        EventThemeDrawable() {
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(AndroidUtilities.dp(2));
        }

        public void drawBackground(Canvas canvas) {
            canvas.save();
            canvas.clipPath(clipPath);
            Paint level_paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
            level_paint1.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            canvas.drawRect(new RectF(0, 0, getWidth(), getHeight()), level_paint1);
            Paint level_paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
            level_paint2.setColor(Color.BLACK);
            level_paint2.setAlpha(Math.round(255 * 0.25f));
            canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), level_paint2);
            Paint level_paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
            level_paint3.setColor(Theme.getColor(Theme.key_chats_menuBackground));
            int nav_width = Math.round(getWidth() * 0.83f);
            canvas.drawRect(new Rect(0, 0, nav_width, getHeight()), level_paint3);
            Paint level_paint4 = new Paint(Paint.ANTI_ALIAS_FLAG);
            level_paint4.setColor(Theme.getColor(Theme.key_chats_menuTopBackgroundCats));
            int avatarHeight = Math.round(nav_width * 0.59f);
            canvas.drawRect(new Rect(0, 0, nav_width, avatarHeight), level_paint4);
            PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), PorterDuff.Mode.SRC_ATOP);
            Paint level_paint5 = new Paint(Paint.ANTI_ALIAS_FLAG);
            level_paint5.setColor(Theme.getColor(Theme.key_chats_menuItemIcon));
            float[] text_widths = new float[]{
                    0.95f,
                    0.85f,
                    0.90f,
                    0.95f,
                    1.0f,
            };
            if (icons != null) {
                for (int i = 0; i < icons.length; i++) {
                    Drawable icon = getResources().getDrawable(icons[i]);
                    int iconSize = Math.round(nav_width * 0.19f);
                    int textSize = Math.round(iconSize * 0.6f);
                    int yOffsetText = Math.round((iconSize - textSize) / 2.0f);
                    int xOffset = Math.round(nav_width * 0.12f);
                    int xEndOffset = Math.round(nav_width * 0.11f);
                    int yOffset = (iconSize * i) + avatarHeight + Math.round(nav_width * 0.10f) * (i + 1);
                    int radius = Math.round(iconSize / 2.0f);
                    Rect rectParams = new Rect(xOffset, yOffset, xOffset + iconSize, yOffset + iconSize);
                    icon.setBounds(rectParams);
                    icon.setColorFilter(colorFilter);
                    icon.draw(canvas);
                    RectF roundRectParams = new RectF((xOffset * 2) + iconSize, yOffset + yOffsetText, Math.round((nav_width - xEndOffset) * text_widths[i]), yOffsetText + yOffset + textSize);
                    canvas.drawRoundRect(roundRectParams, radius, radius, level_paint5);
                }
            }
            int height = Math.round((getHeight() * 30) / 100f);
            int colorBackground = Theme.getColor(Theme.key_chats_menuBackground);
            Rect rectParamGradient = new Rect(0, getHeight() - height, getWidth(), getHeight());
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{colorBackground, AndroidUtilities.getTransparentColor(colorBackground, 0)});
            gd.setCornerRadius(0f);
            gd.setBounds(rectParamGradient);
            gd.draw(canvas);
            outlineBackgroundPaint.setAlpha((int) (255 * 0.3));
            float padding = INNER_RECT_SPACE;
            AndroidUtilities.rectTmp.set(padding, padding, getWidth() - padding, getHeight() - padding);
            canvas.drawRoundRect(AndroidUtilities.rectTmp, INNER_RADIUS, INNER_RADIUS, outlineBackgroundPaint);
            canvas.restore();
        }

        public void draw(Canvas canvas, float alpha) {
            if (isSelected || strokeAlphaAnimator != null) {
                strokePaint.setColor(Theme.getColor(Theme.key_dialogTextBlue));
                strokePaint.setAlpha((int) (selectionProgress * alpha * 255));
                float rectSpace = strokePaint.getStrokeWidth() * 0.5f + AndroidUtilities.dp(4) * (1f - selectionProgress);
                rectF.set(rectSpace, rectSpace, getWidth() - rectSpace, getHeight() - rectSpace);
                canvas.drawRoundRect(rectF, STROKE_RADIUS, STROKE_RADIUS, strokePaint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == oldw && h == oldh) {
            return;
        }
        rectF.set(INNER_RECT_SPACE, INNER_RECT_SPACE, w - INNER_RECT_SPACE, h - INNER_RECT_SPACE);
        clipPath.reset();
        clipPath.addRoundRect(rectF, INNER_RADIUS, INNER_RADIUS, Path.Direction.CW);
    }

    public boolean canBeAnimate() {
        return lottieImageView.getAnimatedDrawable() != null;
    }

    public void setEvent(int eventId, int icon, int[] icons) {
        if (lottieImageView.getAnimatedDrawable() == null) {
            this.icons = icons;
            this.eventId = eventId;
            lottieImageView.setAnimation(icon, 32, 32);
        }
    }

    public ThemeDrawerCell(Context context) {
        super(context);
        lottieImageView = new RLottieImageView(context);
        lottieImageView.setScaleType(ImageView.ScaleType.CENTER);
        addView(lottieImageView, LayoutHelper.createFrame(32, 32, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 12));
        outlineBackgroundPaint.setStrokeWidth(AndroidUtilities.dp(2));
        outlineBackgroundPaint.setStyle(Paint.Style.STROKE);
        outlineBackgroundPaint.setColor(Theme.getColor(Theme.key_switchTrack));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = AndroidUtilities.dp(90);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        lottieImageView.setPivotY(lottieImageView.getMeasuredHeight());
        lottieImageView.setPivotX(lottieImageView.getMeasuredWidth() / 2f);
    }

    boolean isSelected;


    public void setSelected(boolean selected, boolean animated) {
        if (!animated) {
            if (strokeAlphaAnimator != null) {
                strokeAlphaAnimator.cancel();
            }
            isSelected = selected;
            selectionProgress = selected ? 1f : 0;
            invalidate();
            return;
        }
        if (isSelected != selected) {
            float currentProgress = selectionProgress;
            if (strokeAlphaAnimator != null) {
                strokeAlphaAnimator.cancel();
            }
            strokeAlphaAnimator = ValueAnimator.ofFloat(currentProgress, selected ? 1f : 0);
            strokeAlphaAnimator.addUpdateListener(valueAnimator -> {
                selectionProgress = (float) valueAnimator.getAnimatedValue();
                invalidate();
            });
            strokeAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    selectionProgress = selected ? 1f : 0;
                    invalidate();
                }
            });
            strokeAlphaAnimator.setDuration(250);
            strokeAlphaAnimator.start();
        }
        isSelected = selected;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (changeThemeProgress != 0) {
            themeDrawable.drawBackground(canvas);
        }
        if (changeThemeProgress != 0) {
            themeDrawable.draw(canvas, changeThemeProgress);
        }
        if (changeThemeProgress != 1f) {
            changeThemeProgress += 16 / 150f;
            if (changeThemeProgress >= 1f) {
                changeThemeProgress = 1f;
            }
            invalidate();
        }
        super.dispatchDraw(canvas);
    }

    public void playEmojiAnimation() {
        AndroidUtilities.cancelRunOnUIThread(animationCancelRunnable);
        lottieImageView.setProgress(0);
        lottieImageView.playAnimation();
        lottieImageView.animate().scaleX(2f).scaleY(2f).setDuration(300).setInterpolator(AndroidUtilities.overshootInterpolator).start();

        AndroidUtilities.runOnUIThread(animationCancelRunnable = () -> {
            animationCancelRunnable = null;
            lottieImageView.animate().scaleX(1f).scaleY(1f).setDuration(150).setInterpolator(CubicBezierInterpolator.DEFAULT).start();
        }, 2500);
    }

    public void cancelAnimation() {
        if (animationCancelRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(animationCancelRunnable);
            animationCancelRunnable.run();
        }
    }
}
