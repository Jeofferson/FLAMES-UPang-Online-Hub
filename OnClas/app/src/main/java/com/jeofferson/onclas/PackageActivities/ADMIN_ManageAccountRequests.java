package com.jeofferson.onclas.PackageActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeofferson.onclas.PackageAdapters.ADMIN_AccountRequestAdapter;
import com.jeofferson.onclas.PackageObjectModel.AdminAccountRequestHolder;
import com.jeofferson.onclas.PackageOthers.MyRecyclerView;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.jeofferson.onclas.PackageActivities.MainActivity.paginationNumOfItemPerPage;

public class ADMIN_ManageAccountRequests extends AppCompatActivity {


    private FirebaseFirestore db;
    private CollectionReference adminAccountRequestsCollection;

    private List<AdminAccountRequestHolder> adminAccountRequestList;

    private Query adminAccountRequestsOrder;
    private ListenerRegistration listenerRegistrationAdminAccountRequests;

    private boolean areAdminAccountRequestsFirstTimeLoaded = true;
    private DocumentSnapshot lastVisible;

    private ADMIN_AccountRequestAdapter adminAccountRequestAdapter;
    private MyRecyclerView adminManageAccountRequestsMyRecyclerViewAdminAccountRequests;
    private RelativeLayout adminManageAccountRequestsRelativeLayoutEmptyView;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_account_requests);

        db = FirebaseFirestore.getInstance();
        adminAccountRequestsCollection = db.collection("AdminAccountRequests");

        toolbar = findViewById(R.id.toolbar);

        adminManageAccountRequestsRelativeLayoutEmptyView = findViewById(R.id.adminManageAccountRequestsRelativeLayoutEmptyView);

        setStatusBar();

        setUpToolbar();

        setUpAdapter();

        getInitialAdminAccountRequestsFromDatabase();
        adminManageAccountRequestsMyRecyclerViewAdminAccountRequests.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean isBottomReached = !recyclerView.canScrollVertically(1);

                if (isBottomReached) {

                    getMoreAdminAccountRequestsFromDatabase();

                }

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_set_up_account, menu);
        return true;

    }


    public void setStatusBar() {

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

    }


    public void setUpToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.admin));

    }


    public void setUpAdapter() {

        adminAccountRequestList = new ArrayList<>();
        adminAccountRequestAdapter = new ADMIN_AccountRequestAdapter(adminAccountRequestList);

        adminManageAccountRequestsMyRecyclerViewAdminAccountRequests = findViewById(R.id.adminManageAccountRequestsMyRecyclerViewAdminAccountRequests);

        // Empty view
        adminManageAccountRequestsMyRecyclerViewAdminAccountRequests.showIfEmpty(adminManageAccountRequestsRelativeLayoutEmptyView);

        adminManageAccountRequestsMyRecyclerViewAdminAccountRequests.setHasFixedSize(true);
        adminManageAccountRequestsMyRecyclerViewAdminAccountRequests.setLayoutManager(new LinearLayoutManager(ADMIN_ManageAccountRequests.this));

        adminManageAccountRequestsMyRecyclerViewAdminAccountRequests.setAdapter(adminAccountRequestAdapter);

    }


    public void getInitialAdminAccountRequestsFromDatabase() {

        adminAccountRequestsOrder = adminAccountRequestsCollection
                .whereEqualTo("decided", false)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .limit(paginationNumOfItemPerPage);

        getAdminAccountRequests(0);

    }


    public void getMoreAdminAccountRequestsFromDatabase() {

        adminAccountRequestsOrder = adminAccountRequestsCollection
                .whereEqualTo("decided", false)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(paginationNumOfItemPerPage);

        getAdminAccountRequests(1);

    }


    public void getAdminAccountRequests(final int nthQuery) {

        listenerRegistrationAdminAccountRequests = adminAccountRequestsOrder.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {

                    Log.e("My Error: ", e.getMessage());
                    return;

                }

                if (!queryDocumentSnapshots.isEmpty()) {

                    if (areAdminAccountRequestsFirstTimeLoaded || nthQuery == 1) {

                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    }

                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                        switch (documentChange.getType()) {

                            case ADDED:

                                AdminAccountRequestHolder adminAccountRequestHolder = documentChange.getDocument().toObject(AdminAccountRequestHolder.class);
                                adminAccountRequestHolder.setObjectId(documentChange.getDocument().getId());

                                if (areAdminAccountRequestsFirstTimeLoaded || nthQuery == 1) {

                                    adminAccountRequestList.add(adminAccountRequestHolder);
                                    adminAccountRequestAdapter.notifyDataSetChanged();

                                } else {

                                    adminAccountRequestList.add(0, adminAccountRequestHolder);
                                    adminAccountRequestAdapter.notifyItemInserted(0);

                                }

                                break;

                        }

                    }
                    areAdminAccountRequestsFirstTimeLoaded = false;

                }

            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menuSetUpAccountLogout:

                goToLogOut();
                break;

        }

        return true;

    }


    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.onclas_logo)
                .setTitle(getResources().getString(R.string.app_name))
                .setMessage(getResources().getString(R.string.doWantToExit))
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finishAffinity();

                    }

                })
                .setNegativeButton(getResources().getString(R.string.no), null)
                .show();

    }


    public void goToLogOut() {

        Intent intentLogOut = new Intent(ADMIN_ManageAccountRequests.this, LogOut.class);
        startActivity(intentLogOut);
        finish();

    }


    @Override
    public void onStop() {
        super.onStop();

        listenerRegistrationAdminAccountRequests.remove();

    }


}
