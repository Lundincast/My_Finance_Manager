package com.lundincast.my_finance_manager.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditTransactionActivity extends ListActivity implements TheListener {

    private CategoriesDataSource categoryDatasource;
    private TransactionDataSource transactionDatasource;
    private Transaction transaction;
    private TextView priceTextView;
    private String selectedCategory;
    private Date selectedDate;
    private CategoryCursorAdapter adapter;
    private TextView transactionPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        transactionDatasource = new TransactionDataSource(this);
        try {
            transactionDatasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        categoryDatasource = new CategoriesDataSource(this);
        try {
            categoryDatasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // retrieve transactionId from intent and instantiate object
        Intent incomingIntent = getIntent();
        Bundle extras = incomingIntent.getExtras();
        if (extras != null) {
            long id = extras.getLong("transactionId");
            this.transaction = transactionDatasource.getTransaction(id);
        }

        // get preferences for currency display
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String currPref = sharedPref.getString("pref_key_currency", "1");

        // set elements from Transaction variables
        priceTextView = (TextView) findViewById(R.id.transaction_price);
        double transacPrice = transaction.getPrice();
        if (currPref.equals("2")) {
            priceTextView.setText(String.format("%.2f", transacPrice) + " $");
        } else {
            priceTextView.setText(String.format("%.2f", transacPrice) + " €");
        }
        selectedCategory = transaction.getCategory();

        TextView dateTextView = (TextView) findViewById(R.id.transaction_date);
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(transaction.getDate().getTime());
        dateTextView.setText(days[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", "
                + Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + " "
                + months[cal.get(Calendar.MONTH)] + " "
                + Integer.toString(cal.get(Calendar.YEAR)));
        selectedDate = transaction.getDate();
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        EditText commentEditText = (EditText) findViewById(R.id.transaction_comment);
        commentEditText.setText(transaction.getComment());

        // set onClick listener on price TextView
        transactionPrice = (TextView) findViewById(R.id.transaction_price);
        transactionPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDialog();
            }
        });


        // get categories list cursor
        Cursor cursor = categoryDatasource.getAllCategories();

        adapter = new CategoryCursorAdapter(this, cursor);
        setListAdapter(adapter);

        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setSelector(android.R.color.darker_gray);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) adapter.getItem(position);
                String category = cursor.getString(cursor.getColumnIndex(DbSQLiteHelper.COLUMN_CATEGORY));
                EditTransactionActivity.this.selectedCategory = category;

            }
        });
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
        userInput.setText(String.format("%.2f", transaction.getPrice()));

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
                                if (!userInput.getText().toString().equals("")) {
                                    transactionPrice.setText(userInput.getText().toString() + " €");
                                } else {
                                    transactionPrice.setText("00.00 €");
                                }
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
            categoryDatasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            transactionDatasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetInvalidated();
        super.onResume();
    }

    @Override
    protected void onPause() {
//        categoryDatasource.close();
//        transactionDatasource.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_transaction, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete_action) {
            new AlertDialog.Builder(this)
                .setTitle("Delete transaction")
                .setMessage("Are you sure ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        transactionDatasource.delete(transaction);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
        }
        if (id == R.id.update_action) {
            // retrieve transaction input
            final TextView transactionPrice = (TextView) findViewById(R.id.transaction_price);
            final EditText transactionComment = (EditText) findViewById(R.id.transaction_comment);

            String transacPriceString = transactionPrice.getText().toString();
            transacPriceString = transacPriceString.substring(0, transacPriceString.length() - 2);
            double transacPrice = Double.parseDouble(transacPriceString);
            String transacComment = transactionComment.getText().toString();

            Transaction transaction = new Transaction(this.transaction.getId(),
                    transacPrice, selectedCategory, selectedDate, transacComment);
            transactionDatasource.updateTransaction(transaction);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("selectedTab", 1);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            EditTransactionActivity.this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void returnDate(Date date) {
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        TextView transactionDate = (TextView) findViewById(R.id.transaction_date);
        transactionDate.setText(days[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", "
                + Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + " "
                + months[cal.get(Calendar.MONTH)] + " "
                + Integer.toString(cal.get(Calendar.YEAR)));
        this.selectedDate = date;
    }

    // Inner class for creating the DatePickerFragment displayed for choosing date
    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        TheListener listener;



        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // use the current date as the default date in the picker
            Calendar c = Calendar.getInstance();
            c.setTime(selectedDate);
            listener = (TheListener) getActivity();
            // create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
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
