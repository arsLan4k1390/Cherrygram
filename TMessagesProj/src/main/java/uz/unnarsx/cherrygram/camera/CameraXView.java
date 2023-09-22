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
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.camera.core.ImageCapture;
import androidx.camera.view.PreviewView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.camera.CameraView;
import org.telegram.messenger.camera.Size;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;

@SuppressLint("ViewConstructor")
public class CameraXView extends BaseCameraView {
    private boolean isStreaming = false;
    private final PreviewView previewView;
    private final ImageView placeholderView;
    private final ImageView blurredStubView;
    private final Paint outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private CameraXController.CameraLifecycle lifecycle;
    private CameraView.CameraViewDelegate delegate;
    private float focusProgress = 1.0f;
    private float innerAlpha;
    private float outerAlpha;
    private final DecelerateInterpolator interpolator = new DecelerateInterpolator();
    private long lastDrawTime;
    private int cx;
    private int cy;
    private CameraXController controller;

    private int displayOrientation = 0;
    private int worldOrientation = 0;
    boolean firstFrameRendered;

    private ValueAnimator flipAnimator;
    private boolean flipHalfReached;
    private Drawable thumbDrawable;

    private long mLastClickTime;

    private final DisplayManager.DisplayListener displayOrientationListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
        }

        @Override
        public void onDisplayRemoved(int displayId) {
        }

        @Override
        public void onDisplayChanged(int displayId) {
            if (getRootView().getDisplay().getDisplayId() == displayId) {
                displayOrientation = getRootView().getDisplay().getRotation();
                if (controller != null) {
                    controller.setTargetOrientation(displayOrientation);
                }
            }
        }
    };

    private final OrientationEventListener worldOrientationListener = new OrientationEventListener(getContext()) {
        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }
            int rotation = 0;
            if (orientation >= 45 && orientation < 135) {
                rotation = Surface.ROTATION_270;
            } else if (orientation >= 135 && orientation < 225) {
                rotation = Surface.ROTATION_180;
            } else if (orientation >= 225 && orientation < 315) {
                rotation = Surface.ROTATION_90;
            }
            worldOrientation = rotation;
            if (controller != null) {
                controller.setWorldCaptureOrientation(rotation);
            }
        }
    };

    public interface VideoSavedCallback {
        void onFinishVideoRecording(String thumbPath, long duration);
    }


    public CameraXView(Context context, boolean frontface, boolean lazy) {
        super(context, null);
        this.frontface = frontface;
        setWillNotDraw(!lazy);
        previewView = new PreviewView(context);
        previewView.setAlpha(0);
        placeholderView = new ImageView(context);
        placeholderView.setVisibility(View.GONE);
        placeholderView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        previewView.setFocusableInTouchMode(false);
        previewView.setBackgroundColor(Color.BLACK);
        if (!lazy) {
            initTexture();
        } else {
            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.cameraInitied));
        }
        addView(placeholderView);
        blurredStubView = new ImageView(context);
        addView(blurredStubView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));
        blurredStubView.setVisibility(View.GONE);
        outerPaint.setColor(0xffffffff);
        outerPaint.setStyle(Paint.Style.STROKE);
        outerPaint.setStrokeWidth(AndroidUtilities.dp(2));
        innerPaint.setColor(0x7fffffff);
        ((DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE)).registerDisplayListener(displayOrientationListener, null);
        worldOrientationListener.enable();
    }

    @Override
    public boolean isInited() {
        return isStreaming;
    }

    @Override
    public boolean isFrontface() {
        return controller.isFrontface();
    }

    //ugly api behaviour after permission check
    public void rebind() {
        if (isStreaming) {
            Bitmap previewBitmap = previewView.getBitmap();
            if (previewBitmap != null) {
                placeholderView.setImageBitmap(previewBitmap);
                placeholderView.setVisibility(View.VISIBLE);
            }
        }
        controller.bindUseCases();
    }

    public void closeCamera() {
        if (controller != null) {
            controller.closeCamera();
        }
    }

    private void observeStream() {
        previewView.getPreviewStreamState().observe(lifecycle, streamState -> {
            if (streamState == PreviewView.StreamState.STREAMING) {
                delegate.onCameraInit();
                isStreaming = true;
                firstFrameRendered = true;
                placeholderView.setImageBitmap(null);
                placeholderView.setVisibility(View.GONE);
                AndroidUtilities.runOnUIThread(this::onFirstFrameRendered);
                if (previewView.getAlpha() == 0) {
                    showTexture(true, true);
                }
            }
            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.cameraInitied));
        });
    }

    private void onFirstFrameRendered() {
        if (blurredStubView.getVisibility() == View.VISIBLE) {
            blurredStubView.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    blurredStubView.setVisibility(View.GONE);
                }
            }).start();
        }
    }

    public void switchCamera() {
        if (isStreaming) {
            Bitmap previewBitmap = previewView.getBitmap();
            if (previewBitmap != null) {
                placeholderView.setImageBitmap(previewBitmap);
                placeholderView.setVisibility(View.VISIBLE);
            }
        }
        controller.switchCamera();
    }

    public void changeEffect(@CameraXController.EffectFacing int effect) {
        if (isStreaming) {
            Bitmap previewBitmap = previewView.getBitmap();
            if (previewBitmap != null) {
                placeholderView.setImageBitmap(previewBitmap);
                placeholderView.setVisibility(View.VISIBLE);
            }
        }
        controller.setCameraEffect(effect);
    }

    public int getCameraEffect() {
        return controller.getCameraEffect();
    }

    public void startChangeEffectAnimation() {
        placeholderView.setVisibility(View.GONE);
        blurredStubView.animate().setListener(null).cancel();
        if (firstFrameRendered) {
            Bitmap bitmap = getTextureView().getBitmap(100, 100);
            if (bitmap != null) {
                Utilities.blurBitmap(bitmap, 3, 1, bitmap.getWidth(), bitmap.getHeight(), bitmap.getRowBytes());
                Drawable drawable = new BitmapDrawable(ApplicationLoader.applicationContext.getResources(), bitmap);
                blurredStubView.setBackground(drawable);
            }
        }
        blurredStubView.setAlpha(1f);
        blurredStubView.setVisibility(View.VISIBLE);
        firstFrameRendered = false;
        invalidate();
    }

    public void startSwitchingAnimation() {
        if (flipAnimator != null) {
            flipAnimator.cancel();
        }
        placeholderView.setVisibility(View.GONE);
        blurredStubView.animate().setListener(null).cancel();
        if (firstFrameRendered) {
            Bitmap bitmap = getTextureView().getBitmap(100, 100);
            if (bitmap != null) {
                Utilities.blurBitmap(bitmap, 3, 1, bitmap.getWidth(), bitmap.getHeight(), bitmap.getRowBytes());
                Drawable drawable = new BitmapDrawable(ApplicationLoader.applicationContext.getResources(), bitmap);
                blurredStubView.setBackground(drawable);
            }
            blurredStubView.setAlpha(0f);
        } else {
            blurredStubView.setAlpha(1f);
        }
        blurredStubView.setVisibility(View.VISIBLE);
        firstFrameRendered = false;
        flipHalfReached = false;

        flipAnimator = ValueAnimator.ofFloat(0, 1f);
        flipAnimator.addUpdateListener(valueAnimator -> {
            float v = (float) valueAnimator.getAnimatedValue();

            float rotation;
            boolean halfReached = false;
            if (v < 0.5f) {
                rotation = v;
            } else {
                halfReached = true;
                rotation = v - 1f;
            }
            rotation *= 180;
            previewView.setRotationY(rotation);
            blurredStubView.setRotationY(rotation);
            if (halfReached && !flipHalfReached) {
                blurredStubView.setAlpha(1f);
                flipHalfReached = true;
            }
        });
        flipAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                flipAnimator = null;
                previewView.setRotationY(0);
                blurredStubView.setRotationY(0);
                if (!flipHalfReached) {
                    blurredStubView.setAlpha(1f);
                    flipHalfReached = true;
                }
                invalidate();
            }
        });
        flipAnimator.setDuration(400);
        flipAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
        flipAnimator.start();
        invalidate();
    }

    @Override
    public void setThumbDrawable(Drawable drawable) {
        if (thumbDrawable != null) {
            thumbDrawable.setCallback(null);
        }
        thumbDrawable = drawable;
        if (thumbDrawable != null) {
            thumbDrawable.setCallback(this);
        }
    }

    private ValueAnimator textureViewAnimator;
    @Override
    public void showTexture(boolean show, boolean animated) {
        if (previewView == null) {
            return;
        }
        if (textureViewAnimator != null) {
            textureViewAnimator.cancel();
            textureViewAnimator = null;
        }
        if (animated) {
            textureViewAnimator = ValueAnimator.ofFloat(previewView.getAlpha(), show ? 1 : 0);
            textureViewAnimator.addUpdateListener(anm -> previewView.setAlpha((float) anm.getAnimatedValue()));
            textureViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    previewView.setAlpha(show ? 1 : 0);
                    textureViewAnimator = null;
                }
            });
            textureViewAnimator.start();
        } else {
            previewView.setAlpha(show ? 1 : 0);
        }
    }

    private boolean textureInited = false;
    private final boolean frontface;
    public void initTexture() {
        if (textureInited) {
            return;
        }
        addView(previewView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));
        lifecycle = new CameraXController.CameraLifecycle();
        controller = new CameraXController(lifecycle, previewView.getMeteringPointFactory(), previewView.getSurfaceProvider());
        controller.setFrontFace(frontface);
        controller.initCamera(getContext(), controller.isFrontface(), this::observeStream);
        textureInited = true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (flipAnimator != null) {
            canvas.drawColor(Color.BLACK);
        }
        super.dispatchDraw(canvas);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean hasFrontFaceCamera() {
        return controller.hasFrontFaceCamera();
    }

    @SuppressLint("RestrictedApi")
    public static boolean hasGoodCamera(Context context) {
        return CameraXController.hasGoodCamera(context);
    }

    @Override
    public TextureView getTextureView() {
        return (TextureView) (previewView.getChildAt(0));
    }

    public Bitmap getBitmap() {
        return previewView.getBitmap();
    }

    public String setNextFlashMode() {
        return mapFlashMode(controller.setNextFlashMode());
    }

    public String getCurrentFlashMode() {
        return mapFlashMode(controller.getCurrentFlashMode());
    }

    public String mapFlashMode(int result) {
        switch (result) {
            case ImageCapture.FLASH_MODE_ON:
                return "on";
            case ImageCapture.FLASH_MODE_OFF:
                return "off";
            case ImageCapture.FLASH_MODE_AUTO:
            default:
                return "auto";
        }
    }

    public boolean isFlashAvailable() {
        return CameraXController.isFlashAvailable();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (lifecycle != null) {
            lifecycle.stop();
        }
        ((DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE)).unregisterDisplayListener(displayOrientationListener);
        worldOrientationListener.disable();
    }

    public void setDelegate(CameraView.CameraViewDelegate cameraViewDelegate) {
        delegate = cameraViewDelegate;
    }

    public void setZoom(float value) {
        controller.setZoom(value);
    }

    @Override
    public float resetZoom() {
        return controller.resetZoom();
    }

    public boolean isHdrModeSupported() {
        return controller.isAvailableHdrMode();
    }

    public boolean isWideModeSupported() {
        return controller.isAvailableWideMode();
    }

    public boolean isNightModeSupported() {
        return controller.isAvailableNightMode();
    }

    public boolean isAutoModeSupported() {
        return controller.isAvailableAutoMode();
    }

    public boolean isExposureCompensationSupported() {
        if (controller == null) {
            return false;
        }
        return controller.isExposureCompensationSupported();
    }

    public void setExposureCompensation(float value) {
        controller.setExposureCompensation(value);
    }

    public void focusToPoint(int x, int y) {
        controller.focusToPoint(x, y);
        focusProgress = 0.0f;
        innerAlpha = 1.0f;
        outerAlpha = 1.0f;
        cx = x;
        cy = y;
        lastDrawTime = System.currentTimeMillis();
        invalidate();
    }

    public Size getPreviewSize() {
        return controller.getPreviewSize();
    }

    public float getTextureHeight(float width, float height) {
        if (controller == null) {
            return height;
        }
        Size previewSize = getPreviewSize();
        int frameWidth, frameHeight;
        if (worldOrientation == 90 || worldOrientation == 270) {
            frameWidth = previewSize.getWidth();
            frameHeight = previewSize.getHeight();
        } else {
            frameWidth = previewSize.getHeight();
            frameHeight = previewSize.getWidth();
        }
        float s = Math.max(width / (float) frameWidth, height / (float) frameHeight);
        return (int) (s * frameHeight);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
        if (focusProgress != 1.0f || innerAlpha != 0.0f || outerAlpha != 0.0f) {
            int baseRad = AndroidUtilities.dp(30);
            long newTime = System.currentTimeMillis();
            long dt = newTime - lastDrawTime;
            if (dt < 0 || dt > 17) {
                dt = 17;
            }
            lastDrawTime = newTime;
            outerPaint.setAlpha((int) (interpolator.getInterpolation(outerAlpha) * 255));
            innerPaint.setAlpha((int) (interpolator.getInterpolation(innerAlpha) * 127));
            float interpolated = interpolator.getInterpolation(focusProgress);
            canvas.drawCircle(cx, cy, baseRad + baseRad * (1.0f - interpolated), outerPaint);
            canvas.drawCircle(cx, cy, baseRad * interpolated, innerPaint);

            if (focusProgress < 1) {
                focusProgress += dt / 200.0f;
                if (focusProgress > 1) {
                    focusProgress = 1;
                }
                invalidate();
            } else if (innerAlpha != 0) {
                innerAlpha -= dt / 150.0f;
                if (innerAlpha < 0) {
                    innerAlpha = 0;
                }
                invalidate();
            } else if (outerAlpha != 0) {
                outerAlpha -= dt / 150.0f;
                if (outerAlpha < 0) {
                    outerAlpha = 0;
                }
                invalidate();
            }
        }
        return result;
    }

    @SuppressLint("RestrictedApi")
    public void recordVideo(final File path, boolean mirrorThumb, VideoSavedCallback onStop) {
        controller.recordVideo(path, mirrorThumb, onStop);
    }


    @SuppressLint("RestrictedApi")
    public void stopVideoRecording(final boolean abandon) {
        controller.stopVideoRecording(abandon);
    }

    public boolean isFlooding() {
        return SystemClock.elapsedRealtime() - mLastClickTime < 1250;
    }

    public void takePicture(final File file, Runnable onTake) {
        mLastClickTime = SystemClock.elapsedRealtime();
        controller.takePicture(file, onTake);
        runHaptic();
    }

    public boolean isSameTakePictureOrientation() {
        return displayOrientation == worldOrientation;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void runHaptic() {
        long[] vibrationWaveFormDurationPattern = {0, 1};
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            final Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(vibrationWaveFormDurationPattern, -1);
            vibrator.cancel();
            vibrator.vibrate(vibrationEffect);
        } else {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (controller != null) {
            int frameWidth, frameHeight;
            Size previewSize = getPreviewSize();
            if (worldOrientation == 90 || worldOrientation == 270) {
                frameWidth = previewSize.getWidth();
                frameHeight = previewSize.getHeight();
            } else {
                frameWidth = previewSize.getHeight();
                frameHeight = previewSize.getWidth();
            }
            float s = Math.max(MeasureSpec.getSize(widthMeasureSpec) / (float) frameWidth, MeasureSpec.getSize(heightMeasureSpec) / (float) frameHeight);
            blurredStubView.getLayoutParams().width = (int) (s * frameWidth);
            blurredStubView.getLayoutParams().height = (int) (s * frameHeight);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setRecordFile(File generateVideoPath) {
    }

    Rect bounds = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        if (thumbDrawable != null) {
            bounds.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
            int W = thumbDrawable.getIntrinsicWidth(), H = thumbDrawable.getIntrinsicHeight();
            float scale = 1f / Math.min(W / (float) Math.max(1, bounds.width()), H / (float) Math.max(1, bounds.height()));
            thumbDrawable.setBounds(
                    (int) (bounds.centerX() - W * scale / 2f),
                    (int) (bounds.centerY() - H * scale / 2f),
                    (int) (bounds.centerX() + W * scale / 2f),
                    (int) (bounds.centerY() + H * scale / 2f)
            );
            thumbDrawable.draw(canvas);
        }
        super.onDraw(canvas);
    }

    @Override
    public void setFpsLimit(int fpsLimit) {
    }
}