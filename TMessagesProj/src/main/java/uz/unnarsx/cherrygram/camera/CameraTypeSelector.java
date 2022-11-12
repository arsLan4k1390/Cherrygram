package uz.unnarsx.cherrygram.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;

import uz.unnarsx.cherrygram.CherrygramConfig;

public class CameraTypeSelector extends LinearLayout {
    Paint pickerDividersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    String[] strings = new String[]{
            LocaleController.getString("CP_CameraTypeDefault", R.string.CP_CameraTypeDefault),
            "CameraX",
            LocaleController.getString("CP_CameraTypeSystem", R.string.CP_CameraTypeSystem),
    };
    private final NumberPicker picker1;

    public CameraTypeSelector(Context context) {
        super(context);

        pickerDividersPaint.setStyle(Paint.Style.STROKE);
        pickerDividersPaint.setStrokeCap(Paint.Cap.ROUND);
        pickerDividersPaint.setStrokeWidth(AndroidUtilities.dp(2));
        int colorIcon = Theme.getColor(Theme.key_switchTrack);
        int color = AndroidUtilities.getTransparentColor(colorIcon, 0.5f);
        ImageView imageView = new ImageView(context) {
            @Override
            @SuppressLint("DrawAllocation")
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int h = getMeasuredHeight() - AndroidUtilities.dp(20);
                int w = Math.round((h * 222.22F) / 100F);

                int left = (getMeasuredWidth() / 2) - (w / 2);
                int top = (getMeasuredHeight() / 2) - (h / 2);
                int right = w + left;
                int bottom = top + h;
                int radius = Math.round((h * 14.77F) / 100F);

                RectF rectF = new RectF(left, top, right, bottom);
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setStyle(Paint.Style.STROKE);
                p.setColor(color);
                p.setStrokeWidth(AndroidUtilities.dp(2));
                canvas.drawRoundRect(rectF, radius, radius, p);

                int h2 = Math.round((h * 32.01F) / 100F);
                int w2 = Math.round((h2 * 176.92F) / 100F);
                int spaceBetween = AndroidUtilities.dp(6);
                int left2 = left + spaceBetween;
                int bottom2 = bottom - spaceBetween;
                int top2 = bottom2 - h2;
                int right2 = left2 + w2;
                int radius2 = radius - spaceBetween;

                RectF rectF2 = new RectF(left2, top2, right2, bottom2);
                canvas.drawRoundRect(rectF2, radius2, radius2, p);

                int strokeSpace = AndroidUtilities.dp(2);
                int bottomEnd = bottom2 - strokeSpace;
                int leftStart = left2 + strokeSpace;
                int topStart = top2 + strokeSpace;
                int rightEnd = right2 - strokeSpace;
                int blockSizeW = Math.round((rightEnd - leftStart) / 3F);
                int blockSizeH = Math.round((bottomEnd - topStart) / 2F);

                Paint pCamera = new Paint(Paint.ANTI_ALIAS_FLAG);
                for (int y = 0; y < 2; y++) {
                    for (int x = 0; x < 3; x++) {
                        int xLeftPos = leftStart + (blockSizeW * x);
                        int yTopPos = topStart + (blockSizeH * y);
                        int lensRad2 = Math.round((blockSizeH * 70F) / 100F);
                        int leftLens = xLeftPos + (blockSizeW / 2);
                        int topLens = yTopPos + (blockSizeH / 2);
                        pCamera.setColor(color);
                        canvas.drawCircle(leftLens, topLens, Math.round(lensRad2 / 2F), pCamera);
                        if (y == 0 && x == 1) {
                            break;
                        }
                    }
                }
                int cameraIdEffect;
                switch (picker1.getValue()) {
                    case 0:
                        cameraIdEffect = R.drawable.telegram_camera_icon;
                        break;
                    case 1:
                        cameraIdEffect = R.drawable.camerax_icon;
                        break;
                    case 2:
                    default:
                        cameraIdEffect = R.drawable.android_camera_icon;
                }
                Drawable d = getResources().getDrawable(cameraIdEffect);
                int iconH = Math.round((h * 37F) / 100F);
                int iconW = Math.round((iconH * 98.03F) / 100F);
                int leftIcon = (getMeasuredWidth() / 2) - (iconW / 2);
                int topIcon = (getMeasuredHeight() / 2) - (iconH / 2);
                int rightIcon = leftIcon + iconW;
                int bottomIcon = topIcon + iconH;
                Rect rect = new Rect(leftIcon, topIcon, rightIcon, bottomIcon);
                d.setBounds(rect);
                d.setAlpha(Math.round(255 * 0.5F));
                d.setColorFilter(new PorterDuffColorFilter(colorIcon, PorterDuff.Mode.SRC_ATOP));
                d.draw(canvas);
            }
        };
        imageView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));

        addView(imageView);

        picker1 = new NumberPicker(context, 13) {
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

        picker1.setWrapSelectorWheel(true);
        picker1.setMinValue(0);
        picker1.setDrawDividers(false);
        picker1.setMaxValue(strings.length - 1);
        picker1.setFormatter(value -> strings[value]);
        picker1.setOnValueChangedListener((picker, oldVal, newVal) -> {
            imageView.invalidate();
            CherrygramConfig.INSTANCE.saveCameraType(newVal);
            invalidate();
            onSelectedCamera(newVal);
            picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });
        int selectedButton = CherrygramConfig.INSTANCE.getCameraType();
        picker1.setValue(selectedButton);
        addView(picker1, LayoutHelper.createFrame(132, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 0, 0, 21, 0));
    }

    protected void onSelectedCamera(int cameraSelected) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(102), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (picker1.getValue() == 1) {
            canvas.drawLine(AndroidUtilities.dp(8), getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(8), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }
}
