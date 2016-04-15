package com.example.stockportfoliomanager.app.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.stockportfoliomanager.app.sync.StockPriceSyncAdapter;


/**
 * Created by Nish on 13-04-2015.
 */
public class PortSummaryWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, PortSummaryWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, PortSummaryWidgetIntentService.class));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (StockPriceSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, PortSummaryWidgetIntentService.class));
        }
    }
}
