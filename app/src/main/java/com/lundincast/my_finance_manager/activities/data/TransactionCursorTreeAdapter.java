package com.lundincast.my_finance_manager.activities.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.TextView;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.model.Category;

import java.sql.SQLException;
import java.util.Calendar;

/**
 * Created by lundincast on 6/04/15.
 */
public class TransactionCursorTreeAdapter extends CursorTreeAdapter {

    private TransactionDataSource datasource;
    private CategoriesDataSource catDatasource;

    private String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private String[] monthsComplete = {"January", "February", "March", "April", "May", "June", "July", "August",
                                        "September", "October", "November", "December"};

    public TransactionCursorTreeAdapter(Cursor cursor, Context context) {

        super(cursor, context);
        datasource = new TransactionDataSource(context);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {

        // Extract month and year from groupCursor for database query
        String month = groupCursor.getString(groupCursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_MONTH));
        String year = groupCursor.getString(groupCursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_YEAR));

        Cursor cursor = datasource.getTransactionsByMonthAndYear(month, year);

        return cursor;
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.activity_list_transaction_group, null);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {

        TextView textView = (TextView) view.findViewById(R.id.transaction_list_header);
        textView.setText(monthsComplete[cursor.getInt(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_MONTH))]
                 + " " + cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_YEAR)));

    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.activity_list_transactions_entry, null);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {

        // Find fields to populate in inflated templates
        TextView categoryIconTv = (TextView) view.findViewById(R.id.category_icon);
        TextView dateTv = (TextView) view.findViewById(R.id.name_entry);
        TextView commentTv = (TextView) view.findViewById(R.id.comment_entry);
        TextView priceTv = (TextView) view.findViewById(R.id.transaction_price);

        // Extract properties from cursor
        String category = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_CATEGORY));
        long date = cursor.getLong(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_DATE));
        String comment = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_COMMENT));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_PRICE));

        // Populate fields with extracted properties
        String firstLetter = category.substring(0, 1).toUpperCase();
        categoryIconTv.setText(firstLetter);
        // find color to be displayed
        catDatasource = new CategoriesDataSource(context);
        try {
            catDatasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Category catObj = catDatasource.getCategoryByName(category);
        catDatasource.close();
        String color = catObj.getColor();
        String[] colorsArray =  context.getResources().getStringArray(R.array.colors_array);
        String[] colorValue = context.getResources().getStringArray(R.array.colors_value);

        int it = 0;
        for (String s: colorsArray) {
            if (s.equals(color)) {
                color = colorValue[it];
                break;
            }
            it++;
        }
        categoryIconTv.setBackgroundColor(Color.parseColor(color));

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        dateTv.setText(days[cal.get(Calendar.DAY_OF_WEEK) - 1].toUpperCase() + ", "
                + Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + " "
                + months[cal.get(Calendar.MONTH)] + " "
                + Integer.toString(cal.get(Calendar.YEAR)));


        commentTv.setText(comment);
        // get preferences for currency display
        // TODO ListTransactionActivity needs to be refreshed after currency preferences is changed so that this adapter is updated as well
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String currPref = sharedPref.getString("pref_key_currency", "1");

        if (currPref.equals("2")) {
            priceTv.setText(Double.toString(price) + " $");
        } else {
            priceTv.setText(Double.toString(price) + " €");
        }

    }



}
