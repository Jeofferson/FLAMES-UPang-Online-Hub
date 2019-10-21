package com.jeofferson.onclas.PackageActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.jeofferson.onclas.PackageForms.FinishSetUpAccount;
import com.jeofferson.onclas.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class PendingSetUpAccount extends AppCompatActivity {


    private FirebaseAuth mAuth;

    private Toolbar toolbar;

    private CircleImageView pendingSetUpAccountCircleImageViewUserPicture;
    private TextView pendingSetUpAccountTextViewUserFullName;
    private TextView pendingSetUpAccountTextViewUserFullType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_set_up_account);

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);

        pendingSetUpAccountCircleImageViewUserPicture = findViewById(R.id.pendingSetUpAccountCircleImageViewUserPicture);
        pendingSetUpAccountTextViewUserFullName = findViewById(R.id.pendingSetUpAccountTextViewUserFullName);
        pendingSetUpAccountTextViewUserFullType = findViewById(R.id.pendingSetUpAccountTextViewUserFullType);

        setStatusBar();

        setUpToolbar();

        Glide.with(PendingSetUpAccount.this).load(mAuth.getCurrentUser().getPhotoUrl()).placeholder(R.drawable.placeholder).into(pendingSetUpAccountCircleImageViewUserPicture);
        pendingSetUpAccountTextViewUserFullName.setText(mAuth.getCurrentUser().getDisplayName());
        pendingSetUpAccountTextViewUserFullType.setText(getIntent().getStringExtra("fullType"));

        pendingSetUpAccountCircleImageViewUserPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goToFullScreenImage(mAuth.getCurrentUser().getPhotoUrl());

            }
        });

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
        getSupportActionBar().setTitle(getResources().getString(R.string.finish));

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


    public void goToFullScreenImage(Uri uri) {

        Intent intentFullScreenImage = new Intent(PendingSetUpAccount.this, FullScreenImage.class);
        intentFullScreenImage.setData(uri);
        startActivity(intentFullScreenImage);

    }


    public void goToLogOut() {

        Intent intentLogOut = new Intent(PendingSetUpAccount.this, LogOut.class);
        startActivity(intentLogOut);
        finish();

    }


}
