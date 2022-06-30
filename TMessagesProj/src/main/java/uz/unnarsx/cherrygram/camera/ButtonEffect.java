/*
 * This is the source code of OwlGram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Laky64, 2021-2022.
 */
package uz.unnarsx.cherrygram.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.Components.CubicBezierInterpolator;

@SuppressLint("ViewConstructor")
public class ButtonEffect extends RelativeLayout {
    final private ImageView imageView;
    private ValueAnimator toggleAnimation;
    private boolean isSelected = false;
    private boolean reachedHalf = false;
    private float currAn = 0f;
    final public int cameraType;

    @SuppressLint("ClickableViewAccessibility")
    public ButtonEffect(Context context, int camera_type) {
        super(context);
        cameraType = camera_type;
        imageView = new ImageView(context);
        imageView.setClickable(true);
        imageView.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !isSelected) {
                onItemClick(this, cameraType);
            }
            return false;
        });
        imageView.setImageBitmap(getIcon());

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageView.setLayoutParams(layoutParams);
        addView(imageView);
    }

    private Bitmap getIcon() {
        int w = AndroidUtilities.dp(50);
        Bitmap bmp = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Drawable d = getResources().getDrawable(getIconRes(cameraType));
        d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        int s = (w * 60) / 100;
        int x = (w >> 1) - (s >> 1);
        int y = (w >> 1) - (s >> 1);
        d.setBounds(x, y, x + s, y + s);
        d.draw(canvas);
        if (isSelected) {
            Paint level_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            level_paint.setColor(Color.WHITE);
            level_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            int s2 = ((w * 80) / 100) >> 1;
            int x2 = (w >> 1);
            int y2 = (w >> 1);
            canvas.drawCircle(x2,y2, s2, level_paint);
        }
        return bmp;
    }

    private int getIconRes(int icon) {
        switch (icon) {
            case CameraXController.CAMERA_HDR:
                return R.drawable.round_hdr_on_black;
            case CameraXController.CAMERA_NIGHT:
                return R.drawable.round_bedtime_black;
            case CameraXController.CAMERA_AUTO:
                return R.drawable.round_auto_fix_high_black;
            case CameraXController.CAMERA_WIDE:
                return R.drawable.round_landscape_black;
            case CameraXController.CAMERA_NONE:
            default:
                return R.drawable.round_photo_camera_black;
        }
    }

    public void toggleButton(boolean enabled, boolean animated) {
        isSelected = enabled;
        if (!animated) {
            imageView.setImageBitmap(getIcon());
        } else {
            currAn = toggleAnimation != null ? 2f - currAn:0;
            reachedHalf = false;
            if (toggleAnimation != null) {
                toggleAnimation.cancel();
            }
            imageView.animate().setListener(null).cancel();
            float timeAnimation = (2f - currAn) / 2f;
            toggleAnimation = ValueAnimator.ofFloat(currAn, 2f);
            toggleAnimation.addUpdateListener(valueAnimator -> {
                float v = (float) valueAnimator.getAnimatedValue();
                currAn = v;
                float rAn;
                if (v > 1f) {
                    if (!reachedHalf) {
                        reachedHalf = true;
                        imageView.setImageBitmap(getIcon());
                    }
                    rAn = v - 1f;
                } else {
                    rAn = 1f - v;
                }
                imageView.setScaleX(rAn);
                imageView.setScaleY(rAn);
            });
            toggleAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    toggleAnimation = null;
                    imageView.setScaleX(1f);
                    imageView.setScaleY(1f);
                }
            });
            toggleAnimation.setDuration(Math.round(300 * timeAnimation));
            toggleAnimation.setInterpolator(CubicBezierInterpolator.DEFAULT);
            toggleAnimation.start();
        }
    }

    protected void onItemClick(ButtonEffect buttonEffect, int camera_type) {}
}
