/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.camera.roundvideo;

import android.graphics.SurfaceTexture;
import android.util.Range;

import org.telegram.ui.Components.InstantCameraView;
import org.telegram.ui.Components.InstantCameraViewBase;

import uz.unnarsx.cherrygram.camera.CameraXController;
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;

public class VideoMessagesHelper {

    public CameraXController cameraXController;
    private final CameraXController.CameraLifecycle camLifecycle = new CameraXController.CameraLifecycle();

    public void createSingleCameraX(InstantCameraView instantCameraView, final SurfaceTexture surfaceTexture) {

    }

    public void createDualCameraX(InstantCameraView instantCameraView, final SurfaceTexture[] surfaceTextures) {

    }

    public void prepareCameraSwitch(InstantCameraViewBase view) {

    }

    public void switchCameraX(InstantCameraView instantCameraView) {

    }

    public void destroyCameraX(InstantCameraView instantCameraView) {

    }

    @Deprecated
    public void setZoom(float zoom) {

    }

    public boolean createFlashConfigurator(InstantCameraViewBase instantCameraView) {
        return false;
    }

    public void checkFlash(InstantCameraViewBase instantCameraView) {

    }

    public boolean toggleTorch(InstantCameraView instantCameraView) {
        return false;
    }

    public void updateCameraXFlash(InstantCameraView instantCameraView) {

    }

    public void showExposureControls(InstantCameraViewBase instantCameraView, boolean show) {

    }

    public static Range<Integer> getCameraXFpsRange() {
        return isCamera60Fps() && CherrygramCameraConfig.INSTANCE.getUseNewEncoder() ? new Range<>(60, 60) : new Range<>(30, 30);
    }

    public static boolean isCamera60Fps() {
        return CherrygramCameraConfig.INSTANCE.getCameraXFpsRange() == CherrygramCameraConfig.CameraXFpsRange60;
    }

}