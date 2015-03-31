package com.lundincast.my_finance_manager.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.model.Category;

import java.sql.SQLException;

public class EditCategoriesActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private CategoriesDataSource datasource;
    private Category category;
    private String color;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_categories);
        EditText text = (EditText) findViewById(R.id.category_name);
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
            text.setText((CharSequence) category.getName());
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                                                                    R.array.colors_array,
                                                                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            int spinnerPosition = adapter.getPosition(category.getColor());
            spinner.setAdapter(adapter);
            spinner.setSelection(spinnerPosition);
            spinner.setOnItemSelectedListener(this);
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
            new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        datasource.deleteCategory(category);
                        Intent intent = new Intent(getApplicationContext(), ListCategoriesActivity.class);
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
        if (id == R.id.accept_action) {
            final EditText categoryName = (EditText) findViewById(R.id.category_name);
            category.setName(categoryName.getText().toString());
            category.setColor(spinner.getSelectedItem().toString());
            datasource.updateCategory(category);
            Intent intent = new Intent(getApplicationContext(), ListCategoriesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


}
