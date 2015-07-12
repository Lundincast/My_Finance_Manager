package com.lundincast.my_finance_manager.activities;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.BroadcastReceivers.NotificationAlarmReceiver;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.data.FirstTimeDataBaseHelper;
import com.lundincast.my_finance_manager.activities.data.TransactionDataSource;
import com.lundincast.my_finance_manager.activities.fragments.ListTransactionsFragment;
import com.lundincast.my_finance_manager.activities.fragments.OverviewFragment;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    public TransactionDataSource datasource;
    public CategoriesDataSource catDatasource;
    public boolean firstOverviewFragInit = true;
    public int spinnerSelected = 0;
    SharedPreferences sharedPref;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private static final String STATE_SELECTED_SPINNER_ITEM = "selected_spinner_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if a database file exists already. If it doesn't, it's the first time the
        // app is launch so we create a db and load the shipped database file situated in the
        // asset folder
        FirstTimeDataBaseHelper dbHelper = new FirstTimeDataBaseHelper(this);
        try {

            dbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        // Initialize database connection, centralized in MainActivity and
        // available to all fragments
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


        // set up the actionbar to show tabs
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // add 2 tabs: Overview and List
        actionBar.addTab(actionBar.newTab().setText("Overview").setTabListener(this), true);
        actionBar.addTab(actionBar.newTab().setText("List").setTabListener(this));

        // display specific tab if specified in Intent
        Intent incomingIntent = getIntent();
        Bundle extras = incomingIntent.getExtras();
        if (extras != null) {
            int id = extras.getInt("selectedTab");
            //actionBar.setSelectedNavigationItem(id);
        }

        // Set intent to be broadcast for reminder
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = new Intent(this, NotificationAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Set calendar to time defined in preferences
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, sharedPref.getInt("hour_of_day_alarm", 23));
        cal.set(Calendar.MINUTE, sharedPref.getInt("minute_of_day_alarm", 00));
        // Compare it with current time. If it is lower, set DAY + 1
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(System.currentTimeMillis());
        if (cal.getTimeInMillis() < cal2.getTimeInMillis()) {
            cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
        }
        // Set AlarmManager to trigger broadcast
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current tab position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
                .getSelectedNavigationIndex());
        outState.putInt(STATE_SELECTED_SPINNER_ITEM, this.spinnerSelected);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current tab position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        } else {
            getActionBar().setSelectedNavigationItem(0);
        }
        // Restore the previously serialized spinner item selected
        if (savedInstanceState.containsKey(STATE_SELECTED_SPINNER_ITEM)) {
            this.spinnerSelected = savedInstanceState.getInt(STATE_SELECTED_SPINNER_ITEM);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        Fragment fragment;

        if (tab.getPosition() == 0) {
            fragment = new OverviewFragment();
        } else {
            fragment = new ListTransactionsFragment();
        }
        getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment)
                            .commit();

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if (id == R.id.action_feedback) {
            new AlertDialog.Builder(this)
                .setItems(R.array.feedbackoptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                String rateAppUrl = "https://play.google.com/store/apps/details?id=com.lundincast.my_finance_manager";
                                Intent rateAppIntent = new Intent(Intent.ACTION_VIEW);
                                rateAppIntent.setData(Uri.parse(rateAppUrl));
                                startActivity(rateAppIntent);
                                break;
                            case 1:
                                String url = "http://lundincast.com";
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                                break;
                        }
                    }
                }).show();
        }


        return super.onOptionsItemSelected(item);
    }

}
