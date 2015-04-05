package com.lundincast.my_finance_manager.activities.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by lundincast on 21/02/15.
 */
public class DbSQLiteHelper extends SQLiteOpenHelper {

    // All static variables
    // Database version
    private static final int DATABASE_VERSION = 1;
    // Database name
    private static final String DATABASE_NAME = "financemanager.db";
    // Categories table name
    public static final String TABLE_CATEGORIES = "categories";
    // Transactions table name
    public static final String TABLE_TRANSACTIONS = "transactions";
    // Categories table columns names
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_COLOR = "color";
    // Transactions table columns names
    public static final String TRANSACTION_ID = "_id";
    public static final String TRANSACTION_PRICE = "price";
    public static final String TRANSACTION_CATEGORY = "category";
    public static final String TRANSACTION_DATE = "date";
    public static final String TRANSACTION_MONTH = "month";
    public static final String TRANSACTION_YEAR = "year";
    public static final String TRANSACTION_COMMENT = "comment";


    // Category database creation sql statement
    private static final String DATABASE_CREATE_CATEGORIES = "create table "
            + TABLE_CATEGORIES + "(" + COLUMN_ID + " integer primary key autoincrement, "
                                     + COLUMN_CATEGORY + " text not null, "
                                     + COLUMN_COLOR + " text not null);";

    // Transaction database creation sql statement
    private static final String DATABASE_CREATE_TRANSACTIONS = "create table "
            + TABLE_TRANSACTIONS + "(" + TRANSACTION_ID + " integer primary key autoincrement, "
                                       + TRANSACTION_PRICE + " real not null, "
                                       + TRANSACTION_CATEGORY + " text not null, "
                                       + TRANSACTION_DATE + " int not null, "
                                       + TRANSACTION_MONTH + " int not null, "
                                       + TRANSACTION_YEAR + " int not null, "
                                       + TRANSACTION_COMMENT + " text not null);";

    public DbSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_CATEGORIES);
        db.execSQL(DATABASE_CREATE_TRANSACTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DbSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }
}
