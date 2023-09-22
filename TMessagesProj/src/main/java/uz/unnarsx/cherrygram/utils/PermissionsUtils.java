package uz.unnarsx.cherrygram.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.BasePermissionsActivity;

public class PermissionsUtils {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isPermissionGranted(String permission) {
        return ApplicationLoader.applicationContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isImagesPermissionGranted() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES) : isStoragePermissionGranted();
    }

    public static boolean isImagesAndVideoPermissionGranted() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? isImagesPermissionGranted() && isVideoPermissionGranted() : isStoragePermissionGranted();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestImagesAndVideoPermission(Activity activity) {
        requestImagesAndVideoPermission(activity, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestImagesPermission(Activity activity) {
        requestImagesPermission(activity, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestImagesPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(activity, requestCode, Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            requestPermissions(activity, requestCode, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestImagesAndVideoPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(activity, requestCode, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            requestPermissions(activity, requestCode, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestAudioPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(activity, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_AUDIO);
        } else {
            requestPermissions(activity, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(activity, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO);
        } else {
            requestPermissions(activity, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestPermissions(Activity activity, int requestCode, String... permissions) {
        if (activity == null) {
            return;
        }
        activity.requestPermissions(permissions, requestCode);
    }

    public static boolean isVideoPermissionGranted() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? isPermissionGranted(Manifest.permission.READ_MEDIA_VIDEO) : isStoragePermissionGranted();
    }

    public static boolean isAudioPermissionGranted() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? isPermissionGranted(Manifest.permission.READ_MEDIA_AUDIO) : isStoragePermissionGranted();
    }

    public static boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                    isImagesPermissionGranted() && isVideoPermissionGranted() && isAudioPermissionGranted()
                    : isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            return true;
        }
    }
}
