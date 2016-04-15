package com.example.stockportfoliomanager.app.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.stockportfoliomanager.app.MainActivity;
import com.example.stockportfoliomanager.app.R;
import com.example.stockportfoliomanager.app.Utilities;
import com.example.stockportfoliomanager.app.data.PortContract;
import com.example.stockportfoliomanager.app.data.PortProvider;

/**
 * Created by Nish on 13-04-2015.
 */
public class PortSummaryWidgetIntentService extends IntentService {
    public static final String LOG_TAG = PortSummaryWidgetIntentService.class.getSimpleName();

    public PortSummaryWidgetIntentService() {
        super("PortSummaryWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "widget handler start");
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                PortSummaryWidgetProvider.class));

        Uri uriPortSummary = PortContract.HoldingEntry.buildPortSummary();
        Cursor cursor = getContentResolver().query(
                uriPortSummary,
                null,
                null,
                null,
                null);

        if (cursor == null) {
            Log.w(LOG_TAG, "widget cursor is null");
            return;
        } else if (!cursor.moveToFirst()) {
            Log.w(LOG_TAG, "widget unable to cursor to move first");
            cursor.close();
            return;
        }

        Log.v(LOG_TAG, "widget cursor created");

        double costValue = cursor.getDouble(PortProvider.COL_PORT_SUMMARY_COST_VALUE);
        double marketValue = cursor.getDouble(PortProvider.COL_PORT_SUMMARY_MARKET_VALUE);
        double changes = marketValue - costValue;

        for (int appWidgetId : appWidgetIds) {
            Log.v(LOG_TAG, "widget app " + appWidgetId + " started");

            int layoutId = R.layout.widget_port_summary;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            views.setViewVisibility(R.id.widget_empty, View.GONE);

            /*views.setTextViewText(R.id.widget_score_textview, scores);
            views.setTextViewText(R.id.widget_data_textview, matchDateTime);
            */

            String strMarketValue = Utilities.formatNumber(this, marketValue, false);
            views.setTextViewText(R.id.widget_text_port_summary_value, strMarketValue);

            String strChanges = Utilities.formatNumber(this, changes, true);
            views.setTextViewText(R.id.widget_text_port_summary_changes, strChanges);
            if (changes > 0) {
                views.setTextColor(R.id.widget_text_port_summary_changes, getResources().getColor(R.color.colorGreen));
            }
            else if (changes < 0){
                views.setTextColor(R.id.widget_text_port_summary_changes, getResources().getColor(R.color.colorRed));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, R.id.widget_text_port_summary_value, strMarketValue);
                setRemoteContentDescription(views, R.id.widget_text_port_summary_changes, strChanges);
            }

            // Create an Intent to launch MainActivity

            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.v(LOG_TAG, "widget app " + appWidgetId + " created");
        }
        cursor.close();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, int viewId, String description) {
        views.setContentDescription(viewId, description);
    }
}
