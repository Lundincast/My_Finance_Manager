package com.lundincast.my_finance_manager.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;

import java.sql.SQLException;

public class CreateCategoriesActivity extends ActionBarActivity {

    private CategoriesDataSource datasource;

    private static final String TAG = "ListCategoriesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_category);

        datasource = new CategoriesDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        getMenuInflater().inflate(R.menu.menu_create_categories, menu);
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
                final EditText categoryName = (EditText) findViewById(R.id.category_name);
                String category = categoryName.getText().toString();
                datasource.createCategory(category);
                Intent intent = new Intent(this, ListCategoriesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
