package com.example.stockportfoliomanager.app;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.analytics.Tracker;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StockPageActivity extends AppCompatActivity {

    static String STOCK_CODE = "STOCK_CODE";
    static String STOCK_NAME = "STOCK_NAME";
    private int mStockCode = 0;

    private final String LOG_TAG = TransactionFragment.class.getSimpleName();
    private CoordinatorLayout mCoordinatorLayout;
    private GraphView mGraph;
    private TextView mTvStockName;
    private Context mContext;
    private Toolbar mToolbar;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_page);

        // Obtain the shared Tracker instance.
        StockPortfolioManagerApplication application = (StockPortfolioManagerApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mToolbar = (Toolbar)findViewById(R.id.toolbar_stock_page);
        setSupportActionBar(mToolbar);
        if(null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mContext = StockPageActivity.this;
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        mStockCode = Integer.parseInt(STOCK_CODE);
        Log.d(LOG_TAG, "Stock Code : " + mStockCode);

        mTvStockName = (TextView) findViewById(R.id.stock_name);
        if(null == STOCK_NAME || STOCK_NAME.equalsIgnoreCase("STOCK_NAME") == true) {
            //mTvStockName.setVisibility(View.GONE);
        }
        else{
            mTvStockName.setText(STOCK_NAME);
            setTitle(STOCK_NAME);
        }
        Utilities.sendScreenImageName(mTracker, "stock~"+STOCK_NAME);

        mGraph = (GraphView) findViewById(R.id.graph);

        new GraphStockPriceTask().execute();

    }

    private class GraphStockPriceTask extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = GraphStockPriceTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String priceJsonStr = null;
            try {
                //https://www.valueresearchonline.com/stocks/priceGraph_addSeries_json.asp?code=1764&type=stock&exchange=&stockcode=1&stDate=2016-01-01&stockStPrice=10
                final String PRICE_BASE_URL = "https://www.valueresearchonline.com/stocks/priceGraph_addSeries_json.asp?type=stock&exchange=&stockcode=1&stockStPrice=10";
                final String CODE_PARAM = "code";
                final String START_DATE_PARAM = "stDate";

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -365); //1 year back date
                String startDate = dateFormat.format(cal.getTime());

                Uri builtUri = Uri.parse(PRICE_BASE_URL).buildUpon()
                        .appendQueryParameter(CODE_PARAM, Integer.toString(mStockCode))
                        .appendQueryParameter(START_DATE_PARAM, startDate)
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

                    String stockName = priceJsonArray.getJSONArray(1).getString(0);
                    if(null!=stockName) {
                        mTvStockName.setText(stockName);
                    }

                    JSONArray arr = priceJsonArray.getJSONArray(0);

                    DataPoint[] dataPoint = new DataPoint[arr.length()];
                    Long minDate = new Date().getTime();
                    Long maxDate = new Date().getTime();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONArray point = arr.getJSONArray(i);

                        if(i==0){
                            minDate = point.getLong(0);
                        }
                        else{
                            maxDate = point.getLong(0);
                        }

                        //Date d = new Date(point.getLong(0)*1000L); // *1000 is to convert seconds to milliseconds
                        Date d = new Date(point.getLong(0)); // *1000 is to convert seconds to milliseconds
                        double val = point.getDouble(1);
                        dataPoint[i] = new DataPoint(d, val);
                    }

                    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoint);
                    mGraph.addSeries(series);

                    // set date label formatter
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    mGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(mContext, dateFormat));
                    mGraph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

                    // set manual x bounds to have nice steps
                    mGraph.getViewport().setMinX(minDate);
                    mGraph.getViewport().setMaxX(maxDate);
                    mGraph.getViewport().setXAxisBoundsManual(true);
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }
}

