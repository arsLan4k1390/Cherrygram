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
import android.view.WindowManager;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.camera.Size;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CameraXController {

    private boolean isFrontface;
    private final CameraLifecycle lifecycle;
    private ProcessCameraProvider provider;
    private final MeteringPointFactory meteringPointFactory;
    private final Preview.SurfaceProvider surfaceProvider;
    public static final int CAMERA_NONE = 0;
    public static final int CAMERA_NIGHT = 1;
    public static final int CAMERA_HDR = 2;
    public static final int CAMERA_AUTO = 3;
    public static final int CAMERA_WIDE = 4;
    public static final int CAMERA_ASPECT_RATIO_SELECTOR = 5;
    private int selectedEffect = CAMERA_NONE;

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

        @NonNull
        public Lifecycle getLifecycle() {
            return lifecycleRegistry;
        }

    }

    public CameraXController(CameraLifecycle lifecycle, MeteringPointFactory factory, Preview.SurfaceProvider surfaceProvider) {
        this.lifecycle = lifecycle;
        this.meteringPointFactory = factory;
        this.surfaceProvider = surfaceProvider;
    }

    public boolean isInitiated() {
        return false;
    }

    public void setFrontFace(boolean isFrontFace) {

    }

    public boolean isFrontface() {
        return false;
    }

    public void initCamera(Context context, boolean isInitialFrontface, Runnable onPreInit) {

    }

    public void setCameraEffect(@EffectFacing int effect) {
        selectedEffect = effect;
        bindUseCases();
    }

    public int getCameraEffect() {
        return selectedEffect;
    }

    public void switchCamera() {
        isFrontface = !isFrontface;
        bindUseCases();
    }

    public void closeCamera() {
        provider.unbindAll();
        lifecycle.stop();
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

    public static boolean isFlashAvailable() {
        return false;
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

    @SuppressLint({"RestrictedApi", "UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
    public void bindUseCases() {

    }

    public void setZoom(float value) {

    }

    public float resetZoom() {
        return 0f;
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    public boolean isExposureCompensationSupported() {
        return false;
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    public void setExposureCompensation(float value) {

    }

    @SuppressLint({"UnsafeExperimentalUsageError", "RestrictedApi"})
    public void setTargetOrientation(int rotation) {

    }

    @SuppressLint({"UnsafeExperimentalUsageError", "RestrictedApi"})
    public void setWorldCaptureOrientation(int rotation) {

    }

    @SuppressLint({"UnsafeExperimentalUsageError", "RestrictedApi"})
    public void focusToPoint(int x, int y/*, boolean disableAutoCancel*/) {

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
        WindowManager mgr = (WindowManager) ApplicationLoader.applicationContext.getSystemService(Context.WINDOW_SERVICE);
        return mgr.getDefaultDisplay().getRotation();
    }

    @IntDef({CAMERA_NONE, CAMERA_AUTO, CAMERA_HDR, CAMERA_NIGHT})
    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public @interface EffectFacing {
    }

}