package com.lundincast.my_finance_manager.activities.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.MainActivity;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.data.MyExpandableItemAdapter;
import com.lundincast.my_finance_manager.activities.data.TransactionCursorTreeAdapter;
import com.lundincast.my_finance_manager.activities.data.TransactionDataSource;
import com.lundincast.my_finance_manager.activities.data.TransactionExpandableDataProvider;

import java.util.ArrayList;


public class ListTransactionsFragment extends Fragment {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    private TransactionDataSource datasource;
    private CategoriesDataSource catDatasource;
    private Cursor cursor;
    TransactionCursorTreeAdapter adapter;
    SharedPreferences sharedPref;

    ArrayList<String> catFilter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;


    public ListTransactionsFragment() {
        super();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_transactions, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());

        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);

        // Get cursor
        MainActivity activity = (MainActivity) getActivity();
        Cursor cursor = activity.datasource.getAllTransaction();
        final MyExpandableItemAdapter myItemAdapter = new MyExpandableItemAdapter(getActivity(), new TransactionExpandableDataProvider(getActivity(), cursor));

        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(myItemAdapter);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setHasFixedSize(false);

        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current state to support screen rotation, etc...
        if (mRecyclerViewExpandableItemManager != null) {
            outState.putParcelable(
                    SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
                    mRecyclerViewExpandableItemManager.getSavedState());
        }
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewExpandableItemManager != null) {
            mRecyclerViewExpandableItemManager.release();
            mRecyclerViewExpandableItemManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        super.onDestroyView();
    }


    //    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        datasource = new TransactionDataSource(getActivity());
//        try {
//            datasource.open();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        catDatasource = new CategoriesDataSource(getActivity());
//        try {
//            catDatasource.open();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        catFilter = catDatasource.getAllCategoriesStringList();
//
//        cursor = datasource.getTransactionsGroupByUniqueMonthAndYear(null);
//        adapter = new TransactionCursorTreeAdapter(cursor, getActivity());
//        final ExpandableListView lv = (ExpandableListView) getListView();
//        lv.setAdapter(adapter);
//        lv.expandGroup(0);
//        lv.setSaveEnabled(true);
//        lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//
//                CursorTreeAdapter adapter = (CursorTreeAdapter) lv.getExpandableListAdapter();
//                Cursor adapterCursor = adapter.getChild(groupPosition, childPosition);
//                long transactionId = adapterCursor.getLong(adapterCursor.getColumnIndex(DbSQLiteHelper.TRANSACTION_ID));
//                // We only pass transaction id to EditActivity, it'll instantiate the object from there
//                Intent editIntent = new Intent(getActivity(), EditTransactionActivity.class);
//                editIntent.putExtra("transactionId", transactionId);
//                startActivityForResult(editIntent, 2);
//                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//                return false;
//            }
//        });
//    }



//    @Override
//    public void onPause() {
//        datasource.close();
//        super.onPause();
//    }
}
