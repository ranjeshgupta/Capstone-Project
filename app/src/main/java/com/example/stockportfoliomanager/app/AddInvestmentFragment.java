package com.example.stockportfoliomanager.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.stockportfoliomanager.app.data.PortContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AddInvestmentFragment extends Fragment {
    private final String LOG_TAG = AddInvestmentFragment.class.getSimpleName();
    private Calendar mCalendar = Calendar.getInstance();
    HashMap<String, String> mResponseData =  new HashMap<String, String>();
    ArrayAdapter<String> mAdapter;
    EditText mEdTvStockCode;
    EditText mEdTvInvestDate;
    EditText mEdTvUnit;
    EditText mEdTvPrice;
    Context mContext;
    View mRootView;
    Activity mActivity;

    public AddInvestmentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = getContext();
        mActivity = getActivity();

        mRootView = inflater.inflate(R.layout.fragment_add_investment, container, false);

        mEdTvStockCode = (EditText) mRootView.findViewById(R.id.txt_stock_code);
        mEdTvInvestDate = (EditText) mRootView.findViewById(R.id.edit_text_date);
        mEdTvUnit = (EditText) mRootView.findViewById(R.id.edit_text_unit);
        mEdTvPrice = (EditText) mRootView.findViewById(R.id.edit_text_price);

        final AutoCompleteTextView actvStockName = (AutoCompleteTextView)
                mRootView.findViewById(R.id.autoCompleteTextViewStockName);

        mAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, mResponseData.keySet().toArray(new String[0]));
        mAdapter.setNotifyOnChange(true);
        actvStockName.setThreshold(2);
        actvStockName.setAdapter(mAdapter);

        actvStockName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String val = mAdapter.getItem(position).toString() + "";
                String code = mResponseData.get(val);
                mEdTvStockCode.setText(code);
                setPrice();
            }
        });

        actvStockName.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                //do nothing
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                //do nothing
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                mEdTvStockCode.setText("");

                if (!Utilities.isNetworkAvailable(mContext)){
                    mAdapter.clear();
                    mAdapter.notifyDataSetChanged();
                    Snackbar.make(mRootView, getResources().getString(R.string.no_network),
                            Snackbar.LENGTH_LONG).show();
                }
                else {
                    if (actvStockName.getText().toString().length() >= 3) {
                        new StockSearchTask().execute(actvStockName.getText().toString());
                    } else {
                        if (!mAdapter.isEmpty()) {
                            mAdapter.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });


        mEdTvInvestDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(mContext, date, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.show();
            }
        });

        Button btnAddInvestment = (Button) mRootView.findViewById(R.id.btn_add_inv);
        btnAddInvestment.setOnClickListener(new View.OnClickListener(){
           @Override
            public void onClick(View v) {
               addInvestment(mRootView);
           }
        });

        return mRootView;
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Utilities.updateInvestmentDate(mCalendar, mEdTvInvestDate);
            setPrice();
            mEdTvUnit.requestFocus();
        }
    };

    private void setPrice(){
        if(!Utilities.isNetworkAvailable(mContext)){
            return;
        }

        int stockCode;
        String investDate;
        if(mEdTvStockCode.getText().toString().length() > 0) {
            stockCode = Integer.parseInt(mEdTvStockCode.getText().toString());
        }
        else{
            return;
        }

        if(mEdTvInvestDate.getText().toString().length() > 0) {
            if(Utilities.isValidDate(mEdTvInvestDate.getText().toString())) {
                investDate = mEdTvInvestDate.getText().toString();
            }
            else{
                return;
            }
        }
        else{
            return;
        }

        //Snackbar.make(mRootView, stockCode + "; " + investDate, Snackbar.LENGTH_LONG).show();
        new StockPriceTask().execute(Integer.toString(stockCode), investDate);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        getActivity().setTitle(R.string.title_add_investment);
    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().setTitle(R.string.title_add_investment);
    }

    private class StockSearchTask extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = StockSearchTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String stockJsonStr = null;
            try {
                final String STOCK_BASE_URL = "https://www.valueresearchonline.com/stock_search_json.asp?";
                final String TERM_PARAM = "term";

                Uri builtUri = Uri.parse(STOCK_BASE_URL).buildUpon()
                        .appendQueryParameter(TERM_PARAM, params[0])
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
                stockJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Stock string: " + stockJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                Snackbar.make(mRootView, getResources().getString(R.string.server_error),
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
            return stockJsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (null==result){
                    Snackbar.make(mRootView, getResources().getString(R.string.no_data_stock_name),
                            Snackbar.LENGTH_LONG).show();

                    mAdapter.clear();
                    mAdapter.notifyDataSetChanged();
                    return;
                }

                // These are the names of the JSON objects that need to be extracted.
                final String STOCK_CODE = "id";
                final String STOCK_NAME = "value";
                final String INV_TYPE = "CodeType";

                //Log.e(LOG_TAG, result);

                //JSONObject stockJson = new JSONObject(stockJsonStr);
                JSONArray stockArray = new JSONArray(result);
                Log.v(LOG_TAG, result);

                if(!mResponseData.isEmpty()) {
                    mResponseData.clear();
                }
                for(int i = 1; i < stockArray.length(); i++) {
                    String company_code;
                    String company_name;
                    String inv_type;

                    JSONObject jsonObjStock = stockArray.getJSONObject(i);

                    company_code = jsonObjStock.getString(STOCK_CODE);
                    company_name = jsonObjStock.getString(STOCK_NAME);
                    inv_type = jsonObjStock.getString(INV_TYPE);
                    Log.v(LOG_TAG, company_code + " : " + company_name);

                    //responseList.add(company_name);
                    mResponseData.put(company_name, company_code);
                }

                if(!mAdapter.isEmpty()){
                    mAdapter.clear();
                }
                if(!mResponseData.isEmpty()) {
                    mAdapter.addAll(mResponseData.keySet().toArray(new String[0]));
                }
                mAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
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
                Snackbar.make(mRootView, getResources().getString(R.string.server_error),
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
                    Snackbar.make(mRootView, getResources().getString(R.string.no_data_stock_price),
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
                    mEdTvPrice.setText(price.toString());
                    mCalendar.set(Calendar.YEAR, yearPart);
                    mCalendar.set(Calendar.MONTH, monthPart-1);
                    mCalendar.set(Calendar.DAY_OF_MONTH, datePart);

                    Utilities.updateInvestmentDate(mCalendar, mEdTvInvestDate);
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    public void addInvestment(View view) {
        Utilities.hideSoftKeyboard(mActivity);

        EditText edTvStockCode = (EditText) view.findViewById(R.id.txt_stock_code);
        if(edTvStockCode.getText().toString().length() == 0) {
            Snackbar.make(mRootView, getResources().getString(R.string.invalid_stock_code), Snackbar.LENGTH_LONG).show();
            return;
        }
        else if(!Utilities.isInteger(edTvStockCode.getText().toString(), 10)){
            Snackbar.make(mRootView, getResources().getString(R.string.invalid_stock_code), Snackbar.LENGTH_LONG).show();
            return;
        }
        int stockCode = Integer.parseInt(edTvStockCode.getText().toString());

        AutoCompleteTextView acTvStockName = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextViewStockName);
        if(acTvStockName.getText().toString().length() == 0) {
            Snackbar.make(mRootView, getResources().getString(R.string.invalid_stock_code), Snackbar.LENGTH_LONG).show();
            return;
        }
        String stockName = acTvStockName.getText().toString();

        EditText edTvDate = (EditText) view.findViewById(R.id.edit_text_date);
        if(edTvDate.getText().toString().length() == 0) {
            Snackbar.make(mRootView, getResources().getString(R.string.invalid_date), Snackbar.LENGTH_LONG).show();
            return;
        }
        else if(!Utilities.isValidDate(edTvDate.getText().toString())){
            Snackbar.make(mRootView, getResources().getString(R.string.invalid_date), Snackbar.LENGTH_LONG).show();
            return;
        }
        String investDate = edTvDate.getText().toString();

        EditText edTvUnit = (EditText) view.findViewById(R.id.edit_text_unit);
        if(edTvUnit.getText().toString().length() == 0) {
            Snackbar.make(mRootView, getResources().getString(R.string.invalid_unit), Snackbar.LENGTH_LONG).show();
            return;
        }
        else if(!Utilities.isInteger(edTvUnit.getText().toString(), 10)){
            Snackbar.make(mRootView, getResources().getString(R.string.invalid_unit), Snackbar.LENGTH_LONG).show();
            return;
        }
        int unit = Integer.parseInt(edTvUnit.getText().toString());

        double brokerage;
        EditText edTvBrokerage = (EditText) view.findViewById(R.id.edit_text_brokerage);
        if(edTvBrokerage.getText().toString().length() == 0) {
            brokerage = 0;
        }
        else if(!Utilities.isDouble(edTvBrokerage.getText().toString())){
            brokerage = 0;
        }
        else{
            brokerage = Double.parseDouble(edTvBrokerage.getText().toString());
        }

        EditText edTvPrice = (EditText) view.findViewById(R.id.edit_text_price);
        if(edTvPrice.getText().toString().length() == 0) {
            Snackbar.make(mRootView, getResources().getString(R.string.invalid_price), Snackbar.LENGTH_LONG).show();
            return;
        }
        else if(!Utilities.isDouble(edTvPrice.getText().toString())){
            Snackbar.make(mRootView, getResources().getString(R.string.invalid_price), Snackbar.LENGTH_LONG).show();
            return;
        }
        double price = Double.parseDouble(edTvPrice.getText().toString());

        long stockId = addStock(stockCode, stockName);
        Log.v(LOG_TAG, "stock id: " + stockId);

        int portId = Integer.parseInt(Utilities.getPreferencePortId(mContext));
        Log.v(LOG_TAG, "Port id: " + portId);


        int holdingId = (int) addHolding(portId, stockCode);
        Log.v(LOG_TAG, "Holding Id: " + holdingId);

        long transactionId = addTransaction(holdingId, investDate, 1, 0, 0, 0, unit, price, brokerage);
        Log.v(LOG_TAG, "Transaction Id: " + transactionId);

        if(transactionId != -1){
            //Snackbar.make(mRootView, getResources().getString(R.string.success_msg_add_inv), Snackbar.LENGTH_LONG).show();
            final Snackbar snackBar = Snackbar.make(mRootView, mContext.getString(R.string.success_msg_add_inv),
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

            Utilities.clearForm((ViewGroup) view.findViewById(R.id.layout_add_inv));
        }

    }


    long addStock(int stockCode, String stockName) {
        long stockId;

        // First, check if the stock code exists in the db
        Cursor stockCursor = getContext().getContentResolver().query(
                PortContract.StockEntry.buildStockUri(),
                new String[]{PortContract.StockEntry._ID},
                PortContract.StockEntry.COLUMN_COMPANY_CODE + " = ?",
                new String[]{Integer.toString(stockCode)},
                null);

        if (stockCursor.moveToFirst()) {
            int stockIdIndex = stockCursor.getColumnIndex(PortContract.StockEntry._ID);
            stockId = stockCursor.getLong(stockIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues stockValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            stockValues.put(PortContract.StockEntry.COLUMN_COMPANY_CODE, Integer.toString(stockCode));
            stockValues.put(PortContract.StockEntry.COLUMN_COMPANY_NAME, stockName);

            // Finally, insert stock data into the database.
            Uri insertedUri = getContext().getContentResolver().insert(
                    PortContract.StockEntry.buildStockUri(),
                    stockValues
            );

            // The resulting URI contains the ID for the row.  Extract the stockId from the Uri.
            stockId = ContentUris.parseId(insertedUri);
        }
        stockCursor.close();
        return stockId;
    }

    long addHolding(int portId, int stockCode) {
        long holdingId;

        ContentValues holdValues = new ContentValues();

        // Then add the data, along with the corresponding name of the data type,
        // so the content provider knows what kind of value is being inserted.
        holdValues.put(PortContract.HoldingEntry.COLUMN_PORT_ID, Integer.toString(portId));
        holdValues.put(PortContract.HoldingEntry.COLUMN_COMPANY_CODE, Integer.toString(stockCode));

        // Finally, insert stock data into the database.
        Uri insertedUri = getContext().getContentResolver().insert(
                PortContract.HoldingEntry.buildHoldingUri(),
                holdValues
        );

        // The resulting URI contains the ID for the row.  Extract the stockId from the Uri.
        holdingId = ContentUris.parseId(insertedUri);

        return holdingId;
    }

    long addTransaction(int holdingId, String transactDate, int transactType,
                        double div, int offered, int held, int units, double price, double brokerage) {
        long transactionId;

        ContentValues transValues = new ContentValues();

        // Then add the data, along with the corresponding name of the data type,
        // so the content provider knows what kind of value is being inserted.
        transValues.put(PortContract.TransactionEntry.COLUMN_HOLDING_ID, Integer.toString(holdingId));
        transValues.put(PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE, transactDate);
        transValues.put(PortContract.TransactionEntry.COLUMN_TRANSACTION_TYPE, Integer.toString(transactType));
        transValues.put(PortContract.TransactionEntry.COLUMN_DIVIDEND_PERCENTAGE, Double.toString(div));
        transValues.put(PortContract.TransactionEntry.COLUMN_OFFERED, Integer.toString(offered));
        transValues.put(PortContract.TransactionEntry.COLUMN_HELD, Integer.toString(held));
        transValues.put(PortContract.TransactionEntry.COLUMN_UNITS_FLOW, Integer.toString(units));
        transValues.put(PortContract.TransactionEntry.COLUMN_PRICE, Double.toString(price));
        transValues.put(PortContract.TransactionEntry.COLUMN_CASH_FLOW, Double.toString(((units * price) + ((units * price) * brokerage / 100))));
        transValues.put(PortContract.TransactionEntry.COLUMN_BROKERAGE, Double.toString(brokerage));

        // Finally, insert stock data into the database.
        Uri insertedUri = getContext().getContentResolver().insert(
                PortContract.TransactionEntry.buildTransactionUri(),
                transValues
        );

        // The resulting URI contains the ID for the row.  Extract the stockId from the Uri.
        transactionId = ContentUris.parseId(insertedUri);

        return transactionId;
    }

}