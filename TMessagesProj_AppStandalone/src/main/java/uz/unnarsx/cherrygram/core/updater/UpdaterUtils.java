/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.updater;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.TextView;

import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.RadialProgress2;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

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
import uz.unnarsx.cherrygram.helpers.network.NetworkHelper;
import uz.unnarsx.cherrygram.misc.Constants;

public class UpdaterUtils {

    public static final DispatchQueue otaQueue = new DispatchQueue("otaQueue");

    private static final String RELEASE_URI = "https://api.github.com/repos/arsLan4k1390/Cherrygram/releases/latest";
    private static final String BETA_URI = "https://api.github.com/repos/arsLan4k1390/CherrygramBeta-APKs/releases/latest";
    public static String downloadURL = null;
    public static String version, changelog, size, uploadDate;
    public static File otaPath, versionPath, apkFile;

    public static long id = 1L;
    private static final long updateCheckInterval = 3600000L; // 1 hour
    private static volatile boolean updateDownloaded = false;
    private static volatile boolean checkingForUpdates = false;

    private static final Handler handler = new Handler();
    private static Runnable progressRunnable;

    /**
     * Ensure OTA dirs exist. Returns true if otaPath/versionPath/apkFile are ready.
     */
    public static boolean checkDirs() {
        try {
            otaPath = new File(ApplicationLoader.applicationContext.getExternalFilesDir(null), "ota");

            if (version == null || version.isEmpty()) {
                updateDownloaded = false;
                return false;
            }

            versionPath = new File(otaPath, version);
            if (!versionPath.exists()) {
                if (!versionPath.mkdirs() && !versionPath.exists()) {
                    updateDownloaded = false;
                    return false;
                }
            }
            apkFile = new File(versionPath, "update.apk");
            updateDownloaded = apkFile.exists();
            return true;
        } catch (Exception e) {
            FileLog.e(e);
            updateDownloaded = false;
            return false;
        }
    }

    public static boolean updateFileExists() {
        version = CherrygramCoreConfig.INSTANCE.getUpdateVersionName();

        if (version.isEmpty()) {
            CherrygramCoreConfig.INSTANCE.setUpdateAvailable(false);
            return false;
        }

        otaPath = new File(ApplicationLoader.applicationContext.getExternalFilesDir(null), "ota");
        versionPath = new File(otaPath, version);
        apkFile = new File(versionPath, "update.apk");

        try {
            if (!versionPath.exists()) versionPath.mkdirs();
        } catch (Exception e) {
            FileLog.e(e);
        }

        String[] current = CGResourcesHelper.getCherryVersion().split("\\.");
        String[] downloaded = version.split("\\.");

        int cmp = compareVersions(current, downloaded);
        boolean isNew = cmp < 0;
        CherrygramCoreConfig.INSTANCE.setUpdateAvailable(isNew);

        return isNew && apkFile.exists();
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
        if (CherrygramCoreConfig.isStandalonePremiumBuild()) return;

        if (checkingForUpdates || id != 1L || (System.currentTimeMillis() - CherrygramCoreConfig.INSTANCE.getUpdateScheduleTimestamp() < updateCheckInterval && !manual))
            return;

        checkingForUpdates = true;
        otaQueue.postRunnable(() -> {
            CherrygramCoreConfig.INSTANCE.getLastUpdateCheckTime();
            CherrygramCoreConfig.INSTANCE.setLastUpdateCheckTime(System.currentTimeMillis());
            try {
                HttpURLConnection connection = (HttpURLConnection) new URI(RELEASE_URI).toURL().openConnection();
                if (CherrygramCoreConfig.INSTANCE.getInstallBetas()) {
                    connection = (HttpURLConnection) new URI(BETA_URI).toURL().openConnection();
                }
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", NetworkHelper.formatUserAgent());
                connection.setRequestProperty("Content-Type", "application/json");

                StringBuilder textBuilder = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    int c;
                    while ((c = reader.read()) != -1)
                        textBuilder.append((char) c);
                }

                JSONObject obj = new JSONObject(textBuilder.toString());
                JSONArray arr = obj.getJSONArray("assets");

                if (arr.length() == 0)
                    return;

                String[] supportedTypes = {"arm64-v8a", "armeabi-v7a", "x86", "x86_64", "universal"};
                loop:
                for (int i = 0; i < arr.length(); i++) {
                    String link;
                    link = arr.getJSONObject(i).getString("browser_download_url");
                    downloadURL = link;
                    if (CherrygramCoreConfig.isDevBuild()) FileLog.d("DownloadLink: " + downloadURL);

                    size = AndroidUtilities.formatFileSize(arr.getJSONObject(i).getLong("size"));
                    for (String type : supportedTypes) {
                        if (link.contains(type) && Objects.equals(CGResourcesHelper.getAbiCode(), type)) {
                            break loop;
                        }
                    }
                }
                version = obj.getString("name"); // Can be changed to tag_name
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
                    CherrygramCoreConfig.INSTANCE.setUpdateIsDownloading(false);
                    CherrygramCoreConfig.INSTANCE.setUpdateAvailable(true);
                    if (version != null && !version.equals("0")) CherrygramCoreConfig.INSTANCE.setUpdateVersionName(version);
                    CherrygramCoreConfig.INSTANCE.setUpdateSize(size);
                } else {
                    if (onUpdateNotFound != null)
                        AndroidUtilities.runOnUIThread(onUpdateNotFound::run);
                    if (progress != null) progress.end();
                    cleanOtaDir();
                }
            } catch (Exception e) {
                FileLog.e(e);
                if (progress != null) progress.end();
            }
            checkingForUpdates = false;
        }, 200);
    }

    public static void downloadApk(Context context, String link, String title, ButtonWithCounterView progressTextView) {
        if (context != null && !updateDownloaded) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));

            File baseDir = new File(context.getExternalFilesDir(null), "ota/" + version);
            if (!baseDir.exists()) {
                boolean created = baseDir.mkdirs();
                if (!created) {
                    throw new IllegalStateException("Cannot create dir: " + baseDir.getAbsolutePath());
                }
            }

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            request.setTitle(title);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(context, "ota/" + version, "update.apk");

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            id = manager.enqueue(request);

            DownloadReceiver downloadBroadcastReceiver = new DownloadReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.registerReceiver(context, downloadBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
            } else  {
                Util.registerReceiverNotExported(context, downloadBroadcastReceiver, intentFilter, Util.createHandlerForCurrentOrMainLooper());
            }
            CherrygramCoreConfig.INSTANCE.setUpdateIsDownloading(true);
            trackDownloadProgress(context, progressTextView, null, null);
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateLoading);
        } else {
            CherrygramCoreConfig.INSTANCE.setUpdateIsDownloading(false);
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
            installApk(context, apkFile.getAbsolutePath());
        }
    }

    public static void installApk(Context context, String path) {
        if (context == null || path == null) return;
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
        if (!checkDirs()) return "0 B";
        return AndroidUtilities.formatFileSize(Utilities.getDirSize(otaPath.getAbsolutePath(), 5, true), true, false);
    }

    public static void cleanOtaDir() {
        // remove only ota folder content if exists
        if (!checkDirs()) return;
        cleanFolder(otaPath);
    }

    public static void cleanFolder(File folder) {
        if (folder == null) return;
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    cleanFolder(file);
                }
            }
        }
        // ignore return value
        try {
            folder.delete();
        } catch (Exception e) {
            FileLog.e(e);
        }
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
                font = switch (i) {
                    case 0 -> {
                        symbol = "**";
                        yield AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM;
                    }
                    case 1 -> {
                        symbol = "_";
                        yield AndroidUtilities.TYPEFACE_ROBOTO_ITALIC;
                    }
                    case 2 -> {
                        symbol = "`";
                        yield AndroidUtilities.TYPEFACE_ROBOTO_MONO;
                    }
                    default -> font;
                };
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
        public void onReceive(Context context, Intent intent) {
            String action = intent != null ? intent.getAction() : null;
            if (context != null && DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 1L);
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                if (downloadManager == null) return;
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                try (Cursor cursor = downloadManager.query(query)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = cursor.getInt(columnIndex);
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            if (progressRunnable != null) {
                                handler.removeCallbacks(progressRunnable);
                            }
                            // refresh apkFile reference
                            if (versionPath == null) checkDirs();
                            if (apkFile != null) installApk(context, apkFile.getAbsolutePath());
                            id = 1L;
                            updateDownloaded = false;
                            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                        } else {
                            // failed or paused - stop polling
                            if (progressRunnable != null) {
                                handler.removeCallbacks(progressRunnable);
                            }
                        }
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
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

    public static void trackDownloadProgress(Context context, ButtonWithCounterView progressTextView, TextView progressTextViewInDrawer, RadialProgress2 updateLayoutIcon) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) return;

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                try (Cursor cursor = downloadManager.query(query)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int indexBytesDownloaded = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                        int indexBytesTotal = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                        if (indexBytesDownloaded >= 0 && indexBytesTotal >= 0) {
                            int bytesDownloaded = cursor.getInt(indexBytesDownloaded);
                            int bytesTotal = cursor.getInt(indexBytesTotal);

                            if (bytesTotal > 0) {
                                int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
                                if (progressTextView != null) {
                                    progressTextView.setText(
                                            LocaleController.formatString(org.telegram.messenger.R.string.AppUpdateDownloading, progress),
                                            true
                                    );
                                }
                                if (progressTextViewInDrawer != null && updateLayoutIcon != null) {
                                    progressTextViewInDrawer.setText(
                                            LocaleController.formatString(org.telegram.messenger.R.string.AppUpdateDownloading, progress)
                                    );
                                    updateLayoutIcon.setProgress((float) progress / 100, true);
                                }
                                CherrygramCoreConfig.INSTANCE.setUpdateDownloadingProgress(progress);
                                if (CherrygramCoreConfig.isDevBuild()) FileLog.e("Загрузка: " + progress + "%");
                            }
                        }
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }

                // schedule next poll only if download still active
                handler.postDelayed(this, 50);
            }
        };

        handler.post(progressRunnable);
    }

    public static void cancelDownload(Context context, long downloadId) {
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.remove(downloadId);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }

        if (progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
        }

        CherrygramCoreConfig.INSTANCE.setUpdateIsDownloading(false);
        CherrygramCoreConfig.INSTANCE.setUpdateDownloadingProgress(0f);
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
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
            String[] current = CGResourcesHelper.getCherryVersion().split("\\.");
            String[] latest = version.split("\\.");
            int cmp = compareVersions(current, latest);
            CherrygramCoreConfig.INSTANCE.setUpdateAvailable(cmp < 0);
            return cmp < 0;
        }

        public boolean isForce() {
            return version.toLowerCase().contains("force");
        }
    }

    /**
     * Returns negative if a < b, 0 if equals, positive if a > b
     */
    private static int compareVersions(String[] a, String[] b) {
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            int v1 = i < a.length ? Utilities.parseInt(a[i]) : 0;
            int v2 = i < b.length ? Utilities.parseInt(b[i]) : 0;
            if (v1 != v2) {
                return Integer.compare(v1, v2);
            }
        }
        return 0;
    }

    /*public static String getLastCheckUpdateTime() {
        return getString(R.string.UP_LastCheck) + ": " + LocaleController.formatDateTime(CherrygramCoreConfig.INSTANCE.getLastUpdateCheckTime() / 1000, true);
    }*/

}