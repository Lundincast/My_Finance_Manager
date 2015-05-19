package com.lundincast.my_finance_manager.activities;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.fragments.ListTransactionsFragment;
import com.lundincast.my_finance_manager.activities.fragments.OverviewFragment;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            actionBar.setSelectedNavigationItem(id);
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


        return super.onOptionsItemSelected(item);
    }


}
