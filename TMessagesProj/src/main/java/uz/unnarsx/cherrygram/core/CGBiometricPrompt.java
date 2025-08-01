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
import android.content.Context;
import android.hardware.biometrics.BiometricManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.FingerprintController;
import org.telegram.messenger.R;
import org.telegram.messenger.support.fingerprint.FingerprintManagerCompat;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;

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

    private static BiometricPrompt.AuthenticationCallback createCallback(
            java.util.function.Consumer<BiometricPrompt.AuthenticationResult> onSuccess,
            Runnable onFailed,
            java.util.function.BiConsumer<Integer, CharSequence> onError
    ) {
        return new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                if (onSuccess != null) onSuccess.accept(result);
            }

            @Override
            public void onAuthenticationFailed() {
                if (onFailed != null) onFailed.run();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                if (onError != null) onError.accept(errorCode, errString);
            }
        };
    }

    public static void callBiometricPrompt(Activity activity, CGBiometricListener listener) {
        BiometricPrompt prompt = new BiometricPrompt(
                LaunchActivity.instance,
                ContextCompat.getMainExecutor(activity),
                createCallback(
                        listener::onSuccess,
                        listener::onFailed,
                        listener::onError
                )
        );
        prompt.authenticate(createPromptInfo());
    }

    public static void prompt(Activity activity, Runnable successCallback) {
        prompt(activity, successCallback, null);
    }

    public static void prompt(Activity activity, Runnable successCallback, Runnable failCallback) {
        CGBiometricPrompt.callBiometricPrompt(activity, new CGBiometricPrompt.CGBiometricListener() {
            @Override
            public void onSuccess(BiometricPrompt.AuthenticationResult result) {
                successCallback.run();
                if (CherrygramDebugConfig.INSTANCE.getShowRPCErrors())
                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                if (failCallback != null) failCallback.run();
                if (CherrygramDebugConfig.INSTANCE.getShowRPCErrors())
                    Toast.makeText(activity, "Fail", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error, CharSequence msg) {
                if (failCallback != null) failCallback.run();
                if (CherrygramDebugConfig.INSTANCE.getShowRPCErrors())
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface CGBiometricListener {
        void onSuccess(BiometricPrompt.AuthenticationResult result);
        void onFailed();
        void onError(int error, CharSequence msg);
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

    // Octogram fix

    private static final ArrayList<BiometricPrompt> pendingAuths = new ArrayList<>();
    private static final String TAG = "FingerprintUtils";

    public static void fixFingerprint(Context context, CGBiometricListener callback) {
        if (Build.VERSION.SDK_INT < 23) {
            callback.onFailed();
            return;
        }

        FingerprintController.checkKeyReady();
        FingerprintController.deleteInvalidKey();
        FingerprintController.checkKeyReady();

        cancelPendingAuthentications();

        BiometricPrompt prompt = new BiometricPrompt(
                LaunchActivity.instance,
                ContextCompat.getMainExecutor(context),
                createCallback(
                        result -> {
                            Log.d(TAG, "PasscodeView onAuthenticationSucceeded");
                            if (FingerprintController.isKeyReady() && FingerprintController.checkDeviceFingerprintsChanged()) {
                                FingerprintController.deleteInvalidKey();
                            }
                            callback.onSuccess(result);
                        },
                        callback::onFailed,
                        (code, errStr) -> {
                            Log.d(TAG, "PasscodeView onAuthenticationError: " + code + " \"" + errStr + "\"");
                            callback.onError(code, errStr);
                        }
                )
        );

        pendingAuths.add(prompt);
        prompt.authenticate(createPromptInfo());
    }

    public static void cancelPendingAuthentications() {
        for (BiometricPrompt prompt : pendingAuths) {
            prompt.cancelAuthentication();
        }
        pendingAuths.clear();
    }

    private static boolean isFirstCheck = true;
    private static boolean fingerprintCachedState = false;

    public static boolean hasFingerprintCached() {
        if (isFirstCheck) {
            reloadFingerprintState();
        }

        return fingerprintCachedState;
    }

    public static void reloadFingerprintState() {
        isFirstCheck = false;
        fingerprintCachedState = hasFingerprintInternal();
    }

    private static boolean hasFingerprintInternal() {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Log.d(TAG, "Starting fingerprint check...");

                FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(ApplicationLoader.applicationContext);

                boolean conditions = fingerprintManager.isHardwareDetected();
                Log.d(TAG, "Fingerprint hardware detected: " + conditions);

                conditions &= fingerprintManager.hasEnrolledFingerprints();
                Log.d(TAG, "Enrolled fingerprints: " + fingerprintManager.hasEnrolledFingerprints());

                conditions &= FingerprintController.isKeyReady();
                Log.d(TAG, "Fingerprint key ready: " + FingerprintController.isKeyReady());

                conditions &= !FingerprintController.checkDeviceFingerprintsChanged();
                Log.d(TAG, "Device fingerprints changed: " + !FingerprintController.checkDeviceFingerprintsChanged());

                Log.d(TAG, "Final fingerprint check result: " + conditions);
                return conditions;
            } catch (Throwable e) {
                FileLog.e("Error checking fingerprint availability", e);
            }
        } else {
            Log.d(TAG, "Fingerprint check skipped: SDK version < 23");
        }
        return false;
    }

}
