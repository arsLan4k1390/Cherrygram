/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.camera;

import static android.hardware.camera2.CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS;
import static android.hardware.camera2.CameraMetadata.LENS_FACING_BACK;

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
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.SharedConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;

public class CameraXUtils {

    private static Map<Quality, Size> qualityToSize;
    private static Exception qualityException;
    private static int cameraResolution = -1;

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
        return selector.filter(provider.getAvailableCameraInfos()).stream()
                .findFirst()
                .map(camInfo -> QualitySelector.getSupportedQualities(camInfo).stream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                quality -> Optional.ofNullable(QualitySelector.getResolution(camInfo, quality))
                                        .orElse(new Size(0, 0))
                        ))
                ).orElseGet(HashMap::new);
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
                provider.unbindAll();
            } catch (Exception e) {
                qualityException = e;
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public static void loadSuggestedResolution() {
        int suggestedRes = getSuggestedResolution(false);

        int minResolution = getAvailableVideoSizes().values().stream().mapToInt(Size::getHeight).min().orElse(0);
        int maxResolution = getAvailableVideoSizes().values().stream().mapToInt(Size::getHeight).max().orElse(0);

        getAvailableVideoSizes().values().stream()
                .mapToInt(Size::getHeight)
                .filter(height -> height <= suggestedRes)
                .max()
                .ifPresent(height -> {
                    cameraResolution = height;
                    if (CherrygramCameraConfig.INSTANCE.getCameraResolution() == -1 || CherrygramCameraConfig.INSTANCE.getCameraResolution() > maxResolution || CherrygramCameraConfig.INSTANCE.getCameraResolution() < minResolution) {
                        CherrygramCameraConfig.INSTANCE.setCameraResolution(
                                Math.min(Math.max(height, minResolution), maxResolution)
                        );
                    }
                });
    }

    public static Size getPreviewBestSize() {
        int suggestedRes = getSuggestedResolution(true);
        return getAvailableVideoSizes().values().stream()
                .filter(size -> size.getHeight() <= cameraResolution && size.getHeight() <= suggestedRes)
                .max(Comparator.comparingInt(Size::getHeight))
                .orElse(new Size(0, 0));
    }

    public static Quality getVideoQuality() {
        return getAvailableVideoSizes().entrySet().stream()
                .filter(entry -> entry.getValue().getHeight() == cameraResolution)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(Quality.HIGHEST);
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
                CameraCharacteristics cameraCharacteristics = camera2Info.getCameraCharacteristicsMap()
                        .get(camera2Info.getCameraId());

                if (cameraCharacteristics == null) continue;

                Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing == null || lensFacing != LENS_FACING_BACK) continue; // Потому что некоторые девайсы не отдают lensFacing :)

                availableBackCamera++;
                ZoomState zoomState = cameraInfo.getZoomState().getValue();

                if (zoomState != null && zoomState.getMinZoomRatio() < 1.0F && zoomState.getMinZoomRatio() > 0) {
                    foundWideAngleOnPrimaryCamera = true;
                }

                float[] listLensAngle = cameraCharacteristics.get(LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
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
