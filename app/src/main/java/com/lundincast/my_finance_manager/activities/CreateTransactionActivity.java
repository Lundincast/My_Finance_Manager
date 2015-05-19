package com.lundincast.my_finance_manager.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.data.CategoryCursorAdapter;
import com.lundincast.my_finance_manager.activities.data.DbSQLiteHelper;
import com.lundincast.my_finance_manager.activities.data.TransactionDataSource;
import com.lundincast.my_finance_manager.activities.interfaces.TheListener;
import com.lundincast.my_finance_manager.activities.model.Transaction;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateTransactionActivity extends ListActivity implements TheListener {

    private CategoriesDataSource datasource;
    private TransactionDataSource transacDatasource;
    private TextView transactionPrice;
    private String selectedCategory;
    private Date selectedDate;
    private CategoryCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_transaction);

        datasource = new CategoriesDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        transacDatasource = new TransactionDataSource(this);
        try {
            transacDatasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Get Categories list cursor
        Cursor cursor = datasource.getAllCategories();

        adapter = new CategoryCursorAdapter(this, cursor);
        setListAdapter(adapter);

        transactionPrice = (TextView) findViewById(R.id.transaction_price);
        transactionPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDialog();
            }
        });

        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setSelector(android.R.color.darker_gray);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) adapter.getItem(position);
                String category = cursor.getString(cursor.getColumnIndex(DbSQLiteHelper.COLUMN_CATEGORY));
                CreateTransactionActivity.this.selectedCategory = category;
            }
        });

        // Set date to today by default
        TextView dateTextView = (TextView) findViewById(R.id.transaction_date);
        dateTextView.setText("Today");
        this.selectedDate = new Date();
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });


        // Launch dialog on create
        launchDialog();

    }

    private void launchDialog() {

        // get price_input_prompt.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View priceInputView = li.inflate(R.layout.price_input_prompt, null);

        // creating and building the price input dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(priceInputView);
        builder.setTitle("Set price");

        // set keyboard to numbers only to enter price
        final EditText userInput = (EditText) priceInputView.findViewById(R.id.editTextDialogPriceInput);
        userInput.setRawInputType(Configuration.KEYBOARD_12KEY);
        userInput.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5, 2)});

        // set dialog message
        builder
                .setCancelable(false)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                transactionPrice.setText(userInput.getText().toString() + " €");
                            }
                        });

        AlertDialog CategoryDialog = builder.create();
        CategoryDialog.show();
    }


    public void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }


    @Override
    protected void onResume() {
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            transacDatasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        transacDatasource.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_transaction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create) {
            // retrieve transaction input
            final TextView transactionPrice = (TextView) findViewById(R.id.transaction_price);
            final EditText transactionComment = (EditText) findViewById(R.id.transaction_comment);

            String transacPriceString = transactionPrice.getText().toString();
            transacPriceString = transacPriceString.substring(0, transacPriceString.length() - 2);
            double transacPrice = Double.parseDouble(transacPriceString);
            String transacComment = transactionComment.getText().toString();

            Transaction transaction = new Transaction(transacPrice, selectedCategory, selectedDate, transacComment);
            transacDatasource.createTransaction(transaction);

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void returnDate(Date date) {
        TextView transactionDate = (TextView) findViewById(R.id.transaction_date);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        transactionDate.setText(sdf.format(date));
        this.selectedDate = date;
    }

    // Inner class for creating the DatePickerFragment displayed for choosing date
    public static class DatePickerFragment extends DialogFragment
                                            implements DatePickerDialog.OnDateSetListener {

        TheListener listener;



        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            listener = (TheListener) getActivity();

            // create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            Calendar c = Calendar.getInstance();
            c.set(year, monthOfYear, dayOfMonth);
            Date date = c.getTime();

            if (listener != null) {
                listener.returnDate(date);
            }
        }
    }

    public class DecimalDigitsInputFilter implements InputFilter {

        Pattern mPattern;

        public DecimalDigitsInputFilter(int digitsBeforeZero,int digitsAfterZero) {
            mPattern=Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            Matcher matcher=mPattern.matcher(dest);
            if(!matcher.matches())
                return "";
            return null;
        }

    }

}


