package com.jeofferson.onclas.PackageActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeofferson.onclas.PackageBottomNavigationItems.Home;
import com.jeofferson.onclas.PackageBottomNavigationItems.Notifications;
import com.jeofferson.onclas.PackageBottomNavigationItems.Announcements;
import com.jeofferson.onclas.PackageBottomNavigationItems.Social;
import com.jeofferson.onclas.PackageForms.CreatePost;
import com.jeofferson.onclas.PackageForms.LogIn;
import com.jeofferson.onclas.PackageForms.SetUpAccount;
import com.jeofferson.onclas.PackageObjectModel.User;
import com.jeofferson.onclas.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private CollectionReference usersCollection;

    public static int paginationNumOfItemPerPage = 25;

    private User user;

    private long lastClicked = 0;

    private Toolbar toolbarMain;

    private BottomNavigationView mainActivityBottomNavigationView;
    private int selectedBottomNavigationItemIndex;

    private FloatingActionButton mainActivityFloatingActionButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);

        mAuth = FirebaseAuth.getInstance();
//        mAuth.signOut();    <-- If you delete a user but forgot to log him out...
        if (mAuth.getCurrentUser() != null) {

            db = FirebaseFirestore.getInstance();
            usersCollection = db.collection("Users");

            usersCollection.document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        user = task.getResult().toObject(User.class);

                        if (!user.getFullType().equals("NA")) {

                            if(user.isAuthorized()) {

                                setUpToolbar();

                                setUpBottomNavigationView();

                            } else {

                                goToPendingSetUpAccount(task.getResult().getString("fullType"));

                            }

                        } else {

                            goToSetUpAccount();

                        }

                    } else {

//                        Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }

                }
            });

        } else {

            goToLogIn();

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_toolbar_main, menu);
        return true;

    }


    @Override
    protected void onStart() {
        super.onStart();

        checkUserExistence();

        checkUserType();

    }


    public void checkUserExistence() {

        if (mAuth.getCurrentUser() == null) {

            goToLogIn();

        }

    }


    public void checkUserType() {

        if (mAuth.getCurrentUser() != null) {

            db = FirebaseFirestore.getInstance();
            usersCollection = db.collection("Users");

            usersCollection.document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        user = task.getResult().toObject(User.class);

                        if (user.getFullType().equals("NA")) {

                            goToSetUpAccount();

                        } else if (!user.isAuthorized()) {

                            checkAuthorization();

                        }

                    } else {

//                        Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }

                }
            });

        }

    }


    public void checkAuthorization() {

        if (mAuth.getCurrentUser() != null) {

            db = FirebaseFirestore.getInstance();
            usersCollection = db.collection("Users");

            usersCollection.document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        if (!task.getResult().getBoolean("authorized")) {

                            goToPendingSetUpAccount(task.getResult().getString("fullType"));

                        }

                    } else {

//                        Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }

                }
            });

        }

    }


    public void setUpToolbar() {

        CircleImageView toolbarMainCircleImageViewUserPicture = findViewById(R.id.toolbarMainCircleImageViewUserPicture);
        Glide.with(MainActivity.this).load(mAuth.getCurrentUser().getPhotoUrl()).placeholder(R.drawable.placeholder).into(toolbarMainCircleImageViewUserPicture);

        toolbarMain = findViewById(R.id.toolbarMain);
        ViewCompat.setElevation(toolbarMain, 0);
        setSupportActionBar(toolbarMain);

        getSupportActionBar().setTitle("");

        toolbarMainCircleImageViewUserPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long nowClicked = System.currentTimeMillis();

                if ((nowClicked - lastClicked) > 1000) {

                    lastClicked = nowClicked;
                    goToUserActivity(mAuth.getCurrentUser().getUid());

                }

            }
        });

    }


    public void setUpBottomNavigationView() {

        mainActivityBottomNavigationView = findViewById(R.id.mainActivityBottomNavigationView);
        mainActivityFloatingActionButton = findViewById(R.id.mainActivityFloatingActionButton);

        setInitialBottomNavigationViewItem();

        mainActivityBottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        mainActivityFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goToCreatePost();

            }
        });

    }


    public void setInitialBottomNavigationViewItem() {

        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, new Home()).commit();
        mainActivityBottomNavigationView.setSelectedItemId(R.id.menuBottomNavigationViewHome);
        selectedBottomNavigationItemIndex = 2;

    }


    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            Fragment selectedFragment = null;

            switch (menuItem.getItemId()) {

                case R.id.menuBottomNavigationViewMessages:
                    selectedFragment = new Announcements();
                    selectedBottomNavigationItemIndex = 1;
                    break;

                case R.id.menuBottomNavigationViewHome:
                    selectedFragment = new Home();
                    selectedBottomNavigationItemIndex = 2;
                    break;

                case R.id.menuBottomNavigationViewExtra:
                    return false;

                case R.id.menuBottomNavigationViewSocial:
                    selectedFragment = new Social();
                    selectedBottomNavigationItemIndex = 3;
                    break;

                case R.id.menuBottomNavigationViewNotifications:
                    selectedFragment = new Notifications();
                    selectedBottomNavigationItemIndex = 4;
                    break;

            }

            getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, selectedFragment).commit();

            return true;

        }
    };


    @Override
    public void onBackPressed() {

        if (selectedBottomNavigationItemIndex != 2) {

            getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, new Home()).commit();
            mainActivityBottomNavigationView.setSelectedItemId(R.id.menuBottomNavigationViewHome);
            selectedBottomNavigationItemIndex = 2;

        } else {

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

    }


    public void goToCreatePost() {

        Intent intentCreatePost = new Intent(MainActivity.this, CreatePost.class);
        intentCreatePost.putExtra("from", "MainActivity");
        startActivity(intentCreatePost);

    }


    public void goToUserActivity(String userId) {

        Intent intentUserActivity = new Intent(MainActivity.this, UserActivity.class);
        intentUserActivity.putExtra("userId", userId);
        startActivity(intentUserActivity);

    }


    public void goToPendingSetUpAccount(String fullType) {

        Intent intentSetUpAccount = new Intent(MainActivity.this, PendingSetUpAccount.class);
        intentSetUpAccount.putExtra("fullType", fullType);
        startActivity(intentSetUpAccount);
        finish();

    }


    public void goToSetUpAccount() {

        Intent intentSetUpAccount = new Intent(MainActivity.this, SetUpAccount.class);
        startActivity(intentSetUpAccount);
        finish();

    }


    public void goToLogIn() {

        Intent intentLogIn = new Intent(MainActivity.this, LogIn.class);
        startActivity(intentLogIn);
        finish();

    }


}
