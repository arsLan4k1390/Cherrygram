package uz.unnarsx.cherrygram.core;

import android.app.Activity;
import android.hardware.biometrics.BiometricManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import uz.unnarsx.cherrygram.CherrygramConfig;

public class CGBiometricPrompt {

    private static BiometricPrompt.PromptInfo createPromptInfo() {
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder();
        builder.setTitle(LocaleController.getString("CG_AppName", R.string.CG_AppName));
        if (!CherrygramConfig.INSTANCE.getAllowSystemPasscode()) {
            builder.setNegativeButtonText(LocaleController.getString("Cancel", R.string.Cancel));
        }
//        builder.setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
        builder.setDeviceCredentialAllowed(CherrygramConfig.INSTANCE.getAllowSystemPasscode());
        builder.setConfirmationRequired(false);

        /*return new BiometricPrompt.PromptInfo.Builder()
                .setTitle(LocaleController.getString("CG_AppName", R.string.CG_AppName))
                .setConfirmationRequired(false)
                .build();*/
        return builder.build();
    }

    public static void callBiometricPrompt(Activity activity, CGBiometricListener listener) {
        new BiometricPrompt((FragmentActivity) activity, ContextCompat.getMainExecutor(activity), new BiometricPrompt.AuthenticationCallback() {
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
        CGBiometricPrompt.callBiometricPrompt(activity, new CGBiometricPrompt.CGBiometricListener() {
            @Override
            public void onError(CharSequence msg) {
                if (CherrygramConfig.INSTANCE.getShowRPCErrors())
                    Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                if (CherrygramConfig.INSTANCE.getShowRPCErrors())
                    Toast.makeText(activity, "Fail", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(BiometricPrompt.AuthenticationResult result) {
                successCallback.run();
                if (CherrygramConfig.INSTANCE.getShowRPCErrors())
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
                return false;
            }
            return fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
        }
        return false;
    }

}
