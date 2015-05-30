package com.lundincast.my_finance_manager.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.data.TransactionDataSource;
import com.lundincast.my_finance_manager.activities.model.Category;

import java.sql.SQLException;

public class EditCategoriesActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private CategoriesDataSource datasource;
    private Category category;
    private String color;
    private EditText nameEditText;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_categories);
        nameEditText = (EditText) findViewById(R.id.category_name);
        spinner = (Spinner) findViewById(R.id.category_color);

        datasource = new CategoriesDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // retrieve item data from intent and displays it in EditText and spinner
        Intent incomingIntent = getIntent();
        Bundle extras = incomingIntent.getExtras();
        if (extras != null)
        {
            long id = extras.getLong("categoryId");
            this.category = datasource.getCategory(id);
            nameEditText.setText((CharSequence) category.getName());
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                                                                    R.array.colors_array,
                                                                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            int spinnerPosition = adapter.getPosition(category.getColor());
            spinner.setAdapter(adapter);
            spinner.setSelection(spinnerPosition);
            spinner.setOnItemSelectedListener(this);
            spinner.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
                    return false;
                }
            });
        }

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        color = (String) parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_categories, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete_action) {
            final TransactionDataSource transacDS = new TransactionDataSource(this);
            try {
                transacDS.open();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            final Cursor cursor = transacDS.getTransactionsPerCategory(category.getName());
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        datasource.deleteCategory(category);
                        if (cursor.getCount() > 0) {
                            transacDS.deleteAllRowsByCategory(category);
                            Toast.makeText(getApplicationContext(), cursor.getCount() + " transaction(s) deleted", Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(getApplicationContext(), ListCategoriesActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            if (cursor.getCount() == 0) {
                builder.setMessage("Are you sure ?");
            } else {
                builder.setMessage(String.valueOf(cursor.getCount()) + " transaction(s) pertain to this category. Note that" +
                        " deleting a category will also delete all transactions associated with it.");
            }
            builder.show();
        }
        if (id == R.id.accept_action) {
            final EditText categoryName = (EditText) findViewById(R.id.category_name);
            if (categoryName.getText().toString().equals("")) {
                Toast.makeText(this, "Category name can't be empty", Toast.LENGTH_SHORT).show();
            } else {
                category.setName(categoryName.getText().toString());
                category.setColor(spinner.getSelectedItem().toString());
                datasource.updateCategory(category);
                Intent intent = new Intent(getApplicationContext(), ListCategoriesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                EditCategoriesActivity.this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        }

        return super.onOptionsItemSelected(item);
    }


}
