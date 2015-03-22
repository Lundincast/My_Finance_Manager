package com.lundincast.my_finance_manager.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.DbSQLiteHelper;
import com.lundincast.my_finance_manager.activities.data.TransactionDataSource;
import com.lundincast.my_finance_manager.activities.model.Category;
import com.lundincast.my_finance_manager.activities.model.Transaction;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ListTransactionsActivity extends ListActivity {

    private TransactionDataSource datasource;


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

        Cursor cursor = datasource.getAllTransaction();
        startManagingCursor(cursor);

        // The desired columns to be bound
        String[] columns = new String[] {cursor.getColumnName(1), cursor.getColumnName(3), cursor.getColumnName(4)};
        // The xml defined views which the data will be bound to
        int[] to = new int[] {R.id.transaction_price, R.id.name_entry, R.id.comment_entry};

        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.activity_list_transactions_entry,
                datasource.getAllTransaction(),
                columns,
                to);
        this.setListAdapter(adapter);

        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) adapter.getItem(position);
                int test = cursor.getShort(0);
                long transactionId = cursor.getLong(cursor.getColumnIndex(DbSQLiteHelper.TRANSACTION_ID));
                // We only pass transaction id to EditActivity, it'll instantiate the object from there
                Intent editIntent = new Intent(getApplicationContext(), EditTransactionActivity.class);
                editIntent.putExtra("transactionId", transactionId);
                startActivityForResult(editIntent, 2);
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
