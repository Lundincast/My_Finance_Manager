package com.lundincast.my_finance_manager.activities.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lundincast.my_finance_manager.activities.model.Category;
import com.lundincast.my_finance_manager.activities.model.Transaction;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by lundincast on 1/03/15.
 */
public class TransactionDataSource {

    // Database fields
    private SQLiteDatabase database;
    private DbSQLiteHelper dbHelper;
    private String[] allColumns = {DbSQLiteHelper.TRANSACTION_ID,
            DbSQLiteHelper.TRANSACTION_PRICE,
            DbSQLiteHelper.TRANSACTION_CATEGORY,
            DbSQLiteHelper.TRANSACTION_DATE,
            DbSQLiteHelper.TRANSACTION_DAY,
            DbSQLiteHelper.TRANSACTION_MONTH,
            DbSQLiteHelper.TRANSACTION_YEAR,
            DbSQLiteHelper.TRANSACTION_COMMENT };

    private String[] priceAndCategoryColumns = {DbSQLiteHelper.TRANSACTION_ID,
            DbSQLiteHelper.TRANSACTION_PRICE,
            DbSQLiteHelper.TRANSACTION_CATEGORY};

    private String[] monthAndYearColumns = {DbSQLiteHelper.TRANSACTION_ID,
            DbSQLiteHelper.TRANSACTION_MONTH,
            DbSQLiteHelper.TRANSACTION_YEAR};

    private String[] monthAndYearColumnsWithCategory = {DbSQLiteHelper.TRANSACTION_ID,
            DbSQLiteHelper.TRANSACTION_MONTH,
            DbSQLiteHelper.TRANSACTION_YEAR,
            DbSQLiteHelper.TRANSACTION_CATEGORY};

    public TransactionDataSource(Context context) {
        dbHelper = new DbSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createTransaction(Transaction transaction) {
        ContentValues values = new ContentValues();
        values.put(DbSQLiteHelper.TRANSACTION_PRICE, transaction.getPrice());
        values.put(DbSQLiteHelper.TRANSACTION_CATEGORY, transaction.getCategory());
        Calendar cal = Calendar.getInstance();
        cal.setTime(transaction.getDate());
        values.put(DbSQLiteHelper.TRANSACTION_DATE, transaction.getDate().getTime());
        values.put(DbSQLiteHelper.TRANSACTION_DAY, cal.get(Calendar.DAY_OF_MONTH));
        values.put(DbSQLiteHelper.TRANSACTION_MONTH, cal.get(Calendar.MONTH));
        values.put(DbSQLiteHelper.TRANSACTION_YEAR, cal.get(Calendar.YEAR));
        values.put(DbSQLiteHelper.TRANSACTION_COMMENT, transaction.getComment());
        long insertId = database.insert(DbSQLiteHelper.TABLE_TRANSACTIONS, null, values);
        int i = 0;
    }

    public void updateTransaction(Transaction transaction) {
        ContentValues values = new ContentValues();
        values.put(DbSQLiteHelper.TRANSACTION_PRICE, transaction.getPrice());
        values.put(DbSQLiteHelper.TRANSACTION_CATEGORY, transaction.getCategory());
        Calendar cal = Calendar.getInstance();
        cal.setTime(transaction.getDate());
        values.put(DbSQLiteHelper.TRANSACTION_DATE, transaction.getDate().getTime());
        values.put(DbSQLiteHelper.TRANSACTION_DAY, cal.get(Calendar.DAY_OF_MONTH));
        values.put(DbSQLiteHelper.TRANSACTION_MONTH, cal.get(Calendar.MONTH));
        values.put(DbSQLiteHelper.TRANSACTION_YEAR, cal.get(Calendar.YEAR));
        values.put(DbSQLiteHelper.TRANSACTION_COMMENT, transaction.getComment());
        int updateId = database.update(DbSQLiteHelper.TABLE_TRANSACTIONS, values,
                                    DbSQLiteHelper.TRANSACTION_ID + " = " + transaction.getId(), null);
    }

    public void delete(Transaction transaction) {
        database.delete(DbSQLiteHelper.TABLE_TRANSACTIONS, DbSQLiteHelper.TRANSACTION_ID
        + " = " + transaction.getId(), null);
    }

    public void deleteAllRowsByCategory(Category category) {
        String[] args = new String[] {category.getName()};
        database.delete(DbSQLiteHelper.TABLE_TRANSACTIONS, DbSQLiteHelper.COLUMN_CATEGORY
        + " = ? ", args);
    }

    public Transaction getTransaction( long id) {
        Cursor cursor = database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
                allColumns, DbSQLiteHelper.TRANSACTION_ID + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        Transaction transaction = cursorToTransaction(cursor);
        cursor.close();
        return transaction;
    }


    public Cursor getAllTransaction() {
        return database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
                allColumns, null, null, null, null, DbSQLiteHelper.TRANSACTION_DATE + " DESC");
    }


    public Cursor getTransactionByDate(Date date) {
        Cursor cursor;
        String[] args;
        Calendar cal;
        cal = Calendar.getInstance();
        cal.setTime(date);
        args = new String[] {String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), String.valueOf(cal.get(Calendar.MONTH)), String.valueOf(cal.get(Calendar.YEAR))};
        cursor = database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
                monthAndYearColumns, DbSQLiteHelper.TRANSACTION_DAY + " = ? AND " + DbSQLiteHelper.TRANSACTION_MONTH + " = ? AND " + DbSQLiteHelper.TRANSACTION_YEAR + " = ? ",
                args, null, null, null);
        return cursor;
    }

    public Cursor getTransactionsByMonth(Date date) {
        Cursor cursor;
        String[] args;
        Calendar cal;
        cal = Calendar.getInstance();
        cal.setTime(date);
        args = new String[] {String.valueOf(cal.get(Calendar.MONTH))};
        cursor = database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
                priceAndCategoryColumns, DbSQLiteHelper.TRANSACTION_MONTH + " = ? ", args, null, null, null);
        return cursor;
    }

    public Cursor getTransactionsPerCategory(String category) {
        Cursor cursor;
        String[] args = new String[] {category};
        if (category.equals("All")) {
            cursor = database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
                    allColumns, null, null, null, null,
                    DbSQLiteHelper.TRANSACTION_DATE + " DESC");
        } else {
            cursor = database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
                    allColumns, DbSQLiteHelper.TRANSACTION_CATEGORY + " =?", args,
                    null, null,
                    DbSQLiteHelper.TRANSACTION_DATE + " DESC");
        }
        return cursor;
    }

    public Cursor getTransactionsGroupByUniqueMonthAndYear(String category) {

        Cursor cursor;
        String[] args;

        if (category == null) {
            cursor = database.query(true, DbSQLiteHelper.TABLE_TRANSACTIONS,
                    monthAndYearColumns, null, null,
                    DbSQLiteHelper.TRANSACTION_MONTH + ", " + DbSQLiteHelper.TRANSACTION_YEAR, null,
                    DbSQLiteHelper.TRANSACTION_DATE + " DESC", null);
        } else {
            args = new String[] {category};
            cursor = database.query(true, DbSQLiteHelper.TABLE_TRANSACTIONS,
                    monthAndYearColumnsWithCategory, DbSQLiteHelper.TRANSACTION_CATEGORY + " =?", args, DbSQLiteHelper.TRANSACTION_MONTH + ", " + DbSQLiteHelper.TRANSACTION_YEAR, null,
                    DbSQLiteHelper.TRANSACTION_DATE + " DESC", null);
        }
        return cursor;
    }

    public Cursor getTransactionsByMonthAndYear(String month, String year, String category) {

        Cursor cursor;
        String[] args;

        if (category == null) {
            args = new String[] {month, year};
            cursor = database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
                    allColumns, DbSQLiteHelper.TRANSACTION_MONTH + " = ? AND " + DbSQLiteHelper.TRANSACTION_YEAR + " = ? ",
                    args, null, null, DbSQLiteHelper.TRANSACTION_DATE + " DESC");
        } else {
            args = new String[] {month, year, category};
            cursor = database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
                    allColumns, DbSQLiteHelper.TRANSACTION_MONTH + " = ? AND " + DbSQLiteHelper.TRANSACTION_YEAR + " = ? AND " + DbSQLiteHelper.TRANSACTION_CATEGORY + " = ? ",
                    args, null, null, DbSQLiteHelper.TRANSACTION_DATE + " DESC");
        }

        return cursor;
    }


    private Transaction cursorToTransaction(Cursor cursor) {

        int idIndex = cursor.getColumnIndex(DbSQLiteHelper.TRANSACTION_ID);
        int priceIndex = cursor.getColumnIndex(DbSQLiteHelper.TRANSACTION_PRICE);
        int categoryIndex = cursor.getColumnIndex(DbSQLiteHelper.TRANSACTION_CATEGORY);
        int dateIndex = cursor.getColumnIndex(DbSQLiteHelper.TRANSACTION_DATE);
        int commentIndex = cursor.getColumnIndex(DbSQLiteHelper.TRANSACTION_COMMENT);

        // extract data from cursor
        long id = cursor.getLong(idIndex);
        double price = cursor.getDouble(priceIndex);
        String category = cursor.getString(categoryIndex);
        long dateInt = cursor.getLong(dateIndex);
        Date date = new Date(dateInt);
        String comment = cursor.getString(commentIndex);

        return new Transaction(id, price, category, date, comment);

    }
}
