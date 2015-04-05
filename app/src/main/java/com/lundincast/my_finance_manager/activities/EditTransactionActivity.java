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
import android.util.Log;
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

public class EditTransactionActivity extends ListActivity implements TheListener {

    // TODO implement activity transitions

    private CategoriesDataSource categoryDatasource;
    private TransactionDataSource transactionDatasource;
    private Transaction transaction;
    private TextView priceTextView;
    private String selectedCategory;
    private Date selectedDate;
    private CategoryCursorAdapter adapter;

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

        // set elements from Transaction variables
        priceTextView = (TextView) findViewById(R.id.transaction_price);
        priceTextView.setText(Short.toString((short) transaction.getPrice()) + " €");
        selectedCategory = transaction.getCategory();

        EditText dateEditText = (EditText) findViewById(R.id.transaction_date);
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(transaction.getDate().getTime());
        dateEditText.setText(days[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", "
                + Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + " "
                + months[cal.get(Calendar.MONTH)] + " "
                + Integer.toString(cal.get(Calendar.YEAR)));

        EditText commentEditText = (EditText) findViewById(R.id.transaction_comment);
        commentEditText.setText(transaction.getComment());

        // set onClick listener on price TextView
        priceTextView = (TextView) findViewById(R.id.transaction_price);
        priceTextView.setOnClickListener(new View.OnClickListener() {
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
                                Log.w("FINANCE_MANAGER", userInput.getText().toString());
                                EditTransactionActivity.this.priceTextView.setText(userInput.getText().toString() + " €");
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
        super.onResume();
    }

    @Override
    protected void onPause() {
        categoryDatasource.close();
        transactionDatasource.close();
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
                        Intent intent = new Intent(getApplicationContext(), ListTransactionsActivity.class);
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
            short transacPrice = Short.parseShort(transacPriceString);
            String transacComment = transactionComment.getText().toString();

            Transaction transaction = new Transaction(this.transaction.getId(),
                    transacPrice, selectedCategory, selectedDate, transacComment);
            transactionDatasource.updateTransaction(transaction);

            Intent intent = new Intent(this, ListTransactionsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void returnDate(Date date) {
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        EditText transactionDate = (EditText) findViewById(R.id.transaction_date);
        transactionDate.setText(days[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", "
                + Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + " "
                + months[cal.get(Calendar.MONTH)] + " "
                + Integer.toString(cal.get(Calendar.YEAR)));
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
}
