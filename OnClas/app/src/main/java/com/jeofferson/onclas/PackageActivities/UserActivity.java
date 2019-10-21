package com.jeofferson.onclas.PackageActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeofferson.onclas.PackageAdapters.PostAdapter;
import com.jeofferson.onclas.PackageAdapters.UserPhotoAdapter;
import com.jeofferson.onclas.PackageForms.EditProfile;
import com.jeofferson.onclas.PackageObjectModel.PostHolder;
import com.jeofferson.onclas.PackageObjectModel.User;
import com.jeofferson.onclas.PackageObjectModel.UserPhoto;
import com.jeofferson.onclas.PackageOthers.MyRecyclerView;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;

import static com.jeofferson.onclas.PackageActivities.MainActivity.paginationNumOfItemPerPage;


public class UserActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private CollectionReference userPhotosCollection;
    private CollectionReference newsfeedCollection;

    private String userId;
    private User user;
    private String userCover;
    private String userPicture;
    private String userFullName;
    private String fullType;
    private String userBio;

    private List<UserPhoto> userPhotoList = new ArrayList<>();
    private UserPhotoAdapter userPhotoAdapter;
    private Query userPhotosQuery;

    private List<PostHolder> postHolderList = new ArrayList<>();
    private PostAdapter postAdapter;
    private Query newsfeedQuery;

    private DocumentSnapshot lastVisible2;
    private DocumentSnapshot lastVisible;

    private Toolbar toolbar;

    private ImageView userActivityImageViewUserCover;
    private CircleImageView userActivityCircleImageViewUserPicture;
    private TextView userActivityTextViewUserFullName;
    private TextView userActivityTextViewFullType;
    private TextView userActivityTextViewUserBio;
    private Button userActivityTextViewUserBioEdit;

    private RelativeLayout userActivityRelativeLayoutEmptyView;
    private MyRecyclerView userActivityMyRecyclerViewUserPhotos;

    private TextView userActivityTextViewViewAll;

    private LinearLayout userActivityLinearLayoutEmptyView;
    private MyRecyclerView userActivityMyRecyclerViewNewsfeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_activity);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("Users");
        userPhotosCollection = db.collection("UserPhotos");
        newsfeedCollection = db.collection("Newsfeed");

        userId = getIntent().getStringExtra("userId");
        usersCollection.document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        user = documentSnapshot.toObject(User.class);
                        userCover = user.getUserCover();
                        userPicture = user.getUserPicture();
                        userFullName = user.getUserFullName();
                        fullType = user.getFullType();
                        userBio = user.getUserBio();

                        setUpToolbar(user.getUsername());

                        userActivityImageViewUserCover = findViewById(R.id.userActivityImageViewUserCover);
                        userActivityCircleImageViewUserPicture = findViewById(R.id.userActivityCircleImageViewUserPicture);
                        userActivityTextViewUserFullName = findViewById(R.id.userActivityTextViewUserFullName);
                        userActivityTextViewFullType = findViewById(R.id.userActivityTextViewFullType);
                        userActivityTextViewUserBio = findViewById(R.id.userActivityTextViewUserBio);
                        userActivityTextViewUserBioEdit = findViewById(R.id.userActivityTextViewUserBioEdit);

                        userActivityTextViewViewAll = findViewById(R.id.userActivityTextViewViewAll);

                        Glide.with(UserActivity.this).load(userCover).placeholder(R.drawable.placeholder).into(userActivityImageViewUserCover);
                        Glide.with(UserActivity.this).load(userPicture).placeholder(R.drawable.placeholder).into(userActivityCircleImageViewUserPicture);
                        userActivityTextViewUserFullName.setText(userFullName);
                        userActivityTextViewFullType.setText(fullType);
                        userActivityTextViewUserBio.setText(userBio);

                        if (userId.equals(mAuth.getCurrentUser().getUid())) {

                            userActivityTextViewUserBioEdit.setVisibility(View.VISIBLE);

                        } else {

                            userActivityTextViewUserBioEdit.setVisibility(View.GONE);

                        }

                        setUpAdapter2();
                        setUpAdapter();

                        startRetrievingUserPhotosFromDatabase();
                        startRetrievingNewsfeedFromDatabase();

                        userActivityImageViewUserCover.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            goToFullScreenImage(Uri.parse(userCover));

                            }
                        });

                        userActivityCircleImageViewUserPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            goToFullScreenImage(Uri.parse(userPicture));

                            }
                        });

                        userActivityTextViewUserBioEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            goToEditProfile();

                            }
                        });

                        userActivityTextViewViewAll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            goToUserPhotos(userId);

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

//                        Toast.makeText(UserActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (userId.equals(mAuth.getCurrentUser().getUid())) {

            getMenuInflater().inflate(R.menu.menu_toolbar, menu);

        } else {

            getMenuInflater().inflate(R.menu.menu_toolbar_main, menu);

        }

        return true;

    }


    public void setUpToolbar(String username) {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(username);

    }


    public void setUpAdapter2() {

        userPhotoList = new ArrayList<>();
        userPhotoAdapter = new UserPhotoAdapter(userPhotoList, false, userId, "userActivity", false);

        userActivityMyRecyclerViewUserPhotos = findViewById(R.id.userActivityMyRecyclerViewUserPhotos);
        userActivityRelativeLayoutEmptyView = findViewById(R.id.userActivityRelativeLayoutEmptyView);

        // Empty view
        userActivityMyRecyclerViewUserPhotos.showIfEmpty(userActivityRelativeLayoutEmptyView);

        userActivityMyRecyclerViewUserPhotos.setHasFixedSize(true);
        userActivityMyRecyclerViewUserPhotos.setLayoutManager(new LinearLayoutManager(UserActivity.this, LinearLayoutManager.HORIZONTAL, false));

        userActivityMyRecyclerViewUserPhotos.setAdapter(userPhotoAdapter);
        ViewCompat.setNestedScrollingEnabled(userActivityMyRecyclerViewUserPhotos, false);

    }


    public void startRetrievingUserPhotosFromDatabase() {

        getInitialUserPhotosFromDatabase();

        userActivityMyRecyclerViewUserPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                Boolean isEndReached = !recyclerView.canScrollHorizontally(1);

                if (isEndReached) {

                    getMoreUserPhotosFromDatabase();

                }

            }
        });

    }


    public void getInitialUserPhotosFromDatabase() {

        userPhotosQuery = userPhotosCollection
                .whereEqualTo("userId", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .limit(paginationNumOfItemPerPage);

        getUserPhotos();

    }


    public void getMoreUserPhotosFromDatabase() {

        userPhotosQuery = userPhotosCollection
                .whereEqualTo("userId", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .startAfter(lastVisible2)
                .limit(paginationNumOfItemPerPage);

        getUserPhotos();

    }


    public void getUserPhotos() {

        userPhotosQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            lastVisible2 = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

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
//                        Toast.makeText(UserActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void setUpAdapter() {

        postHolderList = new ArrayList<>();
        postAdapter = new PostAdapter(postHolderList);

        userActivityMyRecyclerViewNewsfeed = findViewById(R.id.userActivityMyRecyclerViewNewsfeed);
        userActivityLinearLayoutEmptyView = findViewById(R.id.userActivityLinearLayoutEmptyView);

        // Empty view
        userActivityMyRecyclerViewNewsfeed.showIfEmpty(userActivityLinearLayoutEmptyView);

        userActivityMyRecyclerViewNewsfeed.setHasFixedSize(true);
        userActivityMyRecyclerViewNewsfeed.setLayoutManager(new LinearLayoutManager(this));

        userActivityMyRecyclerViewNewsfeed.setAdapter(postAdapter);
        ViewCompat.setNestedScrollingEnabled(userActivityMyRecyclerViewNewsfeed, false);

    }


    public void startRetrievingNewsfeedFromDatabase() {

        getInitialNewsfeedFromDatabase();
        userActivityMyRecyclerViewNewsfeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean isBottomReached = !recyclerView.canScrollVertically(1);

                if (isBottomReached) {

                    getMoreNewsfeedFromDatabase();

                }

            }
        });

    }


    public void getInitialNewsfeedFromDatabase() {

        newsfeedQuery = newsfeedCollection
                .whereEqualTo("poster1Id", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .limit(paginationNumOfItemPerPage);

        getNewsfeed();

    }


    public void getMoreNewsfeedFromDatabase() {

        newsfeedQuery = newsfeedCollection
                .whereEqualTo("poster1Id", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(paginationNumOfItemPerPage);

        getNewsfeed();

    }


    public void getNewsfeed() {

        newsfeedQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                PostHolder postHolder = queryDocumentSnapshot.toObject(PostHolder.class);
                                postHolder.setObjectId(queryDocumentSnapshot.getId());

                                postHolderList.add(postHolder);
                                postAdapter.notifyDataSetChanged();

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e("Error: ", e.getLocalizedMessage());
//                        Toast.makeText(UserActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menuToolbarAbout:
                goToAbout();
                break;

            case R.id.menuToolbarLogOut:
                goToLogOut();
                break;

        }

        return true;

    }


    public void goToFullScreenImage(Uri uri) {

        Intent intentFullScreenImage = new Intent(UserActivity.this, FullScreenImage.class);
        intentFullScreenImage.setData(uri);
        startActivity(intentFullScreenImage);

    }


    public void goToUserPhotos(String userId) {

        Intent intentUserPhotos = new Intent(UserActivity.this, UserPhotos.class);
        intentUserPhotos.putExtra("userId", userId);
        startActivity(intentUserPhotos);

    }


    public void goToEditProfile() {

        Intent intentEditProfile = new Intent(UserActivity.this, EditProfile.class);
        intentEditProfile.putExtra("userPicture", userPicture);
        intentEditProfile.putExtra("userCover", userCover);

        if (userBio.equals(getResources().getString(R.string.userBioDefault))) {

            intentEditProfile.putExtra("userBio", "");

        } else {

            intentEditProfile.putExtra("userBio", userBio);

        }

        startActivity(intentEditProfile);

    }


    public void goToAbout() {

        Intent intentAbout = new Intent(UserActivity.this, About.class);
        startActivity(intentAbout);

    }


    public void goToLogOut() {

        Intent intentLogOut = new Intent(UserActivity.this, LogOut.class);
        startActivity(intentLogOut);
        finish();

    }


    @Override
    public void onResume() {
        super.onResume();

        if (postAdapter != null) {

            postAdapter.notifyDataSetChanged();

        }

    }


}
