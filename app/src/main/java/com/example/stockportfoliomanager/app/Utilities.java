package com.example.stockportfoliomanager.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.example.stockportfoliomanager.app.data.PortContract;
import com.example.stockportfoliomanager.app.data.PortDbHelper;
import com.example.stockportfoliomanager.app.data.PortProvider;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Nish on 17-03-2016.
 */
public class Utilities {

    public static String getPreferencePortId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_port_key), "1");
    }

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    //check given value is integer or not
    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        }
        catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean isValidDate(String date, String dateFormat) {
        try {
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isValidDate(String date) {
        try {
            String dateFormat = "yyyy-MM-dd";
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static Integer getPartFromDate(String strDate, String part) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = format.parse(strDate);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            if (part.equalsIgnoreCase("year") || part.equalsIgnoreCase("yy") || part.equalsIgnoreCase("yyyy")) {
                return cal.get(Calendar.YEAR);
            } else if (part.equalsIgnoreCase("month") || part.equalsIgnoreCase("MM")){
                return cal.get(Calendar.MONTH)+1;
            } else {
                return cal.get(Calendar.DAY_OF_MONTH);
            }
        } catch (ParseException e) {
            return null;
        }
    }

    public static String createDate(int year, int month, int date) {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month-1);
        calendar.set(Calendar.DAY_OF_MONTH, date);

        return sdf.format(calendar.getTime());
    }

    public static void updateInvestmentDate(Calendar calendar, EditText editText) {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editText.setText(sdf.format(calendar.getTime()));
    }

    public static int getPortCountByPortName(Context context, String portName) {
        Cursor c = null;
        PortDbHelper openHelper = null;
        SQLiteDatabase db = null;
        try {

            openHelper = new PortDbHelper(context);
            db = openHelper.getReadableDatabase();
            String query = "SELECT COUNT(*) FROM "+
                    PortContract.PortEntry.TABLE_NAME +
                    " WHERE " + PortContract.PortEntry.COLUMN_PORT_NAME + "= ?";
            c = db.rawQuery(query, new String[] {portName});
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        }
        finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
            if(openHelper != null){
                openHelper.close();
            }
        }

    }

    public static int[] getStockCodesForPortId(Context context, int portId) {
        final String LOG_TAG = "Utilities_GetStockCodes";
        Cursor c = null;
        PortDbHelper openHelper = null;
        SQLiteDatabase db = null;
        try {

            openHelper = new PortDbHelper(context);
            db = openHelper.getReadableDatabase();
            String query = "SELECT DISTINCT " +
                    PortContract.HoldingEntry.COLUMN_COMPANY_CODE +
                    " FROM "+
                    PortContract.HoldingEntry.TABLE_NAME +
                    " WHERE " + PortContract.HoldingEntry.COLUMN_PORT_ID + " = ? ";
            c = db.rawQuery(query, new String[] {Integer.toString(portId)});
            if (null != c) {
                int[] arrStockCodes = new int[c.getCount()];
                int i = 0;
                while(c.moveToNext()){
                    int stockCode = c.getInt(0);
                    Log.d(LOG_TAG, Integer.toString(stockCode));
                    arrStockCodes[i] = stockCode;
                    i++;
                }
                return arrStockCodes;
            }

            int[] arrBlank = new int[0];
            return arrBlank;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            int[] arrBlank = new int[0];
            return arrBlank;
        }
        finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
            if(openHelper != null){
                openHelper.close();
            }
        }
    }

    public static Double[] getRealisedUnrealisedGainLoss(Context context, int holdId, Double currentPrice) {
        final String LOG_TAG = "Utilities_GetRGainLoss";
        Cursor c = null;
        PortDbHelper openHelper = null;
        SQLiteDatabase db = null;

        Double rGainLoss = null;
        Double urGainLoss = null;
        Double[] result = new Double[2];

        try {
            openHelper = new PortDbHelper(context);
            db = openHelper.getReadableDatabase();
            String query = "WITH X AS ( " +
                    " SELECT " + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID +
                    ", " + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    ", 1+(" + PortContract.TransactionEntry.COLUMN_OFFERED + "/" + PortContract.TransactionEntry.COLUMN_HELD + ") AS adj_fact " +
                    " FROM " + PortContract.TransactionEntry.TABLE_NAME +
                    " WHERE " + PortContract.TransactionEntry.COLUMN_HOLDING_ID + " = ? " +
                    " AND " + PortContract.TransactionEntry.COLUMN_TRANSACTION_TYPE + " = 5 " +
                    " ORDER BY " + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    " , " + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID +
                    ") " +
                    ", Y AS (SELECT " +
                    " ( SELECT COUNT(*) + 1 " +
                    " FROM X " +
                    " WHERE " + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    "  < t." + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    " OR (" + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    " = t." + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    " AND " + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID +
                    " < t." + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID + ")" +
                    ") as row_no, " +
                    " * FROM X t " +
                    " ORDER BY " + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    ", " + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID +
                    " ) " +
                    " , Z  AS( " +
                    " SELECT row_no, " +
                    PortContract.TransactionEntry.COLUMN_TRANSACTION_ID +
                    ", " + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    ", adj_fact " +
                    " FROM Y WHERE row_no = 1 " +
                    " UNION ALL " +
                    " SELECT Y.row_no " +
                    ", Y." + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID +
                    ", Y." + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    ", Y.adj_fact * Z.adj_fact " +
                    " FROM Y " +
                    " JOIN Z ON Z.row_no + 1 = Y.row_no " +
                    " ) " +
                    ", adj_factor AS ( " +
                    " SELECT row_no" +
                    ", " + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID +
                    ", " + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    ", adj_fact " +
                    " FROM Z " +
                    " ) " +
                    " SELECT " +
                    PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    ", " + PortContract.TransactionEntry.COLUMN_TRANSACTION_TYPE +
                    ", ABS(" + PortContract.TransactionEntry.COLUMN_UNITS_FLOW + ") * " +
                    " IFNULL((SELECT adj_fact FROM adj_factor ORDER BY row_no DESC limit 1),1) AS adjusted_units " +
                    " , (CASE WHEN IFNULL(" + PortContract.TransactionEntry.COLUMN_BROKERAGE + ", 0) = 0 THEN " +
                    " IFNULL(" + PortContract.TransactionEntry.COLUMN_PRICE + ",0) " +
                    " WHEN " + PortContract.TransactionEntry.COLUMN_TRANSACTION_TYPE + " = 2 THEN " +
                    PortContract.TransactionEntry.COLUMN_PRICE + " - " +
                    "(" + PortContract.TransactionEntry.COLUMN_BROKERAGE + " / 100.0) " +
                    " ELSE " +
                    PortContract.TransactionEntry.COLUMN_PRICE + " + " +
                    "(" + PortContract.TransactionEntry.COLUMN_BROKERAGE + " / 100.0) END)" +
                    " / IFNULL((SELECT adj_fact FROM adj_factor ORDER BY row_no DESC limit 1),1) AS adjusted_price " +
                    " , ABS(" + PortContract.TransactionEntry.COLUMN_CASH_FLOW + ") AS " + PortContract.TransactionEntry.COLUMN_CASH_FLOW +
                    " FROM " + PortContract.TransactionEntry.TABLE_NAME +
                    " WHERE " + PortContract.TransactionEntry.COLUMN_HOLDING_ID + " = ? " +
                    " AND " + PortContract.TransactionEntry.COLUMN_TRANSACTION_TYPE + " != 5 " +
                    " ORDER BY " + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE + " ASC" +
                    ", " + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID + " ASC ";

            c = db.rawQuery(query, new String[] {Integer.toString(holdId), Integer.toString(holdId)});

            Log.d(LOG_TAG, "Hold ID: " + Integer.toString(holdId));
            Log.d(LOG_TAG, "Query: " + query);
            Double dividend = 0.0;
            if (null != c) {
                ArrayList<Double[]> transArray = new ArrayList<Double[]>();
                while(c.moveToNext()){

                    Double transType = c.getDouble(c.getColumnIndex(PortContract.TransactionEntry.COLUMN_TRANSACTION_TYPE));
                    Double adjUnit = c.getDouble(c.getColumnIndex("adjusted_units"));
                    Double adjPrice = c.getDouble(c.getColumnIndex("adjusted_price"));
                    Double cashFlow = c.getDouble(c.getColumnIndex(PortContract.TransactionEntry.COLUMN_CASH_FLOW));

                    if(transType == 3){
                        dividend += cashFlow;
                    }
                    else {
                        Double[] data = new Double[3];
                        data[0] = transType;
                        data[1] = adjUnit;
                        data[2] = adjPrice;
                        transArray.add(data);
                    }

                    Log.d(LOG_TAG, "Getting transactions: " + Double.toString(transType) + " - " + Double.toString(adjUnit) + " - " + Double.toString(adjPrice) + " - " + Double.toString(cashFlow));
                }

                rGainLoss = dividend;
                urGainLoss = 0.0;

                //get realised gain / loss FIFO based
                for(int i = 0; i < transArray.size(); i++ ) {
                    Double[] data = transArray.get(i);
                    if(data[0] == 2) { //sell
                        Double sellUnit = data[1];
                        Double sellPrice = data[2];

                        for(int j = 0; j < i; j ++) {
                            Double[] data2 = transArray.get(j);
                            Double buyUnit = data2[1];
                            Double buyPrice = data2[2];

                            if((data2[0] == 1 || data2[0] == 4) && data2[1] > 0 ) { //buy or bonus
                                if (sellUnit > buyUnit) {
                                    sellUnit = buyUnit - sellUnit;
                                    rGainLoss += ((buyUnit * sellPrice) - (buyUnit * buyPrice));
                                    buyUnit = 0.0;
                                    data2[1] = buyUnit;
                                    transArray.set(j, data2);
                                }
                                else {
                                    sellUnit = 0.0;
                                    buyUnit = buyUnit - sellUnit;
                                    rGainLoss += ((sellUnit * sellPrice) - (sellPrice * buyPrice));
                                    data2[1] = buyUnit;
                                    transArray.set(j, data2);
                                    j = i;
                                }
                            }
                        }
                    }
                }

                //get unrealised gain / loss
                for(int i = 0; i < transArray.size(); i++ ) {
                    Double[] data = transArray.get(i);
                    urGainLoss += ((data[1] * currentPrice) - (data[1] * data[2]));
                }

                if(rGainLoss == 0.0) {
                    rGainLoss = null;
                }
                if(urGainLoss == 0.0) {
                    urGainLoss = null;
                }
                Log.d(LOG_TAG, "RGL: " + rGainLoss + "; URGL: " + urGainLoss);
                result[0] = rGainLoss;
                result[1] = urGainLoss;

                return result;
            }

            result[0] = null;
            result[1] = null;
            return result;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            result[0] = null;
            result[1] = null;
            return result;
        }
        finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
            if(openHelper != null){
                openHelper.close();
            }
        }
    }

    public static String formatNumber(Context context, Double num, boolean prefixSymbol){
        if(null == num){
            return "--";
        }

        DecimalFormat formatter;
        if(prefixSymbol) {
            if(num > 0) {
                formatter = new DecimalFormat(context.getResources().getString(R.string.signed_number_format));
            }
            else {
                formatter = new DecimalFormat(context.getResources().getString(R.string.unsigned_number_format));
            }
        }
        else{
            formatter = new DecimalFormat(context.getResources().getString(R.string.unsigned_number_format));
        }
        return formatter.format(num);
    }

    public static String formatNumber(Context context, Double num){
        if(null == num){
            return "--";
        }

        DecimalFormat formatter = new DecimalFormat(context.getResources().getString(R.string.unsigned_number_format));
        return formatter.format(num);
    }

    public static int removeHolding(Context context, int holdingId) {
        final String LOG_TAG = "Utilities_removeHolding";
        //delete transactions
        Uri uriTransaction = PortContract.TransactionEntry.buildTransactionUri();
        final String selectionTransactionWithHoldingID =
                PortContract.TransactionEntry.TABLE_NAME +
                        "." + PortContract.TransactionEntry.COLUMN_HOLDING_ID + " = ? ";
        String[] selectionArgs = new String[]{Integer.toString(holdingId)};

        int rowsDeleted = context.getContentResolver().delete(uriTransaction, selectionTransactionWithHoldingID, selectionArgs);
        Log.d(LOG_TAG, "Row deleted for transaction: " + rowsDeleted);

        //delete holding
        Uri uriHolding = PortContract.HoldingEntry.buildHoldingUri();
        final String selectionHoldingID =
                PortContract.HoldingEntry.TABLE_NAME +
                        "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID + " = ? ";

        rowsDeleted = context.getContentResolver().delete(uriHolding, selectionHoldingID, selectionArgs);
        Log.d(LOG_TAG, "Row deleted for Holding: " + rowsDeleted);

        return -1;
    }

    public static void removeTransaction(Context context, int transactionId, int holdingId, CoordinatorLayout coordinatorLayout) {
        final String LOG_TAG = "Utilities_removeTrans";
        Cursor c = null;
        PortDbHelper openHelper = null;
        SQLiteDatabase db = null;

        try {
            //delete transactions
            openHelper = new PortDbHelper(context);
            db = openHelper.getWritableDatabase();
            db.beginTransaction();

            final String selectionTransactionWithTransactionID =
                    PortContract.TransactionEntry.TABLE_NAME +
                            "." + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID + " = ? ";
            String[] selectionArgs = new String[]{Integer.toString(transactionId)};

            int rowsDeleted = db.delete(PortContract.TransactionEntry.TABLE_NAME,
                    selectionTransactionWithTransactionID, selectionArgs);
            Log.d(LOG_TAG, "Row deleted for transaction: " + rowsDeleted);

            if (rowsDeleted != 0) {
                if (adjustTransaction(db, c, openHelper, context, coordinatorLayout, holdingId, transactionId) == 1) {
                    Snackbar.make(coordinatorLayout, context.getString(R.string.success_msg_delete_transaction),
                            Snackbar.LENGTH_LONG).show();
                }
            }
            else {
                db.endTransaction();
                Snackbar.make(coordinatorLayout, context.getString(R.string.transaction_delete_error_zero_row),
                        Snackbar.LENGTH_LONG).show();
            }
        }
        finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.endTransaction();
                db.close();
            }
            if(openHelper != null){
                openHelper.close();
            }
        }
    }

    public static int adjustTransaction(SQLiteDatabase db, Cursor c, PortDbHelper openHelper,
                                         Context context, CoordinatorLayout coordinatorLayout,
                                         int holdingId, int transactionId) {
        final String LOG_TAG = "Utilities_adjustTrans";
        Uri uriTransaction = PortContract.TransactionEntry.buildHoldingAllTransactions(holdingId);
        final String selectionTransactionWithTransactionID =
                PortContract.TransactionEntry.TABLE_NAME +
                        "." + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID + " = ? ";
        String[] selectionArgs;

        String query = "SELECT " +
                PortContract.TransactionEntry.COLUMN_TRANSACTION_ID +
                ", " + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                ", " + PortContract.TransactionEntry.COLUMN_UNITS_FLOW +
                ", " + PortContract.TransactionEntry.COLUMN_CASH_FLOW +
                ", " + PortContract.TransactionEntry.COLUMN_OFFERED +
                ", " + PortContract.TransactionEntry.COLUMN_HELD +
                ", " + PortContract.TransactionEntry.COLUMN_TRANSACTION_TYPE +
                ", " + PortContract.TransactionEntry.COLUMN_DIVIDEND_PERCENTAGE +
                    /*
                    ",( SELECT SUM(" + PortContract.TransactionEntry.COLUMN_UNITS_FLOW + ")" +
                    "  FROM " + PortContract.TransactionEntry.TABLE_NAME +
                    "  WHERE " + PortContract.TransactionEntry.COLUMN_HOLDING_ID +
                    "    = t." + PortContract.TransactionEntry.COLUMN_HOLDING_ID +
                    "  AND " + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    "    <= t." + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE +
                    " ) AS balance_unit " +
                    */
                " FROM " +
                PortContract.TransactionEntry.TABLE_NAME + " AS t " +
                " WHERE " + PortContract.TransactionEntry.COLUMN_HOLDING_ID + "= ? " +
                " ORDER BY " +
                PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE + " ASC " +
                ", " + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID + " ASC ";
        c = db.rawQuery(query, new String[]{Integer.toString(holdingId)});

        final int COL_TRANS_ID = 0;
        final int COL_TRANS_DATE = 1;
        final int COL_TRANS_UNIT = 2;
        final int COL_TRANS_CASH_FLOW = 3;
        final int COL_TRANS_OFFERED = 4;
        final int COL_TRANS_HELD = 5;
        final int COL_TRANS_TYPE = 6;
        final int COL_DIVIDEND_PERC = 7;

        if (c.moveToFirst()) {
            //now try to adjust balance unit, if it turning to negative then delete it, else adjust offered and held units
            Double balanceUnit = 0.0;
            int transactionIdFromCursor = 0;
            int transactionType;
            Double offered = 0.0;
            Double held = 0.0;
            Double cashFlow = 0.0;
            Double unit = 0.0;
            Double divPerc = 0.0;

            do {
                transactionIdFromCursor = c.getInt(COL_TRANS_ID);
                transactionType = c.getInt(COL_TRANS_TYPE);
                unit = c.getDouble(COL_TRANS_UNIT);
                cashFlow = c.getDouble(COL_TRANS_CASH_FLOW);

                if (transactionType == 3) { //divided
                    divPerc = c.getDouble(COL_DIVIDEND_PERC);
                    cashFlow = balanceUnit * divPerc / 100.00;

                    ContentValues cv = new ContentValues();
                    cv.put(PortContract.TransactionEntry.COLUMN_CASH_FLOW, cashFlow);

                    db.update(PortContract.TransactionEntry.TABLE_NAME,
                            cv,
                            selectionTransactionWithTransactionID,
                            new String[]{Integer.toString(transactionIdFromCursor)});

                } else if (transactionType == 4 || transactionType == 5) { //bonus or split
                    offered = c.getDouble(COL_TRANS_OFFERED);
                    held = c.getDouble(COL_TRANS_HELD);
                    unit = (balanceUnit * (offered / held));
                    balanceUnit += unit;

                    ContentValues cv = new ContentValues();
                    cv.put(PortContract.TransactionEntry.COLUMN_UNITS_FLOW, unit);

                    db.update(PortContract.TransactionEntry.TABLE_NAME,
                            cv,
                            selectionTransactionWithTransactionID,
                            new String[]{Integer.toString(transactionIdFromCursor)});
                } else { //buy or sell
                    balanceUnit += unit;
                }

                if (balanceUnit < 0) { //balance unit turning negative while adjusting so revert
                    db.endTransaction();
                    Snackbar.make(coordinatorLayout, context.getString(R.string.transaction_delete_error_adjustment_negative_unit),
                            Snackbar.LENGTH_LONG).show();
                    return 0;
                }
            } while (c.moveToNext());

            db.setTransactionSuccessful();
            if (null != context.getContentResolver()) {
                context.getContentResolver().notifyChange(uriTransaction, null);
            }
            return 1;
        } else {
            //no transactions so delete holding too
            Uri uriHolding = PortContract.HoldingEntry.buildHoldingUri();

            final String selectionHoldingID =
                    PortContract.HoldingEntry.TABLE_NAME +
                            "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID + " = ? ";
            selectionArgs = new String[]{Integer.toString(holdingId)};
            int rowsDeleted = db.delete(PortContract.HoldingEntry.TABLE_NAME, selectionHoldingID, selectionArgs);
            Log.d(LOG_TAG, "Row deleted for Holding: " + rowsDeleted);

            if (rowsDeleted != 0) {
                db.setTransactionSuccessful();
                if (null != context.getContentResolver()) {
                    context.getContentResolver().notifyChange(uriTransaction, null);
                }
                return 1;
            } else {
                db.endTransaction();
                Snackbar.make(coordinatorLayout, context.getString(R.string.transaction_delete_error_zero_row),
                        Snackbar.LENGTH_LONG).show();
                return 0;
            }
        }

    }

    public static void clearForm(ViewGroup group) {
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText)view).setText("");
            }
            else if (view instanceof AutoCompleteTextView) {
                ((AutoCompleteTextView)view).setText("");
            }

            if(view instanceof ViewGroup && (((ViewGroup)view).getChildCount() > 0))
                clearForm((ViewGroup)view);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if(view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Record a screen view hit for the visible
     */
    public static void sendScreenImageName(Tracker tracker, String screenName) {
        final String LOG_TAG = "GASendScreenImage";
        Log.i(LOG_TAG, "Setting screen name: " + screenName);
        tracker.setScreenName("Image~" + screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void sendActionEvent(Tracker tracker, String actionName) {
        final String LOG_TAG = "GASendActionEvent";
        Log.i(LOG_TAG, "Sending Action event: " + actionName);
        //event tracking--------------------
        tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction(actionName)
                    .build());
    }
}
