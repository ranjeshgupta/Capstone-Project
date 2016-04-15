package com.example.stockportfoliomanager.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.stockportfoliomanager.app.data.PortContract.PortEntry;
import com.example.stockportfoliomanager.app.data.PortContract.StockEntry;
import com.example.stockportfoliomanager.app.data.PortContract.StockPriceEntry;
import com.example.stockportfoliomanager.app.data.PortContract.TransactionTypeEntry;
import com.example.stockportfoliomanager.app.data.PortContract.HoldingEntry;
import com.example.stockportfoliomanager.app.data.PortContract.TransactionEntry;

/**
 * Created by Nish on 16-03-2016.
 */
public class PortDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "stockportfolio.db";

    public PortDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_PORT_TABLE = "CREATE TABLE " + PortEntry.TABLE_NAME + " (" +
                PortEntry.COLUMN_PORT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PortEntry.COLUMN_PORT_NAME + " TEXT UNIQUE NOT NULL " +
                " );";

        final String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " + StockEntry.TABLE_NAME + " ( " +
                StockEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StockEntry.COLUMN_COMPANY_CODE + " INTEGER UNIQUE NOT NULL, " +
                StockEntry.COLUMN_COMPANY_NAME + " TEXT UNIQUE NOT NULL " +
                "ON CONFLICT REPLACE );";

        final String SQL_CREATE_STOCK_PRICE_TABLE = "CREATE TABLE " + StockPriceEntry.TABLE_NAME + " (" +
                StockPriceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StockPriceEntry.COLUMN_COMPANY_CODE + " INTEGER UNIQUE NOT NULL REFERENCES " +
                    StockEntry.TABLE_NAME + "("+ StockEntry.COLUMN_COMPANY_CODE +"), " +
                StockPriceEntry.COLUMN_PRICE_DATE + " INTEGER NOT NULL, " +
                StockPriceEntry.COLUMN_PRICE + " REAL NOT NULL " +
                "ON CONFLICT REPLACE)";

        final String SQL_CREATE_TRANSACTION_TYPE_TABLE = "CREATE TABLE " + TransactionTypeEntry.TABLE_NAME + " (_" +
                TransactionTypeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TransactionTypeEntry.COLUMN_TRANSACTION_TYPE + " INTEGER UNIQUE NOT NULL, " +
                TransactionTypeEntry.COLUMN_TRANSACTION_DESC + " TEXT UNIQUE NOT NULL " +
                "ON CONFLICT REPLACE);";

        final String SQL_CREATE_HOLDING_TABLE = "CREATE TABLE " + HoldingEntry.TABLE_NAME + " ( " +
                HoldingEntry.COLUMN_HOLDING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HoldingEntry.COLUMN_PORT_ID + " INTEGER NOT NULL REFERENCES " +
                    PortEntry.TABLE_NAME + " (" + PortEntry.COLUMN_PORT_ID + "), " +
                HoldingEntry.COLUMN_COMPANY_CODE + " INTEGER NOT NULL REFERENCES " +
                    StockEntry.TABLE_NAME + " (" + StockEntry.COLUMN_COMPANY_CODE + ") " +
                ")";

        final String SQL_CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TransactionEntry.TABLE_NAME + " ( " +
                TransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TransactionEntry.COLUMN_HOLDING_ID + " INTEGER REFERENCES " +
                    HoldingEntry.TABLE_NAME + "(" + HoldingEntry.COLUMN_HOLDING_ID + "), " +
                TransactionEntry.COLUMN_TRANSACTION_DATE + " TEXT NOT NULL, " +
                TransactionEntry.COLUMN_TRANSACTION_TYPE + " INTEGER NOT NULL REFERENCES " +
                    TransactionTypeEntry.TABLE_NAME + "(" + TransactionTypeEntry.COLUMN_TRANSACTION_TYPE + "), " +
                TransactionEntry.COLUMN_DIVIDEND_PERCENTAGE + " REAL, " +
                TransactionEntry.COLUMN_OFFERED + " INTEGER, " +
                TransactionEntry.COLUMN_HELD + " INTEGER, " +
                TransactionEntry.COLUMN_UNITS_FLOW + " REAL, " +
                TransactionEntry.COLUMN_PRICE + " REAL, " +
                TransactionEntry.COLUMN_CASH_FLOW + " REAL, " +
                TransactionEntry.COLUMN_BROKERAGE + " REAL " +
                ");";

        //create table
        sqLiteDatabase.execSQL(SQL_CREATE_PORT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STOCK_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STOCK_PRICE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRANSACTION_TYPE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_HOLDING_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRANSACTIONS_TABLE);

        //insert initial data
        sqLiteDatabase.execSQL("INSERT INTO " + PortEntry.TABLE_NAME + "(" + PortEntry.COLUMN_PORT_NAME + ") " +
                " VALUES ('My Portfolio')");

        sqLiteDatabase.execSQL("INSERT INTO " + TransactionTypeEntry.TABLE_NAME + "(" +
                TransactionTypeEntry.COLUMN_TRANSACTION_TYPE + ", " +
                TransactionTypeEntry.COLUMN_TRANSACTION_DESC + ") " +
                " VALUES ('1','Buy'), ('2', 'Sell'), ('3', 'Dividend'), ('4', 'Bonus'), ('5', 'FV Changed')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TransactionEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HoldingEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TransactionTypeEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StockPriceEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StockEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PortEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}
