package com.lundincast.my_finance_manager.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.data.TransactionDataSource;
import com.lundincast.my_finance_manager.activities.fragments.ListTransactionsFragment;
import com.lundincast.my_finance_manager.activities.fragments.OverviewFragment;

import java.sql.SQLException;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    public TransactionDataSource datasource;
    public CategoriesDataSource catDatasource;
    public boolean firstOverviewFragInit = true;
    public int spinnerSelected = 1;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private static final String STATE_SELECTED_SPINNER_ITEM = "selected_spinner_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        getFragmentManager().beginTransaction()
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

                                break;
                            case 1:
                                String url = "https://github.com/Lundincast/My_Finance_Manager/issues/new";
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                            case 2:

                                break;
                        }
                    }
                }).show();
        }


        return super.onOptionsItemSelected(item);
    }

}
