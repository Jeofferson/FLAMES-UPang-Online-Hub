package com.jeofferson.onclas.PackageForms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jeofferson.onclas.PackageActivities.UserActivity;
import com.jeofferson.onclas.PackageObjectModel.UserPhoto;
import com.jeofferson.onclas.R;

import java.util.Date;

public class AddUserPhoto extends AppCompatActivity {


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 14;
    static final int REQUEST_CODE_PHONE_GALLERY = 15;

    private FirebaseAuth mAuth;
    private String currentUserId;

    private StorageReference mStorageRef;
    private StorageReference userPhotoPath;

    private FirebaseFirestore db;
    private CollectionReference userPhotosCollection;

    private String userPhotoDownloadUrl;
    private @ServerTimestamp Date dateCreated;

    private Uri chosenImageUri = null;

    private ImageView addUserPhotoImageViewUserPhoto;
    private ImageView addUserPhotoImageViewRemoveUserPhoto;
    private ProgressBar addUserPhotoProgressBar;

    private TextView addUserPhotoTextViewErrorMessage;

    private Menu mMenu;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_add_user_photo);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        db = FirebaseFirestore.getInstance();
        userPhotosCollection = db.collection("UserPhotos");

        setUpToolbar();

        addUserPhotoImageViewUserPhoto = findViewById(R.id.addUserPhotoImageViewUserPhoto);
        addUserPhotoImageViewRemoveUserPhoto = findViewById(R.id.addUserPhotoImageViewRemoveUserPhoto);
        addUserPhotoProgressBar = findViewById(R.id.addUserPhotoProgressBar);

        addUserPhotoTextViewErrorMessage = findViewById(R.id.addUserPhotoTextViewErrorMessage);

        chosenImageUri = getIntent().getData();
        addUserPhotoImageViewUserPhoto.setImageURI(chosenImageUri);

        addUserPhotoImageViewRemoveUserPhoto.setVisibility(View.VISIBLE);

        addUserPhotoTextViewErrorMessage.setVisibility(View.GONE);

        addUserPhotoImageViewUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setUpReadExternalStoragePermission();

            }
        });

        addUserPhotoImageViewRemoveUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                chosenImageUri = null;

                addUserPhotoImageViewUserPhoto.setImageResource(R.drawable.placeholder_add_image2);
                addUserPhotoImageViewRemoveUserPhoto.setVisibility(View.GONE);

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_single_check, menu);
        mMenu = menu;
        return true;

    }


    public void setUpToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.addPhoto));

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menuSingleCheckDone:
                addUserPhotoToStorage();
                break;

        }

        return true;

    }


    public void setUpReadExternalStoragePermission() {

        if (Build.VERSION.SDK_INT >= 22) {

            if (ContextCompat.checkSelfPermission(AddUserPhoto.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(AddUserPhoto.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Toast.makeText(AddUserPhoto.this, "Permission Required", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(AddUserPhoto.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                } else {

                    ActivityCompat.requestPermissions(AddUserPhoto.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                }

            } else {

                openGallery();

            }

        } else {

            openGallery();

        }

    }


    public void openGallery() {

        Intent intentPhoneGallery = new Intent(Intent.ACTION_GET_CONTENT);
        intentPhoneGallery.setType("image/*");
        startActivityForResult(intentPhoneGallery, REQUEST_CODE_PHONE_GALLERY);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PHONE_GALLERY && resultCode == RESULT_OK && data != null) {

            chosenImageUri = data.getData();
            addUserPhotoImageViewUserPhoto.setImageURI(chosenImageUri);

            addUserPhotoImageViewRemoveUserPhoto.setVisibility(View.VISIBLE);

            addUserPhotoTextViewErrorMessage.setVisibility(View.GONE);

        }

    }


    public void addUserPhotoToStorage() {

        if (chosenImageUri != null) {

            mMenu.getItem(0).setEnabled(false);
            addUserPhotoProgressBar.setVisibility(View.VISIBLE);

            userPhotoPath = mStorageRef.child("userPhoto").child(System.currentTimeMillis() + ".jpg");
            userPhotoPath.putFile(chosenImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        userPhotoPath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    userPhotoDownloadUrl = task.getResult().toString();
                                    addUserPhotoToDatabase();

                                } else {

                                    addUserPhotoProgressBar.setVisibility(View.GONE);
                                    mMenu.getItem(0).setEnabled(true);
                                    Toast.makeText(AddUserPhoto.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                                }

                            }
                        });

                    } else {

                        addUserPhotoProgressBar.setVisibility(View.GONE);
                        mMenu.getItem(0).setEnabled(true);
                        Toast.makeText(AddUserPhoto.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                    }

                }
            });

        } else {

            addUserPhotoTextViewErrorMessage.setVisibility(View.VISIBLE);

        }

    }


    public void addUserPhotoToDatabase() {

        UserPhoto userPhoto = new UserPhoto(currentUserId, userPhotoDownloadUrl, dateCreated);

        userPhotosCollection.add(userPhoto).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if (task.isSuccessful()) {

                    addUserPhotoProgressBar.setVisibility(View.GONE);
                    goToUserActivity(currentUserId);

                } else {

                    addUserPhotoProgressBar.setVisibility(View.GONE);
                    mMenu.getItem(0).setEnabled(true);
                    Toast.makeText(AddUserPhoto.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    public void goToUserActivity(String userId) {

        Intent intentUserActivity = new Intent(AddUserPhoto.this, UserActivity.class);
        intentUserActivity.putExtra("userId", userId);
        intentUserActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentUserActivity);

    }


}
