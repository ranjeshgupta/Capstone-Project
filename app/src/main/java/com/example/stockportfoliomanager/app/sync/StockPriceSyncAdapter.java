package com.example.stockportfoliomanager.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.stockportfoliomanager.app.MainActivity;
import com.example.stockportfoliomanager.app.R;
import com.example.stockportfoliomanager.app.Utilities;
import com.example.stockportfoliomanager.app.data.PortContract;
import com.example.stockportfoliomanager.app.data.PortProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class StockPriceSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = StockPriceSyncAdapter.class.getSimpleName();
    public static final String ACTION_DATA_UPDATED = "com.example.stockportfoliomanager.ACTION_DATA_UPDATED";
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 6; //every 6 hours
    private static final int PORT_NOTIFICATION_ID = 6005;

    public static int[] STOCK_CODES_SELECTED_PORT_ID;
    private Vector<ContentValues> mCVStockPrice;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PRICE_STATUS_OK, PRICE_STATUS_SERVER_DOWN, PRICE_STATUS_SERVER_INVALID, PRICE_STATUS_UNKNOWN})
    public @interface PriceStatus {}

    public static final int PRICE_STATUS_OK = 0;
    public static final int PRICE_STATUS_SERVER_DOWN = 1;
    public static final int PRICE_STATUS_SERVER_INVALID = 2;
    public static final int PRICE_STATUS_UNKNOWN = 3;

    public StockPriceSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        int portId = Integer.parseInt(Utilities.getPreferencePortId(getContext()));
        Log.d(LOG_TAG, "port id: " + portId);
        STOCK_CODES_SELECTED_PORT_ID = Utilities.getStockCodesForPortId(getContext(), portId);
        mCVStockPrice = new Vector<ContentValues>();

        getStockPrice();
        Log.d(LOG_TAG, "getting stock price done ");

        if(mCVStockPrice.size() > 0) {
            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[mCVStockPrice.size()];
            mCVStockPrice.toArray(insert_data);
            inserted_data = getContext().getContentResolver().bulkInsert(
                    PortContract.StockPriceEntry.buildStockPriceUri(), insert_data);

            Log.v(LOG_TAG, "Successfully Inserted : " + String.valueOf(inserted_data));
            setPriceStatus(getContext(), PRICE_STATUS_OK);

            Uri holdingUri = PortContract.HoldingEntry.buildPortAllHoldings(portId);
            getContext().getContentResolver().notifyChange(holdingUri, null);

            updateWidgets();

            notifyPortSummary();
        }

        Log.d(LOG_TAG, "inserted to db");
    }

    void getStockPrice(){
        for(final int stockCode : STOCK_CODES_SELECTED_PORT_ID) {
            if (stockCode != 0) {
                DateFormat dateFormat = new SimpleDateFormat(getContext().getResources().getString(R.string.date_format));
                Calendar cal = Calendar.getInstance();
                String JSON_price = null;

                for (int i = 0; i <= 9; i++) {
                    if (i >= 1) {
                        cal.add(Calendar.DATE, -1);
                    }
                    String modifiedDate = dateFormat.format(cal.getTime());
                    Log.d(LOG_TAG, "Get json: for stock " + stockCode + " dated " + modifiedDate);
                    JSON_price = getJsonData(stockCode, modifiedDate);

                    if (null != JSON_price) {
                        if (!JSON_price.toLowerCase().contains("error")) {
                            i = 10; //exit loop to donot check price for previous date
                        }
                    }
                }

                // process the returned json data
                if (JSON_price != null) {
                    createCVStockPrice(stockCode, JSON_price);
                } else {
                    Log.d(LOG_TAG, "Failed to load json for code " + stockCode);
                }
            }
        }
    }

    private String getJsonData(int stockCode, String date) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String priceJsonStr = null;
        try {
            //https://www.valueresearchonline.com/port/getentrypriceStock.asp?code=1764&day=18&month=3&year=2016
            final String PRICE_BASE_URL = "https://www.valueresearchonline.com/port/getentrypriceStock.asp?";
            final String CODE_PARAM = "code";
            final String DAY_PARAM = "day";
            final String MONTH_PARAM = "month";
            final String YEAR_PARAM = "year";

            Uri builtUri = Uri.parse(PRICE_BASE_URL).buildUpon()
                    .appendQueryParameter(CODE_PARAM, Integer.toString(stockCode))
                    .appendQueryParameter(DAY_PARAM, Utilities.getPartFromDate(date, "dd").toString())
                    .appendQueryParameter(MONTH_PARAM, Utilities.getPartFromDate(date, "MM").toString())
                    .appendQueryParameter(YEAR_PARAM, Utilities.getPartFromDate(date, "yyyy").toString())
                    .build();

            URL url = new URL(builtUri.toString());
            Log.d(LOG_TAG, "Built URI " + builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                setPriceStatus(getContext(), PRICE_STATUS_SERVER_DOWN);
                return null;
            }
            priceJsonStr = buffer.toString();
            Log.d(LOG_TAG, "Price string: " + priceJsonStr);
        } catch (IOException e) {
            setPriceStatus(getContext(), PRICE_STATUS_UNKNOWN);
            Log.e(LOG_TAG, "Exception here" + e.getMessage());
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return priceJsonStr;
    }

    void createCVStockPrice(int stockCode, String result){
        try {
            if (null==result){
                Log.e(LOG_TAG, "Price JSON is null");
                return;
            }
            Log.v(LOG_TAG, result);

            JSONArray priceJsonArray = new JSONArray(result);
            if(!priceJsonArray.getJSONArray(0).getString(0).equalsIgnoreCase("error")) {
                Double price = priceJsonArray.getJSONArray(0).getDouble(0);
                int datePart = priceJsonArray.getJSONArray(1).getInt(0);
                int monthPart = priceJsonArray.getJSONArray(2).getInt(0);
                int yearPart = priceJsonArray.getJSONArray(3).getInt(0);
                //Log.e(LOG_TAG, "price: " + priceJsonArray.getJSONArray(0).getDouble(0));

                ContentValues cvPrice = new ContentValues();
                cvPrice.put(PortContract.StockPriceEntry.COLUMN_COMPANY_CODE, stockCode);
                cvPrice.put(PortContract.StockPriceEntry.COLUMN_PRICE_DATE, Utilities.createDate(yearPart, monthPart, datePart));
                cvPrice.put(PortContract.StockPriceEntry.COLUMN_PRICE, price);

                mCVStockPrice.add(cvPrice);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        StockPriceSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Sets the location status into shared preference.  This function should not be called from
     * the UI thread because it uses commit to write to the shared preferences.
     * @param c Context to get the PreferenceManager from.
     * @param priceStatus The IntDef value to set
     */
    static private void setPriceStatus(Context c, @PriceStatus int priceStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_price_status_key), priceStatus);
        spe.commit();
    }

    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    private void notifyPortSummary() {
        Log.d(LOG_TAG, "Notification started");
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);

        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
            // Last sync was more than 1 day ago, let's send a notification with the weather.
            Uri uriPortSummary = PortContract.HoldingEntry.buildPortSummary();
            Cursor cursor = context.getContentResolver().query(
                    uriPortSummary,
                    null,
                    null,
                    null,
                    null);

            if(null != cursor) {
                if (cursor.moveToFirst()) {
                    double costValue = cursor.getDouble(PortProvider.COL_PORT_SUMMARY_COST_VALUE);
                    double marketValue = cursor.getDouble(PortProvider.COL_PORT_SUMMARY_MARKET_VALUE);
                    double changes = marketValue - costValue;

                    String strMarketValue = Utilities.formatNumber(context, marketValue, false);
                    String strChanges = Utilities.formatNumber(context, changes, true);

                    Resources resources = context.getResources();
                    int artResourceId = R.mipmap.ic_launcher;

                    // On Honeycomb and higher devices, we can retrieve the size of the large icon
                    // Prior to that, we use a fixed size
                    @SuppressLint("InlinedApi")
                    int largeIconWidth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                            ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
                            : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);
                    @SuppressLint("InlinedApi")
                    int largeIconHeight = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                            ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
                            : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);

                    // Retrieve the large icon
                    Bitmap largeIcon;
                    try {
                        largeIcon = Glide.with(context)
                                .load(artResourceId)
                                .asBitmap()
                                .error(artResourceId)
                                .fitCenter()
                                .into(largeIconWidth, largeIconHeight).get();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(LOG_TAG, "Error retrieving large icon from " + artResourceId, e);
                        largeIcon = BitmapFactory.decodeResource(resources, artResourceId);
                    }
                    String title = context.getString(R.string.app_name);
                    // Define the text.
                    String contentText = String.format(context.getString(R.string.format_notification),
                            strMarketValue,
                            strChanges);

                    //Log.d(LOG_TAG, "Notification app title: " + title);
                    Log.d(LOG_TAG, "Notification content text: " + contentText);

                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setColor(resources.getColor(R.color.colorPrimary))
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(largeIcon)
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(PORT_NOTIFICATION_ID, mBuilder.build());

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
            }
            cursor.close();
        }
        Log.d(LOG_TAG, "Notification end");
    }
}
