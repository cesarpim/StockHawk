package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by CesarPim on 28-04-2017.
 */

public class StocksWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            // Associating intent to launch main activity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.text_widget_title, pendingIntent);

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
        // TODO: replace "bla" with the proper action constant name (defined in QuoteSyncJob ?)
//        cesarpim: do you know where we're supposed to broadcast an action to update our widget?
//        is it in the QuoteSyncJob? where?
//        gabor: In the original code it is in the QuoteSyncJob
//        But I moved it into the Provider. That way the widget can be updated even when you delete something from it.
        if (intent.getAction().equals("bla")) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds =
                    appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_widget_stocks);
        }
    }
}
