/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.misc.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class KaboomWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (context == null || appWidgetManager == null || appWidgetIds == null) return;

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        if (context == null || appWidgetManager == null) return;

        updateAppWidget(context, appWidgetManager, appWidgetId);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        ApplicationLoader.postInitApplication();

        if (context == null || appWidgetManager == null) return;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.kaboom_widget);

        Intent intent = new Intent(ApplicationLoader.applicationContext, KaboomWidgetActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                ApplicationLoader.applicationContext,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        views.setOnClickPendingIntent(R.id.tvWidget, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void forceUpdateKaboomWidgets(Context context) {
        if (context == null) return;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ApplicationLoader.applicationContext);
        if (appWidgetManager == null) return;

        ComponentName thisWidget = new ComponentName(context, KaboomWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if (allWidgetIds == null) return;

        for (int appWidgetId : allWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

}