package uz.unnarsx.cherrygram.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.SurfaceTexture;
import android.os.CountDownTimer;
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
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.InstantCameraView;

import uz.unnarsx.cherrygram.core.CGFeatureHooks;
import uz.unnarsx.cherrygram.CherrygramConfig;

public class VideoMessagesHelper {

    public void createCameraX(InstantCameraView instantCameraView, final SurfaceTexture surfaceTexture) {
        AndroidUtilities.runOnUIThread(() -> {
            if (instantCameraView.cameraThread == null) {
                return;
            }
            if (instantCameraView.zoomControlView != null) {
                instantCameraView.zoomControlView.setSliderValue(0f, false);
            }
            if (instantCameraView.evControlView != null) {
                instantCameraView.evControlView.setValue(0.5f);
            }
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("InstantCamera create camera session");
            }

            surfaceTexture.setDefaultBufferSize(instantCameraView.previewSize[0].getWidth(), instantCameraView.previewSize[0].getHeight());
            MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(instantCameraView.previewSize[0].getWidth(), instantCameraView.previewSize[0].getHeight());
            Preview.SurfaceProvider surfaceProvider = request -> {
                Surface surface = new Surface(surfaceTexture);
                request.provideSurface(surface, ContextCompat.getMainExecutor(instantCameraView.getContext()), result -> {});
            };
            instantCameraView.cameraXController = new CameraXController(instantCameraView.camLifecycle, factory, surfaceProvider);
            instantCameraView.cameraXController.setStableFPSPreviewOnly(true);
            instantCameraView.cameraXController.initCamera(instantCameraView.getContext(), instantCameraView.isFrontface, ()-> {
                if (instantCameraView.cameraThread != null) {
                    instantCameraView.cameraThread.setOrientation();
                }
            });
            instantCameraView.camLifecycle.start();
        });
    }

    public void switchCameraX(InstantCameraView instantCameraView) {
        instantCameraView.saveLastCameraBitmap();
        if (instantCameraView.cameraZoom > 0) {
            instantCameraView.cameraZoom = 0;
        }
        disableTorch(instantCameraView);
        if (instantCameraView.zoomControlView != null && instantCameraView.zoomControlView.getTag() != null) {
            instantCameraView.zoomControlView.setSliderValue(0f, false);
            instantCameraView.zoomControlView.setTag(null);
        }
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
            disableTorch(instantCameraView);
            if (instantCameraView.zoomControlView != null && instantCameraView.zoomControlView.getTag() != null) instantCameraView.zoomControlView.setTag(null);
            if (instantCameraView.evControlView != null && instantCameraView.evControlView.getTag() != null) instantCameraView.evControlView.setTag(null);

            instantCameraView.cameraXController.stopVideoRecording(true);
            instantCameraView.cameraXController.closeCamera();
        }  catch (Exception ignored) {}
    }

    public void toggleTorch(InstantCameraView instantCameraView) {
        if (instantCameraView.flashlightButton.getTag() == null) {
            instantCameraView.flashlightButton.setTag(1);
            if (instantCameraView.isFrontface) {
                setMaxBrightness(instantCameraView);
            } else {
                CameraXController.setTorchEnabled(true);
            }
        } else {
            instantCameraView.flashlightButton.setBackgroundDrawable(Theme.createCircleDrawable(AndroidUtilities.dp(60), 0x22ffffff));
            instantCameraView.flashlightButton.setTag(null);
            if (instantCameraView.isFrontface) {
                setOldBrightness(instantCameraView);
            } else {
                CameraXController.setTorchEnabled(false);
            }
        }
    }

    public void disableTorch(InstantCameraView instantCameraView) {
        instantCameraView.flashlightButton.setBackgroundDrawable(Theme.createCircleDrawable(AndroidUtilities.dp(60), 0x22ffffff));
        instantCameraView.flashlightButton.setTag(null);
        if (instantCameraView.isFrontface) {
            setOldBrightness(instantCameraView);
        } else {
            CameraXController.setTorchEnabled(false);
        }
    }

    public void setMaxBrightness(InstantCameraView instantCameraView) {
        WindowManager.LayoutParams attributes = ((Activity) instantCameraView.getContext()).getWindow().getAttributes();
        attributes.screenBrightness = 1F; //maxBrightness
        ((Activity) instantCameraView.getContext()).getWindow().setAttributes(attributes);

        CGFeatureHooks.setFlashLight(true);
        AndroidUtilities.setLightStatusBar(((Activity) instantCameraView.getContext()).getWindow(), true);
        instantCameraView.flashlightButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_alwaysBlack), PorterDuff.Mode.MULTIPLY));
        instantCameraView.switchCameraDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_alwaysBlack), PorterDuff.Mode.MULTIPLY));
        instantCameraView.switchCameraButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_alwaysBlack), PorterDuff.Mode.MULTIPLY));
    }

    public void setOldBrightness(InstantCameraView instantCameraView) {
        WindowManager.LayoutParams attributes = ((Activity) instantCameraView.getContext()).getWindow().getAttributes();
        attributes.screenBrightness = -1F; //previousBrightness
        ((Activity) instantCameraView.getContext()).getWindow().setAttributes(attributes);

        new CountDownTimer(300, 100) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                CGFeatureHooks.setFlashLight(false);
                AndroidUtilities.setLightStatusBar(((Activity) instantCameraView.getContext()).getWindow(), false);
                instantCameraView.invalidateBlur();
                instantCameraView.flashlightButton.clearColorFilter();
                instantCameraView.switchCameraDrawable.clearColorFilter();
                instantCameraView.switchCameraButton.clearColorFilter();
            }
        }.start();
    }

    public void showZoomControls(InstantCameraView instantCameraView, boolean show, boolean animated) {
        if (instantCameraView == null && instantCameraView.zoomControlView == null) {
            return;
        }
        if (instantCameraView.zoomControlView.getTag() != null && show || instantCameraView.zoomControlView.getTag() == null && !show) {
            if (show) {
                if (instantCameraView.zoomControlHideRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(instantCameraView.zoomControlHideRunnable);
                }
                AndroidUtilities.runOnUIThread(instantCameraView.zoomControlHideRunnable = () -> {
                    showZoomControls(instantCameraView, false, true);
                    instantCameraView.zoomControlHideRunnable = null;
                    instantCameraView.zoomControlView.setVisibility(View.INVISIBLE);
                }, 3000);
            }
            return;
        }
        if (instantCameraView.zoomControlAnimation != null) {
            instantCameraView.zoomControlAnimation.cancel();
        }
        instantCameraView.zoomControlView.setTag(show ? 1 : null);
        instantCameraView.zoomControlAnimation = new AnimatorSet();
        instantCameraView.zoomControlAnimation.setDuration(180);
        instantCameraView.zoomControlAnimation.playTogether(ObjectAnimator.ofFloat(instantCameraView.zoomControlView, View.ALPHA, show ? 1.0f : 0.0f));
        instantCameraView.zoomControlAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                instantCameraView.zoomControlAnimation = null;
            }
        });
        instantCameraView.zoomControlAnimation.start();
        if (show) {
            AndroidUtilities.runOnUIThread(instantCameraView.zoomControlHideRunnable = () -> {
                showZoomControls(instantCameraView, false, true);
                instantCameraView.zoomControlHideRunnable = null;
                instantCameraView.zoomControlView.setVisibility(View.INVISIBLE);
            }, 3000);
        }
    }

    /*public float getZoomForSlider() {
        float value = 0;
        if (
                !isFrontface
                && !CherrygramConfig.INSTANCE.getStartFromUltraWideCam()
                && ((CameraXUtils.isCameraXSupported() || CherrygramConfig.INSTANCE.getCameraType() == CherrygramConfig.CAMERA_X))
        ) {
            value = 0.5f;
        }
        return value;
    }*/

    public void setZoomForSlider(InstantCameraView instantCameraView, float zoom) {
        if (!CameraXUtils.isCameraXSupported() || CherrygramConfig.INSTANCE.getCameraType() != CherrygramConfig.CAMERA_X) {
            if (instantCameraView.useCamera2) {
                if (instantCameraView.camera2SessionCurrent != null) {
                    instantCameraView.camera2SessionCurrent.setZoom(zoom);
                }
            } else {
                instantCameraView.cameraSession.setZoom(zoom);
            }
        } else {
            if (instantCameraView.cameraXController != null) {
                instantCameraView.cameraXController.setZoom(zoom);
            }
        }
    }

    public void showExposureControls(InstantCameraView instantCameraView, boolean show, boolean animated) {
        if (instantCameraView.evControlView == null) {
            return;
        }
        if (instantCameraView.evControlView.getTag() != null && show || instantCameraView.evControlView.getTag() == null && !show) {
            if (show) {
                if (instantCameraView.evControlHideRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(instantCameraView.evControlHideRunnable);
                }
                AndroidUtilities.runOnUIThread(instantCameraView.evControlHideRunnable = () -> {
                    showExposureControls(instantCameraView, false, true);
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
                showExposureControls(instantCameraView, false, true);
                instantCameraView.evControlHideRunnable = null;
                instantCameraView.evControlView.setVisibility(View.INVISIBLE);
            }, 3000);
        }
    }

}
