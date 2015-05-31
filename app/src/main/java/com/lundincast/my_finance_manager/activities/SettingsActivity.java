package com.lundincast.my_finance_manager.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TimePicker;

import com.lundincast.my_finance_manager.R;

public class SettingsActivity extends PreferenceActivity {

    ListPreference currencyPref;
    SharedPreferences sharedPref;
    Preference timeOfDayPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // set currency preference
        ListPreference currencyPref = (ListPreference) findPreference("pref_key_currency");
        String test = sharedPref.getString("pref_key_currency", "1");
        if (test.equals("2")) {
            currencyPref.setSummary("Dollar");
        } else {
            currencyPref.setSummary("Euro");
        }

        // set currency dialog listener to get chosen value
        currencyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ListPreference pref = (ListPreference) preference;
                CharSequence[] values = pref.getEntries();
                pref.setSummary(values[Integer.parseInt((String) newValue) - 1]);
                return true;
            }
        });

        // Set onClick listener on Categories button to launch ListCategoriesActivity
        Preference categoryPref = (Preference) findPreference("pref_key_categories");
        categoryPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getApplicationContext(), ListCategoriesActivity.class);
                startActivity(intent);
                return false;
            }
        });


        timeOfDayPref = (Preference) findPreference("pref_key_time_day");
        // Retrieve time of day from sharedPref and set summary
        int hour = sharedPref.getInt("hour_of_day_alarm", 23);
        int minute = sharedPref.getInt("minute_of_day_alarm", 00);
        timeOfDayPref.setSummary(hour + "." + minute);
        // Set onClick listener on "Time of day" button to launch TimePicker
        timeOfDayPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.show(getFragmentManager(), "timePicker");
                return false;
            }
        });
        timeOfDayPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int hour = sharedPref.getInt("hour_of_day_alarm", 23);
                int minute = sharedPref.getInt("minute_of_day_alarm", 00);
                timeOfDayPref.setSummary(hour + "." + minute);
                return false;
            }
        });

        // set app version
        Preference versionPref = (Preference) findPreference("pref_key_about");
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionPref.setSummary("App version " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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



    @SuppressLint("ValidFragment")
    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        SharedPreferences sharedPref;

        public TimePickerFragment() {
            // Required empty public constructor
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);

            // check in SharedPref if hour is set and apply. If not, set 23.00
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int hour = sharedPref.getInt("hour_of_day_alarm", 23);
            int minute = sharedPref.getInt("minute_of_day_alarm", 00);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }


        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            // set variables in SharedPreference with chosen values
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("hour_of_day_alarm", hourOfDay);
            editor.putInt("minute_of_day_alarm", minute);
            editor.commit();
            // update Time of day summary with new time
            timeOfDayPref.setSummary(hourOfDay + "." + minute);
        }
    }

}
