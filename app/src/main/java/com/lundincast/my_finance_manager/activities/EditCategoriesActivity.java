package com.lundincast.my_finance_manager.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.data.CategoriesDataSource;
import com.lundincast.my_finance_manager.activities.model.Category;

import java.sql.SQLException;

public class EditCategoriesActivity extends ActionBarActivity {

    private CategoriesDataSource datasource;
    private Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_categories);
        EditText text = (EditText) findViewById(R.id.category_name);

        datasource = new CategoriesDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // retrieve item data from intent and displays it in EditText
        Intent incomingIntent = getIntent();
        Bundle extras = incomingIntent.getExtras();
        if (extras != null)
        {
            long id = extras.getLong("categoryId");
            this.category = datasource.getCategory(id);
            text.setText((CharSequence) category.getName());
        }
    }

    public void onClick(View view) {
        final EditText categoryName = (EditText) findViewById(R.id.category_name);
        category.setName(categoryName.getText().toString());
        datasource.updateCategory(category);
        Intent intent = new Intent(getApplicationContext(), ListCategoriesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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

        return super.onOptionsItemSelected(item);
    }
}