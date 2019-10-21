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
import com.jeofferson.onclas.PackageActivities.CommentActivity;
import com.jeofferson.onclas.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditComment extends AppCompatActivity {


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 16;
    static final int REQUEST_CODE_PHONE_GALLERY = 17;

    private FirebaseAuth mAuth;

    private StorageReference mStorageRef;
    private StorageReference commentPicturePath;

    private FirebaseFirestore db;
    private CollectionReference commentHoldersCollection;
    private CollectionReference commentsCollection;
    private CollectionReference replyHoldersCollection;

    private String newCommentPicture;

    private String newCommentContent;

    private String typeOfEdit;
    private String postHolderId;
    private String postId;
    private String commentHolderId;
    private String commentId;
    private String commentHolder2Id;
    private String comment2Id;
    private String displayCommenterPicture;
    private String displayCommenterFullName;
    private String displayCommenterFullType;
    private String dateCreated;
    private String commentContent;
    private String commentPicture;

    private boolean haveChangedCommentPicture = false;
    private Uri chosenCommentPictureUri = null;

    private Toolbar toolbar;
    private Menu mMenu;
    private ProgressBar editCommentProgressBar;

    private CircleImageView editCommentCircleImageViewDisplayCommenterPicture;
    private TextView editCommentTextViewDisplayCommenterFullName;
    private TextView editCommentTextViewDisplayCommenterFullType;
    private TextView editCommentTextViewDateCreated;
    private EditText editCommentTextViewContent;
    private ImageView editCommentImageViewCommentPicture;
    private ImageView editCommentImageViewRemoveCommentPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_edit_comment);

        mAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        db = FirebaseFirestore.getInstance();
        commentHoldersCollection = db.collection("CommentHolders");
        commentsCollection = db.collection("Comments");
        replyHoldersCollection = db.collection("ReplyHolders");

        editCommentProgressBar = findViewById(R.id.editCommentProgressBar);

        editCommentCircleImageViewDisplayCommenterPicture = findViewById(R.id.editCommentCircleImageViewDisplayCommenterPicture);
        editCommentTextViewDisplayCommenterFullName = findViewById(R.id.editCommentTextViewDisplayCommenterFullName);
        editCommentTextViewDisplayCommenterFullType = findViewById(R.id.editCommentTextViewDisplayCommenterFullType);
        editCommentTextViewDateCreated = findViewById(R.id.editCommentTextViewDateCreated);
        editCommentTextViewContent = findViewById(R.id.editCommentTextViewContent);
        editCommentImageViewCommentPicture = findViewById(R.id.editCommentImageViewCommentPicture);
        editCommentImageViewRemoveCommentPicture = findViewById(R.id.editCommentImageViewRemoveCommentPicture);

        typeOfEdit = getIntent().getStringExtra("typeOfEdit");
        postHolderId = getIntent().getStringExtra("postHolderId");
        postId = getIntent().getStringExtra("postId");
        commentHolderId = getIntent().getStringExtra("commentHolderId");
        commentId = getIntent().getStringExtra("commentId");
        commentHolder2Id = getIntent().getStringExtra("commentHolder2Id");
        comment2Id = getIntent().getStringExtra("comment2Id");
        displayCommenterPicture = getIntent().getStringExtra("displayCommenterPicture");
        displayCommenterFullName = getIntent().getStringExtra("displayCommenterFullName");
        displayCommenterFullType = getIntent().getStringExtra("displayCommenterFullType");
        dateCreated = getIntent().getStringExtra("dateCreated");
        commentContent = getIntent().getStringExtra("commentContent");
        commentPicture = getIntent().getStringExtra("commentPicture");

        setUpToolbar();

        Glide.with(EditComment.this).load(displayCommenterPicture).placeholder(R.drawable.placeholder).into(editCommentCircleImageViewDisplayCommenterPicture);

        editCommentTextViewDisplayCommenterFullName.setText(displayCommenterFullName);
        editCommentTextViewDisplayCommenterFullType.setText(displayCommenterFullType);

        editCommentTextViewDateCreated.setText(dateCreated);

        if (commentPicture.equals("NA")) {

            chosenCommentPictureUri = null;

            editCommentImageViewCommentPicture.setImageResource(R.drawable.placeholder_add_image2);
            editCommentImageViewRemoveCommentPicture.setVisibility(View.GONE);

        } else {

            chosenCommentPictureUri = Uri.parse(commentPicture);

            Glide.with(EditComment.this).load(commentPicture).placeholder(R.drawable.placeholder).into(editCommentImageViewCommentPicture);
            editCommentImageViewRemoveCommentPicture.setVisibility(View.VISIBLE);

        }

        editCommentTextViewContent.setText(commentContent);
        editCommentTextViewContent.setSelection(commentContent.length());

        switch (typeOfEdit) {

            case "editComment":
                editCommentTextViewContent.setHint(getResources().getString(R.string.writeAComment));
                break;

            case "editReply":
                getSupportActionBar().setTitle(getResources().getString(R.string.editReply));
                editCommentTextViewContent.setHint(getResources().getString(R.string.writeAReply));
                break;

        }

        editCommentImageViewCommentPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setUpReadExternalStoragePermission();

            }
        });

        editCommentImageViewRemoveCommentPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                haveChangedCommentPicture = true;

                chosenCommentPictureUri = null;

                editCommentImageViewCommentPicture.setImageResource(R.drawable.placeholder_add_image2);
                editCommentImageViewRemoveCommentPicture.setVisibility(View.GONE);

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

        switch (typeOfEdit) {

            case "editComment":
                getSupportActionBar().setTitle(getResources().getString(R.string.editComment));
                break;

            case "editReply":
                getSupportActionBar().setTitle(getResources().getString(R.string.editReply));
                break;

        }

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menuSingleCheckDone:
                addCommentPictureToStorage();
                break;

        }

        return true;

    }


    public void addCommentPictureToStorage() {

        mMenu.getItem(0).setEnabled(false);
        editCommentProgressBar.setVisibility(View.VISIBLE);

        if (haveChangedCommentPicture) {

            if (chosenCommentPictureUri != null) {

                commentPicturePath = mStorageRef.child("postPicture").child(System.currentTimeMillis() + ".jpg");
                commentPicturePath.putFile(chosenCommentPictureUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            commentPicturePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    if (task.isSuccessful()) {

                                        newCommentPicture = task.getResult().toString();
                                        updateComment();

                                    } else {

                                        editCommentProgressBar.setVisibility(View.GONE);
                                        mMenu.getItem(0).setEnabled(true);
                                        Toast.makeText(EditComment.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                        } else {

                            editCommentProgressBar.setVisibility(View.GONE);
                            mMenu.getItem(0).setEnabled(true);
                            Toast.makeText(EditComment.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            } else {

                newCommentPicture = "NA";
                updateComment();


            }

        } else {

            updateComment();

        }

    }


    public void updateComment() {

        newCommentContent = editCommentTextViewContent.getText().toString().trim();

        WriteBatch writeBatch = db.batch();

        switch (typeOfEdit) {

            case "editComment":

                writeBatch.update(commentHoldersCollection.document(commentHolder2Id), "commentContent", newCommentContent);
                writeBatch.update(commentsCollection.document(comment2Id), "commentContent", newCommentContent);

                if (haveChangedCommentPicture) {

                    writeBatch.update(commentHoldersCollection.document(commentHolder2Id), "commentPicture", newCommentPicture);
                    writeBatch.update(commentsCollection.document(comment2Id), "commentPicture", newCommentPicture);

                }

                break;

            case "editReply":

                writeBatch.update(replyHoldersCollection.document(commentHolder2Id), "replyContent", newCommentContent);

                if (haveChangedCommentPicture) {

                    writeBatch.update(replyHoldersCollection.document(commentHolder2Id), "replyPicture", newCommentPicture);

                }

                break;

        }

        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    editCommentProgressBar.setVisibility(View.GONE);
                    goToCommentActivity(commentHolderId, commentId, false, true);

                } else {

                    editCommentProgressBar.setVisibility(View.GONE);
                    mMenu.getItem(0).setEnabled(true);
                    Toast.makeText(EditComment.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    public void setUpReadExternalStoragePermission() {

        if (Build.VERSION.SDK_INT >= 22) {

            if (ContextCompat.checkSelfPermission(EditComment.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(EditComment.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Toast.makeText(EditComment.this, "Permission Required", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(EditComment.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                } else {

                    ActivityCompat.requestPermissions(EditComment.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

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

            haveChangedCommentPicture = true;

            chosenCommentPictureUri = data.getData();
            editCommentImageViewCommentPicture.setImageURI(chosenCommentPictureUri);

            editCommentImageViewRemoveCommentPicture.setVisibility(View.VISIBLE);

            editCommentTextViewContent.setError(null);

        }

    }


    public void goToCommentActivity(String commentHolderId, String commentId, boolean willComment, boolean isFromNotifications) {

        Intent intentCommentActivity = new Intent(EditComment.this, CommentActivity.class);

        intentCommentActivity.putExtra("postHolderId", postHolderId);
        intentCommentActivity.putExtra("postId", postId);
        intentCommentActivity.putExtra("commentHolderId", commentHolderId);
        intentCommentActivity.putExtra("commentId", commentId);
        intentCommentActivity.putExtra("willComment", willComment);
        intentCommentActivity.putExtra("isFromNotifications", isFromNotifications);

        startActivity(intentCommentActivity);
        finish();

    }


}
