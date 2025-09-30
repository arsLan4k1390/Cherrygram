/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class NotificationsService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationLoader.postInitApplication();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM && CherrygramCoreConfig.INSTANCE.getResidentNotification()) {
            NotificationChannelCompat channel = new NotificationChannelCompat.Builder("cgPush", NotificationManagerCompat.IMPORTANCE_DEFAULT)
                    .setName(LocaleController.getString(R.string.CG_PushService))
                    .setLightsEnabled(false)
                    .setVibrationEnabled(false)
                    .setSound(null, null)
                    .build();
            if (CherrygramCoreConfig.INSTANCE.isDevBuild()) Log.d("cgPush", "Starting resident notification...");
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(channel);
            startForeground(1390,
                    new NotificationCompat.Builder(this, "cgPush")
                            .setSmallIcon(CGResourcesHelper.INSTANCE.getResidentNotificationIcon())
                            .setShowWhen(false)
                            .setOngoing(true)
                            .setContentText(LocaleController.getString(R.string.CG_PushService))
                            .setCategory(NotificationCompat.CATEGORY_STATUS)
                            .build());
            if (CherrygramCoreConfig.INSTANCE.isDevBuild()) Log.d("cgPush", "Started foreground");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = MessagesController.getGlobalNotificationsSettings();
        if (preferences.getBoolean("pushService", true)) {
            Intent intent = new Intent("org.telegram.start");
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
        }
    }
}
