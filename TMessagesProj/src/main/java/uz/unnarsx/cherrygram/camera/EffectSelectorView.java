package uz.unnarsx.cherrygram.camera;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.WindowInsets;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectSelectorView extends LinearLayout {

    private ButtonEffect oldSelection;
    private boolean isEnabledButtons = true;

    public EffectSelectorView(Context context) {
        super(context);
        setPadding(0, getSpaceNotch(), 0, 0);
        setGravity(Gravity.CENTER);
        int colorBackground = Color.BLACK;
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        AndroidUtilities.getTransparentColor(colorBackground, 0.4f),
                        AndroidUtilities.getTransparentColor(colorBackground, 0)
                });
        setBackground(gd);
    }

    public void resetSelectedEffect() {
        for (int i = 0; i < getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) getChildAt(i);
            ButtonEffect buttonEffect = (ButtonEffect) linearLayout.getChildAt(0);
            buttonEffect.toggleButton(buttonEffect.cameraType == CameraXController.CAMERA_NONE, false);
            if (buttonEffect.cameraType == CameraXController.CAMERA_NONE) {
                oldSelection = buttonEffect;
            }
        }
    }

    private static final String[] GOOGLE_FUCKUPS = {"raven", "bluejay", "panther", "cheetah", "lynx"};

    public void loadEffects(CameraXView cameraXView) {
        if (getChildCount() == 0) {
            boolean fuckup = Arrays.asList(GOOGLE_FUCKUPS).contains(Build.DEVICE);
            ArrayList<Integer> list_effect = new ArrayList<>();
            if (!fuckup && cameraXView.isNightModeSupported()) {
                list_effect.add(CameraXController.CAMERA_NIGHT);
            }
            if (!fuckup && cameraXView.isAutoModeSupported()) {
                list_effect.add(CameraXController.CAMERA_AUTO);
            }
            list_effect.add(CameraXController.CAMERA_NONE);
            if (cameraXView.isWideModeSupported()) {
                list_effect.add(CameraXController.CAMERA_WIDE);
            }
            if (!fuckup && cameraXView.isHdrModeSupported()) {
                list_effect.add(CameraXController.CAMERA_HDR);
            }
            if (list_effect.size() == 1) {
                return;
            }
            for (int i = 0; i < list_effect.size(); i++) {
                int effect = list_effect.get(i);
                LinearLayout linearLayout = new LinearLayout(getContext());
                linearLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
                linearLayout.setGravity(Gravity.CENTER);
                ButtonEffect buttonEffect = new ButtonEffect(getContext(), effect) {
                    @Override
                    protected void onItemClick(ButtonEffect buttonEffect, int camera_type) {
                        if (isEnabledButtons) {
                            super.onItemClick(buttonEffect, camera_type);
                            if (oldSelection != null) {
                                oldSelection.toggleButton(false, true);
                            }
                            buttonEffect.toggleButton(true, true);
                            oldSelection = buttonEffect;
                            onEffectSelected(camera_type);
                        }
                    }
                };
                buttonEffect.toggleButton(effect == CameraXController.CAMERA_NONE, false);
                if (effect == CameraXController.CAMERA_NONE) {
                    oldSelection = buttonEffect;
                }
                linearLayout.addView(buttonEffect, LayoutHelper.createLinear(50, 50));
                addView(linearLayout);
            }
        }
    }

    protected void onEffectSelected(int cameraEffect) {
    }

    public void setEnabledButtons(boolean clickable) {
        isEnabledButtons = clickable;
    }

    public void setScreenOrientation(int screenOrientation) {
        setOrientation(screenOrientation);
        int orientation = screenOrientation == VERTICAL ? -180 : 0;
        for (int i = 0; i < getChildCount(); i++) {
            ((LinearLayout) getChildAt(i)).getChildAt(0).setRotationX(orientation);
        }
        int colorBackground = Color.BLACK;
        if (screenOrientation == HORIZONTAL) {
            setPadding(0, getSpaceNotch(), 0, 0);
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            AndroidUtilities.getTransparentColor(colorBackground, 0.4f),
                            AndroidUtilities.getTransparentColor(colorBackground, 0)
                    });
            setBackground(gd);
        } else {
            setPadding(getSpaceNotch(), 0, 0, 0);
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{
                            AndroidUtilities.getTransparentColor(colorBackground, 0.4f),
                            AndroidUtilities.getTransparentColor(colorBackground, 0)
                    });
            setBackground(gd);
        }
    }

    public int getSpaceNotch() {
        int notchSize = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets windowInsets = AndroidUtilities.findActivity(getContext()).getWindow().getDecorView().getRootWindowInsets();
            if (windowInsets != null) {
                DisplayCutout cutout = windowInsets.getDisplayCutout();
                if (cutout != null) {
                    List<Rect> boundRect = cutout.getBoundingRects();
                    if (boundRect.size() > 0) {
                        if (getOrientation() == HORIZONTAL) {
                            notchSize = boundRect.get(0).bottom;
                        } else {
                            notchSize = boundRect.get(0).right;
                            notchSize = notchSize > 500 ? 0 : notchSize;
                        }
                    }
                }
            }
        }
        return notchSize;
    }
}
