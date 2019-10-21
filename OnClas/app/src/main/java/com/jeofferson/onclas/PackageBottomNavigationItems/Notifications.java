package com.jeofferson.onclas.PackageBottomNavigationItems;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeofferson.onclas.PackageAdapters.NotificationAdapter;
import com.jeofferson.onclas.PackageAdapters.PostAdapter;
import com.jeofferson.onclas.PackageObjectModel.NotificationHolder;
import com.jeofferson.onclas.PackageObjectModel.PostHolder;
import com.jeofferson.onclas.PackageOthers.MyRecyclerView;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.jeofferson.onclas.PackageActivities.MainActivity.paginationNumOfItemPerPage;


public class Notifications extends Fragment {


    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private CollectionReference notificationsCollection;

    private List<NotificationHolder> notificationHolderList;

    private Query notificationsQuery;
    private ListenerRegistration listenerRegistrationNotifications;

    private boolean areNotificationsFirstTimeLoaded;
    private DocumentSnapshot lastVisible;

    private NotificationAdapter notificationAdapter;
    private MyRecyclerView notificationsMyRecyclerViewNotifications;
    private LinearLayout notificationsLinearLayoutEmptyView;


    public Notifications() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.bottom_navigation_view_notifications, container, false);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        notificationsCollection = db.collection("Notifications");

        setUpAdapter(view);

        startRetrievingNotificationsFromDatabase();

        return view;
    }


    public void setUpAdapter(View view) {

        notificationHolderList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationHolderList);

        notificationsMyRecyclerViewNotifications = view.findViewById(R.id.notificationsMyRecyclerViewNotifications);
        notificationsLinearLayoutEmptyView = view.findViewById(R.id.notificationsLinearLayoutEmptyView);

        // Empty view
        notificationsMyRecyclerViewNotifications.showIfEmpty(notificationsLinearLayoutEmptyView);

        notificationsMyRecyclerViewNotifications.setHasFixedSize(true);
        notificationsMyRecyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getActivity()));

        notificationsMyRecyclerViewNotifications.setAdapter(notificationAdapter);

    }


    public void startRetrievingNotificationsFromDatabase() {

        areNotificationsFirstTimeLoaded = true;

        getInitialNotificationsFromDatabase();
        notificationsMyRecyclerViewNotifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean isBottomReached = !recyclerView.canScrollVertically(1);

                if (isBottomReached) {

                    getMoreNotificationsFromDatabase();

                }

            }
        });

    }


    public void getInitialNotificationsFromDatabase() {

        notificationsQuery = notificationsCollection
                .whereEqualTo("toId", mAuth.getCurrentUser().getUid())
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .limit(paginationNumOfItemPerPage);

        getNotifications(0);

    }


    public void getMoreNotificationsFromDatabase() {

        notificationsQuery = notificationsCollection
                .whereEqualTo("toId", mAuth.getCurrentUser().getUid())
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(paginationNumOfItemPerPage);

        getNotifications(1);

    }


    public void getNotifications(final int nthQuery) {

        listenerRegistrationNotifications = notificationsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {

                    Log.e("My Error: ", e.getMessage());
                    return;

                }

                if (!queryDocumentSnapshots.isEmpty()) {

                    if (areNotificationsFirstTimeLoaded || nthQuery == 1) {

                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    }

                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                        switch (documentChange.getType()) {

                            case ADDED:

                                NotificationHolder notificationHolder = documentChange.getDocument().toObject(NotificationHolder.class);
                                notificationHolder.setObjectId(documentChange.getDocument().getId());

                                if (areNotificationsFirstTimeLoaded || nthQuery == 1) {

                                    notificationHolderList.add(notificationHolder);
                                    notificationAdapter.notifyDataSetChanged();

                                } else {

                                    notificationHolderList.add(0, notificationHolder);
                                    notificationAdapter.notifyItemInserted(0);

                                }

                                break;

                        }

                    }
                    areNotificationsFirstTimeLoaded = false;

                }

            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();

        if (notificationAdapter != null) {

            notificationAdapter.notifyDataSetChanged();

        }


    }


    @Override
    public void onStop() {
        super.onStop();

        if (listenerRegistrationNotifications != null) {

            listenerRegistrationNotifications.remove();

        }

    }


}
