package com.jeofferson.onclas.PackageForms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jeofferson.onclas.PackageActivities.LogOut;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectYear extends AppCompatActivity {


    private String type;
    private List<String> departments;

    private Toolbar toolbar;

    private ListView selectYearListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_select_year);

        type = getIntent().getStringExtra("type");
        departments = getIntent().getStringArrayListExtra("departments");

        selectYearListView = findViewById(R.id.selectYearListView);

        setStatusBar();

        setUpToolbar();

        setUpAdapter();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_set_up_account, menu);
        return true;

    }


    public void setStatusBar() {

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

    }


    public void setUpToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (departments.get(0).equals("SHS")) {

            getSupportActionBar().setTitle(getResources().getString(R.string.selectGrade));

        } else {

            getSupportActionBar().setTitle(getResources().getString(R.string.selectYear));

        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    public void setUpAdapter() {

        final List<String> optionsYear;
        if (departments.get(0).equals("SHS")) {

            // SHS
            optionsYear = new ArrayList<>(Arrays.asList("11", "12"));

        } else {

            // College
            optionsYear = new ArrayList<>(Arrays.asList("2", "3", "4+"));

        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SelectYear.this, R.layout.list_list_view_item, optionsYear);
        selectYearListView.setAdapter(arrayAdapter);

        selectYearListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedYear = optionsYear.get(i);

                List<String> year = new ArrayList<>(Arrays.asList(selectedYear));

                goToFinishSetUpAccount(type, departments, year);

            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();
                break;

            case R.id.menuSetUpAccountLogout:

                goToLogOut();
                break;

        }

        return true;

    }


    public void goToFinishSetUpAccount(String type, List<String> departments, List<String> year) {

        Intent intentFinishSetUpAccount = new Intent(SelectYear.this, FinishSetUpAccount.class);

        intentFinishSetUpAccount.putExtra("type", type);
        intentFinishSetUpAccount.putStringArrayListExtra("departments", (ArrayList<String>) departments);
        intentFinishSetUpAccount.putStringArrayListExtra("year", (ArrayList<String>) year);

        startActivity(intentFinishSetUpAccount);

    }


    public void goToLogOut() {

        Intent intentLogOut = new Intent(SelectYear.this, LogOut.class);
        startActivity(intentLogOut);
        finish();

    }


}
