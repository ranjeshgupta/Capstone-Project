package com.example.stockportfoliomanager.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.stockportfoliomanager.app.data.PortContract;
import com.example.stockportfoliomanager.app.data.PortDbHelper;
import com.example.stockportfoliomanager.app.data.PortProvider;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class AddTransactionActivity extends AppCompatActivity {

    private final String LOG_TAG = AddTransactionActivity.class.getSimpleName();
    static int mHoldingId = 0;
    static int mTransactionId = 0;
    private int mStockCode = 0;
    private int mTransactionType= 1;
    private boolean mIsEditTrans = false;

    private CoordinatorLayout mCoordinatorLayout;
    private Calendar mCalendar = Calendar.getInstance();
    private Context mContext;
    private Activity mActivity;

    private TextView mTvStockName;
    private EditText mEdDate;
    private Spinner mSpinnerType;
    private EditText mEdUnit;
    private EditText mEdBrokerage;
    private EditText mEdPrice;
    private EditText mEdDividend;
    private EditText mEdOffered;
    private EditText mEdHeld;
    private Button mBtnAddEditTransaction;

    private TextView mLblStockName;
    private TextView mLblDate;
    private TextView mLblType;
    private TextView mLblUnit;
    private TextView mLblBrokerage;
    private TextView mLblPrice;
    private TextView mLblDividend;
    private TextView mLblOffered;
    private TextView mLblHeld;
    private SimpleCursorAdapter mAdapter;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Obtain the shared Tracker instance.
        StockPortfolioManagerApplication application = (StockPortfolioManagerApplication) getApplication();
        mTracker = application.getDefaultTracker();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_edit_trans);
        setSupportActionBar(toolbar);
        if(null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mContext = this;
        mActivity = this;

        mTvStockName = (TextView) findViewById(R.id.textViewStockName);
        mEdDate = (EditText) findViewById(R.id.edit_text_date);
        mSpinnerType = (Spinner) findViewById(R.id.spinner_transaction_type);
        mEdUnit = (EditText) findViewById(R.id.edit_text_unit);
        mEdBrokerage = (EditText) findViewById(R.id.edit_text_brokerage);
        mEdPrice = (EditText) findViewById(R.id.edit_text_price);
        mEdDividend = (EditText) findViewById(R.id.edit_text_div_amount);
        mEdOffered = (EditText) findViewById(R.id.edit_text_offered);
        mEdHeld = (EditText) findViewById(R.id.edit_text_held);
        mBtnAddEditTransaction = (Button) findViewById(R.id.btn_add_edit_trans);

        mLblStockName = (TextView) findViewById(R.id.txt_stock_name);
        mLblDate = (TextView) findViewById(R.id.txt_date);
        mLblType = (TextView) findViewById(R.id.txt_transaction_type);
        mLblUnit = (TextView) findViewById(R.id.txt_unit);
        mLblBrokerage = (TextView) findViewById(R.id.txt_brokerage);
        mLblPrice = (TextView) findViewById(R.id.txt_price);
        mLblDividend = (TextView) findViewById(R.id.txt_div_amount);
        mLblOffered = (TextView) findViewById(R.id.txt_offered);
        mLblHeld = (TextView) findViewById(R.id.txt_held);

        Uri uriTransType = PortContract.TransactionTypeEntry.buildTransactionTypeUri();
        Cursor cTransTYpe = this.getContentResolver().query(
                uriTransType,
                null,
                null,
                null,
                null);
        //startManagingCursor(cTransTYpe);
        String[] from = new String[] {PortContract.TransactionTypeEntry.COLUMN_TRANSACTION_DESC};
        int[] to = new int[] { android.R.id.text1 };
        mAdapter = new SimpleCursorAdapter(this, R.layout.spinner_item, cTransTYpe, from, to);
        mAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mSpinnerType.setAdapter(mAdapter);

        mSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                Cursor cursor = (Cursor) mAdapter.getItem(pos);
                if(cursor != null) {
                    int transType = cursor.getInt(cursor.getColumnIndex("_id"));
                    Log.d(LOG_TAG, "trans type ID :" + transType);
                    changeTransType(transType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                changeTransType(1);
            }
        });

        mEdDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(mContext, date, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.show();
            }
        });

        if( mTransactionId != 0 ) {
            setTitle(R.string.title_activity_edit_transaction);
            mBtnAddEditTransaction.setText(R.string.edit_transaction_btn);
            mIsEditTrans = true;

            Uri uriTransaction = PortContract.TransactionEntry.buildTransaction(mTransactionId);
            Cursor cTrans = this.getContentResolver().query(
                    uriTransaction,
                    null,
                    null,
                    null,
                    null);

            if(null != cTrans) {
                Utilities.sendScreenImageName(mTracker, getString(R.string.title_activity_edit_transaction));
                if (cTrans.moveToFirst()) {
                    mStockCode = cTrans.getInt(PortProvider.COL_TRANS_COMPANY_CODE);

                    mTvStockName.setText(cTrans.getString(PortProvider.COL_TRANS_COMPANY_NAME));

                    String transDate = cTrans.getString(PortProvider.COL_TRANS_TRANSACTION_DATE);
                    if(Utilities.isValidDate(transDate)) {
                        mCalendar.set(Calendar.YEAR, Utilities.getPartFromDate(transDate, "yyyy"));
                        mCalendar.set(Calendar.MONTH, Utilities.getPartFromDate(transDate, "MM") - 1);
                        mCalendar.set(Calendar.DAY_OF_MONTH, Utilities.getPartFromDate(transDate, "dd"));

                        Utilities.updateInvestmentDate(mCalendar, mEdDate);
                    }

                    mTransactionType = cTrans.getInt(PortProvider.COL_TRANS_TRANSACTION_TYPE);
                    mSpinnerType.setSelection(mTransactionType - 1);

                    if(mTransactionType == 3) {
                        mEdDividend.setText(cTrans.getString(PortProvider.COL_TRANS_VALUE));
                    }
                    else if(mTransactionType == 4 || mTransactionType == 5) {
                        mEdOffered.setText(cTrans.getString(PortProvider.COL_TRANS_OFFERED));
                        mEdHeld.setText(cTrans.getString(PortProvider.COL_TRANS_HELD));
                    }
                    else {
                        mEdUnit.setText(cTrans.getString(PortProvider.COL_TRANS_UNITS));
                        mEdBrokerage.setText(cTrans.getString(PortProvider.COL_TRANS_BROKERAGE));
                        mEdPrice.setText(cTrans.getString(PortProvider.COL_TRANS_PRICE));
                    }
                }
            }
        }
        else if (mHoldingId != 0 ) {
            setTitle(R.string.title_activity_add_transaction);
            mBtnAddEditTransaction.setText(R.string.add_transaction_btn);
            mIsEditTrans = false;

            Utilities.sendScreenImageName(mTracker, getString(R.string.title_activity_add_transaction));

            Uri uriHolding = PortContract.HoldingEntry.buildHolding(mHoldingId);
            Cursor cHold = this.getContentResolver().query(
                    uriHolding,
                    null,
                    null,
                    null,
                    null);
            if(null != cHold) {
                if (cHold.moveToFirst()) {
                    mStockCode = cHold.getInt(PortProvider.COL_HOLD_COMPANY_CODE);
                    mTvStockName.setText(cHold.getString(PortProvider.COL_HOLD_COMPANY_NAME));
                }
            }
        }

        mBtnAddEditTransaction.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                addEditTransaction(view);
            }
        });

    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Utilities.updateInvestmentDate(mCalendar, mEdDate);
            setPrice();
        }
    };

    private void changeTransType(int transType) {
        if(transType == 0) {
            transType = 1;
        }
        mTransactionType = transType;

        mLblUnit.setVisibility(View.GONE);
        mEdUnit.setVisibility(View.GONE);
        mLblBrokerage.setVisibility(View.GONE);
        mEdBrokerage.setVisibility(View.GONE);
        mLblPrice.setVisibility(View.GONE);
        mEdPrice.setVisibility(View.GONE);
        mLblDividend.setVisibility(View.GONE);
        mEdDividend.setVisibility(View.GONE);
        mLblOffered.setVisibility(View.GONE);
        mEdOffered.setVisibility(View.GONE);
        mLblHeld.setVisibility(View.GONE);
        mEdHeld.setVisibility(View.GONE);

        if (transType == 3) {
            mLblDividend.setVisibility(View.VISIBLE);
            mEdDividend.setVisibility(View.VISIBLE);
        }
        else if (transType == 4 || transType == 5) {
            mLblOffered.setVisibility(View.VISIBLE);
            mEdOffered.setVisibility(View.VISIBLE);
            mLblHeld.setVisibility(View.VISIBLE);
            mEdHeld.setVisibility(View.VISIBLE);
        }
        else {
            mLblUnit.setVisibility(View.VISIBLE);
            mEdUnit.setVisibility(View.VISIBLE);
            mLblBrokerage.setVisibility(View.VISIBLE);
            mEdBrokerage.setVisibility(View.VISIBLE);
            mLblPrice.setVisibility(View.VISIBLE);
            mEdPrice.setVisibility(View.VISIBLE);
        }

    }

    private void setPrice(){
        if(!Utilities.isNetworkAvailable(mContext)){
            return;
        }

        if (!(mTransactionType == 1 || mTransactionType == 2)) {
            return;
        }

        String investDate;

        if(mEdDate.getText().toString().length() > 0) {
            if(Utilities.isValidDate(mEdDate.getText().toString())) {
                investDate = mEdDate.getText().toString();
            }
            else{
                return;
            }
        }
        else{
            return;
        }

        //Snackbar.make(mCoordinatorLayout, stockCode + "; " + investDate, Snackbar.LENGTH_LONG).show();
        new StockPriceTask().execute(Integer.toString(mStockCode), investDate);
    }


    private class StockPriceTask extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = StockPriceTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length < 2) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
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
                        .appendQueryParameter(CODE_PARAM, params[0])
                        .appendQueryParameter(DAY_PARAM, Utilities.getPartFromDate(params[1], "dd").toString())
                        .appendQueryParameter(MONTH_PARAM, Utilities.getPartFromDate(params[1], "MM").toString())
                        .appendQueryParameter(YEAR_PARAM, Utilities.getPartFromDate(params[1], "yyyy").toString())
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

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
                    return null;
                }
                priceJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Price string: " + priceJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.server_error),
                        Snackbar.LENGTH_LONG).show();
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

        @Override
        protected void onPostExecute(String result) {
            try {
                if (null==result){
                    Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.no_data_stock_price),
                            Snackbar.LENGTH_LONG).show();
                    return;
                }
                //Log.v(LOG_TAG, result);

                JSONArray priceJsonArray = new JSONArray(result);
                if(!priceJsonArray.getJSONArray(0).getString(0).equalsIgnoreCase("error")) {
                    Double price = priceJsonArray.getJSONArray(0).getDouble(0);
                    int datePart = priceJsonArray.getJSONArray(1).getInt(0);
                    int monthPart = priceJsonArray.getJSONArray(2).getInt(0);
                    int yearPart = priceJsonArray.getJSONArray(3).getInt(0);

                    //Log.e(LOG_TAG, "price: " + priceJsonArray.getJSONArray(0).getDouble(0));
                    mEdPrice.setText(price.toString());
                    mCalendar.set(Calendar.YEAR, yearPart);
                    mCalendar.set(Calendar.MONTH, monthPart-1);
                    mCalendar.set(Calendar.DAY_OF_MONTH, datePart);

                    Utilities.updateInvestmentDate(mCalendar, mEdDate);
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    public void addEditTransaction(View view) {
        Utilities.hideSoftKeyboard(mActivity);

        if(mEdDate.getText().toString().length() == 0) {
            Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_date),
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        else if(!Utilities.isValidDate(mEdDate.getText().toString())){
            Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_date),
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        String investDate = mEdDate.getText().toString();

        Integer unit = 0;
        Double price = 0.0;
        Double brokerage = 0.0;
        Double dividend = 0.0;
        Integer offered = 0;
        Integer held = 0;

        if (mTransactionType == 3) {
            if(mEdDividend.getText().toString().length() == 0) {
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_dividend),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            else if(!Utilities.isDouble(mEdDividend.getText().toString())){
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_dividend),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            dividend = Double.parseDouble(mEdDividend.getText().toString());
        }
        else if (mTransactionType == 4 || mTransactionType == 5) {
            if(mEdOffered.getText().toString().length() == 0) {
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_offered),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            else if(!Utilities.isInteger(mEdOffered.getText().toString(), 10)){
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_offered),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            offered = Integer.parseInt(mEdOffered.getText().toString());

            if(mEdHeld.getText().toString().length() == 0) {
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_held),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            else if(!Utilities.isInteger(mEdHeld.getText().toString(), 10)){
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_held),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            held = Integer.parseInt(mEdHeld.getText().toString());

        }
        else {
            if (mEdUnit.getText().toString().length() == 0) {
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_unit),
                        Snackbar.LENGTH_LONG).show();
                return;
            } else if (!Utilities.isInteger(mEdUnit.getText().toString(), 10)) {
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_unit),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            unit = Integer.parseInt(mEdUnit.getText().toString());

            if (mEdBrokerage.getText().toString().length() == 0) {
                brokerage = 0.0;
            } else if (!Utilities.isDouble(mEdBrokerage.getText().toString())) {
                brokerage = 0.0;
            } else {
                brokerage = Double.parseDouble(mEdBrokerage.getText().toString());
            }

            if (mEdPrice.getText().toString().length() == 0) {
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_price),
                        Snackbar.LENGTH_LONG).show();
                return;
            } else if (!Utilities.isDouble(mEdPrice.getText().toString())) {
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.invalid_price),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            price = Double.parseDouble(mEdPrice.getText().toString());
        }

        addEditTransaction(investDate, mTransactionType,
                dividend, offered, held, unit, price, brokerage);
    }

    private void addEditTransaction(String transactDate, int transactType,
                                    double div, int offered, int held, int units, double price, double brokerage) {
        long transactionId;

        Double cashFlow;
        Double divPerc = 0.0;
        if (transactType == 3) {
            cashFlow = div;

            Cursor c = null;
            PortDbHelper openHelper = null;
            SQLiteDatabase db = null;

            try {
                //delete transactions
                openHelper = new PortDbHelper(mContext);
                db = openHelper.getReadableDatabase();

                String query = "SELECT SUM(" +
                        PortContract.TransactionEntry.COLUMN_UNITS_FLOW + ") " +
                        " FROM "+
                        PortContract.TransactionEntry.TABLE_NAME +
                        " WHERE " + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE + " <= ? " +
                        " AND " + PortContract.TransactionEntry.COLUMN_HOLDING_ID + " = ? ";
                c = db.rawQuery(query, new String[] {transactDate, Integer.toString(mHoldingId)});
                if (c.moveToFirst()) {
                    Double balanceUnit = c.getDouble(0);
                    divPerc = (div / balanceUnit) * 100.00;

                    Log.e(LOG_TAG, "Dividend - balanceUnit:" + balanceUnit + "; divAmt:" + div + " ; divPerc:" + divPerc);
                }
                else {
                    Snackbar.make(mCoordinatorLayout, mContext.getString(R.string.error_mgs_add_transaction),
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

            } finally {
                if (c != null) {
                    c.close();
                }
                if (db != null) {
                    db.close();
                }
                if (openHelper != null) {
                    openHelper.close();
                }
            }
        }
        else if (transactType == 4 || transactType == 5) {
            cashFlow = 0.0;
        }
        else {
            cashFlow = (units * price) + ((units * price) * brokerage / 100);
        }

        ContentValues transValues = new ContentValues();

        // Then add the data, along with the corresponding name of the data type,
        // so the content provider knows what kind of value is being inserted.
        transValues.put(PortContract.TransactionEntry.COLUMN_HOLDING_ID, Integer.toString(mHoldingId));
        transValues.put(PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE, transactDate);
        transValues.put(PortContract.TransactionEntry.COLUMN_TRANSACTION_TYPE, Integer.toString(transactType));
        transValues.put(PortContract.TransactionEntry.COLUMN_DIVIDEND_PERCENTAGE, Double.toString(divPerc));
        transValues.put(PortContract.TransactionEntry.COLUMN_OFFERED, Integer.toString(offered));
        transValues.put(PortContract.TransactionEntry.COLUMN_HELD, Integer.toString(held));
        transValues.put(PortContract.TransactionEntry.COLUMN_UNITS_FLOW, Integer.toString(units));
        transValues.put(PortContract.TransactionEntry.COLUMN_PRICE, Double.toString(price));
        transValues.put(PortContract.TransactionEntry.COLUMN_CASH_FLOW, Double.toString(cashFlow));
        transValues.put(PortContract.TransactionEntry.COLUMN_BROKERAGE, Double.toString(brokerage));

        Cursor c = null;
        PortDbHelper openHelper = null;
        SQLiteDatabase db = null;

        try {
            //delete transactions
            openHelper = new PortDbHelper(mContext);
            db = openHelper.getWritableDatabase();
            db.beginTransaction();

            if (!mIsEditTrans) { //add transaction
                long _id = db.insert(PortContract.TransactionEntry.TABLE_NAME, null, transValues);
                Log.d(LOG_TAG, "Inserted transaction id : " + _id);

                if (_id != -1) {
                    if (Utilities.adjustTransaction(db, c, openHelper, mContext, mCoordinatorLayout, mHoldingId, (int) _id) == 1) {
                        final Snackbar snackBar = Snackbar.make(mCoordinatorLayout, mContext.getString(R.string.success_msg_add_transaction),
                                Snackbar.LENGTH_INDEFINITE);
                        snackBar.setAction(R.string.return_to_view, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //snackBar.dismiss();
                                Intent intentMain = new Intent(mContext, MainActivity.class);
                                mContext.startActivity(intentMain);
                            }
                        });
                        snackBar.show();

                        Utilities.clearForm((ViewGroup) findViewById(R.id.layout_add_edit_transaction));
                    }
                } else {
                    db.endTransaction();
                    Snackbar.make(mCoordinatorLayout, mContext.getString(R.string.error_mgs_add_transaction),
                            Snackbar.LENGTH_INDEFINITE).show();
                }
            }
            else { //edit transaction

                int rowsUpdated;

                final String selectionTransactionWithTransactionID =
                        PortContract.TransactionEntry.TABLE_NAME +
                                "." + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID + " = ? ";
                String[] selectionArgs = new String[]{Integer.toString(mTransactionId)};

                rowsUpdated = db.update(PortContract.TransactionEntry.TABLE_NAME, transValues,
                        selectionTransactionWithTransactionID, selectionArgs);


                if (rowsUpdated != 0) {
                    if (Utilities.adjustTransaction(db, c, openHelper, mContext, mCoordinatorLayout, mHoldingId, mTransactionId) == 1) {
                        final Snackbar snackBar = Snackbar.make(mCoordinatorLayout, mContext.getString(R.string.success_msg_edit_transaction),
                                Snackbar.LENGTH_INDEFINITE);
                        snackBar.setAction(R.string.return_to_view, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //snackBar.dismiss();
                                Intent intentMain = new Intent(mContext, MainActivity.class);
                                mContext.startActivity(intentMain);
                            }
                        });
                        snackBar.show();

                        //Utilities.clearForm((ViewGroup) findViewById(R.id.layout_add_edit_transaction));
                    }
                } else {
                    db.endTransaction();
                    Snackbar.make(mCoordinatorLayout, mContext.getString(R.string.error_mgs_edit_transaction),
                            Snackbar.LENGTH_INDEFINITE).show();
                }
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
}
