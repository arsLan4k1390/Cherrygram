/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.Components.AnimationProperties;

@SuppressLint("ViewConstructor")
public class SlideControlView extends View {

    public static int SLIDER_MODE_ZOOM = 0;
    public static int SLIDER_MODE_EV = 1;

    private final int mode;

    public Drawable minusDrawable;
    public Drawable plusDrawable;
    public final Drawable progressDrawable;
    public Drawable filledProgressDrawable;
    public final Drawable knobDrawable;
    public final Drawable pressedKnobDrawable;

    private int minusCx;
    private int minusCy;
    private int plusCx;
    private int plusCy;

    private int progressStartX;
    private int progressStartY;
    private int progressEndX;
    private int progressEndY;

    private float sliderValue;

    private boolean pressed;
    private boolean knobPressed;
    private float knobStartX;
    private float knobStartY;

    private float animatingToZoom;
    private AnimatorSet animatorSet;

    private SliderControlViewDelegate delegate;

    public interface SliderControlViewDelegate {
        void didSlide(float sliderValue);
    }

    public final Property<SlideControlView, Float> SLIDER_PROPERTY = new AnimationProperties.FloatProperty<>("clipProgress") {
        @Override
        public void setValue(SlideControlView object, float value) {
            sliderValue = value;
            if (delegate != null) {
                delegate.didSlide(sliderValue);
            }
            invalidate();
        }

        @Override
        public Float get(SlideControlView object) {
            return sliderValue;
        }
    };

    public SlideControlView(Context context, int mode) {
        super(context);
        this.mode = mode;

        if (mode == SLIDER_MODE_ZOOM) {
            minusDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.zoom_minus, context.getTheme());
            plusDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.zoom_plus, context.getTheme());
        } else if (mode == SLIDER_MODE_EV) {
            minusDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.camera_moon_solar, context.getTheme());
            plusDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.camera_sun_solar, context.getTheme());
        }

        progressDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.zoom_slide, context.getTheme());
        if (mode == SLIDER_MODE_ZOOM) {
            filledProgressDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.zoom_slide_a, context.getTheme());
        } else if (mode == SLIDER_MODE_EV) {
            filledProgressDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.zoom_slide, context.getTheme());
        }
        knobDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.zoom_round, context.getTheme());
        pressedKnobDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.zoom_round_b, context.getTheme());
    }

    public float getSliderValue() {
        if (animatorSet != null) {
            return animatingToZoom;
        }
        return sliderValue;
    }

    public void setSliderValue(float value, boolean notify) {
        if (value == sliderValue) {
            return;
        }
        if (value < 0) {
            value = 0;
        } else if (value > 1.0f) {
            value = 1.0f;
        }
        sliderValue = value;
        if (notify && delegate != null) {
            delegate.didSlide(sliderValue);
        }
        invalidate();
    }

    public void setDelegate(SliderControlViewDelegate sliderControlViewDelegate) {
        delegate = sliderControlViewDelegate;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        boolean handled = false;
        boolean isPortrait = getMeasuredWidth() > getMeasuredHeight();
        int knobX = (int) (progressStartX + (progressEndX - progressStartX) * sliderValue);
        int knobY = (int) (progressStartY + (progressEndY - progressStartY) * sliderValue);
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            if (x >= knobX - AndroidUtilities.dp(20) && x <= knobX + AndroidUtilities.dp(20) && y >= knobY - AndroidUtilities.dp(25) && y <= knobY + AndroidUtilities.dp(25)) {
                if (action == MotionEvent.ACTION_DOWN) {
                    knobPressed = true;
                    knobStartX = x - knobX;
                    knobStartY = y - knobY;
                    invalidate();
                }
                handled = true;
            } else if (x >= minusCx - AndroidUtilities.dp(16) && x <= minusCx + AndroidUtilities.dp(16) && y >= minusCy - AndroidUtilities.dp(16) && y <= minusCy + AndroidUtilities.dp(16)) {
                if (action == MotionEvent.ACTION_UP && animateToValue((float) Math.floor(getSliderValue() / 0.25f) * 0.25f - 0.25f)) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                } else {
                    pressed = true;
                }
                handled = true;
            } else if (x >= plusCx - AndroidUtilities.dp(16) && x <= plusCx + AndroidUtilities.dp(16) && y >= plusCy - AndroidUtilities.dp(16) && y <= plusCy + AndroidUtilities.dp(16)) {
                if (action == MotionEvent.ACTION_UP && animateToValue((float) Math.floor(getSliderValue() / 0.25f) * 0.25f + 0.25f)) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                } else {
                    pressed = true;
                }
                handled = true;
            } else if (isPortrait) {
                if (x >= progressStartX && x <= progressEndX) {
                    if (action == MotionEvent.ACTION_DOWN) {
                        knobStartX = x;
                        pressed = true;
                    } else if (Math.abs(knobStartX - x) <= AndroidUtilities.dp(10)) {
                        sliderValue = (x - progressStartX) / (progressEndX - progressStartX);
                        if (delegate != null) {
                            delegate.didSlide(sliderValue);
                        }
                        invalidate();
                    }
                    handled = true;
                }
            } else {
                if (y >= progressStartY && y <= progressEndY) {
                    if (action == MotionEvent.ACTION_UP) {
                        knobStartY = y;
                        pressed = true;
                    } else if (Math.abs(knobStartY - y) <= AndroidUtilities.dp(10)) {
                        sliderValue = (y - progressStartY) / (progressEndY - progressStartY);
                        if (delegate != null) {
                            delegate.didSlide(sliderValue);
                        }
                        invalidate();
                    }
                    handled = true;
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (knobPressed) {
                if (isPortrait) {
                    sliderValue = ((x + knobStartX) - progressStartX) / (progressEndX - progressStartX);
                } else {
                    sliderValue = ((y + knobStartY) - progressStartY) / (progressEndY - progressStartY);
                }
                if (sliderValue < 0) {
                    sliderValue = 0;
                } else if (sliderValue > 1.0f) {
                    sliderValue = 1.0f;
                }
                if (delegate != null) {
                    delegate.didSlide(sliderValue);
                }
                invalidate();
            }
        }
        if (action == MotionEvent.ACTION_UP) {
            pressed = false;
            knobPressed = false;
            invalidate();
        }
        return handled || pressed || knobPressed || super.onTouchEvent(event);
    }

    public boolean animateToValue(float zoom) {
        if (zoom < 0 || zoom > 1.0f) {
            return false;
        }
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        animatingToZoom = zoom;
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(this, SLIDER_PROPERTY, zoom));
        animatorSet.setDuration(180);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animatorSet = null;
            }
        });
        animatorSet.start();
        return true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int cx = getMeasuredWidth() / 2;
        int cy = getMeasuredHeight() / 2;
        boolean isPortrait = getMeasuredWidth() > getMeasuredHeight();

        if (isPortrait) {
            minusCx = AndroidUtilities.dp(16 + 25);
            minusCy = cy;
            plusCx = getMeasuredWidth() - AndroidUtilities.dp(16 + 25);
            plusCy = cy;

            progressStartX = minusCx + AndroidUtilities.dp(18);
            progressStartY = cy;

            progressEndX = plusCx - AndroidUtilities.dp(18);
            progressEndY = cy;
        } else {
            minusCx = cx;
            minusCy = AndroidUtilities.dp(16 + 25);
            plusCx = cx;
            plusCy = getMeasuredHeight() - AndroidUtilities.dp(16 + 25);

            progressStartX = cx;
            progressStartY = minusCy + AndroidUtilities.dp(18);

            progressEndX = cx;
            progressEndY = plusCy - AndroidUtilities.dp(18);
        }

        if (mode == SLIDER_MODE_ZOOM) {
            minusDrawable.setBounds(minusCx - AndroidUtilities.dp(7), minusCy - AndroidUtilities.dp(7), minusCx + AndroidUtilities.dp(7), minusCy + AndroidUtilities.dp(7));
            minusDrawable.draw(canvas);
            plusDrawable.setBounds(plusCx - AndroidUtilities.dp(7), plusCy - AndroidUtilities.dp(7), plusCx + AndroidUtilities.dp(7), plusCy + AndroidUtilities.dp(7));
            plusDrawable.draw(canvas);
        } else if (mode == SLIDER_MODE_EV) {
            //minusDrawable;
            minusDrawable.setBounds(minusCx - AndroidUtilities.dp(7), minusCy - AndroidUtilities.dp(7), minusCx + AndroidUtilities.dp(7), minusCy + AndroidUtilities.dp(7));
            minusDrawable.draw(canvas);
            plusDrawable.setBounds(plusCx - AndroidUtilities.dp(8), plusCy - AndroidUtilities.dp(8), plusCx + AndroidUtilities.dp(8), plusCy + AndroidUtilities.dp(8));
            plusDrawable.draw(canvas);
        }


        int totalX = progressEndX - progressStartX;
        int totalY = progressEndY - progressStartY;
        int knobX = (int) (progressStartX + totalX * sliderValue);
        int knobY = (int) (progressStartY + totalY * sliderValue);

        if (isPortrait) {
            progressDrawable.setBounds(progressStartX, progressStartY - AndroidUtilities.dp(3), progressEndX, progressStartY + AndroidUtilities.dp(3));
            filledProgressDrawable.setBounds(progressStartX, progressStartY - AndroidUtilities.dp(3), knobX, progressStartY + AndroidUtilities.dp(3));
        } else {
            progressDrawable.setBounds(progressStartY, 0, progressEndY, AndroidUtilities.dp(6));
            filledProgressDrawable.setBounds(progressStartY, 0, knobY, AndroidUtilities.dp(6));
            canvas.save();
            canvas.rotate(90);
            canvas.translate(0, -progressStartX - AndroidUtilities.dp(3));
        }
        progressDrawable.draw(canvas);
        filledProgressDrawable.draw(canvas);
        if (!isPortrait) {
            canvas.restore();
        }

        Drawable drawable = knobPressed ? pressedKnobDrawable : knobDrawable;
        int size = drawable.getIntrinsicWidth();
        drawable.setBounds(knobX - size / 2, knobY - size / 2, knobX + size / 2, knobY + size / 2);
        drawable.draw(canvas);
    }

    public void invertColors(float invert) {
        minusDrawable.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, invert), PorterDuff.Mode.MULTIPLY));
        plusDrawable.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, invert), PorterDuff.Mode.MULTIPLY));
        progressDrawable.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, invert), PorterDuff.Mode.MULTIPLY));
        filledProgressDrawable.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, invert), PorterDuff.Mode.MULTIPLY));
        knobDrawable.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, invert), PorterDuff.Mode.MULTIPLY));
        pressedKnobDrawable.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, invert), PorterDuff.Mode.MULTIPLY));

        invalidate();
    }
}
