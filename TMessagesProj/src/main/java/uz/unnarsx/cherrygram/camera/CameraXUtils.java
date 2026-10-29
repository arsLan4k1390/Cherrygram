/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.camera;

import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;

public class CameraXUtils {

    public static boolean isCameraXSupported() {
        return false;
    }

    public static boolean isCurrentCameraCameraX() {
        return false;
    }

    public static boolean isCurrentCameraCamera2() {
        return isCameraXSupported() && CherrygramCameraConfig.INSTANCE.getCameraType() == CherrygramCameraConfig.CAMERA_2;
    }

    public static boolean isCurrentCameraNotCameraX() {
        return true;
    }

    public static void loadCameraXSizes() {

    }


}