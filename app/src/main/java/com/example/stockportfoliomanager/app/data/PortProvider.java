package com.example.stockportfoliomanager.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.example.stockportfoliomanager.app.Utilities;

public class PortProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PortDbHelper mOpenHelper;

    static final int PORT = 100;
    static final int STOCK = 200;
    static final int STOCK_PRICE = 300;
    static final int TRANSACTION_TYPE = 400;
    static final int HOLDING = 500;
    static final int HOLDINGS_WITH_PORT_ID = 501;
    static final int HOLDING_WITH_ID = 502;
    static final int PORT_SUMMARY = 503;
    static final int TRANSACTIONS = 600;
    static final int TRANSACTIONS_WITH_PORT_ID = 601;
    static final int TRANSACTIONS_WITH_HOLDING_ID = 602;
    static final int TRANSACTIONS_WITH_ID = 603;

    //Port Name----------------------------
    private static final SQLiteQueryBuilder PORT_QUERY_BUILDER;
    static{
        PORT_QUERY_BUILDER = new SQLiteQueryBuilder();
        PORT_QUERY_BUILDER.setTables(PortContract.PortEntry.TABLE_NAME);
    }
    private Cursor getPortName(
            Uri uri, String[] projection, String sortOrder) {

        return PORT_QUERY_BUILDER.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    //Transaction Type----------------------------
    private static final SQLiteQueryBuilder TRANSACTION_TYPE_QUERY_BUILDER;
    static {
        TRANSACTION_TYPE_QUERY_BUILDER = new SQLiteQueryBuilder();
        TRANSACTION_TYPE_QUERY_BUILDER.setTables(PortContract.TransactionTypeEntry.TABLE_NAME);
    }
    private Cursor getTransactionType(Uri uri) {
        String[] sTransactionTypeColumn = {
                PortContract.TransactionTypeEntry.TABLE_NAME +
                        "." + PortContract.TransactionTypeEntry.COLUMN_TRANSACTION_TYPE + " AS _id",
                PortContract.TransactionTypeEntry.TABLE_NAME +
                        "." + PortContract.TransactionTypeEntry.COLUMN_TRANSACTION_DESC
        };
        String sSortOrder = PortContract.TransactionTypeEntry.TABLE_NAME +
                "." + PortContract.TransactionTypeEntry.COLUMN_TRANSACTION_TYPE + " ASC ";

        return TRANSACTION_TYPE_QUERY_BUILDER.query(mOpenHelper.getReadableDatabase(),
                sTransactionTypeColumn,
                null,
                null,
                null,
                null,
                sSortOrder
        );
    }

    //Holdings Details --------------------------------------------------------------------------------------
    private static final String HOLDING_COLUMN_QUERY = " SELECT " +
            PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_PORT_ID +
            ", " + PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID +
            " , " + PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_COMPANY_CODE +
            " , " + PortContract.StockEntry.TABLE_NAME + "." + PortContract.StockEntry.COLUMN_COMPANY_NAME +
            " , " + PortContract.StockPriceEntry.TABLE_NAME + "." + PortContract.StockPriceEntry.COLUMN_PRICE_DATE +
            " , " + PortContract.StockPriceEntry.TABLE_NAME + "." + PortContract.StockPriceEntry.COLUMN_PRICE +
            " , " + "(SELECT " +
                    " SUM(" + PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_UNITS_FLOW + " ) " +
                    " FROM " + PortContract.TransactionEntry.TABLE_NAME +
                    " WHERE " + PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_HOLDING_ID +
                    " = " + PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID +
                    ") AS " + PortContract.HoldingEntry.COLUMN_NO_OF_UNITS +
            " , " + "(SELECT " +
                    " SUM(" + PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_CASH_FLOW + " ) " +
                    " FROM " + PortContract.TransactionEntry.TABLE_NAME +
                    " WHERE " + PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_HOLDING_ID +
                    " = " + PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID +
                    ") AS " + PortContract.HoldingEntry.COLUMN_COST_VALUE;

    public static final int COL_HOLD_PORT_ID = 0;
    public static final int COL_HOLD_HOLDING_ID = 1;
    public static final int COL_HOLD_COMPANY_CODE = 2;
    public static final int COL_HOLD_COMPANY_NAME = 3;
    public static final int COL_HOLD_PRICE_DATE = 4;
    public static final int COL_HOLD_PRICE = 5;
    public static final int COL_HOLD_UNITS = 6;
    public static final int COL_HOLD_COST_VALUE = 7;

    private static final String HOLDING_TABLE_QUERY =
            " FROM " + PortContract.HoldingEntry.TABLE_NAME +
                " INNER JOIN " +
                    PortContract.StockEntry.TABLE_NAME +
                        " ON " + PortContract.HoldingEntry.TABLE_NAME +
                        "." + PortContract.HoldingEntry.COLUMN_COMPANY_CODE +
                        " = " + PortContract.StockEntry.TABLE_NAME +
                        "." + PortContract.StockEntry.COLUMN_COMPANY_CODE +
                " LEFT JOIN " +
                    PortContract.StockPriceEntry.TABLE_NAME +
                        " ON " + PortContract.HoldingEntry.TABLE_NAME +
                        "." + PortContract.HoldingEntry.COLUMN_COMPANY_CODE +
                        " = " + PortContract.StockPriceEntry.TABLE_NAME +
                        "." + PortContract.StockPriceEntry.COLUMN_COMPANY_CODE;

    private static final String HOLDING_SORT_BY_QUERY = " ORDER BY " +
                PortContract.StockEntry.TABLE_NAME + "." + PortContract.StockEntry.COLUMN_COMPANY_NAME + " ASC, " +
                PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID + " ASC ";

    //Holding with PortId----------------------------
    private static final String HOLDING_WITH_PORT_ID_SELECTION =
            PortContract.HoldingEntry.TABLE_NAME +
                    "." + PortContract.HoldingEntry.COLUMN_PORT_ID + " = ? ";
    private Cursor getHoldingWithPortId(Uri uri) {
        String portId = Integer.toString(PortContract.HoldingEntry.getPortIdFromUri(uri));
        String[] selectionArgs = new String[]{portId};

        String sqlQry = HOLDING_COLUMN_QUERY +
                HOLDING_TABLE_QUERY +
                " WHERE " + HOLDING_WITH_PORT_ID_SELECTION +
                HOLDING_SORT_BY_QUERY;

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.rawQuery(sqlQry, selectionArgs);
    }

    //Port Summary---------------
    private static final String PORT_SUMMARY_COLUMN_QUERY = " SELECT " +
            PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_PORT_ID +
            ", SUM(" + PortContract.StockPriceEntry.TABLE_NAME + "." + PortContract.StockPriceEntry.COLUMN_PRICE +
            " * " + "(SELECT " +
            " SUM(" + PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_UNITS_FLOW + " ) " +
            " FROM " + PortContract.TransactionEntry.TABLE_NAME +
            " WHERE " + PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_HOLDING_ID +
            " = " + PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID +
            ")) AS MarketValue "+
            " , SUM(" + "(SELECT " +
            " SUM(" + PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_CASH_FLOW + " ) " +
            " FROM " + PortContract.TransactionEntry.TABLE_NAME +
            " WHERE " + PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_HOLDING_ID +
            " = " + PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID +
            ")) AS " + PortContract.HoldingEntry.COLUMN_COST_VALUE;
    public static final String PORT_SUMMARY_GROUP_BY = " GROUP BY " +
            PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_PORT_ID;
    public static final int COL_PORT_SUMMARY_PORT_ID = 0;
    public static final int COL_PORT_SUMMARY_MARKET_VALUE = 1;
    public static final int COL_PORT_SUMMARY_COST_VALUE = 2;

    private Cursor getPortSummary(Uri uri) {
        String portId = Utilities.getPreferencePortId(getContext());
        String[] selectionArgs = new String[]{portId};

        String sqlQry = PORT_SUMMARY_COLUMN_QUERY +
                HOLDING_TABLE_QUERY +
                " WHERE " + HOLDING_WITH_PORT_ID_SELECTION +
                PORT_SUMMARY_GROUP_BY;

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.rawQuery(sqlQry, selectionArgs);
    }

    //Holding with HoldingId----------------------------
    private static final String HOLDING_WITH_HOLDING_ID_SELECTION =
            PortContract.HoldingEntry.TABLE_NAME +
                    "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID + " = ? ";
    private Cursor getHoldingWithHoldingId(Uri uri) {
        String holdingId = Integer.toString(PortContract.HoldingEntry.getHoldingIdFromUri(uri));
        String[] selectionArgs = new String[]{holdingId};

        String sqlQry = HOLDING_COLUMN_QUERY +
                HOLDING_TABLE_QUERY +
                " WHERE " + HOLDING_WITH_HOLDING_ID_SELECTION +
                HOLDING_SORT_BY_QUERY;

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.rawQuery(sqlQry, selectionArgs);
    }

    //Holding Transactions details-----------------------------------------------------------------------------------------
    private static final SQLiteQueryBuilder TRANSACTIONS_QUERY_BUILDER;
    static {
        TRANSACTIONS_QUERY_BUILDER = new SQLiteQueryBuilder();
        TRANSACTIONS_QUERY_BUILDER.setTables(
                PortContract.TransactionEntry.TABLE_NAME +
                        " INNER JOIN " +
                            PortContract.HoldingEntry.TABLE_NAME +
                            " ON " + PortContract.TransactionEntry.TABLE_NAME +
                            "." + PortContract.TransactionEntry.COLUMN_HOLDING_ID +
                            " = " + PortContract.HoldingEntry.TABLE_NAME +
                            "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID +
                        " INNER JOIN " +
                            PortContract.StockEntry.TABLE_NAME +
                            " ON " + PortContract.HoldingEntry.TABLE_NAME +
                            "." + PortContract.HoldingEntry.COLUMN_COMPANY_CODE +
                            " = " + PortContract.StockEntry.TABLE_NAME +
                            "." + PortContract.StockEntry.COLUMN_COMPANY_CODE +
                        " INNER JOIN " +
                            PortContract.TransactionTypeEntry.TABLE_NAME +
                            " ON " + PortContract.TransactionEntry.TABLE_NAME +
                            "." + PortContract.TransactionEntry.COLUMN_TRANSACTION_TYPE +
                            " = " + PortContract.TransactionTypeEntry.TABLE_NAME +
                            "." + PortContract.TransactionTypeEntry.COLUMN_TRANSACTION_TYPE
        );
    }

    private static final String[] TRANSACTIONS_COLUMNS = {
            PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_PORT_ID,
            PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID,
            PortContract.HoldingEntry.TABLE_NAME + "." + PortContract.HoldingEntry.COLUMN_COMPANY_CODE,
            PortContract.StockEntry.TABLE_NAME + "." + PortContract.StockEntry.COLUMN_COMPANY_NAME,
            PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID,
            PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE,
            PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_TRANSACTION_TYPE,
            PortContract.TransactionTypeEntry.TABLE_NAME + "." + PortContract.TransactionTypeEntry.COLUMN_TRANSACTION_DESC,
            PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_DIVIDEND_PERCENTAGE,
            PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_OFFERED,
            PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_HELD,
            PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_UNITS_FLOW,
            PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_PRICE,
            PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_CASH_FLOW,
            PortContract.TransactionEntry.TABLE_NAME + "." + PortContract.TransactionEntry.COLUMN_BROKERAGE
    };
    public static final int COL_TRANS_PORT_ID = 0;
    public static final int COL_TRANS_HOLDING_ID = 1;
    public static final int COL_TRANS_COMPANY_CODE = 2;
    public static final int COL_TRANS_COMPANY_NAME = 3;
    public static final int COL_TRANS_TRANSACTION_ID =4;
    public static final int COL_TRANS_TRANSACTION_DATE = 5;
    public static final int COL_TRANS_TRANSACTION_TYPE = 6;
    public static final int COL_TRANS_TRANSACTION_DESC = 7;
    public static final int COL_TRANS_DIVIDEND_PERC = 8;
    public static final int COL_TRANS_OFFERED = 9;
    public static final int COL_TRANS_HELD = 10;
    public static final int COL_TRANS_UNITS = 11;
    public static final int COL_TRANS_PRICE = 12;
    public static final int COL_TRANS_VALUE = 13;
    public static final int COL_TRANS_BROKERAGE = 14;

    private static final String TRANSACTIONS_SORT_ORDER =
            PortContract.StockEntry.TABLE_NAME +
                    "." + PortContract.StockEntry.COLUMN_COMPANY_NAME + " ASC, " +
            PortContract.TransactionEntry.TABLE_NAME +
                    "." + PortContract.TransactionEntry.COLUMN_TRANSACTION_DATE + " DESC, " +
            PortContract.TransactionEntry.TABLE_NAME +
                    "." + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID + " DESC "
            ;

    //transactions by port id----------------------------
    private static final String TRANSACTIONS_WITH_PORT_ID_SELECTION =
            PortContract.HoldingEntry.TABLE_NAME+
                    "." + PortContract.HoldingEntry.COLUMN_PORT_ID + " = ? ";
    private Cursor getTransactionsWithPortId(Uri uri) {
        String portId = Integer.toString(PortContract.TransactionEntry.getPortIdFromUri(uri));
        String[] selectionArgs = new String[]{portId};
        String selection = TRANSACTIONS_WITH_PORT_ID_SELECTION;

        return TRANSACTIONS_QUERY_BUILDER.query(mOpenHelper.getReadableDatabase(),
                TRANSACTIONS_COLUMNS,
                selection,
                selectionArgs,
                null,
                null,
                TRANSACTIONS_SORT_ORDER
        );
    }

    //transactions by holding id----------------------------
    private static final String TRANSACTIONS_WITH_HOLDING_ID_SELECTION =
            PortContract.HoldingEntry.TABLE_NAME+
                    "." + PortContract.HoldingEntry.COLUMN_HOLDING_ID + " = ? ";
    private Cursor getTransactionsWithHoldingId(Uri uri) {
        String holdingId = Integer.toString(PortContract.TransactionEntry.getHoldingIdFromUri(uri));
        String[] selectionArgs = new String[]{holdingId};
        String selection = TRANSACTIONS_WITH_HOLDING_ID_SELECTION;

        return TRANSACTIONS_QUERY_BUILDER.query(mOpenHelper.getReadableDatabase(),
                TRANSACTIONS_COLUMNS,
                selection,
                selectionArgs,
                null,
                null,
                TRANSACTIONS_SORT_ORDER
        );
    }

    //transactions by transaction id----------------------------
    private static final String TRANSACTIONS_WITH_TRANSACTION_ID_SELECTION =
            PortContract.TransactionEntry.TABLE_NAME+
                    "." + PortContract.TransactionEntry.COLUMN_TRANSACTION_ID + " = ? ";
    private Cursor getTransactionsWithTransactionId(Uri uri) {
        String transactionId = Integer.toString(PortContract.TransactionEntry.getTransactionIdFromUri(uri));
        String[] selectionArgs = new String[]{transactionId};
        String selection = TRANSACTIONS_WITH_TRANSACTION_ID_SELECTION;

        return TRANSACTIONS_QUERY_BUILDER.query(mOpenHelper.getReadableDatabase(),
                TRANSACTIONS_COLUMNS,
                selection,
                selectionArgs,
                null,
                null,
                TRANSACTIONS_SORT_ORDER
        );
    }


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PortContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, PortContract.PATH_PORT, PORT);

        matcher.addURI(authority, PortContract.PATH_STOCK, STOCK);
        matcher.addURI(authority, PortContract.PATH_STOCK_PRICE, STOCK_PRICE);
        matcher.addURI(authority, PortContract.PATH_TRANSACTION_TYPE, TRANSACTION_TYPE);

        matcher.addURI(authority, PortContract.PATH_HOLDING, HOLDING);
        matcher.addURI(authority, PortContract.PATH_HOLDING + "/port/#", HOLDINGS_WITH_PORT_ID);
        matcher.addURI(authority, PortContract.PATH_HOLDING + "/holding/#", HOLDING_WITH_ID);
        matcher.addURI(authority, PortContract.PATH_HOLDING + "/summary/*", PORT_SUMMARY);

        matcher.addURI(authority, PortContract.PATH_TRANSACTION, TRANSACTIONS);
        matcher.addURI(authority, PortContract.PATH_TRANSACTION+ "/port/#", TRANSACTIONS_WITH_PORT_ID);
        matcher.addURI(authority, PortContract.PATH_TRANSACTION+ "/holding/#", TRANSACTIONS_WITH_HOLDING_ID);
        matcher.addURI(authority, PortContract.PATH_TRANSACTION + "/transactions/#", TRANSACTIONS_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PortDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PORT:
                return PortContract.PortEntry.CONTENT_TYPE;

            case STOCK:
                return PortContract.StockEntry.CONTENT_TYPE;

            case STOCK_PRICE:
                return PortContract.StockPriceEntry.CONTENT_TYPE;

            case TRANSACTION_TYPE:
                return PortContract.TransactionTypeEntry.CONTENT_TYPE;

            case HOLDING:
                return PortContract.HoldingEntry.CONTENT_TYPE;

            case HOLDINGS_WITH_PORT_ID:
                return PortContract.HoldingEntry.CONTENT_ITEM_TYPE;

            case HOLDING_WITH_ID:
                return PortContract.HoldingEntry.CONTENT_ITEM_TYPE;

            case PORT_SUMMARY:
                return PortContract.HoldingEntry.CONTENT_ITEM_TYPE;

            case TRANSACTIONS:
                return PortContract.TransactionEntry.CONTENT_TYPE;

            case TRANSACTIONS_WITH_PORT_ID:
                return PortContract.TransactionEntry.CONTENT_ITEM_TYPE;

            case TRANSACTIONS_WITH_HOLDING_ID:
                return PortContract.TransactionEntry.CONTENT_ITEM_TYPE;

            case TRANSACTIONS_WITH_ID:
                return PortContract.TransactionEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case PORT: {
                retCursor = getPortName(uri, projection, sortOrder);
                break;
            }

            case STOCK: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PortContract.StockEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case TRANSACTION_TYPE: {
                retCursor = getTransactionType(uri);
                break;
            }

            case HOLDINGS_WITH_PORT_ID:{
                retCursor = getHoldingWithPortId(uri);
                break;
            }
            case HOLDING_WITH_ID: {
                retCursor = getHoldingWithHoldingId(uri);
                break;
            }

            case PORT_SUMMARY: {
                retCursor = getPortSummary(uri);
                break;
            }

            case TRANSACTIONS_WITH_PORT_ID: {
                retCursor = getTransactionsWithPortId(uri);
                break;
            }
            case TRANSACTIONS_WITH_HOLDING_ID: {
                retCursor = getTransactionsWithHoldingId(uri);
                break;
            }
            case TRANSACTIONS_WITH_ID: {
                retCursor = getTransactionsWithTransactionId(uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PORT: {
                long port_id = db.insert(PortContract.PortEntry.TABLE_NAME, null, values);
                if ( port_id > 0 )
                    returnUri = PortContract.PortEntry.buildPortUri(port_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case STOCK: {
                long _id = db.insert(PortContract.StockEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PortContract.StockEntry.buildStockUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case STOCK_PRICE: {
                long _id = db.insert(PortContract.StockPriceEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PortContract.StockPriceEntry.buildStockPriceUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case TRANSACTION_TYPE: {
                long _id = db.insert(PortContract.TransactionTypeEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PortContract.TransactionTypeEntry.buildTransactionTypeUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case HOLDING: {
                long holding_id = db.insert(PortContract.HoldingEntry.TABLE_NAME, null, values);
                if ( holding_id > 0 )
                    returnUri = PortContract.HoldingEntry.buildHoldingUri(holding_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case TRANSACTIONS: {
                long transaction_id = db.insert(PortContract.TransactionEntry.TABLE_NAME, null, values);
                if ( transaction_id > 0 )
                    returnUri = PortContract.TransactionEntry.buildTransactionUri(transaction_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case PORT: {
                rowsDeleted = db.delete(PortContract.PortEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case STOCK: {
                rowsDeleted = db.delete(PortContract.StockEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case STOCK_PRICE: {
                rowsDeleted = db.delete(PortContract.StockPriceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case  TRANSACTION_TYPE:{
                rowsDeleted = db.delete(PortContract.TransactionTypeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case HOLDING: {
                rowsDeleted = db.delete(PortContract.HoldingEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case TRANSACTIONS: {
                rowsDeleted = db.delete(PortContract.TransactionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case PORT: {
                rowsUpdated = db.update(PortContract.PortEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case STOCK: {
                rowsUpdated = db.update(PortContract.StockEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case STOCK_PRICE: {
                rowsUpdated = db.update(PortContract.StockPriceEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case TRANSACTION_TYPE: {
                rowsUpdated = db.update(PortContract.TransactionTypeEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case HOLDING: {
                rowsUpdated = db.update(PortContract.HoldingEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case TRANSACTIONS: {
                rowsUpdated = db.update(PortContract.TransactionEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;

        switch (match) {
            case TRANSACTION_TYPE: {
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PortContract.TransactionTypeEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }

            case STOCK_PRICE: {
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.replace(PortContract.StockPriceEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }

            default:
                return super.bulkInsert(uri, values);
        }

        if (returnCount != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnCount;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
