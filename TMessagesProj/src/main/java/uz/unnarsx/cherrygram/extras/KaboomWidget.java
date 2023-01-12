package uz.unnarsx.cherrygram.extras;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class KaboomWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.kaboom_widget);

//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://settings/folders"));
        Intent intent = new Intent(ApplicationLoader.applicationContext, KaboomWidgetActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);

        views.setOnClickPendingIntent(R.id.tvWidget, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}