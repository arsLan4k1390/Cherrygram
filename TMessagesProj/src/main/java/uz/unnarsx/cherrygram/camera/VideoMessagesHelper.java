package uz.unnarsx.cherrygram.camera;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

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

import uz.unnarsx.cherrygram.core.CGFeatureHooks;
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;

public class VideoMessagesHelper {

    public CameraXController cameraXController;
    public CameraXController.CameraLifecycle camLifecycle = new CameraXController.CameraLifecycle();

    public void createCameraX(InstantCameraView instantCameraView, final SurfaceTexture surfaceTexture) {
        AndroidUtilities.runOnUIThread(() -> {
            if (instantCameraView.cameraThread == null) {
                return;
            }
            if (instantCameraView.zoomControlView != null) instantCameraView.zoomControlView.setSliderValue(getZoomForSlider(instantCameraView), false);
            if (instantCameraView.evControlView != null) instantCameraView.evControlView.setValue(0.5f);
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
        WindowManager.LayoutParams attributes = ((Activity) instantCameraView.getContext()).getWindow().getAttributes();
        attributes.screenBrightness = 1F; //maxBrightness
        ((Activity) instantCameraView.getContext()).getWindow().setAttributes(attributes);

        CGFeatureHooks.setFlashLight(true);
        instantCameraView.blurBehindDrawable.showFlash(true);
        AndroidUtilities.setLightStatusBar(((Activity) instantCameraView.getContext()).getWindow(), true);
        instantCameraView.flashButton.setInvert(1F);
        instantCameraView.switchCameraButton.setInvert(1F);
        instantCameraView.zoomControlView.invertColors(1F);
    }

    public void setOldBrightness(InstantCameraView instantCameraView) {
        WindowManager.LayoutParams attributes = ((Activity) instantCameraView.getContext()).getWindow().getAttributes();
        attributes.screenBrightness = -1F; //previousBrightness
        ((Activity) instantCameraView.getContext()).getWindow().setAttributes(attributes);

        CGFeatureHooks.setFlashLight(false);
        instantCameraView.blurBehindDrawable.showFlash(false);
        AndroidUtilities.setLightStatusBar(((Activity) instantCameraView.getContext()).getWindow(), false);
        instantCameraView.invalidateBlur();
        instantCameraView.flashButton.setInvert(0F);
        instantCameraView.switchCameraButton.setInvert(0F);
        instantCameraView.zoomControlView.invertColors(0F);
    }

    private Boolean wasFlashing;
    public void updateCameraXFlash(InstantCameraView instantCameraView) {
        toggleTorch(instantCameraView);

        if (instantCameraView.flashButton != null && (wasFlashing == null || wasFlashing != instantCameraView.flashing)) {
            instantCameraView.flashButton.setContentDescription(getString(instantCameraView.flashing ? R.string.AccDescrCameraFlashOff : R.string.AccDescrCameraFlashOn));
            if (!instantCameraView.flashing) {
                if (instantCameraView.flashOnDrawable == null) {
                    instantCameraView.flashOnDrawable = new RLottieDrawable(R.raw.roundcamera_flash_on, "roundcamera_flash_on", dp(28), dp(28));
                    instantCameraView.flashOnDrawable.setCallback(instantCameraView.flashButton);
                }
                instantCameraView.flashButton.setImageDrawable(instantCameraView.flashOnDrawable);
                if (wasFlashing == null) {
                    instantCameraView.flashOnDrawable.setCurrentFrame(instantCameraView.flashOnDrawable.getFramesCount() - 1);
                } else {
                    instantCameraView.flashOnDrawable.setCurrentFrame(0);
                    instantCameraView.flashOnDrawable.start();
                }
            } else {
                if (instantCameraView.flashOffDrawable == null) {
                    instantCameraView.flashOffDrawable = new RLottieDrawable(R.raw.roundcamera_flash_off, "roundcamera_flash_off", dp(28), dp(28));
                    instantCameraView.flashOffDrawable.setCallback(instantCameraView.flashButton);
                }
                instantCameraView.flashButton.setImageDrawable(instantCameraView.flashOffDrawable);
                if (wasFlashing == null) {
                    instantCameraView.flashOffDrawable.setCurrentFrame(instantCameraView.flashOffDrawable.getFramesCount() - 1);
                } else {
                    instantCameraView.flashOffDrawable.setCurrentFrame(0);
                    instantCameraView.flashOffDrawable.start();
                }
            }
            wasFlashing = instantCameraView.flashing;
        }
    }

    public float getZoomForSlider(InstantCameraView instantCameraView) {
        float value = 0;
        if (
                !instantCameraView.isFrontface
                && !CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam()
                && cameraXController != null && !cameraXController.isAvailableWideMode() /* Wide camera check to prevent wrong slider value on non-supported devices*/
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

}
