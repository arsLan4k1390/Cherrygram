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

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import org.telegram.messenger.camera.Size;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CameraXController implements CherryZoomSliderView.ZoomController {

    public static final int CAMERA_NONE = 0;
    public static final int CAMERA_NIGHT = 1;
    public static final int CAMERA_HDR = 2;
    public static final int CAMERA_AUTO = 3;
    public static final int CAMERA_WIDE = 4;
    public static final int CAMERA_ASPECT_RATIO_SELECTOR = 5;

    public static class CameraLifecycle implements LifecycleOwner {

        private final LifecycleRegistry lifecycleRegistry;

        public CameraLifecycle() {
            lifecycleRegistry = new LifecycleRegistry(this);
            lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
        }

        public void start() {
            try {
                lifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);
            } catch (IllegalStateException ignored) {
            }
        }

        public void stop() {
            try {
                lifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
            } catch (IllegalStateException ignored) {
            }
        }

        public void pause() {

        }

        public boolean isDestroyed() {
            return false;
        }

        @NonNull
        public Lifecycle getLifecycle() {
            return lifecycleRegistry;
        }

    }

    public CameraXController(CameraLifecycle lifecycle, MeteringPointFactory factory, Preview.SurfaceProvider surfaceProvider) {

    }

    public boolean isInitiated() {
        return false;
    }

    public void setFrontFace(boolean isFrontFace) {

    }

    public boolean isFrontface() {
        return false;
    }

    public void setUltraWideStartAllowed(boolean allowed) {

    }

    public void initCamera(Context context, boolean isInitialFrontface, Runnable onPreInit) {

    }

    public void setCameraEffect(@EffectFacing int effect) {

    }

    public int getCameraEffect() {
        return 0;
    }

    public void switchCamera() {

    }

    public void closeCamera() {

    }

    @SuppressLint("RestrictedApi")
    public boolean hasFrontFaceCamera() {
        return false;
    }

    @SuppressLint("RestrictedApi")
    public static boolean hasGoodCamera(Context context) {
        return false;
    }

    public int setNextFlashMode() {
        return 0;
    }

    public int getCurrentFlashMode() {
        return 0;
    }

    public boolean isFlashAvailable() {
        return false;
    }

    public void setTorchEnabled(boolean enabled) {

    }

    public boolean isAvailableHdrMode() {
        return false;
    }

    public boolean isAvailableNightMode() {
        return false;
    }

    public boolean isAvailableWideMode() {
        return false;
    }

    public boolean isAvailableAutoMode() {
        return false;
    }

    public void bindUseCasesKeepingZoom() {

    }

    public void setZoom(float value) {

    }

    public float resetZoom() {
        return 0f;
    }

    public boolean isCameraReady() {
        return false;
    }

    public float getZoomRatio() {
        return 0;
    }

    public float getMinZoomRatio() {
        return 0;
    }

    public float getMaxZoomRatio() {
        return 0;
    }

    public void setZoomRatio(float ratio) {

    }

    @SuppressLint("UnsafeExperimentalUsageError")
    public boolean isExposureCompensationSupported() {
        return false;
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    public void setExposureCompensation(float value) {

    }

    public void setPreviewRotation(int rotation) {

    }

    public void setCaptureRotation(int rotation) {

    }

    @SuppressLint({"UnsafeExperimentalUsageError", "RestrictedApi"})
    public void focusToPoint(int x, int y, boolean disableAutoCancel) {

    }

    @SuppressLint({"UnsafeExperimentalUsageError", "RestrictedApi"})
    public void focusToPoint(float x, float y, boolean disableAutoCancel) {

    }


    @SuppressLint({"RestrictedApi", "MissingPermission"})
    public void recordVideo(final File path, boolean mirror, CameraXView.VideoSavedCallback onStop) {

    }

    @SuppressLint("RestrictedApi")
    public void stopVideoRecording(final boolean abandon) {

    }

    public void takePicture(final File file, Runnable onTake) {

    }

    @SuppressLint("RestrictedApi")
    public Size getPreviewSize() {
        return new Size(0, 0);
    }

    public int getDisplayOrientation() {
        return 0;
    }

    @IntDef({CAMERA_NONE, CAMERA_AUTO, CAMERA_HDR, CAMERA_NIGHT})
    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public @interface EffectFacing {
    }

}