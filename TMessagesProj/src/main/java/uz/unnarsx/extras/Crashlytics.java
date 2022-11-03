package uz.unnarsx.extras;

import android.os.Build;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.SharedConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import uz.unnarsx.cherrygram.CherrygramConfig;

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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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

    public static File shareLogs() throws IOException {
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
        switch (SharedConfig.getDevicePerformanceClass()) {
            case SharedConfig.PERFORMANCE_CLASS_LOW:
                return "LOW";
            case SharedConfig.PERFORMANCE_CLASS_AVERAGE:
                return "AVERAGE";
            case SharedConfig.PERFORMANCE_CLASS_HIGH:
                return "HIGH";
            default:
                return "UNKNOWN";
        }
    }

    public static String getCrashReportMessage() {
        return getReportMessage() + "\n" +
                "Crash Date: " + LocaleController.getInstance().formatterStats.format(System.currentTimeMillis()) +
                "\n\n#crash";
    }

    public static String getReportMessage() {
        String CameraName;
        switch (CherrygramConfig.INSTANCE.getCameraType()) {
            case 0:
                CameraName = "Tg Camera";
                break;
            case 1:
                CameraName = "CameraX";
                break;
            case 2:
                CameraName = "System Camera";
                break;
            default:
                CameraName = "Unknown";
        }
        return  "Steps to reproduce\n" +
                "Write here the steps to reproduce\n\n" +
                "Details\n"+
                "App Version: " + BuildConfig.VERSION_NAME_CHERRY + "\n" +
                "Base Version: " + BuildVars.BUILD_VERSION_STRING + " (" + BuildVars.BUILD_VERSION + ")\n" +
                "Device: " + AndroidUtilities.capitalize(Build.MANUFACTURER) + " " + Build.MODEL + "\n" +
                "OS Version: " + Build.VERSION.RELEASE + "\n" +
                "Google Play Services: " + ApplicationLoader.hasPlayServices + "\n" +
                "Performance Class: " + getPerformanceClassString() + "\n" +
                "Locale: " + LocaleController.getSystemLocaleStringIso639() + "\n" +
                "Camera: " + CameraName;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteCrashLogs() {
        File file = getLogFile();
        if (file.exists()) {
            file.delete();
        }
    }
}
