package com.lundincast.my_finance_manager.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.model.Category;

import java.sql.SQLException;
import java.util.List;

import android.widget.ListView;
import android.widget.Toast;

public class ListCategoriesActivity extends ListActivity {

    private CategoriesDataSource datasource;

    // This is the Adapter being used to display the list's data
    ArrayAdapter<Category> mAdapter;

    // These are the Contacts rows that we will retrieve
    //static final String[] PROJECTION = new String[] {ContactsContract.Data._ID,
    //        ContactsContract.Data.DISPLAY_NAME};

    // This is the select criteria
    //static final String SELECTION = "((" +
    //        ContactsContract.Data.DISPLAY_NAME + " NOTNULL) AND (" +
    //        ContactsContract.Data.DISPLAY_NAME + " != '' ))";


    private static final String TAG = "ListCategoriesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {ContactsContract.Data.DISPLAY_NAME};
        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        setContentView(R.layout.activity_categories);
        datasource = new CategoriesDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<Category> values = datasource.getAllCategories();

        // use the SimpleCursorAdapter to show the
        // elements in a ListView
        final ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this,
                R.layout.categories_list, R.id.TextView01, values);
        setListAdapter(adapter);
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category selected = adapter.getItem(position);
                Intent editIntent = new Intent(getApplicationContext(), EditCategoriesActivity.class);
                editIntent.putExtra("categoryId", selected.getId());
                startActivityForResult(editIntent, 2);
            }
        });
        this.mAdapter = adapter;

    }

    // Will be called via the onClick attribute
    // of the button in activity_categories.xml
    public void onClick(View view) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<Category> adapter = (ArrayAdapter<Category>) getListAdapter();
        Category category = null;
        switch (view.getId()) {
            case R.id.add:
                Intent editIntent = new Intent(this, CreateCategoriesActivity.class);
                startActivityForResult(editIntent, 1);
                break;
            case R.id.delete:
                if (getListAdapter().getCount() > 0) {
                    category = (Category) getListAdapter().getItem(0);
                    datasource.deleteCategory(category);
                    adapter.remove(category);
                }
                break;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mAdapter = null;
        mAdapter = (ArrayAdapter<Category>) getListAdapter();

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}