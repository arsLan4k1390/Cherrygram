/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.camera;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.SurfaceTexture;
import android.util.Range;
import android.view.Surface;
import android.view.View;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.Components.InstantCameraView;
import org.telegram.ui.Components.RLottieDrawable;

import uz.unnarsx.cherrygram.chats.WindowBlurHelper;
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;

public class VideoMessagesHelper {

    public CameraXController cameraXController;
    private CameraXController.CameraLifecycle camLifecycle = new CameraXController.CameraLifecycle();

    private WindowBlurHelper windowBlurHelper = new WindowBlurHelper();

    public void createCameraX(InstantCameraView instantCameraView, final SurfaceTexture surfaceTexture) {
        AndroidUtilities.runOnUIThread(() -> {
            if (instantCameraView.cameraThread == null) {
                return;
            }

            if (instantCameraView.zoomControlView != null) instantCameraView.zoomControlView.setSliderValue(getZoomForSlider(instantCameraView), false);
            if (instantCameraView.evControlView != null) instantCameraView.evControlView.setValue(0.5f);
            if (instantCameraView.flashViews != null) instantCameraView.flashViews.setForCameraX(true);
            windowBlurHelper.hideStatusBar(instantCameraView.delegate.getParentActivity().getWindow(), true);

            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("InstantCamera create camera session");
            }

            surfaceTexture.setDefaultBufferSize(instantCameraView.previewSize[0].getWidth(), instantCameraView.previewSize[0].getHeight());
            MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(instantCameraView.previewSize[0].getWidth(), instantCameraView.previewSize[0].getHeight());
            Preview.SurfaceProvider surfaceProvider = request -> {
                Surface surface = new Surface(surfaceTexture);
                request.provideSurface(surface, ContextCompat.getMainExecutor(instantCameraView.getContext()), result -> {});
            };
            updateCameraXFlash(instantCameraView);
            cameraXController = new CameraXController(camLifecycle, factory, surfaceProvider);
            cameraXController.setStableFPSPreviewOnly(true);
            cameraXController.initCamera(instantCameraView.getContext(), instantCameraView.isFrontface, ()-> {
                if (instantCameraView.cameraThread != null) {
                    instantCameraView.cameraThread.setOrientation();
                }
            });
            camLifecycle.start();
        });
    }

    public void switchCameraX(InstantCameraView instantCameraView) {
        instantCameraView.saveLastCameraBitmap();
        if (instantCameraView.cameraZoom > 0) {
            instantCameraView.cameraZoom = 0;
        }
        updateCameraXFlash(instantCameraView);

        if (instantCameraView.zoomControlView != null) instantCameraView.zoomControlView.setSliderValue(getZoomForSlider(instantCameraView), true);
        if (instantCameraView.evControlView != null && instantCameraView.evControlView.getTag() != null) {
            instantCameraView.evControlView.setValue(0.5f);
            instantCameraView.evControlView.setTag(null);
        }
        if (instantCameraView.lastBitmap != null) {
            instantCameraView.needDrawFlickerStub = false;
            instantCameraView.textureOverlayView.setImageBitmap(instantCameraView.lastBitmap);
            instantCameraView.textureOverlayView.setAlpha(1f);
        }
        instantCameraView.isFrontface = !instantCameraView.isFrontface;
        instantCameraView.cameraReady = false;
        instantCameraView.cameraThread.reinitForNewCamera();
    }

    public void destroyCameraX(InstantCameraView instantCameraView) {
        try {
            toggleTorch(instantCameraView);

            cameraXController.stopVideoRecording(true);
            cameraXController.closeCamera();
            windowBlurHelper.hideStatusBar(instantCameraView.delegate.getParentActivity().getWindow(), false);
        }  catch (Exception ignored) {}
    }

    public void toggleTorch(InstantCameraView instantCameraView) {
        if (instantCameraView.flashing) {
            if (instantCameraView.isFrontface) {
                setMaxBrightness(instantCameraView);
            } else {
                if (!instantCameraView.cameraReady || !cameraXController.isInitied() || instantCameraView.cameraThread == null){
                    return;
                }
                CameraXController.setTorchEnabled(true);
            }
        } else {
            if (instantCameraView.isFrontface) {
                setOldBrightness(instantCameraView);
            } else {
                if (!instantCameraView.cameraReady || !cameraXController.isInitied() || instantCameraView.cameraThread == null){
                    return;
                }
                CameraXController.setTorchEnabled(false);
            }
        }
    }

    public void setMaxBrightness(InstantCameraView instantCameraView) {
        instantCameraView.flashViews.flashIn(null);
        instantCameraView.zoomControlView.invertColors(1F);
    }

    public void setOldBrightness(InstantCameraView instantCameraView) {
        instantCameraView.flashViews.flashOut();
        instantCameraView.zoomControlView.invertColors(0F);
    }

    private Boolean wasFlashing;
    public void updateCameraXFlash(InstantCameraView instantCameraView) {
        toggleTorch(instantCameraView);

        if (instantCameraView.flashButton == null || (wasFlashing != null && wasFlashing == instantCameraView.flashing)) return;

        instantCameraView.flashButton.setContentDescription(getString(
                instantCameraView.flashing ? R.string.AccDescrCameraFlashOff : R.string.AccDescrCameraFlashOn
        ));

        RLottieDrawable drawable = instantCameraView.flashing ? instantCameraView.flashOffDrawable : instantCameraView.flashOnDrawable;
        if (drawable == null) {
            drawable = new RLottieDrawable(
                    instantCameraView.flashing ? R.raw.roundcamera_flash_off : R.raw.roundcamera_flash_on,
                    instantCameraView.flashing ? "roundcamera_flash_off" : "roundcamera_flash_on",
                    dp(28), dp(28)
            );
            drawable.setCallback(instantCameraView.flashButton);
        }
        instantCameraView.flashButton.setImageDrawable(drawable);
        drawable.setCurrentFrame(wasFlashing == null ? drawable.getFramesCount() - 1 : 0);
        if (wasFlashing != null) drawable.start();

        wasFlashing = instantCameraView.flashing;
    }

    public float getZoomForSlider(InstantCameraView instantCameraView) {
        float value = 0;
        if (
                !instantCameraView.isFrontface
                && !CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam()
                && cameraXController != null && !cameraXController.isAvailableWideMode() /* Wide camera check to prevent wrong slider value on non-supported devices */
        ) {
            value = 0.5f;
        }
        return value;
    }

    public void setZoomForSlider(float zoom) {
        if (cameraXController != null) {
            cameraXController.setZoom(zoom);
        }
    }

    public void showExposureControls(InstantCameraView instantCameraView, boolean show) {
        if (instantCameraView.evControlView == null) {
            return;
        }
        if (instantCameraView.evControlView.getTag() != null && show || instantCameraView.evControlView.getTag() == null && !show) {
            if (show) {
                if (instantCameraView.evControlHideRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(instantCameraView.evControlHideRunnable);
                }
                AndroidUtilities.runOnUIThread(instantCameraView.evControlHideRunnable = () -> {
                    showExposureControls(instantCameraView, false);
                    instantCameraView.evControlHideRunnable = null;
                    instantCameraView.evControlView.setVisibility(View.INVISIBLE);
                }, 3000);
            }
            return;
        }
        if (instantCameraView.evControlAnimation != null) {
            instantCameraView.evControlAnimation.cancel();
        }
        instantCameraView.evControlView.setTag(show ? 1 : null);
        instantCameraView.evControlAnimation = new AnimatorSet();
        instantCameraView.evControlAnimation.setDuration(500);
        instantCameraView.evControlAnimation.playTogether(ObjectAnimator.ofFloat(instantCameraView.evControlView, View.ALPHA, show ? 1.0f : 0.0f));
        instantCameraView.evControlAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                instantCameraView.evControlAnimation = null;
            }
        });
        instantCameraView.evControlAnimation.start();
        if (show) {
            AndroidUtilities.runOnUIThread(instantCameraView.evControlHideRunnable = () -> {
                showExposureControls(instantCameraView, false);
                instantCameraView.evControlHideRunnable = null;
                instantCameraView.evControlView.setVisibility(View.INVISIBLE);
            }, 3000);
        }
    }

    public int getSliderW() {
        int dpi = AndroidUtilities.densityDpi;
        return switch (AndroidUtilities.displaySize.x) {
            case 1440 -> dpi > 560 ? 85 : 105;
            case 1080 -> dpi > 420 ? 115 : 140;
            case 720 -> dpi > 280 ? 175 : 210;
            default -> dpi > 420 ? 120 : 145;
        };
    }

    public int getSliderH() {
        return switch (AndroidUtilities.displaySize.x) {
            case 1440 -> 10;
            case 1080 -> 20;
            case 720 -> 35;
            default -> 25;
        };
    }

    public int getSliderBM() {
        return switch (AndroidUtilities.displaySize.x) {
            case 1440 -> 20;
            case 1080 -> 23;
            case 720 -> 32;
            default -> 25;
        };
    }

    public static Range<Integer> getCameraXFpsRange() {
        return switch (CherrygramCameraConfig.INSTANCE.getCameraXFpsRange()) {
            case CherrygramCameraConfig.CameraXFpsRange25to30 -> new Range<>(25, 30);
            case CherrygramCameraConfig.CameraXFpsRange30to60 -> new Range<>(30, 60);
            case CherrygramCameraConfig.CameraXFpsRange60to60 -> new Range<>(60, 60);
            default -> new Range<>(30, 30); // CherrygramCameraConfig.CameraXFpsRange30to30
        };
    }

    public static int getCameraXAspectRatio() {
        return switch (CherrygramCameraConfig.INSTANCE.getCameraAspectRatio()) {
            case CherrygramCameraConfig.Camera4to3 -> AspectRatio.RATIO_4_3;
            case CherrygramCameraConfig.Camera16to9 -> AspectRatio.RATIO_16_9;
            default -> AspectRatio.RATIO_DEFAULT;
        };
    }

}
