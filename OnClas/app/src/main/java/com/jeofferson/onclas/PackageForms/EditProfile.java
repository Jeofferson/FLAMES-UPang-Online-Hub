package com.jeofferson.onclas.PackageForms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jeofferson.onclas.PackageActivities.MainActivity;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;
    static final int REQUEST_CODE_PHONE_GALLERY_USER_PICTURE = 6;
    static final int REQUEST_CODE_PHONE_GALLERY_USER_COVER = 7;

    private FirebaseAuth mAuth;

    private StorageReference mStorageRef;
    private StorageReference userPicturePath;
    private StorageReference userCoverPath;

    private FirebaseFirestore db;

    private CollectionReference commentHoldersCollection;
    private CollectionReference commentLikersCollection;
    private CollectionReference commentsCollection;
    private CollectionReference generalSearchListCollection;
    private CollectionReference newsfeedCollection;
    private CollectionReference notificationsCollection;
    private CollectionReference postLikersCollection;
    private CollectionReference postsCollection;
    private CollectionReference replyHoldersCollection;
    private CollectionReference replyLikersCollection;
    private CollectionReference usersCollection;

    private List<WriteBatch> writeBatchList;
    private int writeBatchListIndex = 0;
    private int writeBatchCommittedCounter = 0;

    private List<CollectionReference> collectionReferenceList;
    private List<ArrayList<String>> documentFieldList;

    private int collectionsCounter = 0;
    private int databaseChangesCounter = 0;

    private UserProfileChangeRequest userProfileChangeRequest;

    private String initialUserPicture;
    private String newUserPicture;

    private String initialUserCover;
    private String newUserCover;

    private String initialUserBio;
    private String newUserBio;

    private Uri chosenUserPictureUri = null;

    private Uri chosenUserCoverUri = null;

    private Toolbar toolbar;
    private Menu mMenu;
    private ProgressDialog progressDialog;
//    private ProgressBar editProfileProgressBar;

    private CircleImageView editProfileCircleImageViewUserPicture;
    private ImageView editProfileImageViewUserCover;
    private EditText editProfileEditTextUserBio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_edit_profile);

        mAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("Users");
        generalSearchListCollection = db.collection("GeneralSearchList");

        commentHoldersCollection = db.collection("CommentHolders");
        commentLikersCollection = db.collection("CommentLikers");
        commentsCollection = db.collection("Comments");
        newsfeedCollection = db.collection("Newsfeed");
        notificationsCollection = db.collection("Notifications");
        postLikersCollection = db.collection("PostLikers");
        postsCollection = db.collection("Posts");
        replyHoldersCollection = db.collection("ReplyHolders");
        replyLikersCollection = db.collection("ReplyLikers");

        writeBatchList = new ArrayList<>(Arrays.asList(db.batch()));

        collectionReferenceList = new ArrayList<>(Arrays.asList(
                commentHoldersCollection,
                commentLikersCollection,
                commentsCollection,
                generalSearchListCollection,
                newsfeedCollection,
                notificationsCollection,
                notificationsCollection,
                postLikersCollection,
                postsCollection,
                replyHoldersCollection,
                replyLikersCollection,
                usersCollection
        ));

        documentFieldList = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("commenter1Id", "commenterPicture")),     // 0
                new ArrayList<>(Arrays.asList("itemId", "itemPicture")),                // 1
                new ArrayList<>(Arrays.asList("commenter1Id", "commenterPicture")),     // 2
                new ArrayList<>(Arrays.asList("itemId", "itemPicture")),                // 3
                new ArrayList<>(Arrays.asList("poster1Id", "posterPicture")),           // 4
                new ArrayList<>(Arrays.asList("fromId", "fromPicture")),                // 5
                new ArrayList<>(Arrays.asList("toId", "toPicture")),                    // 6
                new ArrayList<>(Arrays.asList("itemId", "itemPicture")),                // 7
                new ArrayList<>(Arrays.asList("poster1Id", "posterPicture")),           // 8
                new ArrayList<>(Arrays.asList("replier1Id", "replierPicture")),         // 9
                new ArrayList<>(Arrays.asList("itemId", "itemPicture")),                // 10
                new ArrayList<>(Arrays.asList("userPicture", "userPicture"))            // 11
        ));

        initialUserPicture = getIntent().getStringExtra("userPicture");
        initialUserCover = getIntent().getStringExtra("userCover");
        initialUserBio = getIntent().getStringExtra("userBio");

//        editProfileProgressBar = findViewById(R.id.editProfileProgressBar);

        editProfileCircleImageViewUserPicture = findViewById(R.id.editProfileCircleImageViewUserPicture);
        editProfileImageViewUserCover = findViewById(R.id.editProfileImageViewUserCover);
        editProfileEditTextUserBio = findViewById(R.id.editProfileEditTextUserBio);

        Glide.with(EditProfile.this).load(initialUserPicture).placeholder(R.drawable.placeholder).into(editProfileCircleImageViewUserPicture);
        Glide.with(EditProfile.this).load(initialUserCover).placeholder(R.drawable.placeholder).into(editProfileImageViewUserCover);
        editProfileEditTextUserBio.setText(initialUserBio);

        setUpToolbar();

        editProfileCircleImageViewUserPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setUpReadExternalStoragePermission(REQUEST_CODE_PHONE_GALLERY_USER_PICTURE);

            }
        });

        editProfileImageViewUserCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setUpReadExternalStoragePermission(REQUEST_CODE_PHONE_GALLERY_USER_COVER);

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
        getSupportActionBar().setTitle(getResources().getString(R.string.editProfile));

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menuSingleCheckDone:
                addUserPictureToStorage();
                break;

        }

        return true;

    }


    public void setUpReadExternalStoragePermission(int myRequestCode) {

        if (Build.VERSION.SDK_INT >= 22) {

            if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(EditProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Toast.makeText(EditProfile.this, "Permission Required", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(EditProfile.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                } else {

                    ActivityCompat.requestPermissions(EditProfile.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                }

            } else {

                openGallery(myRequestCode);

            }

        } else {

            openGallery(myRequestCode);

        }

    }


    public void openGallery(int myRequestCode) {

        Intent intentPhoneGallery = new Intent(Intent.ACTION_GET_CONTENT);
        intentPhoneGallery.setType("image/*");
        startActivityForResult(intentPhoneGallery, myRequestCode);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PHONE_GALLERY_USER_PICTURE && resultCode == RESULT_OK && data != null) {

            chosenUserPictureUri = data.getData();
            editProfileCircleImageViewUserPicture.setImageURI(chosenUserPictureUri);

        } else if (requestCode == REQUEST_CODE_PHONE_GALLERY_USER_COVER && resultCode == RESULT_OK && data != null) {

            chosenUserCoverUri = data.getData();
            editProfileImageViewUserCover.setImageURI(chosenUserCoverUri);

        }

    }


    public void addUserPictureToStorage() {

        mMenu.getItem(0).setEnabled(false);

        progressDialog = ProgressDialog.show(EditProfile.this, getResources().getString(R.string.pleaseWait), getResources().getString(R.string.updatingProfile), true);
//        editProfileProgressBar.setVisibility(View.VISIBLE);

        if (chosenUserPictureUri != null) {

            userPicturePath = mStorageRef.child("userPicture").child(mAuth.getCurrentUser().getUid() + ".jpg");
            userPicturePath.putFile(chosenUserPictureUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        userPicturePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    newUserPicture = task.getResult().toString();
                                    addUserCoverToStorage();

                                } else {

                                    progressDialog.dismiss();
//                                    editProfileProgressBar.setVisibility(View.GONE);
                                    mMenu.getItem(0).setEnabled(true);
                                    Toast.makeText(EditProfile.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                                }

                            }
                        });

                    } else {

                        progressDialog.dismiss();
//                        editProfileProgressBar.setVisibility(View.GONE);
                        mMenu.getItem(0).setEnabled(true);
                        Toast.makeText(EditProfile.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                    }

                }
            });

        } else {

            addUserCoverToStorage();

        }

    }


    public void addUserCoverToStorage() {

        if (chosenUserCoverUri != null) {

            userCoverPath = mStorageRef.child("userCover").child(mAuth.getCurrentUser().getUid() + ".jpg");
            userCoverPath.putFile(chosenUserCoverUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        userCoverPath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    newUserCover = task.getResult().toString();
                                    updateBioUserCover();

                                } else {

                                    progressDialog.dismiss();
//                                    editProfileProgressBar.setVisibility(View.GONE);
                                    mMenu.getItem(0).setEnabled(true);
                                    Toast.makeText(EditProfile.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                                }

                            }
                        });

                    } else {

                        progressDialog.dismiss();
//                        editProfileProgressBar.setVisibility(View.GONE);
                        mMenu.getItem(0).setEnabled(true);
                        Toast.makeText(EditProfile.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                    }

                }
            });

        } else {

            updateBioUserCover();

        }

    }


    public void updateBioUserCover() {

        newUserBio = editProfileEditTextUserBio.getText().toString().trim().isEmpty() ? getResources().getString(R.string.userBioDefault) : editProfileEditTextUserBio.getText().toString().trim();

        generalSearchListCollection
                .whereEqualTo("itemId", mAuth.getCurrentUser().getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                writeBatchList.get(writeBatchListIndex).update(usersCollection.document(mAuth.getCurrentUser().getUid()), "userBio", newUserBio);
                                databaseChangesCounter++;

                                if (newUserCover != null) {

                                    writeBatchList.get(writeBatchListIndex).update(usersCollection.document(mAuth.getCurrentUser().getUid()), "userCover", newUserCover);
                                    databaseChangesCounter++;

                                }

                                if (newUserPicture != null) {

                                    writeBatchList.get(writeBatchListIndex).update(usersCollection.document(mAuth.getCurrentUser().getUid()), "userPicture", newUserPicture);
                                    databaseChangesCounter++;

                                    updateUserPictureInAllPlaces(true);

                                } else {

                                    updateUserPictureInAllPlaces(false);

                                }

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
//                        editProfileProgressBar.setVisibility(View.GONE);
                        mMenu.getItem(0).setEnabled(true);
                        Toast.makeText(EditProfile.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void updateUserPictureInAllPlaces(final boolean didUserPictureChange) {

        final CollectionReference collectionReference = collectionReferenceList.get(collectionsCounter);
        final List<String> documentField = documentFieldList.get(collectionsCounter);

        collectionReference
                .whereEqualTo(documentField.get(0), mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                            if (databaseChangesCounter < 500) {

                                writeBatchList.get(writeBatchListIndex).update(collectionReference.document(queryDocumentSnapshot.getId()), documentField.get(1), newUserPicture);
                                databaseChangesCounter++;

                            } else {

                                databaseChangesCounter = 0;

                                writeBatchList.add(db.batch());
                                writeBatchListIndex++;

                            }

                        }

                        collectionsCounter++;

                        if (collectionsCounter >= collectionReferenceList.size() || !didUserPictureChange) {

                            collectionsCounter = 0;
                            applyChanges();

                        } else {

                            updateUserPictureInAllPlaces(true);

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
//                        editProfileProgressBar.setVisibility(View.GONE);
                        mMenu.getItem(0).setEnabled(true);
                        Toast.makeText(EditProfile.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void applyChanges() {

        writeBatchList.get(writeBatchCommittedCounter).commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    writeBatchCommittedCounter++;

                    if (writeBatchCommittedCounter >= writeBatchList.size()) {

                        writeBatchCommittedCounter = 0;
                        applyFinalChanges();

                    } else {

                        applyChanges();

                    }

                } else {

                    progressDialog.dismiss();
//                    editProfileProgressBar.setVisibility(View.GONE);
                    mMenu.getItem(0).setEnabled(true);
                    Toast.makeText(EditProfile.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(EditProfile.this, "Number of changes: " + databaseChangesCounter, Toast.LENGTH_SHORT).show();

                    return;

                }

            }
        });

    }


    public void applyFinalChanges() {

        if (newUserPicture != null) {

            userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(newUserPicture))
                    .build();

            mAuth.getCurrentUser().updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        progressDialog.dismiss();
    //                    editProfileProgressBar.setVisibility(View.GONE);
                        goToMain();

                    } else {

                        progressDialog.dismiss();
    //                    editProfileProgressBar.setVisibility(View.GONE);
                        mMenu.getItem(0).setEnabled(true);
                        Toast.makeText(EditProfile.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        return;

                    }

                }
            });

        } else {

            progressDialog.dismiss();
            //                    editProfileProgressBar.setVisibility(View.GONE);
            goToMain();

        }

    }


    public void goToMain() {

        Intent intentMain = new Intent(EditProfile.this, MainActivity.class);
        startActivity(intentMain);

    }


}
