package com.lundincast.my_finance_manager.activities;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.BroadcastReceivers.NotificationAlarmReceiver;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.data.DbSQLiteHelper;
import com.lundincast.my_finance_manager.activities.data.TransactionCursorTreeAdapter;
import com.lundincast.my_finance_manager.activities.data.TransactionDataSource;
import com.lundincast.my_finance_manager.activities.model.Category;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ListTransactionsActivity extends ListActivity {

    private TransactionDataSource datasource;
    private CategoriesDataSource catDatasource;
    private Cursor cursor;
    TransactionCursorTreeAdapter adapter;
    SharedPreferences sharedPref;

    ArrayList<String> catFilter;
    private long[] expandedIds;

    // TODO notification triggered on app launch


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transactions);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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

        catFilter = catDatasource.getAllCategoriesStringList();

        cursor = datasource.getTransactionsGroupByUniqueMonthAndYear(null);
        startManagingCursor(cursor);

        adapter = new TransactionCursorTreeAdapter(cursor, this);
        final ExpandableListView lv = (ExpandableListView) getListView();
        lv.setAdapter(adapter);
        lv.expandGroup(0);
        lv.setSaveEnabled(true);
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
                ListTransactionsActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return false;
            }
        });

        //******************************************************************************************
        final ArrayAdapter<String> adapterFilter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, catFilter);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                //Cursor newCursor = ListTransactionsActivity.this.datasource.getTransactionsPerCategory(catFilter.get(itemPosition));
                //adapter.setGroupCursor(newCursor);
                //adapter = new TransactionCursorTreeAdapter(newCursor, getApplicationContext());
                //adapter.notifyDataSetChanged();
                //ListTransactionsActivity.this.adapter.setGroupCursor(newCursor);
                //ListTransactionsActivity.this.adapter.notifyDataSetChanged();
                if (itemId != 0) {
                    // set action bar color depending on selected category
                    Category category = catDatasource.getCategoryByName(adapterFilter.getItem(itemPosition));
                    String color = category.getColor();
                    String[] colorsArray =  getApplicationContext().getResources().getStringArray(R.array.colors_array);
                    String[] colorValue = getApplicationContext().getResources().getStringArray(R.array.colors_value);
                    int it = 0;
                    for (String s: colorsArray) {
                        if (s.equals(color)) {
                            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorValue[it])));
                            break;
                        }
                        it++;
                    }
                    // set new group cursor queried by category
                    Cursor newCursor = datasource.getTransactionsGroupByUniqueMonthAndYear(adapterFilter.getItem(itemPosition));
                    adapter.changeCursor(newCursor);
                } else {
                    // set default action bar color
                    getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#fff3f3f3")));
                    // set new group cursor with all categories
                    Cursor newCursor = datasource.getTransactionsGroupByUniqueMonthAndYear(null);
                    adapter.changeCursor(newCursor);
                }
                return true;
            }
        };
        getActionBar().setListNavigationCallbacks(adapterFilter, navigationListener);
        //******************************************************************************************

        // Set intent to be broadcast for reminder
        Intent intent = new Intent(this, NotificationAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Find out time difference between "now" and next 23h
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, sharedPref.getInt("hour_of_day_alarm", 23));
        cal.set(Calendar.MINUTE, sharedPref.getInt("minute_of_day_alarm", 00));
        cal.set(Calendar.SECOND, 0);
        Date today = new Date();
        long diff = cal.getTime().getTime() - today.getTime();
        // Set AlarmManager to trigger broadcast
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, diff, AlarmManager.INTERVAL_DAY, pendingIntent);
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
        if (this.expandedIds != null) {
            restoreExpandedState(expandedIds);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (this.expandedIds != null) {
            restoreExpandedState(expandedIds);
        }
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
        expandedIds = getExpandedIds();
        int i = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.expandedIds = getExpandedIds();
        outState.putLongArray("ExpandedIds", this.expandedIds);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        long[] expandedIds = state.getLongArray("ExpandedIds");
        if (expandedIds != null) {
            restoreExpandedState(expandedIds);
        }
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

    private long[] getExpandedIds() {
        ExpandableListView lv = (ExpandableListView) getListView();
        TransactionCursorTreeAdapter adapter = (TransactionCursorTreeAdapter) lv.getExpandableListAdapter();
        if (adapter != null) {
            int length = adapter.getGroupCount();
            ArrayList<Long> expandedIds = new ArrayList<Long>();
            for(int i=0; i < length; i++) {
                if(lv.isGroupExpanded(i)) {
                    expandedIds.add(adapter.getGroupId(i));
                }
            }
            return toLongArray(expandedIds);
        } else {
            return null;
        }
    }

    private void restoreExpandedState(long[] expandedIds) {
        this.expandedIds = expandedIds;
        if (expandedIds != null) {
            ExpandableListView lv = (ExpandableListView) getListView();
            TransactionCursorTreeAdapter adapter = (TransactionCursorTreeAdapter) lv.getExpandableListAdapter();
            if (adapter != null) {
                for (int i=0; i<adapter.getGroupCount(); i++) {
                    long id = adapter.getGroupId(i);
                    if (inArray(expandedIds, id)) lv.expandGroup(i);
                }
            }
        }
    }

    private static boolean inArray(long[] array, long element) {
        for (long l : array) {
            if (l == element) {
                return true;
            }
        }
        return false;
    }

    private static long[] toLongArray(List<Long> list)  {
        long[] ret = new long[list.size()];
        int i = 0;
        for (Long e : list)
            ret[i++] = e.longValue();
        return ret;
    }


}
