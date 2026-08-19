/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.camera;

import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Easings;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.LaunchActivity;

import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;

public class CameraTypeSelector extends LinearLayout {

    String[] strings = new String[]{
            "Telegram",
            "CameraX",
            "Camera 2 (Telegram)",
            getString(R.string.CP_CameraTypeSystem),
    };

    int[] icons = new int[]{
            R.drawable.camera_icon_telegram,
            R.drawable.camera_icon_cherrygram,
            R.drawable.camera_icon_camerax,
            R.drawable.camera_icon_system
    };
    int currentIcon = CherrygramCameraConfig.INSTANCE.getCameraType();

    private final NumberPicker numberPicker;
    private final FrameLayout preview;

    private final RectF rect = new RectF();
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pickerDividersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ValueAnimator animator;
    private float progress;

    public CameraTypeSelector(Context context) {
        super(context);

        pickerDividersPaint.setStyle(Paint.Style.STROKE);
        pickerDividersPaint.setStrokeCap(Paint.Cap.ROUND);
        pickerDividersPaint.setStrokeWidth(AndroidUtilities.dp(2));

        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_switchTrack), 0x3F));

        preview = new FrameLayout(context) {
            @Override
            @SuppressLint("DrawAllocation")
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int color = Theme.getColor(Theme.key_switchTrack);
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);

                int offsetX = AndroidUtilities.dp(10);
                int minSidePadding = AndroidUtilities.dp(4);
                int halfPhoneWidth = AndroidUtilities.dp(90);

                int previewWidth = getMeasuredWidth();

                int maxHalfPhoneWidth = Math.max(AndroidUtilities.dp(40), previewWidth / 2 - minSidePadding);
                if (halfPhoneWidth > maxHalfPhoneWidth) {
                    halfPhoneWidth = maxHalfPhoneWidth;
                }

                int maxOffsetX = Math.max(0, previewWidth / 2 - halfPhoneWidth - minSidePadding);
                if (offsetX > maxOffsetX) {
                    offsetX = maxOffsetX;
                }

                int centerX = previewWidth / 2 - offsetX;
                int left = centerX - halfPhoneWidth;
                int right = centerX + halfPhoneWidth;
                int top = AndroidUtilities.dp(10);
                int bottom = getMeasuredHeight() - top;

                outlinePaint.setStrokeWidth(Math.max(2, AndroidUtilities.dp(1)));

                float stroke = outlinePaint.getStrokeWidth() / 2;

                Rect rect1 = new Rect();
                int rad = AndroidUtilities.dp(20);
                ShapeDrawable phoneDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, 0, 0, 0, 0}, null, null));

                rect.set(left, top, right, bottom);
                rect.round(rect1);
                phoneDrawable.setBounds(rect1);
                phoneDrawable.getPaint().setColor(Color.argb(20, r, g, b));
                phoneDrawable.draw(canvas);

                rect.set(left + stroke, top + stroke, right - stroke, bottom - stroke);
                rect.round(rect1);
                phoneDrawable.setBounds(rect1);
                phoneDrawable.getPaint().set(outlinePaint);
                phoneDrawable.draw(canvas);

                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{0x00, Theme.getColor(Theme.key_windowBackgroundWhite)}
                );
                gd.setCornerRadius(0f);
                gd.setBounds((int) (left - stroke), (int) (3 * getMeasuredHeight() / 4 - stroke), (int) (right + stroke), (int) (bottom + stroke));
                gd.draw(canvas);

                Drawable d = ContextCompat.getDrawable(context, icons[currentIcon]);
                int ICON_WIDTH = AndroidUtilities.dp(16 + 2 * progress);
                int iconOffsetY = AndroidUtilities.dp(10);
                d.setBounds(
                        centerX - ICON_WIDTH,
                        getMeasuredHeight() / 2 + iconOffsetY,
                        centerX + ICON_WIDTH,
                        getMeasuredHeight() / 2 + iconOffsetY + 2 * ICON_WIDTH
                );
                d.setColorFilter(new PorterDuffColorFilter(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_switchTrack), (int) (0x4F * progress)), PorterDuff.Mode.MULTIPLY));
                d.draw(canvas);

                gd.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                gd.setColors(new int[]{0x00, Color.argb(30, r, g, b)});
                gd.setCornerRadius(AndroidUtilities.dp(25));

                Theme.dialogs_onlineCirclePaint.setColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_switchTrack), 0x3F));
                outlinePaint.setStrokeWidth(Math.max(3, AndroidUtilities.dp(1.5f)));

                CameraClusterDrawable cameraCluster = new CameraClusterDrawable();
                cameraCluster.draw(
                        canvas,
                        left + AndroidUtilities.dp(16),
                        top + AndroidUtilities.dp(16),
                        color,
                        Math.max(AndroidUtilities.dp(1.5f), 3)
                );
            }
        };
        preview.setWillNotDraw(false);
        addView(preview, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));

        numberPicker = new NumberPicker(context, 13) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float y = AndroidUtilities.dp(31);
                pickerDividersPaint.setColor(Theme.getColor(Theme.key_radioBackgroundChecked));
                canvas.drawLine(AndroidUtilities.dp(2), y, getMeasuredWidth() - AndroidUtilities.dp(2), y, pickerDividersPaint);

                y = getMeasuredHeight() - AndroidUtilities.dp(31);
                canvas.drawLine(AndroidUtilities.dp(2), y, getMeasuredWidth() - AndroidUtilities.dp(2), y, pickerDividersPaint);
            }
        };

        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setMinValue(0);
        numberPicker.setDrawDividers(false);
        numberPicker.setMaxValue(strings.length - 1);
        numberPicker.setFormatter(value -> strings[value]);
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            onSelectedCamera(newVal);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                LaunchActivity.makeRipple((preview.getLeft() | preview.getRight()) / 2f, AndroidUtilities.dp(230), 5);
            }

            updateIcon(true);
            picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });
        int selectedButton = CherrygramCameraConfig.INSTANCE.getCameraType();
        numberPicker.setValue(selectedButton);
        addView(numberPicker, LayoutHelper.createFrame(132, 102, Gravity.RIGHT, 0, 33, 21, 33));
        updateIcon(false);
    }

    private static class CameraClusterDrawable {
        private final Paint outlinePaint;
        private final Paint fillPaint;
        private final GradientDrawable fillDrawable;

        private final int bigSize;
        private final int bigRadius;
        private final int bigSpacing;

        private final int smallSize;
        private final int smallRadius;
        private final int smallExtraOffset;

        private final int flashRadius;

        private final int clusterPadding;

        public CameraClusterDrawable() {
            outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            outlinePaint.setStyle(Paint.Style.STROKE);

            fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            fillPaint.setStyle(Paint.Style.FILL);

            fillDrawable = new GradientDrawable();
            fillDrawable.setShape(GradientDrawable.OVAL);

            bigSize = AndroidUtilities.dp(28);
            bigRadius = bigSize / 2;
            bigSpacing = AndroidUtilities.dp(35);

            smallSize = bigSize / 2;
            smallRadius = smallSize / 2;
            smallExtraOffset = AndroidUtilities.dp(5);

            flashRadius = smallRadius / 2;

            clusterPadding = AndroidUtilities.dp(7);
        }

        public void draw(Canvas canvas, int left, int top, int color, float strokeWidth) {
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            outlinePaint.setColor(ColorUtils.setAlphaComponent(color, 0x3F));
            outlinePaint.setStrokeWidth(strokeWidth);

            fillPaint.setColor(Color.argb(15, r, g, b));
            fillDrawable.setColor(Color.argb(30, r, g, b));

            int bigX = left;
            int smallX = bigX + bigSize + AndroidUtilities.dp(10) + smallExtraOffset;
            int flashX = smallX + smallRadius;

            float cy1 = top + bigRadius;
            float cy2 = cy1 + bigSpacing;
            float cy3 = cy2 + bigSpacing;

            RectF clusterRect = new RectF(
                    bigX - clusterPadding,
                    cy1 - bigRadius - clusterPadding,
                    bigX + bigSize + clusterPadding,
                    cy3 + bigRadius + clusterPadding
            );
            float clusterCorner = clusterRect.width() / 2f;

            canvas.drawRoundRect(clusterRect, clusterCorner, clusterCorner, fillPaint);
            canvas.drawRoundRect(clusterRect, clusterCorner, clusterCorner, outlinePaint);

            drawLens(canvas, bigX, cy1, bigSize, bigRadius);
            drawLens(canvas, bigX, cy2, bigSize, bigRadius);
            drawLens(canvas, bigX, cy3, bigSize, bigRadius);

            drawModule(canvas, smallX, cy1, smallSize, smallRadius);
            drawModule(canvas, smallX, cy2, smallSize, smallRadius);

            float flashCy = (cy1 + cy2) / 2f;
            canvas.drawCircle(flashX, flashCy, flashRadius, outlinePaint);
        }

        private void drawModule(Canvas canvas, int x, float cy, int size, int radius) {
            fillDrawable.setBounds(
                    x,
                    (int) (cy - radius),
                    x + size,
                    (int) (cy + radius)
            );
            fillDrawable.draw(canvas);

            canvas.drawCircle(
                    x + radius,
                    cy,
                    radius,
                    outlinePaint
            );
        }

        private void drawLens(Canvas canvas, int x, float cy, int size, int radius) {
            canvas.drawCircle(
                    x + radius,
                    cy,
                    radius,
                    outlinePaint
            );
        }
    }

    protected void onSelectedCamera(int cameraSelected) {
    }

    public void updateIcon(boolean animate) {
        if (animate) {
            animator = ValueAnimator.ofFloat(1f, 0f).setDuration(100);
            animator.setInterpolator(Easings.easeInOutQuad);
            animator.addUpdateListener(animation -> {
                progress = (Float) animation.getAnimatedValue();
                preview.invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    currentIcon = CherrygramCameraConfig.INSTANCE.getCameraType();
                    animator.setFloatValues(0f, 1f);
                    animator.removeAllListeners();
                    animator.start();
                }
            });
            animator.start();
        } else {
            progress = 1f;
            preview.invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(168), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (numberPicker.getValue() == 1) {
            canvas.drawLine(AndroidUtilities.dp(8), getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(8), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }
}
