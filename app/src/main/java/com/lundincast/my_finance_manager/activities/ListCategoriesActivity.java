package com.lundincast.my_finance_manager.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.data.CategoryCursorAdapter;
import com.lundincast.my_finance_manager.activities.data.DbSQLiteHelper;

import java.sql.SQLException;

public class ListCategoriesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CategoriesDataSource datasource;
    private CategoryCursorAdapter adapter;


    private static final String TAG = "ListCategoriesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        datasource = new CategoriesDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        final Cursor cursor = datasource.getAllCategories();

        // Use the custom adapter to populate the listview
        ListView lv = (ListView) findViewById(R.id.listview);
        adapter = new CategoryCursorAdapter(this, cursor);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) adapter.getItem(position);
                long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(DbSQLiteHelper.COLUMN_ID));
                Intent editIntent = new Intent(getApplicationContext(), EditCategoriesActivity.class);
                editIntent.putExtra("categoryId", categoryId);
                startActivityForResult(editIntent, 1);
                ListCategoriesActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

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
            Intent createIntent = new Intent(this, CreateCategoriesActivity.class);
            startActivityForResult(createIntent, 1);
            this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
