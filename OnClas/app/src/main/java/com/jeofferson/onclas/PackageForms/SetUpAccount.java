package com.jeofferson.onclas.PackageForms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
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

public class SetUpAccount extends AppCompatActivity {


    private Toolbar toolbar;

    private ListView setUpAccountListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_set_up_account);

        setUpAccountListView = findViewById(R.id.setUpAccountListView);

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
        getSupportActionBar().setTitle(getResources().getString(R.string.setUpAccount));

    }

    public void setUpAdapter() {

        final List<String> optionsType = new ArrayList<>(Arrays.asList("Teacher", "SC Officer", "Student"));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SetUpAccount.this, R.layout.list_list_view_item, optionsType);
        setUpAccountListView.setAdapter(arrayAdapter);

        setUpAccountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedType = optionsType.get(i);

                goToSelectDepartment(selectedType);

            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menuSetUpAccountLogout:

                goToLogOut();

                break;

        }

        return true;

    }


    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.onclas_logo)
                .setTitle(getResources().getString(R.string.app_name))
                .setMessage(getResources().getString(R.string.doWantToExit))
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finishAffinity();

                    }

                })
                .setNegativeButton(getResources().getString(R.string.no), null)
                .show();

    }


    public void goToSelectDepartment(String type) {

        Intent intentSelectDepartment = new Intent(SetUpAccount.this, SelectDepartment.class);
        intentSelectDepartment.putExtra("type", type);
        startActivity(intentSelectDepartment);

    }


    public void goToLogOut() {

        Intent intentLogOut = new Intent(SetUpAccount.this, LogOut.class);
        startActivity(intentLogOut);
        finish();

    }


}
