package com.lundincast.my_finance_manager.activities.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lundincast.my_finance_manager.activities.model.Transaction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
            DbSQLiteHelper.TRANSACTION_MONTH,
            DbSQLiteHelper.TRANSACTION_YEAR,
            DbSQLiteHelper.TRANSACTION_COMMENT };

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
        values.put(DbSQLiteHelper.TRANSACTION_MONTH, cal.get(Calendar.MONTH));
        values.put(DbSQLiteHelper.TRANSACTION_YEAR, cal.get(Calendar.YEAR));
        values.put(DbSQLiteHelper.TRANSACTION_COMMENT, transaction.getComment());
        long insertId = database.insert(DbSQLiteHelper.TABLE_TRANSACTIONS, null, values);
//        Cursor cursor = database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
//                allColumns, DbSQLiteHelper.TRANSACTION_ID + " = " + insertId, null, null, null, null);
//        cursor.moveToFirst();
//        Transaction newTransaction = cursorToTransaction(cursor);
//        cursor.close();
//        return newTransaction;
    }

    public void updateTransaction(Transaction transaction) {
        ContentValues values = new ContentValues();
        values.put(DbSQLiteHelper.TRANSACTION_PRICE, transaction.getPrice());
        values.put(DbSQLiteHelper.TRANSACTION_CATEGORY, transaction.getCategory());
        Calendar cal = Calendar.getInstance();
        cal.setTime(transaction.getDate());
        values.put(DbSQLiteHelper.TRANSACTION_DATE, transaction.getDate().getTime());
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

    public Transaction getTransaction( long id) {
        Cursor cursor = database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
                allColumns, DbSQLiteHelper.TRANSACTION_ID + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        Transaction transaction = cursorToTransaction(cursor);
        cursor.close();
        return transaction;
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

    public Cursor getAllTransaction() {
        List<Transaction> transactions = new ArrayList<Transaction>();

        Cursor cursor = database.query(DbSQLiteHelper.TABLE_TRANSACTIONS,
                allColumns, null, null, null, null, DbSQLiteHelper.TRANSACTION_DATE + " DESC");

        //cursor.moveToFirst();
        //while (!cursor.isAfterLast()) {
        //    Transaction transaction = cursorToTransaction(cursor);
        //    transactions.add(transaction);
        //    cursor.moveToNext();
        //}
        // make sure to close the cursor
        //cursor.close();
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

        // populate new Transaction object
        Transaction transaction = new Transaction(id, price, category, date, comment);

        return transaction;

//        Gson gson = new Gson();
//        transaction.setId(cursor.getLong(0));
//        transaction.setPrice(cursor.getShort(1));
//        // Deserialize category from Json
//        byte[] blob = cursor.getBlob(cursor.getColumnIndex(DbSQLiteHelper.TRANSACTION_CATEGORY));
//        String json = new String(blob);
//        Category category = gson.fromJson(json, Category.class);
//        transaction.setCategory(category.getName());
//        // Get date from String
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY", Locale.getDefault());
////        try {
////            transaction.setDate(sdf.parse(cursor.getString(3)));
////        } catch (ParseException e) {
////            e.printStackTrace();
////        }
//        transaction.setComment(cursor.getString(4));
//        return transaction;
    }
}
