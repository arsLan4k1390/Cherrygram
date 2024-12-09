package uz.unnarsx.cherrygram.updater;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.exoplayer2.util.Util;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.BaseFragment;
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
import java.util.Date;
import java.util.Objects;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.misc.Constants;

public class UpdaterUtils {

    public static final DispatchQueue otaQueue = new DispatchQueue("otaQueue");

    private static String uri = "https://api.github.com/repos/arsLan4k1390/Cherrygram/releases/latest";
    private static String betauri = "https://api.github.com/repos/arsLan4k1390/CherrygramBeta-APKs/releases/latest";
    private static String downloadURL = null;
    public static String version, changelog, size, uploadDate;
    public static File otaPath, versionPath, apkFile;

    private static long id = 1L;
    private static final long updateCheckInterval = 3600000L; // 1 hour

    private static boolean updateDownloaded;
    private static boolean checkingForUpdates;

    public static final String[] deviceModels = {
            "Galaxy S6", "Galaxy S7", "Galaxy S8", "Galaxy S9", "Galaxy S10", "Galaxy S21",
            "Pixel 3", "Pixel 4", "Pixel 5",
            "OnePlus 6", "OnePlus 7", "OnePlus 8", "OnePlus 9", "Xperia XZ", "Xperia XZ2", "Xperia XZ3", "Xperia 1", "Xperia 5", "Xperia 10", "Xperia L4"
    };
    private static final String[] chromeVersions = {
            "111.0.5563.57", "94.0.4606.81", "80.0.3987.119", "69.0.3497.100", "92.0.4515.159", "71.0.3578.99"
    };

    public static String formatUserAgent() {
        String androidVersion = String.valueOf(Utilities.random.nextInt(7) + 6);
        String deviceModel = deviceModels[Utilities.random.nextInt(deviceModels.length)];
        String chromeVersion = chromeVersions[Utilities.random.nextInt(chromeVersions.length)];
        return String.format("Mozilla/5.0 (Linux; Android %s; %s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%s Mobile Safari/537.36", androidVersion, deviceModel, chromeVersion);
    }

    public static void checkDirs() {
        otaPath = new File(ApplicationLoader.applicationContext.getExternalFilesDir(null), "ota");
        if (version != null) {
            versionPath = new File(otaPath, version);
            apkFile = new File(versionPath, "update.apk");
            try {
                if (!versionPath.exists())
                    versionPath.mkdirs();
            } catch (Exception e) {
                FileLog.e(e);
            }
            updateDownloaded = apkFile.exists();
        }
    }

    public interface OnUpdateNotFound {
        void run();
    }

    public interface OnUpdateFound {
        void run();
    }

    public static void checkUpdates(BaseFragment fragment, boolean manual) {
        checkUpdates(fragment, manual, null, null, null);
    }

    /*public static void checkUpdates(BaseFragment fragment, boolean manual, OnUpdateNotFound onUpdateNotFound, OnUpdateFound onUpdateFound) {
        checkUpdates(fragment, manual, onUpdateNotFound, onUpdateFound, null);
    }*/

    public static void checkUpdates(BaseFragment fragment, boolean manual, OnUpdateNotFound onUpdateNotFound, OnUpdateFound onUpdateFound, Browser.Progress progress) {
        if (CherrygramCoreConfig.INSTANCE.isStandalonePremiumBuild()) return;

        if (checkingForUpdates || id != 1L || (System.currentTimeMillis() - CherrygramCoreConfig.INSTANCE.getUpdateScheduleTimestamp() < updateCheckInterval && !manual))
            return;

        checkingForUpdates = true;
        otaQueue.postRunnable(() -> {
            CherrygramCoreConfig.INSTANCE.getLastUpdateCheckTime();
            CherrygramCoreConfig.INSTANCE.setLastUpdateCheckTime(System.currentTimeMillis());
            try {
                HttpURLConnection connection = (HttpURLConnection) new URI(uri).toURL().openConnection();
                if (CherrygramCoreConfig.INSTANCE.getInstallBetas()) {
                    connection = (HttpURLConnection) new URI(betauri).toURL().openConnection();
                }
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", formatUserAgent());
                connection.setRequestProperty("Content-Type", "application/json");

                StringBuilder textBuilder = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    int c;
                    while ((c = reader.read()) != -1)
                        textBuilder.append((char) c);
                }

                JSONObject obj = new JSONObject(textBuilder.toString());
                org.json.JSONArray arr = obj.getJSONArray("assets");

                if (arr.length() == 0)
                    return;

                String[] supportedTypes = {"arm64-v8a", "armeabi-v7a", "x86", "x86_64", "universal"};
                loop:
                for (int i = 0; i < arr.length(); i++) {
                    String link;
                    link = arr.getJSONObject(i).getString("browser_download_url")
                            .replace("Cherrygram-", "Cherrygram-Huawei-");
                    downloadURL = link;
                    if (CherrygramCoreConfig.INSTANCE.isDevBuild()) FileLog.d ("DownloadLinkHuawei: " + downloadURL);

                    size = AndroidUtilities.formatFileSize(arr.getJSONObject(i).getLong("size"));
                    for (String type : supportedTypes) {
                        if (link.contains(type) && Objects.equals(CGResourcesHelper.INSTANCE.getAbiCode(), type)) {
                            break loop;
                        }
                    }
                }
                version = obj.getString("tag_name");
                changelog = obj.getString("body");
                uploadDate = obj.getString("published_at").replaceAll("[TZ]", " ");
                uploadDate = LocaleController.formatDateTime(getMillisFromDate(uploadDate, "yyyy-M-dd hh:mm:ss") / 1000, true);
                Update update = new Update(version, changelog, size, downloadURL, uploadDate);
                if (update.isNew() && fragment != null && fragment.getContext() != null) {
                    checkDirs();
                    AndroidUtilities.runOnUIThread(() -> {
                        UpdaterBottomSheet.showAlert(fragment, true, update);
                        if (onUpdateFound != null)
                            onUpdateFound.run();
                        if (progress != null) progress.end();
                    });
                } else {
                    if (onUpdateNotFound != null)
                        AndroidUtilities.runOnUIThread(onUpdateNotFound::run);
                    if (progress != null) progress.end();
                }
            } catch (Exception e) {
                FileLog.e(e);
                if (progress != null) progress.end();
            }
            checkingForUpdates = false;
        }, 200);
    }

    public static void downloadApk(Context context, String link, String title) {
        if (context != null && !updateDownloaded) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            request.setTitle(title);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(context, "ota/" + version, "update.apk");

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            id = manager.enqueue(request);

            DownloadReceiver downloadBroadcastReceiver = new DownloadReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.DOWNLOAD_COMPLETE");
            intentFilter.addAction("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.registerReceiver(context, downloadBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
            } else  {
                Util.registerReceiverNotExported(context, downloadBroadcastReceiver, intentFilter, Util.createHandlerForCurrentOrMainLooper());
            }
        } else {
            installApk(context, apkFile.getAbsolutePath());
        }
    }

    public static void installApk(Context context, String path) {
        File file = new File(path);
        if (!file.exists())
            return;
        Intent install = new Intent(Intent.ACTION_VIEW);
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = FileProvider.getUriForFile(context, Constants.PACKAGE_NAME + ".provider", file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ApplicationLoader.applicationContext.getPackageManager().canRequestPackageInstalls()) {
            AlertsCreator.createApkRestrictedDialog(context, null).show();
            return;
        }
        if (fileUri != null) {
            install.setDataAndType(fileUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (install.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(install);
            }
        }
    }

    public static String getOtaDirSize() {
        checkDirs();
        return AndroidUtilities.formatFileSize(Utilities.getDirSize(otaPath.getAbsolutePath(), 5, true), true, false);
    }

    public static void cleanOtaDir() {
        checkDirs();
        cleanFolder(otaPath);
    }

    public static void cleanFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    cleanFolder(file);
                }
            }
        }
        folder.delete();
    }

    public static long getMillisFromDate(String d, String format) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date date = sdf.parse(d);
            assert date != null;
            return date.getTime();
        } catch (Exception ignore) {
            return 1L;
        }
    }

    public static SpannableStringBuilder replaceTags(CharSequence str) {
        try {
            int start;
            int end;
            StringBuilder stringBuilder = new StringBuilder(str);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
            String symbol = "", font = AndroidUtilities.TYPEFACE_ROBOTO_REGULAR;
            for (int i = 0; i < 3; i++) {
                switch (i) {
                    case 0:
                        symbol = "**";
                        font = AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM;
                        break;
                    case 1:
                        symbol = "_";
                        font = AndroidUtilities.TYPEFACE_ROBOTO_ITALIC;
                        break;
                    case 2:
                        symbol = "`";
                        font = AndroidUtilities.TYPEFACE_ROBOTO_MONO;
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

    public static class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if (context != null && DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 1L);
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = downloadManager.query(query);
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = cursor.getInt(columnIndex);
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        installApk(context, apkFile.getAbsolutePath());
                        id = 1L;
                        updateDownloaded = false;
                    } else {
                        // ignore for now
                    }
                }
                cursor.close();
            } else if (context != null && DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
                try {
                    Intent viewDownloadIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                    viewDownloadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(viewDownloadIntent);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }
    }

    public static class Update {
        public final String version, size, downloadURL, uploadDate, changelog;

        public Update(String version, String changelog, String size, String downloadURL, String uploadDate) {
            this.version = version;
            this.changelog = changelog;
            this.size = size;
            this.downloadURL = downloadURL;
            this.uploadDate = uploadDate;
        }

        // todo: compare by version code, not version
        public boolean isNew() {
            String[] current = Constants.INSTANCE.getCG_VERSION().split("\\.");
            String[] latest = version.split("\\.");

            int length = Math.max(current.length, latest.length);
            for (int i = 0; i < length; i++) {
                int v1 = i < current.length ? Utilities.parseInt(current[i]) : 0;
                int v2 = i < latest.length ? Utilities.parseInt(latest[i]) : 0;
                if (v1 < v2) {
                    return true;
                } else if (v1 > v2) {
                    return false;
                }
            }
            return false;
        }

        // todo: force update
        public boolean isForce() {
            return version.toLowerCase().contains("force");
        }
    }

    /*public static String getLastCheckUpdateTime() {
        return getString(R.string.UP_LastCheck) + ": " + LocaleController.formatDateTime(CherrygramCoreConfig.INSTANCE.getLastUpdateCheckTime() / 1000, true);
    }*/

}