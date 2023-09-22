package uz.unnarsx.cherrygram.camera;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.camera.CameraView;

import java.io.File;

public abstract class BaseCameraView extends FrameLayout {
    public BaseCameraView(@NonNull Context context) {
        super(context);
    }

    public BaseCameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseCameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract boolean isInited();

    public abstract boolean isFrontface();

    public abstract void switchCamera();

    public abstract void setZoom(float value);

    public float resetZoom() {
        setZoom(0.0f);
        return 0.0f;
    }

    public abstract void focusToPoint(int x, int y);

    public abstract void runHaptic();

    public abstract void setRecordFile(File generateVideoPath);

    public abstract void setFpsLimit(int fpsLimit);

    public abstract void setDelegate(CameraView.CameraViewDelegate cameraViewDelegate);

    public abstract boolean hasFrontFaceCamera();

    public abstract TextureView getTextureView();

    public abstract void initTexture();

    public abstract void showTexture(boolean show, boolean animated);

    public abstract void setThumbDrawable(Drawable drawable);

    public abstract float getTextureHeight(float width, float height);

    public abstract void startSwitchingAnimation();
}
