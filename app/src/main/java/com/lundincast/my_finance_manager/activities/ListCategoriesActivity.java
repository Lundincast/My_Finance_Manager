package com.lundincast.my_finance_manager.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.data.CategoryCursorAdapter;
import com.lundincast.my_finance_manager.activities.data.DbSQLiteHelper;

import java.sql.SQLException;

public class ListCategoriesActivity extends ListActivity {

    private CategoriesDataSource datasource;
    private CategoryCursorAdapter adapter;


    private static final String TAG = "ListCategoriesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_categories);
        datasource = new CategoriesDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        final Cursor cursor = datasource.getAllCategories();

        // Use the custom adapter to populate the listview
        adapter = new CategoryCursorAdapter(this, cursor);
        setListAdapter(adapter);
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) adapter.getItem(position);
                long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(DbSQLiteHelper.COLUMN_ID));
                Intent editIntent = new Intent(getApplicationContext(), EditCategoriesActivity.class);
                editIntent.putExtra("categoryId", categoryId);
                startActivityForResult(editIntent, 2);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //mAdapterOld = null;
        //mAdapterOld = (ArrayAdapter<Category>) getListAdapter();

        Toast toast = Toast.makeText(getApplicationContext(), "New category created", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onResume() {
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_categories, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
            Intent editIntent = new Intent(this, CreateCategoriesActivity.class);
            startActivityForResult(editIntent, 1);

        }

        return super.onOptionsItemSelected(item);
    }
}
