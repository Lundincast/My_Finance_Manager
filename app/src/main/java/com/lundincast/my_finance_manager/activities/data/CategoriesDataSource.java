package com.lundincast.my_finance_manager.activities.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lundincast.my_finance_manager.activities.model.Category;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lundincast on 21/02/15.
 */
public class CategoriesDataSource {

    // Database fields
    private SQLiteDatabase database;
    private DbSQLiteHelper dbHelper;
    private String[] allColumns = { DbSQLiteHelper.COLUMN_ID,
        DbSQLiteHelper.COLUMN_CATEGORY };

    public CategoriesDataSource(Context context) {
        dbHelper = new DbSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Category createCategory(String category) {
        ContentValues values = new ContentValues();
        values.put(DbSQLiteHelper.COLUMN_CATEGORY, category);
        long insertId = database.insert(DbSQLiteHelper.TABLE_CATEGORIES, null, values);
        Cursor cursor = database.query(DbSQLiteHelper.TABLE_CATEGORIES,
                allColumns, DbSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Category newCategory = cursorToCategory(cursor);
        cursor.close();
        return newCategory;
    }

    public void updateCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(DbSQLiteHelper.COLUMN_CATEGORY, category.getName());
        int updateId = database.update(DbSQLiteHelper.TABLE_CATEGORIES, values,
                                     DbSQLiteHelper.COLUMN_ID + " = " + category.getId(), null);
    }

    public void deleteCategory(Category category) {
        System.out.println("Category deleted with name: " + category);
        database.delete(DbSQLiteHelper.TABLE_CATEGORIES, DbSQLiteHelper.COLUMN_ID
        + " = " + category.getId(), null);
    }

    public Category getCategory(long id) {
        Cursor cursor = database.query(DbSQLiteHelper.TABLE_CATEGORIES,
                allColumns,
                DbSQLiteHelper.COLUMN_ID + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        Category category = cursorToCategory(cursor);
        cursor.close();
        return category;
    }

    public Cursor getAllCategories() {
        List<Category> categories = new ArrayList<Category>();

        Cursor cursor = database.query(DbSQLiteHelper.TABLE_CATEGORIES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Category category = cursorToCategory(cursor);
            categories.add(category);
            cursor.moveToNext();
        }

        // Make sure to close the cursor
        // cursor.close();
        return cursor;
    }

    public ArrayList<String> getAllCategoriesStringList() {

        ArrayList<String> catList = new ArrayList<String>();

        Cursor cursor = database.query(DbSQLiteHelper.TABLE_CATEGORIES,
                allColumns, null, null, null, null, DbSQLiteHelper.COLUMN_CATEGORY + " ASC");

        catList.add("All");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            catList.add(cursor.getString(1));
            cursor.moveToNext();
        }

        return catList;
    }

    public List<Category> getAllCategoriesList() {
        List<Category> categories = new ArrayList<Category>();

        Cursor cursor = database.query(DbSQLiteHelper.TABLE_CATEGORIES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Category category = cursorToCategory(cursor);
            categories.add(category);
            cursor.moveToNext();
        }

        // Make sure to close the cursor
        cursor.close();
        return categories;
    }

    private Category cursorToCategory(Cursor cursor) {
        Category category = new Category();
        category.setId(cursor.getLong(0));
        category.setName(cursor.getString(1));
        return category;
    }
}
