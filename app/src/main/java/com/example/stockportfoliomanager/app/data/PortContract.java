package com.example.stockportfoliomanager.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Nish on 16-03-2016.
 */
public class PortContract {

    public static final String CONTENT_AUTHORITY = "com.example.stockportfoliomanager.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PORT = "portfolio";
    public static final String PATH_STOCK = "stock";
    public static final String PATH_STOCK_PRICE = "stock_price";
    public static final String PATH_TRANSACTION_TYPE = "transaction_type";
    public static final String PATH_HOLDING = "holding";
    public static final String PATH_TRANSACTION = "transactions";

    /* Inner class that defines the table contents of the portfolio table */
    public static final class PortEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PORT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PORT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PORT;

        // Table name
        public static final String TABLE_NAME = "portfolio";

        public static final String COLUMN_PORT_ID = "port_id";
        public static final String COLUMN_PORT_NAME = "port_name";

        public static Uri buildPortUri() {
            return CONTENT_URI;
        }

        public static Uri buildPortUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getPortIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static final class StockEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STOCK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        // Table name
        public static final String TABLE_NAME = "stock";

        public static final String COLUMN_COMPANY_CODE = "company_code";
        public static final String COLUMN_COMPANY_NAME = "company_name";

        public static Uri buildStockUri() {
            return CONTENT_URI;
        }

        public static Uri buildStockUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class StockPriceEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STOCK_PRICE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK_PRICE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK_PRICE;

        // Table name
        public static final String TABLE_NAME = "stock_price";

        public static final String COLUMN_COMPANY_CODE = "company_code";
        public static final String COLUMN_PRICE_DATE = "price_date";
        public static final String COLUMN_PRICE = "price";

        public static Uri buildStockPriceUri() {
            return CONTENT_URI;
        }

        public static Uri buildStockPriceUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class TransactionTypeEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRANSACTION_TYPE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTION_TYPE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTION_TYPE;

        // Table name
        public static final String TABLE_NAME = "transaction_type";

        public static final String COLUMN_TRANSACTION_TYPE = "transaction_type";
        public static final String COLUMN_TRANSACTION_DESC = "transaction_desc";

        public static Uri buildTransactionTypeUri() {
            return CONTENT_URI;
        }

        public static Uri buildTransactionTypeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class HoldingEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_HOLDING).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HOLDING;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HOLDING;

        // Table name
        public static final String TABLE_NAME = "holding";

        public static final String COLUMN_HOLDING_ID = "holding_id";
        public static final String COLUMN_PORT_ID = "port_id";
        public static final String COLUMN_COMPANY_CODE = "company_code";

        public static final String COLUMN_NO_OF_UNITS = "no_of_units";
        public static final String COLUMN_COST_VALUE = "cost_value";

        public static Uri buildHoldingUri() {
            return CONTENT_URI;
        }

        public static Uri buildHoldingUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPortAllHoldings(int port_id) {
            return CONTENT_URI.buildUpon().appendPath("port")
                    .appendPath(Integer.toString(port_id)).build();
        }

        public static Uri buildPortSummary() {
            return CONTENT_URI.buildUpon()
                    .appendPath("summary")
                    .appendPath("port-summary")
                    .build();
        }

        public static Uri buildHolding(int holding_id) {
            return CONTENT_URI.buildUpon().appendPath("holding")
                    .appendPath(Integer.toString(holding_id)).build();
        }

        public static Integer getPortIdFromUri(Uri uri) {
            if (uri.getPathSegments().get(1).equalsIgnoreCase("port")) {
                return Integer.parseInt(uri.getPathSegments().get(2));
            }
            else {
                return 0;
            }
        }

        public static Integer getHoldingIdFromUri(Uri uri) {
            if (uri.getPathSegments().get(1).equalsIgnoreCase("holding")) {
                return Integer.parseInt(uri.getPathSegments().get(2));
            }
            else {
                return 0;
            }
        }
    }

    public static final class TransactionEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRANSACTION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTION;

        // Table name
        public static final String TABLE_NAME = "transactions";

        public static final String COLUMN_TRANSACTION_ID = "transaction_id";
        public static final String COLUMN_HOLDING_ID = "holding_id";
        public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
        public static final String COLUMN_TRANSACTION_TYPE = "transaction_type";
        public static final String COLUMN_DIVIDEND_PERCENTAGE = "dividend_percentage";
        public static final String COLUMN_OFFERED = "offered";
        public static final String COLUMN_HELD = "held";
        public static final String COLUMN_UNITS_FLOW = "units_flow";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_CASH_FLOW = "cash_flow";
        public static final String COLUMN_BROKERAGE = "brokerage";

        public static Uri buildTransactionUri() {
            return CONTENT_URI;
        }

        public static Uri buildTransactionUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPortAllTransactions(int port_id) {
            return CONTENT_URI.buildUpon().appendPath("port")
                    .appendPath(Integer.toString(port_id)).build();
        }

        public static Uri buildHoldingAllTransactions(int holding_id) {
            return CONTENT_URI.buildUpon().appendPath("holding")
                    .appendPath(Integer.toString(holding_id)).build();
        }

        public static Uri buildTransaction(int transaction_id) {
            return CONTENT_URI.buildUpon().appendPath("transactions")
                    .appendPath(Integer.toString(transaction_id)).build();
        }

        public static Integer getPortIdFromUri(Uri uri) {
            if (uri.getPathSegments().get(1).equalsIgnoreCase("port")) {
                return Integer.parseInt(uri.getPathSegments().get(2));
            }
            else {
                return 0;
            }
        }

        public static Integer getHoldingIdFromUri(Uri uri) {
            if (uri.getPathSegments().get(1).equalsIgnoreCase("holding")) {
                return Integer.parseInt(uri.getPathSegments().get(2));
            }
            else {
                return 0;
            }
        }

        public static Integer getTransactionIdFromUri(Uri uri) {
            if (uri.getPathSegments().get(1).equalsIgnoreCase("transactions")) {
                return Integer.parseInt(uri.getPathSegments().get(2));
            }
            else {
                return 0;
            }
        }
    }

}

