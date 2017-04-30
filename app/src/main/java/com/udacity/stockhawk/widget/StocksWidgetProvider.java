package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * AppWidgetProvider responsible for updating our widget
 *
 * @author CesarPim
 */

public class StocksWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            // Associating intent to launch main activity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.text_widget_heather, pendingIntent);

            views.setRemoteAdapter(
                    R.id.list_widget_stocks,
                    new Intent(context, StocksWidgetRemoteViewsService.class));

            // TODO: Associate pending intent template
//            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
//                    .addNextIntentWithParentStack(new Intent(context, DetailActivity.class))
//                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//            views.setPendingIntentTemplate(R.id.list_widget_stocks, clickPendingIntentTemplate);

            views.setEmptyView(R.id.list_widget_stocks, R.id.text_widget_empty);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(QuoteSyncJob.ACTION_DATA_UPDATED)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            notifyDataChanged(context, appWidgetManager);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(
            Context context,
            AppWidgetManager appWidgetManager,
            int appWidgetId,
            Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        notifyDataChanged(context, appWidgetManager);
    }

    private void notifyDataChanged(Context context, AppWidgetManager manager) {
        int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, getClass()));
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_widget_stocks);
    }

}
