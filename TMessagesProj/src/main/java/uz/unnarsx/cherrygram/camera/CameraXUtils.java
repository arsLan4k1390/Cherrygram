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
import android.hardware.camera2.CameraCharacteristics;
import android.util.Size;

import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;

import com.google.common.util.concurrent.ListenableFuture;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.SharedConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;

public class CameraXUtils {

    private static Map<Quality, Size> qualityToSize;
    private static Exception qualityException;
    private static int cameraResolution = -1;

    private static final ExecutorService CAMERA_EXECUTOR = Executors.newSingleThreadExecutor();

    public static boolean isCameraXSupported() {
        return SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE;
    }

    public static boolean isCurrentCameraCameraX() {
        return isCameraXSupported() && CherrygramCameraConfig.INSTANCE.getCameraType() == CherrygramCameraConfig.CAMERA_X;
    }

    public static boolean isCurrentCameraNotCameraX() {
        return !isCurrentCameraCameraX();
    }

    /*public static int getDefaultCamera() { // Used for Config
        return isCameraXSupported() ? CherrygramCameraConfig.CAMERA_X : CherrygramCameraConfig.TELEGRAM_CAMERA;
    }*/

    public static boolean isWideAngleAvailable(ProcessCameraProvider provider) {
        return getWideCameraId(provider) != null;
    }

    @SuppressLint("UnsafeOptInUsageError")
    public static CameraSelector getDefaultWideAngleCamera(ProcessCameraProvider provider) {
        String wideCamera = getWideCameraId(provider);
        if (wideCamera != null) {
            return new CameraSelector.Builder().addCameraFilter(cameraInfo -> {
                List<CameraInfo> cameraFiltered = new ArrayList<>();
                for (int i = 0; i < cameraInfo.size(); i++) {
                    CameraInfo c = cameraInfo.get(i);
                    String id = Camera2CameraInfo.from(c).getCameraId();
                    if (id.equals(wideCamera)) {
                        cameraFiltered.add(c);
                    }
                }
                return cameraFiltered;
            }).build();
        }
        throw new IllegalArgumentException("This device doesn't support wide camera! "
                + "isWideAngleAvailable should be checked first before calling "
                + "getDefaultWideAngleCamera.");
    }

    public static Map<Quality, Size> getAvailableVideoSizes() {
        if (qualityException != null) {
            throw new IllegalStateException("CameraX sizes failed to load!", qualityException);
        }
        return qualityToSize != null ? qualityToSize : new HashMap<>();
    }

    private static Map<Quality, Size> fetchAvailableVideoSizes(CameraSelector selector, ProcessCameraProvider provider) {
        Map<Quality, Size> map = new HashMap<>();
        try {
            List<CameraInfo> infos = selector.filter(provider.getAvailableCameraInfos());

            if (!infos.isEmpty()) {
                CameraInfo camInfo = infos.get(0);

                List<Quality> qualities = QualitySelector.getSupportedQualities(camInfo);

                for (Quality quality : qualities) {
                    Size size = QualitySelector.getResolution(camInfo, quality);
                    map.put(quality, size != null ? size : new Size(0, 0));
                }
            }

        } catch (Exception e) {
            qualityException = e;
        }
        return map;
    }

    public static void loadCameraXSizes() {
        if (qualityToSize != null || qualityException != null) return;

        Context context = ApplicationLoader.applicationContext;
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(context);
        providerFuture.addListener(() -> {
            try {
                ProcessCameraProvider provider = providerFuture.get();
                qualityToSize = fetchAvailableVideoSizes(new CameraSelector.Builder().build(), provider);
                loadSuggestedResolution();
                AndroidUtilities.runOnUIThread(provider::unbindAll);
            } catch (Exception e) {
                qualityException = e;
            }
        }, CAMERA_EXECUTOR);
    }

    public static void loadSuggestedResolution() {
        Map<Quality, Size> sizes = getAvailableVideoSizes();

        if (sizes.isEmpty()) return;

        int suggestedRes = getSuggestedResolution(false);

        int minResolution = Integer.MAX_VALUE;
        int maxResolution = 0;
        int bestMatch = 0;

        for (Size size : sizes.values()) {
            int h = size.getHeight();

            if (h < minResolution) minResolution = h;
            if (h > maxResolution) maxResolution = h;

            if (h <= suggestedRes && h > bestMatch) {
                bestMatch = h;
            }
        }

        if (bestMatch == 0) return;

        cameraResolution = bestMatch;

        int current = CherrygramCameraConfig.INSTANCE.getCameraResolution();

        if (current == -1 || current > maxResolution || current < minResolution) {
            int clamped = Math.min(Math.max(bestMatch, minResolution), maxResolution);
            CherrygramCameraConfig.INSTANCE.setCameraResolution(clamped);
        }
    }

    public static Quality getVideoQuality() {
        Map<Quality, Size> sizes = getAvailableVideoSizes();

        for (Map.Entry<Quality, Size> entry : sizes.entrySet()) {
            if (entry.getValue().getHeight() == cameraResolution) {
                return entry.getKey();
            }
        }

        return Quality.HIGHEST;
    }

    private static int getSuggestedResolution(boolean isPreview) {
        int perfClass = SharedConfig.getDevicePerformanceClass();
        if (perfClass == SharedConfig.PERFORMANCE_CLASS_LOW) return 720;
        if (perfClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE) return 1080;
        return isPreview ? 1080 : 2160;
    }

    @SuppressLint({"RestrictedApi", "UnsafeOptInUsageError"})
    public static String getWideCameraId(ProcessCameraProvider provider) {
        float lowestAngledCamera = Float.MAX_VALUE;
        String cameraId = null;
        int availableBackCamera = 0;
        boolean foundWideAngleOnPrimaryCamera = false;

        for (CameraInfo cameraInfo : provider.getAvailableCameraInfos()) {
            try {
                Camera2CameraInfo camera2Info = Camera2CameraInfo.from(cameraInfo);
                Integer lensFacing = camera2Info.getCameraCharacteristic(CameraCharacteristics.LENS_FACING);

                if (lensFacing == null || lensFacing != CameraCharacteristics.LENS_FACING_BACK) continue;

                availableBackCamera++;
                ZoomState zoomState = cameraInfo.getZoomState().getValue();

                if (zoomState != null && zoomState.getMinZoomRatio() < 1.0F && zoomState.getMinZoomRatio() > 0) {
                    foundWideAngleOnPrimaryCamera = true;
                }

                float[] listLensAngle = camera2Info.getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                if (listLensAngle != null && listLensAngle.length > 0 && listLensAngle[0] < 3.0f && listLensAngle[0] < lowestAngledCamera) {
                    lowestAngledCamera = listLensAngle[0];
                    cameraId = camera2Info.getCameraId();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        return availableBackCamera >= 2 && !foundWideAngleOnPrimaryCamera ? cameraId : null;
    }

}
