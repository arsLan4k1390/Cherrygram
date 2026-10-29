/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory;
import org.telegram.ui.Components.blur3.drawable.color.BlurredBackgroundColorProvider;

@SuppressLint("ViewConstructor")
public class CherryZoomSliderView extends View {

    public interface OnPresetSelectedListener {

    }

    public interface ZoomController {
        boolean isCameraReady();

        float getZoomRatio();

        float getMinZoomRatio();

        float getMaxZoomRatio();

        void setZoomRatio(float ratio);

        boolean isFrontface();
    }

    public CherryZoomSliderView(Context context, Theme.ResourcesProvider resourcesProvider, boolean greyColor) {
        super(context);
    }

    public void setLiquidGlassBackground(
            BlurredBackgroundDrawableViewFactory factory,
            BlurredBackgroundColorProvider colorProvider
    ) {

    }

    public void setZoom(float newZoom, boolean notify) {

    }

    public void scaleZoom(float factor) {

    }

    public void collapse() {

    }

    public void bindCamera(ZoomController controller) {

    }

    public void unbindCamera() {

    }

    public void prepareForCameraChange() {

    }

    public void hideImmediately() {

    }

    public void setExpanded(boolean value, boolean animated) {

    }

}