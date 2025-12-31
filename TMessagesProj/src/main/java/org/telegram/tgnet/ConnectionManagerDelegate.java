/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package org.telegram.tgnet;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ConnectionManagerDelegate extends ContextWrapper {
//    public static final String TAG = ConnectionManagerDelegate.class.getSimpleName();
    private static final String HASH_TYPE = "SHA-256";
    public static final int NUM_HASHED_BYTES = 9;
    public static final int NUM_BASE64_CHAR = 11;

    public ConnectionManagerDelegate(Context context) {
        super(context);
//        getAppSignatures();
        getAppSignature();
    }

    /**
     * Get all the app signatures for the current package
     * @return
     */
    /*public ArrayList<String> getAppSignatures() {
        ArrayList<String> appCodes = new ArrayList<>();

        try {
            // Get all package signatures for the current package
            String packageName = getPackageName();
            PackageManager packageManager = getPackageManager();
            Signature[] signatures = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES).signatures;

            // For each signature create a compatible hash
            for (Signature signature : signatures) {
                String hash = hash(packageName, signature.toCharsString());
                if (hash != null) {
                    appCodes.add(String.format("%s", hash));
                }

                Log.v(TAG, "Hash " + hash);

            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to find package to obtain hash.", e);
        }
        return appCodes;
    }*/

    private static String hash(String packageName, String signature) {
        String appInfo = packageName + " " + signature;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_TYPE);
            messageDigest.update(appInfo.getBytes(StandardCharsets.UTF_8));
            byte[] hashSignature = messageDigest.digest();

            // truncated into NUM_HASHED_BYTES
            hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES);
            // encode into Base64
            String base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING | Base64.NO_WRAP);
            base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR);

//            Log.d(TAG, String.format("pkg: %s -- hash: %s", packageName, base64Hash));
            return base64Hash;
        } catch (NoSuchAlgorithmException e) {
//            Log.e(TAG, "hash:NoSuchAlgorithm", e);
        }
        return null;
    }

    public String getAppSignature() {
        try {
            // Get all package signatures for the current package
            String packageName = getPackageName();
            PackageManager packageManager = getPackageManager();
            Signature[] signatures = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES).signatures;

            // For each signature create a compatible hash
            for (Signature signature : signatures) {
                String hash = hash(packageName, signature.toCharsString());
//                if (hash != null) {
//                    Log.v(TAG, "Hash " + hash);
//                }
                return hash;
            }
        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, "Unable to find package to obtain hash.", e);
        }
        return "can't get hash";
    }

    public static File getFilesDirFixed(String name) {
        for (int a = 0; a < 10; a++) {
            File path = ApplicationLoader.applicationContext.getDataDir();
            if (path != null) {
                return path;
            }
        }
        try {
            ApplicationInfo info = ApplicationLoader.applicationContext.getApplicationInfo();
            File path = new File(info.dataDir, name);
            path.mkdirs();
            return path;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return new File("/data/data/uz.unnarsx.cherrygram/" + name);
    }

}
