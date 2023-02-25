/*

 This is the source code of exteraGram for Android.

 We do not and cannot prevent the use of our code,
 but be respectful and credit the original author.

 Copyright @immat0x1, 2022.

*/

package uz.unnarsx.cherrygram.updater;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;

import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.TypefaceSpan;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Objects;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.extras.CherrygramExtras;

public class UpdaterUtils {

    private static String uri = "https://api.github.com/repos/arsLan4k1390/Cherrygram/releases/latest";
    private static String betauri = "https://api.github.com/repos/arsLan4k1390/CherrygramBeta-APKs/releases/latest";
    private static String downloadURL = null;
    public static String version, changelog, size, uploadDate;
    public static File otaPath, versionPath, apkFile;

    private static long id = 0L;
    private static final long updateCheckInterval = 3600000L; // 1 hour

    public static boolean updateDownloaded = false;

    private static final String[] userAgents = {
        "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36,gzip(gfe)",
        "Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 7 Build/MRA51D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.133 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/600.8.9 (KHTML, like Gecko) Version/8.0.8 Safari/600.8.9",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/44.0.2403.89 Chrome/44.0.2403.89 Safari/537.36",
        "Mozilla/5.0 (Linux; Android 5.0.2; SAMSUNG SM-G920F Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/3.0 Chrome/38.0.2125.102 Mobile Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; rv:40.0) Gecko/20100101 Firefox/40.0"
    };

    public static String getRandomUserAgent() {
        int randomNum = Utilities.random.nextInt(userAgents.length);
        return userAgents[randomNum];
    }

    public static void checkDirs() {
        otaPath = new File(ApplicationLoader.applicationContext.getExternalFilesDir(null), "ota");
        if (version != null) {
            versionPath = new File(otaPath, version);
            apkFile = new File(versionPath, "update.apk");
            if (!versionPath.exists()) {
                versionPath.mkdirs();
            }
            updateDownloaded = apkFile.exists();
        }
    }

    public static void checkUpdates(Context context, boolean manual) {
        checkUpdates(context, manual, () -> {}, () -> {});
    }
    public interface OnUpdateNotFound {
        void run();
    }
    public interface OnUpdateFound {
        void run();
    }
    public static void checkUpdates(Context context, boolean manual, OnUpdateNotFound onUpdateNotFound, OnUpdateFound onUpdateFound) {

        Utilities.globalQueue.postRunnable(() -> {
            CherrygramConfig.INSTANCE.getLastUpdateCheckTime();
            CherrygramConfig.INSTANCE.setLastUpdateCheckTime(System.currentTimeMillis());

            if (id != 0L || (System.currentTimeMillis() - CherrygramConfig.INSTANCE.getUpdateScheduleTimestamp() < updateCheckInterval && !manual)) {
                return;
            }

            try {
                HttpURLConnection connection = (HttpURLConnection) new URI(uri).toURL().openConnection();
                if (CherrygramConfig.INSTANCE.getInstallBetas()) {
                    connection = (HttpURLConnection) new URI(betauri).toURL().openConnection();
                }
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", getRandomUserAgent());
                connection.setRequestProperty("Content-Type", "application/json");

                StringBuilder textBuilder = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    int c;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                }

                JSONObject obj = new JSONObject(textBuilder.toString());
                JSONArray arr = obj.getJSONArray("assets");

                if (arr.length() == 0) {
                    return;
                }
                for (int i = 0; i < arr.length(); i++) {
                    String cpu = CherrygramExtras.INSTANCE.getAbiCode();
                    String link;
                    downloadURL = link = arr.getJSONObject(i).getString("browser_download_url");
                    //Log.d ("DownloadLink", downloadURL);
                    if (ApplicationLoader.isHuaweiStoreBuild()) {
                        downloadURL = link.replace("Cherrygram-", "Cherrygram-Huawei-");
                        //Log.d ("DownloadLinkHuawei", downloadURL);
                    }
                    size = AndroidUtilities.formatFileSize(arr.getJSONObject(i).getLong("size"));
                    if (link.contains("arm64-v8a") && Objects.equals(cpu, "arm64-v8a") ||
                        link.contains("armeabi-v7a") && Objects.equals(cpu, "armeabi-v7a") ||
                        link.contains("x86") && Objects.equals(cpu, "x86") ||
                        link.contains("x86_64") && Objects.equals(cpu, "x86_64") ||
                        link.contains("universal") && Objects.equals(cpu, "universal")){
                        break;
                    }
                }
                version = obj.getString("name");
                changelog = obj.getString("body");
                uploadDate = obj.getString("published_at").replaceAll("[TZ]", " ");
                uploadDate = LocaleController.formatDateTime(getMillisFromDate(uploadDate, "yyyy-M-dd hh:mm:ss") / 1000);

                if (isNewVersion(CherrygramExtras.INSTANCE.getCG_VERSION(), version)) {
                    checkDirs();
                    AndroidUtilities.runOnUIThread(() -> {
                        (new UpdaterBottomSheet(context, true, version, changelog, size, downloadURL, uploadDate)).show();
                        if (onUpdateFound != null) {
                            onUpdateFound.run();
                        }
                    });
                } else {
                    if (onUpdateNotFound != null) {
                        AndroidUtilities.runOnUIThread(onUpdateNotFound::run);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void downloadApk(Context context, String link, String title) {
        if (!updateDownloaded) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
//            Log.d ("DownloadedApkLink", link);

            request.setAllowedNetworkTypes(3);
            request.setTitle(title);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(context, "ota/" + version, "update.apk");

            id = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);

            DownloadReceiver downloadBroadcastReceiver = new DownloadReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.DOWNLOAD_COMPLETE");
            intentFilter.addAction("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED");
            context.registerReceiver(downloadBroadcastReceiver, intentFilter);
            return;
        }
        installApk(context, apkFile.getAbsolutePath());
    }

    public static void installApk(Context context, String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        Intent install = new Intent(Intent.ACTION_VIEW);
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = FileProvider.getUriForFile(context, ApplicationLoader.getApplicationId() + ".provider", file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ApplicationLoader.applicationContext.getPackageManager().canRequestPackageInstalls()) {
            AlertsCreator.createApkRestrictedDialog(context, null).show();
            return;
        }
        if (fileUri != null) {
            install.setDataAndType(fileUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (install.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(install);
            }
        }
    }

    public static boolean isNewVersion(String... v) {
        if (v.length != 2) {
            return false;
        }
        for (int i = 0; i < 2; i++) {
            v[i] = v[i].replaceAll("[^0-9]+", "");
            if (Integer.parseInt(v[i]) <= 999) {
                v[i] += "0";
            }
        }
        return Integer.parseInt(v[0]) < Integer.parseInt(v[1]);
    }

    public static String getOtaDirSize() {
        checkDirs();
        return AndroidUtilities.formatFileSize(Utilities.getDirSize(otaPath.getAbsolutePath(), 5, true), true);
    }

    public static void cleanOtaDir() {
        checkDirs();
        cleanFolder(otaPath);
    }

    public static void cleanFolder(File file) {
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File file2 : listFiles) {
                if (file2.isDirectory()) {
                    cleanFolder(file2);
                }
                file2.delete();
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static long getMillisFromDate(String d, String format) {
        try {
            return Objects.requireNonNull(new SimpleDateFormat(format).parse(d)).getTime();
        } catch (Exception ignore) {
            return 1L;
        }
    }

    public static SpannableStringBuilder replaceTags(String str) {
        try {
            int start;
            int end;
            StringBuilder stringBuilder = new StringBuilder(str);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
            String symbol = "", font = "fonts/rregular.ttf";
            for (int i = 0; i < 3; i++) {
                switch (i) {
                    case 0:
                        symbol = "**";
                        font = "fonts/rmedium.ttf";
                        break;
                    case 1:
                        symbol = "_";
                        font = "fonts/ritalic.ttf";
                        break;
                    case 2:
                        symbol = "`";
                        font = "fonts/rmono.ttf";
                        break;
                }
                while ((start = stringBuilder.indexOf(symbol)) != -1) {
                    stringBuilder.replace(start, start + symbol.length(), "");
                    spannableStringBuilder.replace(start, start + symbol.length(), "");
                    end = stringBuilder.indexOf(symbol);
                    if (end >= 0) {
                        stringBuilder.replace(end, end + symbol.length(), "");
                        spannableStringBuilder.replace(end, end + symbol.length(), "");
                        spannableStringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface(font)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            return spannableStringBuilder;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return new SpannableStringBuilder(str);
    }

    public interface OnTranslationSuccess {
        void run(String translated);
    }

    public interface OnTranslationFail {
        void run();
    }

    public static void translate(CharSequence text, OnTranslationSuccess onSuccess, OnTranslationFail onFail) {
        Utilities.globalQueue.postRunnable(() -> {
            String uri;
            HttpURLConnection connection;
            try {
                uri = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=";
                uri += Uri.encode(LocaleController.getInstance().getCurrentLocale().getLanguage());
                uri += "&dt=t&ie=UTF-8&oe=UTF-8&otf=1&ssel=0&tsel=0&kc=7&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&q=";
                uri += Uri.encode(text.toString());
                connection = (HttpURLConnection) new URI(uri).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", getRandomUserAgent());
                connection.setRequestProperty("Content-Type", "application/json");

                StringBuilder textBuilder = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    int c;
                    while ((c = reader.read()) != -1) textBuilder.append((char) c);
                }
                JSONTokener tokener = new JSONTokener(textBuilder.toString());
                JSONArray array = new JSONArray(tokener);
                JSONArray array1 = array.getJSONArray(0);
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < array1.length(); ++i) {
                    String blockText = array1.getJSONArray(i).getString(0);
                    if (blockText != null && !blockText.equals("null")) result.append(blockText);
                }
                if (text.length() > 0 && text.charAt(0) == '\n') result.insert(0, "\n");
                if (onSuccess != null) AndroidUtilities.runOnUIThread(() -> onSuccess.run(result.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                if (onFail != null) AndroidUtilities.runOnUIThread(onFail::run);
            }
        });
    }

    public static class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                if (id == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                    installApk(context, apkFile.getAbsolutePath());
                    id = 0L;
                    updateDownloaded = false;
                }
            } else if (Objects.equals(intent.getAction(), DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                try {
                    Intent viewDownloadIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                    viewDownloadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(viewDownloadIntent);
                } catch (Exception e) {
                    FileLog.e("Downloads activity not found: ", e);
                }
            }
        }
    }
}