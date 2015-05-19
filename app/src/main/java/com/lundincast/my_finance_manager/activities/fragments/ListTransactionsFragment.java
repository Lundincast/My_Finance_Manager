package com.lundincast.my_finance_manager.activities.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.EditTransactionActivity;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.data.DbSQLiteHelper;
import com.lundincast.my_finance_manager.activities.data.TransactionCursorTreeAdapter;
import com.lundincast.my_finance_manager.activities.data.TransactionDataSource;

import java.sql.SQLException;
import java.util.ArrayList;


public class ListTransactionsFragment extends ListFragment {

    private TransactionDataSource datasource;
    private CategoriesDataSource catDatasource;
    private Cursor cursor;
    TransactionCursorTreeAdapter adapter;
    SharedPreferences sharedPref;

    ArrayList<String> catFilter;



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        datasource = new TransactionDataSource(getActivity());
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        catDatasource = new CategoriesDataSource(getActivity());
        try {
            catDatasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        catFilter = catDatasource.getAllCategoriesStringList();

        cursor = datasource.getTransactionsGroupByUniqueMonthAndYear(null);
        getActivity().startManagingCursor(cursor);

        adapter = new TransactionCursorTreeAdapter(cursor, getActivity());
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
                Intent editIntent = new Intent(getActivity(), EditTransactionActivity.class);
                editIntent.putExtra("transactionId", transactionId);
                startActivityForResult(editIntent, 2);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return false;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_transactions, container, false);
    }

    @Override
    public void onPause() {
        datasource.close();
        super.onPause();
    }
}
