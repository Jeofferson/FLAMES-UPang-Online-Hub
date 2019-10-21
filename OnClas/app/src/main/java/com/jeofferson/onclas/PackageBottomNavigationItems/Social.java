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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeofferson.onclas.PackageAdapters.GeneralSearchItemAdapter;
import com.jeofferson.onclas.PackageObjectModel.GeneralSearchItem;
import com.jeofferson.onclas.PackageOthers.MyRecyclerView;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.jeofferson.onclas.PackageActivities.MainActivity.paginationNumOfItemPerPage;


public class Social extends Fragment {


    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private CollectionReference generalSearchListCollection;

    private List<GeneralSearchItem> generalSearchItemList;

    private Query generalSearchListQuery;

    private boolean areGeneralSearchListFirstTimeLoaded;
    private DocumentSnapshot lastVisible;

    private GeneralSearchItemAdapter generalSearchItemAdapter;
    private MyRecyclerView socialMyRecyclerViewGeneralSearchList;
    private LinearLayout socialLinearLayoutEmptyView;

    private boolean isSocialSpinner1FirstTimeLoaded = true;
    private Spinner socialSpinner1;
    private String spinnerCurrentlySelectedItem1 = "All Users";

    private Spinner socialSpinner2;
    private String spinnerCurrentlySelectedItem2 = "All Departments";


    public Social() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_navigation_view_social, container, false);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        generalSearchListCollection = db.collection("GeneralSearchList");

        setUpSpinner1(view);
        setUpSpinner2(view);

        setUpAdapter(view);

        return view;
    }


    public void setUpSpinner1(View view) {

        socialSpinner1 = view.findViewById(R.id.socialSpinner1);

        final ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.userTypes, R.layout.list_spinner_item);
        arrayAdapter.setDropDownViewResource(R.layout.list_spinner_item);

        socialSpinner1.setAdapter(arrayAdapter);

        socialSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                spinnerCurrentlySelectedItem1 = adapterView.getItemAtPosition(i).toString();

                TextView selectedText = (TextView) adapterView.getChildAt(0);
                if (selectedText != null) {

                    selectedText.setTextColor(getResources().getColor(R.color.lightTextSelected));

                }

                if (!isSocialSpinner1FirstTimeLoaded) {

                    emptyRecyclerView();
                    startRetrievingGeneralSearchListFromDatabase();

                }

                isSocialSpinner1FirstTimeLoaded = false;

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    public void setUpSpinner2(View view) {

        socialSpinner2 = view.findViewById(R.id.socialSpinner2);

        final ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.departments, R.layout.list_spinner_item);
        arrayAdapter.setDropDownViewResource(R.layout.list_spinner_item);

        socialSpinner2.setAdapter(arrayAdapter);

        socialSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                spinnerCurrentlySelectedItem2 = adapterView.getItemAtPosition(i).toString();

                TextView selectedText = (TextView) adapterView.getChildAt(0);
                if (selectedText != null) {

                    selectedText.setTextColor(getResources().getColor(R.color.lightTextSelected));

                }

                emptyRecyclerView();
                startRetrievingGeneralSearchListFromDatabase();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    public void setUpAdapter(View view) {

        generalSearchItemList = new ArrayList<>();
        generalSearchItemAdapter = new GeneralSearchItemAdapter(generalSearchItemList);

        socialMyRecyclerViewGeneralSearchList = view.findViewById(R.id.socialMyRecyclerViewGeneralSearchList);
        socialLinearLayoutEmptyView = view.findViewById(R.id.socialLinearLayoutEmptyView);

        // Empty view
        socialMyRecyclerViewGeneralSearchList.showIfEmpty(socialLinearLayoutEmptyView);

        socialMyRecyclerViewGeneralSearchList.setHasFixedSize(true);
        socialMyRecyclerViewGeneralSearchList.setLayoutManager(new LinearLayoutManager(getActivity()));

        socialMyRecyclerViewGeneralSearchList.setAdapter(generalSearchItemAdapter);

    }


    public void emptyRecyclerView() {

        generalSearchItemList.clear();
        generalSearchItemAdapter.notifyDataSetChanged();

    }


    public void startRetrievingGeneralSearchListFromDatabase() {

        areGeneralSearchListFirstTimeLoaded = true;

        getInitialGeneralSearchListFromDatabase();
        socialMyRecyclerViewGeneralSearchList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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


    public void getInitialGeneralSearchListFromDatabase() {

        String type;
        String department;

        switch (spinnerCurrentlySelectedItem1) {

            case "All Users":
                type = "All Users Type";
                break;

            case "Students":
                type = "Student";
                break;

            case "SC Officers":
                type = "SC Officer";
                break;

            case "Teachers":
                type = "Teacher";
                break;

            default:
                type = "All Users Type";

        }

        switch (spinnerCurrentlySelectedItem2) {

            case "All Departments":
                department = "All Departments Department";
                break;

            case "SHS":
                department = "SHS";
                break;

            case "CAS":
                department = "CAS";
                break;

            case "CEA":
                department = "CEA";
                break;

            case "CHS":
                department = "CHS";
                break;

            case "CITE":
                department = "CITE";
                break;

            case "CMA":
                department = "CMA";
                break;

            case "CSS":
                department = "CSS";
                break;

            default:
                department = "All Departments Department";

        }

        if (type.equals("All Users Type") && department.equals("All Departments Department")) {

            generalSearchListQuery = generalSearchListCollection
                    .whereEqualTo("itemType", "User")
                    .whereEqualTo("authorized", true)
                    .orderBy("itemName")
                    .limit(paginationNumOfItemPerPage);

        } else if (!type.equals("All Users Type") && department.equals("All Departments Department")) {

            generalSearchListQuery = generalSearchListCollection
                    .whereEqualTo("type", type)
                    .whereEqualTo("itemType", "User")
                    .whereEqualTo("authorized", true)
                    .orderBy("itemName")
                    .limit(paginationNumOfItemPerPage);

        } else if (type.equals("All Users Type") && !department.equals("All Departments Department")) {

            generalSearchListQuery = generalSearchListCollection
                    .whereArrayContains("departments", department)
                    .whereEqualTo("itemType", "User")
                    .whereEqualTo("authorized", true)
                    .orderBy("itemName")
                    .limit(paginationNumOfItemPerPage);

        } else if (!type.equals("All Users Type") && !department.equals("All Departments Department")) {

            generalSearchListQuery = generalSearchListCollection
                    .whereEqualTo("type", type)
                    .whereArrayContains("departments", department)
                    .whereEqualTo("itemType", "User")
                    .whereEqualTo("authorized", true)
                    .orderBy("itemName")
                    .limit(paginationNumOfItemPerPage);

        }

        getGeneralSearchList(0);

    }


    public void getMoreGeneralSearchListFromDatabase() {

        String type;
        String department;

        switch (spinnerCurrentlySelectedItem1) {

            case "All Users":
                type = "All Users Type";
                break;

            case "Students":
                type = "Student";
                break;

            case "SC Officers":
                type = "SC Officer";
                break;

            case "Teachers":
                type = "Teacher";
                break;

            default:
                type = "All Users Type";

        }

        switch (spinnerCurrentlySelectedItem2) {

            case "All Departments":
                department = "All Departments Department";
                break;

            case "SHS":
                department = "SHS";
                break;

            case "CAS":
                department = "CAS";
                break;

            case "CEA":
                department = "CEA";
                break;

            case "CHS":
                department = "CHS";
                break;

            case "CITE":
                department = "CITE";
                break;

            case "CMA":
                department = "CMA";
                break;

            case "CSS":
                department = "CSS";
                break;

            default:
                department = "All Departments Department";

        }

        if (type.equals("All Users Type") && department.equals("All Departments Department")) {

            generalSearchListQuery = generalSearchListCollection
                    .whereEqualTo("itemType", "User")
                    .whereEqualTo("authorized", true)
                    .orderBy("itemName")
                    .startAfter(lastVisible)
                    .limit(paginationNumOfItemPerPage);

        } else if (!type.equals("All Users Type") && department.equals("All Departments Department")) {

            generalSearchListQuery = generalSearchListCollection
                    .whereEqualTo("type", type)
                    .whereEqualTo("itemType", "User")
                    .whereEqualTo("authorized", true)
                    .orderBy("itemName")
                    .startAfter(lastVisible)
                    .limit(paginationNumOfItemPerPage);

        } else if (type.equals("All Users Type") && !department.equals("All Departments Department")) {

            generalSearchListQuery = generalSearchListCollection
                    .whereArrayContains("departments", department)
                    .whereEqualTo("itemType", "User")
                    .whereEqualTo("authorized", true)
                    .orderBy("itemName")
                    .startAfter(lastVisible)
                    .limit(paginationNumOfItemPerPage);

        } else if (!type.equals("All Users Type") && !department.equals("All Departments Department")) {

            generalSearchListQuery = generalSearchListCollection
                    .whereEqualTo("type", type)
                    .whereArrayContains("departments", department)
                    .whereEqualTo("itemType", "User")
                    .whereEqualTo("authorized", true)
                    .orderBy("itemName")
                    .startAfter(lastVisible)
                    .limit(paginationNumOfItemPerPage);

        }

        getGeneralSearchList(1);

    }


    public void getGeneralSearchList(final int nthQuery) {

        generalSearchListQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            if (areGeneralSearchListFirstTimeLoaded || nthQuery == 1) {

                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                            }

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                GeneralSearchItem generalSearchItem = queryDocumentSnapshot.toObject(GeneralSearchItem.class);

                                if (areGeneralSearchListFirstTimeLoaded || nthQuery == 1) {

                                    generalSearchItemList.add(generalSearchItem);
                                    generalSearchItemAdapter.notifyDataSetChanged();

                                } else {

                                    generalSearchItemList.add(0, generalSearchItem);
                                    generalSearchItemAdapter.notifyItemInserted(0);

                                }

                            }
                            areGeneralSearchListFirstTimeLoaded = false;

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

//                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    @Override
    public void onResume() {
        super.onResume();

        if (generalSearchItemAdapter != null) {

            generalSearchItemAdapter.notifyDataSetChanged();

        }

    }


}
