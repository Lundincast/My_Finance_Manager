package com.lundincast.my_finance_manager.activities.fragments;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.MainActivity;
import com.lundincast.my_finance_manager.activities.data.DbSQLiteHelper;
import com.lundincast.my_finance_manager.activities.model.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class OverviewFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private PieChart mChart;
    private MainActivity activity;
    private String[] monthsComplete = {"January", "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December"};
    private String month;
    private String year;
    private ArrayList<String> values;
    private int selected;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_overview, container, false);

        Spinner spinner = (Spinner) v.findViewById(R.id.timeline_spinner);
        Cursor cursor = activity.datasource.getTransactionsGroupByUniqueMonthAndYear(null);
        if (cursor.getCount() == 0) {
            spinner.setVisibility(View.GONE);
        } else {
            // Set spinner data from Transaction table
            values = new ArrayList<>();
            while (cursor.moveToNext()) {
                values.add(monthsComplete[Integer.parseInt(cursor.getString(1))] + " " + cursor.getString(2));
            }
            String[] from = new String[]{DbSQLiteHelper.TRANSACTION_MONTH,
                    DbSQLiteHelper.TRANSACTION_YEAR};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, values);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
            spinner.setSelection(activity.spinnerSelected);
        }

        // set up chart
        mChart = (PieChart) v.findViewById(R.id.chart);
        mChart.setDescription("");

        // radius of the center hole in percent of maximum radius
        mChart.setHoleRadius(50f);
        mChart.setTransparentCircleRadius(55f);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);

        mChart.setData(generatePieData(new Date()));

        // Animate on first time init only
        if (activity.firstOverviewFragInit) {
            mChart.animateX(1000);
        }

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.firstOverviewFragInit = false;
        activity.spinnerSelected = selected;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // update selected item tracker in MainActivity
        this.selected = position;

        String selected = (String) parent.getItemAtPosition(position);
        String[] splited = selected.split("\\s+");
        Date date = new Date(Integer.parseInt(splited[1]), Arrays.asList(monthsComplete).indexOf(splited[0]), 1);
        mChart.setData(generatePieData(date));
        mChart.invalidate();
        if (activity.firstOverviewFragInit) {
            mChart.animateX(1000);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    /**
     * generates data
     *
     * @return PieData object
     */
    protected PieData generatePieData(Date date) {

        ArrayList<String> values = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();
        int position;

        Cursor cursor = ((MainActivity) getActivity()).datasource.getTransactionsByMonth(date);
        if (!(cursor.moveToFirst()) || cursor.getCount() ==0){
            return null;
        } else {
            do {
                // extract category from cursor row
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_CATEGORY));
                // search in values ArrayList if category exists. If it does, get position.
                // If it doesn't, add category and get position
                if (!values.contains(category)) {
                    values.add(category);
                }
            } while (cursor.moveToNext());
            int length = values.size();
            double[] totals = new double[length];
            cursor.moveToFirst();
            do {
                // extract price from cursor row
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_PRICE));
                // extract category from cursor row
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_CATEGORY));
                // Get category position in values ArrayList
                position = values.indexOf(category);
                // fill totals array with prices. Insert if new, add to value if exists.
                totals[position] = totals[position] + price;
            } while (cursor.moveToNext());

            for (int i = 0; i < totals.length; i++) {
                entries.add(new Entry((float) totals[i], i));

            }

            PieDataSet dataSet = new PieDataSet(entries, "");
            // build colors array from resources
            int[] colorsArray = new int[values.size()];
            Category cat;
            String colorValue;
            Resources res = getResources();
            for (int i = 0; i < values.size(); i++) {
                cat = activity.catDatasource.getCategoryByName(values.get(i));
                if (cat.getColor().contains(" ")) {
                    colorValue = cat.getColor().replaceAll(" ", "_");
                } else {
                    colorValue = cat.getColor();
                }

                colorsArray[i] = res.getColor(getResources().getIdentifier(colorValue, "color", "com.lundincast.my_finance_manager"));
            }
            dataSet.setColors(colorsArray);
            dataSet.setSliceSpace(1f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(12f);

            return new PieData(values, dataSet);
        }
    }

}





