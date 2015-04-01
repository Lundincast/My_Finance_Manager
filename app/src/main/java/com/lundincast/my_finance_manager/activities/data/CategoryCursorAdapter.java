package com.lundincast.my_finance_manager.activities.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.model.Category;

import java.sql.SQLException;

/**
 * Created by lundincast on 1/04/15.
 */
public class CategoryCursorAdapter extends CursorAdapter {

    private CategoriesDataSource datasource;

    public CategoryCursorAdapter(Context context, Cursor cursor) {

        super(context, cursor, 0);
    }


    // The newView method is used to inflate a new view and return it,
    // nwe don't bind any data to the view at this point
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.activity_list_category_entry, null);
    }


    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated templates
        TextView categoryColorTv = (TextView) view.findViewById(R.id.category_icon);
        TextView categoryName = (TextView) view.findViewById(R.id.category_name);

        // Extract properties from cursor
        String color = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.COLUMN_COLOR));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.COLUMN_CATEGORY));

        datasource = new CategoriesDataSource(context);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Category catObj = datasource.getCategoryByName(name);
        datasource.close();

        // find color to be displayed
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

        categoryColorTv.setBackgroundColor(Color.parseColor(color));
        categoryName.setText(name);

    }
}
