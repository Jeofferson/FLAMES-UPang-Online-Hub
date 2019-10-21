package com.jeofferson.onclas.PackageActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeofferson.onclas.PackageAdapters.UserPhotoAdapter;
import com.jeofferson.onclas.PackageForms.AddUserPhoto;
import com.jeofferson.onclas.PackageObjectModel.UserPhoto;
import com.jeofferson.onclas.PackageOthers.MyRecyclerView;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.List;

import static com.jeofferson.onclas.PackageActivities.MainActivity.paginationNumOfItemPerPage;

public class UserPhotos extends AppCompatActivity {


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 12;
    static final int REQUEST_CODE_PHONE_GALLERY = 13;

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private CollectionReference uPangPhotosCollection;
    private CollectionReference userPhotosCollection;

    private String userId;
    private boolean isLandscape;

    private List<UserPhoto> userPhotoList;

    private Query userPhotosQuery;

    private DocumentSnapshot lastVisible;

    private UserPhotoAdapter userPhotoAdapter;
    private MyRecyclerView userPhotosMyRecyclerViewUserPhotos;
    private LinearLayout userPhotosLinearLayoutEmptyView;

    private Uri chosenImageUri;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_photos);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        uPangPhotosCollection = db.collection("UPangPhotos");
        userPhotosCollection = db.collection("UserPhotos");

        userId = getIntent().getStringExtra("userId");
        isLandscape = getIntent().getBooleanExtra("isLandscape", false);

        setUpToolbar();

        setUpAdapter();

        startRetrievingUserPhotosFromDatabase();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (userId.equals(mAuth.getCurrentUser().getUid())) {

            getMenuInflater().inflate(R.menu.menu_single_add, menu);

        }

        return true;

    }


    public void setUpToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.photos));

    }


    public void setUpAdapter() {

        userPhotoList = new ArrayList<>();
        userPhotoAdapter = new UserPhotoAdapter(userPhotoList, true, userId, "userPhotos", isLandscape);

        userPhotosMyRecyclerViewUserPhotos = findViewById(R.id.userPhotosMyRecyclerViewUserPhotos);
        userPhotosLinearLayoutEmptyView = findViewById(R.id.userPhotosLinearLayoutEmptyView);

        // Empty view
        userPhotosMyRecyclerViewUserPhotos.showIfEmpty(userPhotosLinearLayoutEmptyView);

        userPhotosMyRecyclerViewUserPhotos.setHasFixedSize(true);

        if (isLandscape) {

            userPhotosMyRecyclerViewUserPhotos.setLayoutManager(new LinearLayoutManager(this));

        } else {

            userPhotosMyRecyclerViewUserPhotos.setLayoutManager(new GridLayoutManager(this, 3));

        }

        userPhotosMyRecyclerViewUserPhotos.setAdapter(userPhotoAdapter);

    }


    public void startRetrievingUserPhotosFromDatabase() {

        getInitialUserPhotosFromDatabase();
        userPhotosMyRecyclerViewUserPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean isBottomReached = !recyclerView.canScrollVertically(1);

                if (isBottomReached) {

                    getMoreUserPhotosFromDatabase();

                }

            }
        });

    }


    public void getInitialUserPhotosFromDatabase() {

        if (isLandscape) {

            userPhotosQuery = uPangPhotosCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("dateCreated", Query.Direction.DESCENDING)
                    .limit(paginationNumOfItemPerPage);

            getUserPhotos();

        } else {

            userPhotosQuery = userPhotosCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("dateCreated", Query.Direction.DESCENDING)
                    .limit(paginationNumOfItemPerPage);

            getUserPhotos();

        }

    }


    public void getMoreUserPhotosFromDatabase() {

        if (isLandscape) {

            userPhotosQuery = uPangPhotosCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("dateCreated", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(paginationNumOfItemPerPage);

        } else {

            userPhotosQuery = userPhotosCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("dateCreated", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(paginationNumOfItemPerPage);

        }

        getUserPhotos();

    }


    public void getUserPhotos() {

        userPhotosQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                UserPhoto userPhoto = queryDocumentSnapshot.toObject(UserPhoto.class);
                                userPhoto.setObjectId(queryDocumentSnapshot.getId());

                                userPhotoList.add(userPhoto);
                                userPhotoAdapter.notifyDataSetChanged();

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e("Error: ", e.getLocalizedMessage());
                        Toast.makeText(UserPhotos.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void setUpReadExternalStoragePermission() {

        if (Build.VERSION.SDK_INT >= 22) {

            if (ContextCompat.checkSelfPermission(UserPhotos.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(UserPhotos.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Toast.makeText(UserPhotos.this, "Permission Required", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(UserPhotos.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                } else {

                    ActivityCompat.requestPermissions(UserPhotos.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

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
            goToAddUserPhoto(chosenImageUri);

        }

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menuSingleCheckAdd:
                setUpReadExternalStoragePermission();
                break;

        }

        return true;

    }


    public void goToAddUserPhoto(Uri chosenImageUri) {

        Intent intentAddUserPhoto = new Intent(UserPhotos.this, AddUserPhoto.class);
        intentAddUserPhoto.setData(chosenImageUri);
        startActivity(intentAddUserPhoto);

    }


}
