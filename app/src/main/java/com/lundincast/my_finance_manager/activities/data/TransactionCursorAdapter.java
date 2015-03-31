package com.lundincast.my_finance_manager.activities.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.model.Category;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by lundincast on 28/03/15.
 */
public class TransactionCursorAdapter extends CursorAdapter implements Filterable {

    private CategoriesDataSource dataSource;

    public TransactionCursorAdapter(Context context, Cursor cursor) {

        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // nwe don't bind any data to the view at this point
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.activity_list_transactions_entry, null);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated templates
        TextView categoryIconTv = (TextView) view.findViewById(R.id.category_icon);
        TextView dateTv = (TextView) view.findViewById(R.id.name_entry);
        TextView commentTv = (TextView) view.findViewById(R.id.comment_entry);
        TextView priceTv = (TextView) view.findViewById(R.id.transaction_price);

        // Extract properties from cursor
        String category = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_CATEGORY));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_DATE));
        String comment = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_COMMENT));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_PRICE));

        // Populate fields with extracted properties
        String firstLetter = category.substring(0, 1).toUpperCase();
        categoryIconTv.setText(firstLetter);
        // find color to be displayed
        dataSource = new CategoriesDataSource(context);
        try {
            dataSource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Category catObj = dataSource.getCategoryByName(category);
        dataSource.close();
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
        dateTv.setText(date);
        commentTv.setText(comment);
        // get preferences for currency display
        // TODO ListTransactionActivity needs to be refreshed after currency preferences is changed so that this adapter is updated as well
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String currPref = sharedPref.getString("pref_key_currency", "1");

        if (currPref.equals("2")) {
            priceTv.setText(Double.toString(price) + " $");
        } else {
            priceTv.setText(Double.toString(price) + " €");        }

    }


}
