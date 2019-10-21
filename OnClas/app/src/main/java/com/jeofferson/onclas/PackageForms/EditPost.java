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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jeofferson.onclas.PackageActivities.Updates;
import com.jeofferson.onclas.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPost extends AppCompatActivity {


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 14;
    static final int REQUEST_CODE_PHONE_GALLERY = 15;

    private FirebaseAuth mAuth;

    private StorageReference mStorageRef;
    private StorageReference postPicturePath;

    private FirebaseFirestore db;
    private CollectionReference newsfeedCollection;
    private CollectionReference postsCollection;

    private String newPostPicture;

    private String newPostContent;

    private String postHolderId;
    private String postId;
    private String displayPosterPicture;
    private String displayPosterFullName;
    private String displayPosterFullType;
    private String postContent;
    private String postPicture;

    private boolean haveChangedPostPicture = false;
    private Uri chosenPostPictureUri = null;

    private Toolbar toolbar;
    private Menu mMenu;
    private ProgressBar editPostProgressBar;

    private CircleImageView editPostCircleImageViewDisplayPosterPicture;
    private TextView editPostTextViewDisplayPosterFullName;
    private TextView editPostTextViewDisplayPosterFullType;
    private EditText editPostEditTextPostContent;
    private ImageView editPostImageViewPostPicture;
    private ImageView editPostImageViewRemovePostPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_edit_post);

        mAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        db = FirebaseFirestore.getInstance();
        newsfeedCollection = db.collection("Newsfeed");
        postsCollection = db.collection("Posts");

        editPostProgressBar = findViewById(R.id.editPostProgressBar);

        editPostCircleImageViewDisplayPosterPicture = findViewById(R.id.editPostCircleImageViewDisplayPosterPicture);
        editPostTextViewDisplayPosterFullName = findViewById(R.id.editPostTextViewDisplayPosterFullName);
        editPostTextViewDisplayPosterFullType = findViewById(R.id.editPostTextViewDisplayPosterFullType);
        editPostEditTextPostContent = findViewById(R.id.editPostEditTextPostContent);
        editPostImageViewPostPicture = findViewById(R.id.editPostImageViewPostPicture);
        editPostImageViewRemovePostPicture = findViewById(R.id.editPostImageViewRemovePostPicture);

        setUpToolbar();

        postHolderId = getIntent().getStringExtra("postHolderId");
        postId = getIntent().getStringExtra("postId");
        displayPosterPicture = getIntent().getStringExtra("displayPosterPicture");
        displayPosterFullName = getIntent().getStringExtra("displayPosterFullName");
        displayPosterFullType = getIntent().getStringExtra("displayPosterFullType");
        postContent = getIntent().getStringExtra("postContent");
        postPicture = getIntent().getStringExtra("postPicture");

        Glide.with(EditPost.this).load(displayPosterPicture).placeholder(R.drawable.placeholder).into(editPostCircleImageViewDisplayPosterPicture);

        editPostTextViewDisplayPosterFullName.setText(displayPosterFullName);
        editPostTextViewDisplayPosterFullType.setText(displayPosterFullType);

        if (postPicture.equals("NA")) {

            chosenPostPictureUri = null;

            editPostImageViewPostPicture.setImageResource(R.drawable.placeholder_add_image2);
            editPostImageViewRemovePostPicture.setVisibility(View.GONE);

        } else {

            chosenPostPictureUri = Uri.parse(postPicture);

            Glide.with(EditPost.this).load(postPicture).placeholder(R.drawable.placeholder).into(editPostImageViewPostPicture);
            editPostImageViewRemovePostPicture.setVisibility(View.VISIBLE);

        }

        editPostEditTextPostContent.setText(postContent);
        editPostEditTextPostContent.setSelection(postContent.length());

        editPostImageViewPostPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setUpReadExternalStoragePermission();

            }
        });

        editPostImageViewRemovePostPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                haveChangedPostPicture = true;

                chosenPostPictureUri = null;

                editPostImageViewPostPicture.setImageResource(R.drawable.placeholder_add_image2);
                editPostImageViewRemovePostPicture.setVisibility(View.GONE);

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
        getSupportActionBar().setTitle(getResources().getString(R.string.editPost));

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menuSingleCheckDone:
                addPostPictureToStorage();
                break;

        }

        return true;

    }


    public void addPostPictureToStorage() {

        mMenu.getItem(0).setEnabled(false);
        editPostProgressBar.setVisibility(View.VISIBLE);

        if (haveChangedPostPicture) {

            if (chosenPostPictureUri != null) {

                postPicturePath = mStorageRef.child("postPicture").child(System.currentTimeMillis() + ".jpg");
                postPicturePath.putFile(chosenPostPictureUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            postPicturePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    if (task.isSuccessful()) {

                                        newPostPicture = task.getResult().toString();
                                        updatePost();

                                    } else {

                                        editPostProgressBar.setVisibility(View.GONE);
                                        mMenu.getItem(0).setEnabled(true);
                                        Toast.makeText(EditPost.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                        } else {

                            editPostProgressBar.setVisibility(View.GONE);
                            mMenu.getItem(0).setEnabled(true);
                            Toast.makeText(EditPost.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            } else {

                newPostPicture = "NA";
                updatePost();


            }

        } else {

            updatePost();

        }

    }


    public void updatePost() {

        newPostContent = editPostEditTextPostContent.getText().toString().trim();

        WriteBatch writeBatch = db.batch();

        writeBatch.update(newsfeedCollection.document(postHolderId), "postContent", newPostContent);
        writeBatch.update(postsCollection.document(postId), "postContent", newPostContent);

        if (haveChangedPostPicture) {

            writeBatch.update(newsfeedCollection.document(postHolderId), "postPicture", newPostPicture);
            writeBatch.update(postsCollection.document(postId), "postPicture", newPostPicture);

        }

        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    editPostProgressBar.setVisibility(View.GONE);
                    goToStudentForum(postHolderId, postId, false, true);

                } else {

                    editPostProgressBar.setVisibility(View.GONE);
                    mMenu.getItem(0).setEnabled(true);
                    Toast.makeText(EditPost.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    public void setUpReadExternalStoragePermission() {

        if (Build.VERSION.SDK_INT >= 22) {

            if (ContextCompat.checkSelfPermission(EditPost.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(EditPost.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Toast.makeText(EditPost.this, "Permission Required", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(EditPost.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                } else {

                    ActivityCompat.requestPermissions(EditPost.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

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

            haveChangedPostPicture = true;

            chosenPostPictureUri = data.getData();
            editPostImageViewPostPicture.setImageURI(chosenPostPictureUri);

            editPostImageViewRemovePostPicture.setVisibility(View.VISIBLE);

            editPostEditTextPostContent.setError(null);

        }

    }


    public void goToStudentForum(String postHolderId, String postId, boolean willComment, boolean isFromNotifications) {

        Intent intentStudentForum = new Intent(EditPost.this, Updates.class);

        intentStudentForum.putExtra("postHolderId", postHolderId);
        intentStudentForum.putExtra("postId", postId);
        intentStudentForum.putExtra("willComment", willComment);
        intentStudentForum.putExtra("isFromNotifications", isFromNotifications);

        intentStudentForum.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentStudentForum);
        finish();

    }


}
