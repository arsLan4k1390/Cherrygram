/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;

import org.telegram.messenger.camera.CameraSessionWrapper;
import org.telegram.messenger.camera.Size;

public class CameraXDualSource {

    public interface OnConfigured {
        void run(Session session);
    }

    public static boolean isDualSupported(Context context) {
        return false;
    }

    public static Size estimateDisplaySize() {
        return new Size(1920, 1080);
    }

    public CameraXDualSource(Context context, boolean mainFront) {

    }

    public CameraXController getController() {
        return null;
    }

    public void setOnMainCameraChanged(Runnable listener) {

    }

    public void setOnError(Runnable listener) {

    }

    public float getMainExposureValue() {
        return 0;
    }

    public void setMainFront(boolean front) {

    }

    public Session open(boolean front, SurfaceTexture texture, OnConfigured onConfigured) {
        return null;
    }

    public void release() {

    }

    public static class Session extends CameraSessionWrapper {
        private Session(CameraXDualSource source, boolean front, SurfaceTexture texture, OnConfigured onConfigured) {

        }

        public android.util.Size getResolution() {
            return new android.util.Size(0, 0);
        }

        public Size getDisplaySize() {
            return estimateDisplaySize();
        }
    }

}