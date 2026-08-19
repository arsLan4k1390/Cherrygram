/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.donates.adsgram;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uz.unnarsx.cherrygram.core.CherrygramLogger;

public class AdsGramApi {

    private static final String BASE_URL = "https://api.adsgram.ai/adv";

    private final Context context;
    private final OkHttpClient client;

    public AdsGramApi(Context context) {
        this.context = context.getApplicationContext();

        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void loadAd(
            String blockId,
            long telegramUserId,
            boolean isPremium,
            int viewportWidth,
            int viewportHeight,
            String realIp,
            String forceLanguage,
            Callback callback
    ) {
        HttpUrl parsedUrl = HttpUrl.parse(BASE_URL);
        if (parsedUrl == null) return;

        String language = !TextUtils.isEmpty(forceLanguage)
                ? forceLanguage
                : LocaleController.getInstance().getCurrentLocaleInfo().shortName;

        HttpUrl url = parsedUrl.newBuilder()
                .addQueryParameter("blockId", blockId)
                .addQueryParameter("tg_id", String.valueOf(telegramUserId))
                .addQueryParameter("language", language)
                .addQueryParameter("is_premium", String.valueOf(isPremium))
                .addQueryParameter("devicebrand", Build.BRAND)
                .addQueryParameter("devicefamily", Build.DEVICE)
                .addQueryParameter("osfamily", "android")
                .addQueryParameter("osversion", Build.VERSION.RELEASE)
                .addQueryParameter("devicemodel", Build.MODEL)
                .addQueryParameter("tg_platform", "android")
                .addQueryParameter("viewportheight", String.valueOf(viewportHeight))
                .addQueryParameter("viewportwidth", String.valueOf(viewportWidth))
                .build();

        String battery = String.valueOf(getBatteryLevel());
        String charging = String.valueOf(isCharging());
        String brightness = String.valueOf(getBrightness());
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String osVersion = "Android " + Build.VERSION.RELEASE;

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-Battery", battery)
                .addHeader("X-Is-Charging", charging)
                .addHeader("X-Brightness", brightness)
                .addHeader("X-Open-Timestamp", timestamp)
                .addHeader("X-Device-OS", osVersion);

        if (!TextUtils.isEmpty(realIp)) {
            requestBuilder.addHeader("X-Real-Ip", realIp);
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(callback);
    }

    private int getBatteryLevel() {
        try {
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } catch (Exception e) {
            return 100;
        }
    }

    private boolean isCharging() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);

            if (batteryStatus == null) {
                return false;
            }

            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        } catch (Exception e) {
            return false;
        }
    }

    private float getBrightness() {
        try {
            int brightness = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS
            );

            return brightness / 255f;
        } catch (Exception e) {
            return 1.0f;
        }
    }

    public static void fetchRealIp(Utilities.Callback<String> callback) {
        try {
            Request request = new Request.Builder()
                    .url("https://api.ipify.org")
                    .get()
                    .build();

            new OkHttpClient()
                    .newCall(request)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            callback.run(null);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            try (response) {
                                if (!response.isSuccessful() || response.body() == null) {
                                    callback.run(null);
                                    return;
                                }

                                String ip = response.body().string().trim();

                                AndroidUtilities.runOnUIThread(() ->
                                        callback.run(ip)
                                );
                            }
                        }
                    });
        } catch (Exception e) {
            CherrygramLogger.e(e);
            callback.run(null);
        }
    }

}