/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.crashlytics;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.LaunchActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.preferences.CameraPreferencesEntry;

public class Crashlytics implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler defaultUEH;

    public Crashlytics() {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        try {
            saveCrashLogs(stacktrace);
        } catch (Exception ignored) { }
        printWriter.close();
        defaultUEH.uncaughtException(t, e);
    }

    private static File getLogFile() {
        return new File(ApplicationLoader.getFilesDirFixed(), "last_crash.log");
    }

    private static File getShareLogFile() {
        return new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), "Logcat.log");
    }

    private static void saveCrashLogs(String logcat) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFile()));
        writer.write(logcat);
        writer.flush();
        writer.close();
    }

    public static boolean isCrashed() {
        return getLogFile().exists();
    }

    private static File shareLogs() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(getLogFile()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        reader.close();
        deleteCrashLogs();
        File file = getShareLogFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(builder.toString());
        writer.flush();
        writer.close();
        return file;
    }

    private static String getPerformanceClassString() {
        return switch (SharedConfig.getDevicePerformanceClass()) {
            case SharedConfig.PERFORMANCE_CLASS_LOW -> "LOW";
            case SharedConfig.PERFORMANCE_CLASS_AVERAGE -> "AVERAGE";
            case SharedConfig.PERFORMANCE_CLASS_HIGH -> "HIGH";
            default -> "UNKNOWN";
        };
    }

    private static String getCrashReportMessage() {
        return getReportMessage() + "\n\n" +
                "Crash Date: " + LocaleController.getInstance().getFormatterStats().format(System.currentTimeMillis()) +
                "\n\n#crash";
    }

    public static String getReportMessage() {
        return  "Steps to reproduce:\n" +
                "Write here the steps to reproduce\n\n" +
                "Details:\n"+
                "• Cherrygram Version: " + CGResourcesHelper.getCherryVersion() + " (" + CGResourcesHelper.getAbiCode() + ")\n" +
                "• Telegram Version: " + BuildVars.BUILD_VERSION_STRING + " (" + CGResourcesHelper.getSourceCodeVersion() + ")\n" +
                "• Build Type: " + CGResourcesHelper.getBuildType() + "\n" +
                "• Device: " + CGResourcesHelper.INSTANCE.capitalize(Build.MANUFACTURER) + " " + Build.MODEL + "\n" +
                "• OS Version: " + Build.VERSION.RELEASE + " • SDK: " + Build.VERSION.SDK_INT + "\n" +
                "• Screen: " + AndroidUtilities.displaySize.x + "x" + AndroidUtilities.displaySize.y + " • DPI: " + AndroidUtilities.densityDpi + "\n" +
                "• Camera: " + CameraPreferencesEntry.getCameraName() + "\n" +
                "• Performance Class: " + getPerformanceClassString() + "\n" +
                "• Google Play Services: " + ApplicationLoader.hasPlayServices + "\n" +
                "• Locale: " + LocaleController.getSystemLocaleStringIso639();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteCrashLogs() {
        File file = getLogFile();
        if (file.exists()) {
            file.delete();
        }
    }

    public static void sendCrashLogs(Activity activity, CrashReportBottomSheet bottomSheet) {
        try {
            File cacheFile = Crashlytics.shareLogs();
            Uri uri;

            Intent i = new Intent(Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId() + ".provider", cacheFile);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(cacheFile);
            }
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_SUBJECT, Crashlytics.getCrashReportMessage());
            i.putExtra(Intent.EXTRA_STREAM, uri);
            i.setClass(activity, LaunchActivity.class);

            activity.startActivity(i);

            bottomSheet.dismiss();
        } catch (IOException e) {
            FileLog.e(e);
        }
    }

}
