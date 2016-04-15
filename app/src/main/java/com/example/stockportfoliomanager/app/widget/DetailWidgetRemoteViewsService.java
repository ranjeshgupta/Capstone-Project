package com.example.stockportfoliomanager.app.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.stockportfoliomanager.app.R;
import com.example.stockportfoliomanager.app.Utilities;
import com.example.stockportfoliomanager.app.data.PortContract;
import com.example.stockportfoliomanager.app.data.PortProvider;

/**
 * Created by Nish on 25-12-2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public static final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor cursor = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) {
                    cursor.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                int portId = Integer.parseInt(Utilities.getPreferencePortId(getApplicationContext()));
                Uri holdingUri = PortContract.HoldingEntry.buildPortAllHoldings(portId);

                cursor = getContentResolver().query(
                        holdingUri,
                        null,
                        null,
                        null,
                        null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                Log.v(LOG_TAG, "cursor created");

                Context context = getApplicationContext();

                views.setTextViewText(R.id.stock_name, cursor.getString(PortProvider.COL_HOLD_COMPANY_NAME));

                double unit = cursor.getDouble(PortProvider.COL_HOLD_UNITS);
                double price = cursor.getDouble(PortProvider.COL_HOLD_PRICE);
                double marketValue = unit * price;
                double costValue = cursor.getDouble(PortProvider.COL_HOLD_COST_VALUE);
                double changes = marketValue - costValue;

                if (changes > 0) {
                    views.setTextColor(R.id.value_changes, context.getResources().getColor(R.color.colorGreen));
                }
                else if (changes < 0){
                    views.setTextColor(R.id.value_changes, context.getResources().getColor(R.color.colorRed));
                }
                views.setTextViewText(R.id.value_changes, Utilities.formatNumber(context, changes, true));

                String mkValueString = context.getString(R.string.currency_symbol) + Utilities.formatNumber(context, marketValue);
                views.setTextViewText(R.id.hold_value, mkValueString);

                String unitString = Utilities.formatNumber(context, unit) + context.getString(R.string.unit_symbol);
                views.setTextViewText(R.id.unit, unitString);

                String priceString = context.getString(R.string.price_prefix) + context.getString(R.string.currency_symbol)
                        + Utilities.formatNumber(context, price);
                views.setTextViewText(R.id.current_price, priceString);

                String priceDate = context.getString(R.string.date_prefix) + cursor.getString(PortProvider.COL_HOLD_PRICE_DATE);
                views.setTextViewText(R.id.price_date, priceDate);

                final Intent fillInIntent = new Intent();
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
