/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import uz.unnarsx.cherrygram.CGFeatureHooks;

public class NotificationsService extends Service {

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationLoader.postInitApplication();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelCompat channel = new NotificationChannelCompat.Builder("cherrygram", NotificationManagerCompat.IMPORTANCE_DEFAULT)
                    .setName(LocaleController.getString("CG_PushService", R.string.CG_PushService))
                    .setLightsEnabled(false)
                    .setVibrationEnabled(false)
                    .setSound(null, null)
                    .build();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(channel);
            Intent explainIntent = new Intent("android.intent.action.VIEW");
            explainIntent.setData(Uri.parse("tg://settings/notifications"));
            PendingIntent explainPendingIntent = PendingIntent.getActivity(this, 0, explainIntent, PendingIntent.FLAG_MUTABLE);
            startForeground(1390,
                    new NotificationCompat.Builder(this, "cherrygram")
                            .setContentIntent(explainPendingIntent)
                            .setShowWhen(false)
                            .setOngoing(true)
                            .setSmallIcon(CGFeatureHooks.getProperNotificationIcon())
                            .setContentText(LocaleController.getString("CG_PushService", R.string.CG_PushService))
                            .setCategory(NotificationCompat.CATEGORY_STATUS)
                            .build());
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
            sendBroadcast(intent);
        }
    }
}
