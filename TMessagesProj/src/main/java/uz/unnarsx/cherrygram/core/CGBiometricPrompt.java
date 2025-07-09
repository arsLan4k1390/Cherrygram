/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.hardware.biometrics.BiometricManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.support.fingerprint.FingerprintManagerCompat;
import org.telegram.ui.LaunchActivity;

import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig;

public class CGBiometricPrompt {

    private static BiometricPrompt.PromptInfo createPromptInfo() {
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder();
        builder.setTitle(getString(R.string.CG_AppName));
        if (!CherrygramPrivacyConfig.INSTANCE.getAllowSystemPasscode()) {
            builder.setNegativeButtonText(getString(R.string.Cancel));
        }
//        builder.setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
        builder.setDeviceCredentialAllowed(CherrygramPrivacyConfig.INSTANCE.getAllowSystemPasscode());
        builder.setConfirmationRequired(false);

        /*return new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.CG_AppName))
                .setConfirmationRequired(false)
                .build();*/
        return builder.build();
    }

    public static void callBiometricPrompt(Activity activity, CGBiometricListener listener) {
        new BiometricPrompt(LaunchActivity.instance, ContextCompat.getMainExecutor(activity), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                listener.onError(errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                listener.onSuccess(result);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                listener.onFailed();
            }
        }).authenticate(createPromptInfo());
    }

    public static void prompt(Activity activity, Runnable successCallback) {
        prompt(activity, successCallback, null);
    }

    public static void prompt(Activity activity, Runnable successCallback, Runnable failCallback) {
        CGBiometricPrompt.callBiometricPrompt(activity, new CGBiometricPrompt.CGBiometricListener() {
            @Override
            public void onError(CharSequence msg) {
                if (failCallback != null) failCallback.run();
                if (CherrygramDebugConfig.INSTANCE.getShowRPCErrors())
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                if (failCallback != null) failCallback.run();
                if (CherrygramDebugConfig.INSTANCE.getShowRPCErrors())
                    Toast.makeText(activity, "Fail", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(BiometricPrompt.AuthenticationResult result) {
                successCallback.run();
                if (CherrygramDebugConfig.INSTANCE.getShowRPCErrors())
                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface CGBiometricListener {
        void onError(CharSequence msg);

        void onFailed();

        void onSuccess(BiometricPrompt.AuthenticationResult result);
    }

    public static boolean hasBiometricEnrolled() {
        if (Build.VERSION.SDK_INT >= 29) {
            BiometricManager biometricManager = ApplicationLoader.applicationContext.getSystemService(BiometricManager.class);
            if (biometricManager == null) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= 30) {
                return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS;
            } else {
                return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            FingerprintManager fingerprintManager = ApplicationLoader.applicationContext.getSystemService(FingerprintManager.class);
            if (fingerprintManager == null) {
                try {
                    FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(ApplicationLoader.applicationContext);
                    return fingerprintManagerCompat.isHardwareDetected() && fingerprintManagerCompat.hasEnrolledFingerprints();
                } catch (Throwable e) {
                    FileLog.e(e);
                    return false;
                }
            }
            return fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
        }
        return false;
    }

}
