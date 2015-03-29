package com.lundincast.my_finance_manager.activities.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.lundincast.my_finance_manager.R;

/**
 * Created by lundincast on 28/03/15.
 */
public class TransactionCursorAdapter extends CursorAdapter implements Filterable {

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
