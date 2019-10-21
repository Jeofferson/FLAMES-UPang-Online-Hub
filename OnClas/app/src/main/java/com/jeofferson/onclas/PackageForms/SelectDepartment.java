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

public class SelectDepartment extends AppCompatActivity {


    private String type;

    private Toolbar toolbar;

    private ListView selectDepartmentListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_select_department);

        type = getIntent().getStringExtra("type");

        selectDepartmentListView = findViewById(R.id.selectDepartmentListView);

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
        getSupportActionBar().setTitle(getResources().getString(R.string.selectDepartment));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void setUpAdapter() {

        final List<String> optionsDepartments;
        if (type.equals("SC Officer")) {

            optionsDepartments = new ArrayList<>(Arrays.asList("All (SSC)", "SHS", "CAS", "CEA", "CHS", "CITE", "CMA", "CSS"));

        } else {

            optionsDepartments = new ArrayList<>(Arrays.asList("SHS", "CAS", "CEA", "CHS", "CITE", "CMA", "CSS"));

        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SelectDepartment.this, R.layout.list_list_view_item, optionsDepartments);
        selectDepartmentListView.setAdapter(arrayAdapter);

        selectDepartmentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedDepartment = optionsDepartments.get(i);

                List<String> departments;

                // for those who can skip choosing of year...
                if (type.equals("Teacher") || type.equals("SC Officer") || selectedDepartment.equals("CAS")) {

                    // Student Council
                    if (selectedDepartment.equals("All (SSC)")) {

                        // SSC
                        departments = new ArrayList<>(Arrays.asList("All", "SHS", "CAS", "CEA", "CHS", "CITE", "CMA", "CSS"));

                    } else {

                        // Regular Student Council or Teacher or CAS Teacher/Student
                        departments = new ArrayList<>(Arrays.asList(selectedDepartment));

                    }

                    // Handled Year Levels
                    List<String> year;
                    if (selectedDepartment.equals("CAS")) {

                        year = new ArrayList<>(Arrays.asList("1"));

                    } else if (selectedDepartment.equals("SHS")) {

                        // Handled SHS Grade Levels
                        year = new ArrayList<>(Arrays.asList("11", "12"));

                    } else {

                        // Handled College Year Levels
                        year = new ArrayList<>(Arrays.asList("1", "2", "3", "4+"));

                    }

                    goToFinishSetUpAccount(type, departments, year);

                } else if (type.equals("Student")) {

                    departments = new ArrayList<>(Arrays.asList(selectedDepartment));

                    goToSelectYear(type, departments);

                }

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

        Intent intentFinishSetUpAccount = new Intent(SelectDepartment.this, FinishSetUpAccount.class);

        intentFinishSetUpAccount.putExtra("type", type);
        intentFinishSetUpAccount.putStringArrayListExtra("departments", (ArrayList<String>) departments);
        intentFinishSetUpAccount.putStringArrayListExtra("year", (ArrayList<String>) year);

        startActivity(intentFinishSetUpAccount);

    }


    public void goToSelectYear(String type, List<String> departments) {

        Intent intentSelectYear = new Intent(SelectDepartment.this, SelectYear.class);

        intentSelectYear.putExtra("type", type);
        intentSelectYear.putStringArrayListExtra("departments", (ArrayList<String>) departments);

        startActivity(intentSelectYear);

    }


    public void goToLogOut() {

        Intent intentLogOut = new Intent(SelectDepartment.this, LogOut.class);
        startActivity(intentLogOut);
        finish();

    }


}
