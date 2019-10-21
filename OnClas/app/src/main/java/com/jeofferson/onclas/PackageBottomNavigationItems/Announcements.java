package com.jeofferson.onclas.PackageBottomNavigationItems;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeofferson.onclas.PackageActivities.FullScreenImage;
import com.jeofferson.onclas.PackageActivities.UserPhotos;
import com.jeofferson.onclas.PackageAdapters.PostAdapter;
import com.jeofferson.onclas.PackageAdapters.UserPhotoAdapter;
import com.jeofferson.onclas.PackageObjectModel.PostHolder;
import com.jeofferson.onclas.PackageObjectModel.UserPhoto;
import com.jeofferson.onclas.PackageOthers.MyRecyclerView;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.List;

import static com.jeofferson.onclas.PackageActivities.MainActivity.paginationNumOfItemPerPage;

public class Announcements extends Fragment {


    private FirebaseFirestore db;
    private CollectionReference uPangPhotosCollection;
    private CollectionReference newsfeedCollection;

    private String bgPhinmaDownloadUrl = "https://firebasestorage.googleapis.com/v0/b/onclas-9415f.appspot.com/o/fixedPicture%2Fbg_upang.jpg?alt=media&token=f49836fb-1282-4153-9676-d402a7bf5231";
    private String bgUpangDownloadUrl = "https://firebasestorage.googleapis.com/v0/b/onclas-9415f.appspot.com/o/fixedPicture%2Fupang_campus_building.JPG?alt=media&token=4fdd802e-a997-46ef-a39d-c85fa4f5eda0";

    private String dpUpangDownloadUrl = "https://firebasestorage.googleapis.com/v0/b/onclas-9415f.appspot.com/o/fixedPicture%2Fdp_upang2.png?alt=media&token=c9c5a2c0-ea62-428b-8865-4ebff65f3f5a";
    private String dpUpangPngDownloadUrl = "https://firebasestorage.googleapis.com/v0/b/onclas-9415f.appspot.com/o/fixedPicture%2Fdp_upang.webp?alt=media&token=e4b27ebe-5ded-4d77-bee6-1dc9d1b47ca4";

    private String userId = "Background PHINMA - University of Pangasinan";

    private List<UserPhoto> userPhotoList = new ArrayList<>();
    private UserPhotoAdapter userPhotoAdapter;
    private Query userPhotosQuery;

    private List<PostHolder> postHolderList = new ArrayList<>();
    private PostAdapter postAdapter;
    private Query newsfeedQuery;

    private DocumentSnapshot lastVisible2;
    private DocumentSnapshot lastVisible;

    private NestedScrollView announcementsNestedScrollView;

    private ImageView announcementsImageViewBgUPang;
    private ImageView announcementsCircleImageViewUPangDisplayPicture;

    private RelativeLayout announcementsRelativeLayoutEmptyView;
    private MyRecyclerView announcementsMyRecyclerViewUserPhotos;

    private TextView announcementsTextViewViewAll;

    private LinearLayout announcementsLinearLayoutEmptyView;
    private MyRecyclerView announcementsMyRecyclerViewNewsfeed;


    public Announcements() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_navigation_view_announcements, container, false);

        db = FirebaseFirestore.getInstance();
        uPangPhotosCollection = db.collection("UPangPhotos");
        newsfeedCollection = db.collection("Newsfeed");

        announcementsNestedScrollView = view.findViewById(R.id.announcementsNestedScrollView);

        announcementsImageViewBgUPang = view.findViewById(R.id.announcementsImageViewBgUPang);
        announcementsCircleImageViewUPangDisplayPicture = view.findViewById(R.id.announcementsCircleImageViewUPangDisplayPicture);

        announcementsTextViewViewAll = view.findViewById(R.id.announcementsTextViewViewAll);

        Glide.with(getActivity()).load(bgUpangDownloadUrl).placeholder(R.drawable.placeholder).into(announcementsImageViewBgUPang);
        Glide.with(getActivity()).load(dpUpangDownloadUrl).placeholder(R.drawable.placeholder).into(announcementsCircleImageViewUPangDisplayPicture);

        setUpAdapter(view);
        setUpAdapter2(view);

        startRetrievingNewsfeedFromDatabase();
        startRetrievingUserPhotosFromDatabase();

        announcementsNestedScrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        announcementsImageViewBgUPang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goToFullScreenImage(Uri.parse(bgUpangDownloadUrl));

            }
        });

        announcementsCircleImageViewUPangDisplayPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goToFullScreenImage(Uri.parse(dpUpangDownloadUrl));

            }
        });

        announcementsTextViewViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goToUserPhotos(userId, true);

            }
        });

        return view;
    }


    public void setUpAdapter2(View view) {

        userPhotoList = new ArrayList<>();
        userPhotoAdapter = new UserPhotoAdapter(userPhotoList, false, userId, "userActivity", true);

        announcementsMyRecyclerViewUserPhotos = view.findViewById(R.id.announcementsMyRecyclerViewUserPhotos);
        announcementsRelativeLayoutEmptyView = view.findViewById(R.id.announcementsRelativeLayoutEmptyView);

        // Empty view
        announcementsMyRecyclerViewUserPhotos.showIfEmpty(announcementsRelativeLayoutEmptyView);

        announcementsMyRecyclerViewUserPhotos.setHasFixedSize(true);
        announcementsMyRecyclerViewUserPhotos.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        announcementsMyRecyclerViewUserPhotos.setAdapter(userPhotoAdapter);
        ViewCompat.setNestedScrollingEnabled(announcementsMyRecyclerViewUserPhotos, false);

    }


    public void startRetrievingUserPhotosFromDatabase() {

        getInitialUserPhotosFromDatabase();

        announcementsMyRecyclerViewUserPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        userPhotosQuery = uPangPhotosCollection
                .whereEqualTo("userId", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .limit(paginationNumOfItemPerPage);

        getUserPhotos();

    }


    public void getMoreUserPhotosFromDatabase() {

        userPhotosQuery = uPangPhotosCollection
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


    public void setUpAdapter(View view) {

        postHolderList = new ArrayList<>();
        postAdapter = new PostAdapter(postHolderList);

        announcementsMyRecyclerViewNewsfeed = view.findViewById(R.id.announcementsMyRecyclerViewNewsfeed);
        announcementsLinearLayoutEmptyView = view.findViewById(R.id.announcementsLinearLayoutEmptyView);

        // Empty view
        announcementsMyRecyclerViewNewsfeed.showIfEmpty(announcementsLinearLayoutEmptyView);

        announcementsMyRecyclerViewNewsfeed.setHasFixedSize(true);
        announcementsMyRecyclerViewNewsfeed.setLayoutManager(new LinearLayoutManager(getActivity()));

        announcementsMyRecyclerViewNewsfeed.setAdapter(postAdapter);
        ViewCompat.setNestedScrollingEnabled(announcementsMyRecyclerViewNewsfeed, false);

    }


    public void startRetrievingNewsfeedFromDatabase() {

        getInitialNewsfeedFromDatabase();
        announcementsMyRecyclerViewNewsfeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                .whereEqualTo("postType", "School Updates")
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .limit(paginationNumOfItemPerPage);

        getNewsfeed();

    }


    public void getMoreNewsfeedFromDatabase() {

        newsfeedQuery = newsfeedCollection
                .whereEqualTo("postType", "School Updates")
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


    public void goToFullScreenImage(Uri uri) {

        Intent intentFullScreenImage = new Intent(getActivity(), FullScreenImage.class);
        intentFullScreenImage.setData(uri);
        startActivity(intentFullScreenImage);

    }


    public void goToUserPhotos(String userId, boolean isLandscape) {

        Intent intentUserPhotos = new Intent(getActivity(), UserPhotos.class);
        intentUserPhotos.putExtra("userId", userId);
        intentUserPhotos.putExtra("isLandscape", isLandscape);
        startActivity(intentUserPhotos);

    }


}
