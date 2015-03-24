package com.lundincast.my_finance_manager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.lundincast.my_finance_manager.R;

public class SettingsActivity extends PreferenceActivity {

    ListPreference currencyPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // set currency preference
        ListPreference currencyPref = (ListPreference) findPreference("pref_key_currency");
        String test = sharedPref.getString("pref_key_currency", "1");
        if (test.equals("2")) {
            currencyPref.setSummary("Dollar");
        } else {
            currencyPref.setSummary("Euro");
        }
    //    sharedPref.edit().putString("Currency", currencyPref.getValue()).commit();
        // set category preference
        Preference categoryPref = (Preference) findPreference("pref_key_categories");
        categoryPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getApplicationContext(), ListCategoriesActivity.class);
                startActivity(intent);
                return false;
            }
        });


        currencyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ListPreference pref = (ListPreference) preference;
                CharSequence[] values = pref.getEntries();
                pref.setSummary(values[Integer.parseInt((String) newValue) - 1]);
                return true;
            }
        });



    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
