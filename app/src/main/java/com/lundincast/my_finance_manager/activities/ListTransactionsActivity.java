package com.lundincast.my_finance_manager.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.data.DbSQLiteHelper;
import com.lundincast.my_finance_manager.activities.data.TransactionCursorTreeAdapter;
import com.lundincast.my_finance_manager.activities.data.TransactionDataSource;

import java.sql.SQLException;
import java.util.ArrayList;

public class ListTransactionsActivity extends ListActivity {

    // TODO implement expandable ListView
    // TODO implement color change animation on category sorting

    private TransactionDataSource datasource;
    private CategoriesDataSource catDatasource;
    private Cursor cursor;
    TransactionCursorTreeAdapter adapter;

    ArrayList<String> catFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transactions);
        datasource = new TransactionDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        catDatasource = new CategoriesDataSource(this);
        try {
            catDatasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // datasource.getTransactionsByMonthAndYear("3", "2015");
        // TransactionCursorTreeAdapter adapter2 = new TransactionCursorTreeAdapter(datasource.getTransactionsGroupByUniqueMonthAndYear(), this);

        catFilter = catDatasource.getAllCategoriesStringList();

        //******************************************************************************************
//        ArrayAdapter<String> adapterFilter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, catFilter);
//        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//        ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {
//            @Override
//            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
//                Cursor newCursor = ListTransactionsActivity.this.datasource.getTransactionsPerCategory(catFilter.get(itemPosition));
//                ListTransactionsActivity.this.adapter.swapCursor(newCursor);
//                return false;
//            }
//        };
//        getActionBar().setListNavigationCallbacks(adapterFilter, navigationListener);
        //******************************************************************************************
        cursor = datasource.getTransactionsGroupByUniqueMonthAndYear();
        startManagingCursor(cursor);

        adapter = new TransactionCursorTreeAdapter(cursor, this);
        final ExpandableListView lv = (ExpandableListView) getListView();
        lv.setAdapter(adapter);

        lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                CursorTreeAdapter adapter = (CursorTreeAdapter) lv.getExpandableListAdapter();
                Cursor adapterCursor = adapter.getChild(groupPosition, childPosition);
                long transactionId = adapterCursor.getLong(adapterCursor.getColumnIndex(DbSQLiteHelper.TRANSACTION_ID));
                // We only pass transaction id to EditActivity, it'll instantiate the object from there
                Intent editIntent = new Intent(getApplicationContext(), EditTransactionActivity.class);
                editIntent.putExtra("transactionId", transactionId);
                startActivityForResult(editIntent, 2);
                return false;
            }
        });



    }


    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    @Override
    protected void onResume() {
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_transactions, menu);
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
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if (id == R.id.add_transaction) {
            Intent intent = new Intent(this, CreateTransactionActivity.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }


}
