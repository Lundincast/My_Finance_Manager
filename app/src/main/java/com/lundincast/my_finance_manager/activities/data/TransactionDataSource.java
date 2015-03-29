package com.lundincast.my_finance_manager.activities.data;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.lundincast.my_finance_manager.activities.model.Category;
import com.lundincast.my_finance_manager.activities.model.Transaction;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            DbSQLiteHelper.TRANSACTION_COMMENT };

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
        values.put(DbSQLiteHelper.TRANSACTION_CATEGORY, transaction.getCategory().toString());
        values.put(DbSQLiteHelper.TRANSACTION_DATE, transaction.getDate().toString());
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
        values.put(DbSQLiteHelper.TRANSACTION_CATEGORY, transaction.getCategory().toString());
        values.put(DbSQLiteHelper.TRANSACTION_DATE, transaction.getDate().toString());
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
                    allColumns, DbSQLiteHelper.TRANSACTION_CATEGORY + " =?", args, null, null,
                    DbSQLiteHelper.TRANSACTION_DATE + " DESC");
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
        short price = cursor.getShort(priceIndex);
        String category = cursor.getString(categoryIndex);
        String date = cursor.getString(dateIndex);
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
