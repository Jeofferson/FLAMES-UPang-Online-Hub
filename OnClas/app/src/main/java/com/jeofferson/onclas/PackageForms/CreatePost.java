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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jeofferson.onclas.PackageActivities.MainActivity;
import com.jeofferson.onclas.PackageObjectModel.GeneralSearchItem;
import com.jeofferson.onclas.PackageObjectModel.Post;
import com.jeofferson.onclas.PackageObjectModel.PostHolder;
import com.jeofferson.onclas.PackageObjectModel.User;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreatePost extends AppCompatActivity {


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
    static final int REQUEST_CODE_PHONE_GALLERY = 4;

    private FirebaseAuth mAuth;

    private StorageReference mStorageRef;
    private StorageReference postPicturePath;

    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private CollectionReference generalSearchListCollection;
    private CollectionReference postsCollection;
    private CollectionReference newsfeedCollection;

    private User user;

    private PostHolder postHolder;
    private Post post;
    private String poster1Id;
    private String poster2Id;
    private String posterPicture;
    private String poster1Name;
    private String poster2Name;
    private String postContent;
    private @ServerTimestamp Date dateCreated;
    private String postPicture;
    private String postType;

    private Toolbar toolbar;
    private Menu mMenu;
    private ProgressBar createBlogProgressBar;

    private CircleImageView createBlogCircleImageViewDisplayPosterPicture;
    private TextView createBlogTextViewDisplayPosterFullName;
    private TextView createBlogTextViewDisplayPosterFullType;
    private EditText createBlogEditTextPostContent;
    private ImageView createBlogImageViewPostPicture;
    private ImageView createBlogImageViewRemovePostPicture;

    private Uri chosenImageUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_create_post);

        mAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("Users");
        generalSearchListCollection = db.collection("GeneralSearchList");
        postsCollection = db.collection("Posts");
        newsfeedCollection = db.collection("Newsfeed");

        createBlogProgressBar = findViewById(R.id.createBlogProgressBar);

        createBlogCircleImageViewDisplayPosterPicture = findViewById(R.id.createBlogCircleImageViewDisplayPosterPicture);
        createBlogTextViewDisplayPosterFullName = findViewById(R.id.createBlogTextViewDisplayPosterFullName);
        createBlogTextViewDisplayPosterFullType = findViewById(R.id.createBlogTextViewDisplayPosterFullType);
        createBlogEditTextPostContent = findViewById(R.id.createBlogEditTextPostContent);
        createBlogImageViewPostPicture = findViewById(R.id.createBlogImageViewPostPicture);
        createBlogImageViewRemovePostPicture = findViewById(R.id.createBlogImageViewRemovePostPicture);

        setUpToolbar();

        Glide.with(createBlogCircleImageViewDisplayPosterPicture.getContext()).load(mAuth.getCurrentUser().getPhotoUrl()).into(createBlogCircleImageViewDisplayPosterPicture);
        createBlogTextViewDisplayPosterFullName.setText(mAuth.getCurrentUser().getDisplayName());

        usersCollection.document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        user = documentSnapshot.toObject(User.class);

                        createBlogTextViewDisplayPosterFullType.setText(user.getFullType());

                        createBlogImageViewPostPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                setUpReadExternalStoragePermission();

                            }
                        });

                        createBlogImageViewRemovePostPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                chosenImageUri = null;

                                createBlogImageViewPostPicture.setImageResource(R.drawable.placeholder_add_image2);
                                createBlogImageViewRemovePostPicture.setVisibility(View.GONE);

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(CreatePost.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_create_post, menu);
        mMenu = menu;
        return true;

    }


    public void setUpToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.createPost));

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menuCreatePostPost:
                addPostPictureToStorage();
                break;

        }

        return true;

    }


    public void setUpReadExternalStoragePermission() {

        if (Build.VERSION.SDK_INT >= 22) {

            if (ContextCompat.checkSelfPermission(CreatePost.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(CreatePost.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Toast.makeText(CreatePost.this, "Permission Required", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(CreatePost.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                } else {

                    ActivityCompat.requestPermissions(CreatePost.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

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
            createBlogImageViewPostPicture.setImageURI(chosenImageUri);

            createBlogImageViewRemovePostPicture.setVisibility(View.VISIBLE);

            createBlogEditTextPostContent.setError(null);

        }

    }


    public void addPostPictureToStorage() {

        postContent = createBlogEditTextPostContent.getText().toString().trim();

        if (!postContent.isEmpty() || chosenImageUri != null) {

            mMenu.getItem(0).setEnabled(false);
            createBlogProgressBar.setVisibility(View.VISIBLE);

            if (chosenImageUri != null) {

                postPicturePath = mStorageRef.child("postPicture").child(System.currentTimeMillis() + ".jpg");
                postPicturePath.putFile(chosenImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            postPicturePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    if (task.isSuccessful()) {

                                        postPicture = task.getResult().toString();
                                        addPostToDatabase();

                                    } else {

                                        createBlogProgressBar.setVisibility(View.GONE);
                                        mMenu.getItem(0).setEnabled(true);
                                        Toast.makeText(CreatePost.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                        } else {

                            createBlogProgressBar.setVisibility(View.GONE);
                            mMenu.getItem(0).setEnabled(true);
                            Toast.makeText(CreatePost.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            } else {

                postPicture = "NA";
                addPostToDatabase();

            }

        } else {

            createBlogEditTextPostContent.setError(getResources().getString(R.string.required));

        }

    }


    public void addPostToDatabase() {

        generalSearchListCollection
                .whereEqualTo("itemId", mAuth.getCurrentUser().getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                GeneralSearchItem generalSearchItem = queryDocumentSnapshot.toObject(GeneralSearchItem.class);

                                switch (generalSearchItem.getType()) {

                                    case "Student":
                                        postType = "Student Updates";
                                        break;

                                    case "Teacher":
                                    case "SC Officer":
                                        postType = "School Updates";
                                        break;

                                }

                                poster1Id = mAuth.getCurrentUser().getUid();
                                poster2Id = mAuth.getCurrentUser().getUid();
                                posterPicture = mAuth.getCurrentUser().getPhotoUrl().toString();
                                poster1Name = mAuth.getCurrentUser().getDisplayName();
                                poster2Name = user.getFullType();

                                String postUid = String.valueOf(System.currentTimeMillis());

                                post = new Post(poster1Id, poster2Id, posterPicture, poster1Name, poster2Name, postContent, dateCreated, postPicture, postType);
                                postHolder = new PostHolder(postUid, poster1Id, poster2Id, posterPicture, poster1Name, poster2Name, postContent, dateCreated, postPicture, postType);

                                WriteBatch writeBatch = db.batch();

                                writeBatch.set(postsCollection.document(postUid), post);
                                writeBatch.set(newsfeedCollection.document(), postHolder);

                                writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            createBlogProgressBar.setVisibility(View.GONE);
                                            goBackToPrevious();

                                        } else {

                                            createBlogProgressBar.setVisibility(View.GONE);
                                            mMenu.getItem(0).setEnabled(true);
                                            Toast.makeText(CreatePost.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

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

                        createBlogProgressBar.setVisibility(View.GONE);
                        mMenu.getItem(0).setEnabled(true);
                        Toast.makeText(CreatePost.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void goBackToPrevious() {

        Intent intentBeforeCreateBlog;

        switch (getIntent().getStringExtra("from")) {

            case "MainActivity":
                intentBeforeCreateBlog = new Intent(CreatePost.this, MainActivity.class);
                break;

            default:
                intentBeforeCreateBlog = new Intent(CreatePost.this, MainActivity.class);

        }

        intentBeforeCreateBlog.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentBeforeCreateBlog);
        finish();

    }


}
