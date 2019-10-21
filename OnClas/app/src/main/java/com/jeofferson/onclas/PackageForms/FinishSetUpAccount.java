package com.jeofferson.onclas.PackageForms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.jeofferson.onclas.PackageActivities.FullScreenImage;
import com.jeofferson.onclas.PackageActivities.LogOut;
import com.jeofferson.onclas.PackageActivities.PendingSetUpAccount;
import com.jeofferson.onclas.PackageObjectModel.AdminAccountRequestHolder;
import com.jeofferson.onclas.R;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FinishSetUpAccount extends AppCompatActivity {


    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private CollectionReference generalSearchListCollection;
    private CollectionReference adminAccountRequestsCollection;

    private String type;
    private List<String> departments;
    private List<String> year;
    private String fullType;
    private Date dateCreated;

    private Toolbar toolbar;
    private ProgressBar finishSetUpAccountProgressBar;

    private ImageView finishSetUpAccountCircleImageViewUserPicture;
    private TextView finishSetUpAccountTextViewUserFullName;
    private TextView finishSetUpAccountTextViewUserFullType;

    private TextView finishSetUpAccountButtonRequestAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_finish_set_up_account);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("Users");
        generalSearchListCollection = db.collection("GeneralSearchList");
        adminAccountRequestsCollection = db.collection("AdminAccountRequests");

        type = getIntent().getStringExtra("type");
        departments = getIntent().getStringArrayListExtra("departments");
        year = getIntent().getStringArrayListExtra("year");
        fullType = "";
        generateFullType();

        toolbar = findViewById(R.id.toolbar);
        finishSetUpAccountProgressBar = findViewById(R.id.finishSetUpAccountProgressBar);

        finishSetUpAccountCircleImageViewUserPicture = findViewById(R.id.finishSetUpAccountCircleImageViewUserPicture);
        finishSetUpAccountTextViewUserFullName = findViewById(R.id.finishSetUpAccountTextViewUserFullName);
        finishSetUpAccountTextViewUserFullType = findViewById(R.id.finishSetUpAccountTextViewUserFullType);

        finishSetUpAccountButtonRequestAccount = findViewById(R.id.finishSetUpAccountButtonRequestAccount);

        setStatusBar();

        setUpToolbar();

        Glide.with(FinishSetUpAccount.this).load(mAuth.getCurrentUser().getPhotoUrl()).placeholder(R.drawable.placeholder).into(finishSetUpAccountCircleImageViewUserPicture);
        finishSetUpAccountTextViewUserFullName.setText(mAuth.getCurrentUser().getDisplayName());
        finishSetUpAccountTextViewUserFullType.setText(fullType);

        finishSetUpAccountCircleImageViewUserPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goToFullScreenImage(mAuth.getCurrentUser().getPhotoUrl());

            }
        });

        finishSetUpAccountButtonRequestAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finishSetUpAccountProgressBar.setVisibility(View.VISIBLE);

                generalSearchListCollection
                        .whereEqualTo("itemId", mAuth.getCurrentUser().getUid())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                if (!queryDocumentSnapshots.isEmpty()) {

                                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                        DocumentReference userReference = usersCollection.document(mAuth.getCurrentUser().getUid());
                                        DocumentReference generalSearchItemReference = queryDocumentSnapshot.getReference();

                                        WriteBatch writeBatch = db.batch();

                                        writeBatch.update(userReference, "type", type);
                                        writeBatch.update(generalSearchItemReference, "type", type);

                                        writeBatch.update(userReference, "departments", departments);
                                        writeBatch.update(generalSearchItemReference, "departments", departments);

                                        writeBatch.update(userReference, "year", year);
                                        writeBatch.update(generalSearchItemReference, "year", year);

                                        writeBatch.update(userReference, "fullType", fullType);
                                        writeBatch.update(generalSearchItemReference, "fullType", fullType);

                                        writeBatch.update(userReference, "authorized", false);

                                        AdminAccountRequestHolder adminAccountRequest = new AdminAccountRequestHolder(userReference, generalSearchItemReference, mAuth.getCurrentUser().getPhotoUrl().toString(), mAuth.getCurrentUser().getDisplayName(), fullType, dateCreated, false);
                                        writeBatch.set(adminAccountRequestsCollection.document(), adminAccountRequest);

                                        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    goToPendingSetUpAccount(fullType);

                                                } else {

                                                    Toast.makeText(FinishSetUpAccount.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                }

                                            }
                                        });

                                    }

                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(FinishSetUpAccount.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_set_up_account, menu);
        return true;

    }


    public void generateFullType() {

        if (departments.size() > 1) {

            fullType += "SSC Officer";
            return;

        } else if (type.equals("SC Officer")) {

            fullType += departments.get(0) + " SC Officer";
            return;

        }

        if (year.size() == 1) {

            if (!type.equals("Teacher")) {

                String rawYear = year.get(0);
                switch (rawYear) {

                    case "11":
                    case "12":

                        fullType += "Grade " + rawYear + " ";
                        break;

                    case "1":

                        fullType += rawYear + "st Year ";
                        break;

                    case "2":

                        fullType += rawYear + "nd Year ";
                        break;

                    case "3":

                        fullType += rawYear + "rd Year ";
                        break;

                    case "4+":

                        fullType += rawYear + " Year ";
                        break;

                }

            }

        }

        fullType += departments.get(0) + " ";

        fullType += type;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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


    public void goToFullScreenImage(Uri uri) {

        Intent intentFullScreenImage = new Intent(FinishSetUpAccount.this, FullScreenImage.class);
        intentFullScreenImage.setData(uri);
        startActivity(intentFullScreenImage);

    }


    public void goToPendingSetUpAccount(String fullType) {

        Intent intentPendingSetUpAccount = new Intent(FinishSetUpAccount.this, PendingSetUpAccount.class);
        intentPendingSetUpAccount.putExtra("fullType", fullType);
        startActivity(intentPendingSetUpAccount);
        finish();

    }


    public void goToLogOut() {

        Intent intentLogOut = new Intent(FinishSetUpAccount.this, LogOut.class);
        startActivity(intentLogOut);
        finish();

    }


}
