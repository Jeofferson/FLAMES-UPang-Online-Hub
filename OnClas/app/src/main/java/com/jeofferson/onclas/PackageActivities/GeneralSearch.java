package com.jeofferson.onclas.PackageActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
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
import com.jeofferson.onclas.PackageAdapters.GeneralSearchItemAdapter;
import com.jeofferson.onclas.PackageObjectModel.GeneralSearchItem;
import com.jeofferson.onclas.PackageObjectModel.Liker;
import com.jeofferson.onclas.PackageOthers.MyRecyclerView;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.List;


import static com.jeofferson.onclas.PackageActivities.MainActivity.paginationNumOfItemPerPage;

public class GeneralSearch extends AppCompatActivity {


    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private Query generalSearchListOrder;
    private CollectionReference postLikersCollection;
    private CollectionReference commentLikersCollection;
    private CollectionReference replyLikersCollection;

    private List<GeneralSearchItem> generalSearchItemList;

    private DocumentSnapshot lastVisible;

    private GeneralSearchItemAdapter generalSearchItemAdapter;
    private MyRecyclerView generalSearchMyRecyclerViewGeneralSearchList;
    private RelativeLayout generalSearchRelativeLayoutEmptyView;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_search);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        postLikersCollection = db.collection("PostLikers");
        commentLikersCollection = db.collection("CommentLikers");
        replyLikersCollection = db.collection("ReplyLikers");

        setStatusBar();
        setUpToolbar();

        generalSearchRelativeLayoutEmptyView = findViewById(R.id.generalSearchRelativeLayoutEmptyView);

        setUpAdapter();

        getInitialGeneralSearchListFromDatabase();
        generalSearchMyRecyclerViewGeneralSearchList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean isBottomReached = !recyclerView.canScrollVertically(1);

                if (isBottomReached) {

                    getMoreGeneralSearchListFromDatabase();

                }

            }
        });

    }


    public void setStatusBar() {

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

    }


    public void setUpToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switch (getIntent().getStringExtra("type")) {

            case "postLikers":
            case "commentLikers":
            case "replyLikers":
                getSupportActionBar().setTitle(getResources().getString(R.string.peopleWhoLiked));
                break;

        }

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();
                break;

        }

        return true;

    }


    public void setUpAdapter() {

        generalSearchItemList = new ArrayList<>();
        generalSearchItemAdapter = new GeneralSearchItemAdapter(generalSearchItemList);

        generalSearchMyRecyclerViewGeneralSearchList = findViewById(R.id.generalSearchMyRecyclerViewGeneralSearchList);

        // Empty view
        generalSearchMyRecyclerViewGeneralSearchList.showIfEmpty(generalSearchRelativeLayoutEmptyView);

        generalSearchMyRecyclerViewGeneralSearchList.setHasFixedSize(true);
        generalSearchMyRecyclerViewGeneralSearchList.setLayoutManager(new LinearLayoutManager(this));

        generalSearchMyRecyclerViewGeneralSearchList.setAdapter(generalSearchItemAdapter);

    }


    public void getInitialGeneralSearchListFromDatabase() {

        switch (getIntent().getStringExtra("type")) {

            case "postLikers":
                generalSearchListOrder = postLikersCollection
                        .whereEqualTo("likedItemId", getIntent().getStringExtra("postId"))
                        .orderBy("itemName")
                        .limit(paginationNumOfItemPerPage);
                break;

            case "commentLikers":
                generalSearchListOrder = commentLikersCollection
                        .whereEqualTo("likedItemId", getIntent().getStringExtra("commentId"))
                        .orderBy("itemName")
                        .limit(paginationNumOfItemPerPage);
                break;

            case "replyLikers":
                generalSearchListOrder = replyLikersCollection
                        .whereEqualTo("likedItemId", getIntent().getStringExtra("replyId"))
                        .orderBy("itemName")
                        .limit(paginationNumOfItemPerPage);
                break;

        }

        getGeneralSearchList();

    }


    public void getMoreGeneralSearchListFromDatabase() {

        switch (getIntent().getStringExtra("type")) {

            case "postLikers":
                generalSearchListOrder = postLikersCollection
                        .whereEqualTo("likedItemId", getIntent().getStringExtra("postId"))
                        .orderBy("itemName")
                        .startAfter(lastVisible)
                        .limit(paginationNumOfItemPerPage);
                break;

            case "commentLikers":
                generalSearchListOrder = commentLikersCollection
                        .whereEqualTo("likedItemId", getIntent().getStringExtra("commentId"))
                        .orderBy("itemName")
                        .startAfter(lastVisible)
                        .limit(paginationNumOfItemPerPage);
                break;

            case "replyLikers":
                generalSearchListOrder = replyLikersCollection
                        .whereEqualTo("likedItemId", getIntent().getStringExtra("replyId"))
                        .orderBy("itemName")
                        .startAfter(lastVisible)
                        .limit(paginationNumOfItemPerPage);
                break;

        }

        getGeneralSearchList();

    }


    public void getGeneralSearchList() {

        generalSearchListOrder.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                Liker liker = queryDocumentSnapshot.toObject(Liker.class);
                                GeneralSearchItem generalSearchItem = new GeneralSearchItem(liker.getItemId(), liker.getItemPicture(), liker.getItemName(), liker.getType(), liker.getDepartments(), liker.getYear(), liker.getFullType(), liker.getItemType(), true);

                                generalSearchItemList.add(generalSearchItem);
                                generalSearchItemAdapter.notifyDataSetChanged();

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e("My Error: ", e.getLocalizedMessage());
                        Toast.makeText(GeneralSearch.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


}
