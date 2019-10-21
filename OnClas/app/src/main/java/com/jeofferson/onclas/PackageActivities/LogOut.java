package com.jeofferson.onclas.PackageActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeofferson.onclas.PackageForms.LogIn;
import com.jeofferson.onclas.R;

import java.util.HashMap;
import java.util.Map;


public class LogOut extends AppCompatActivity {

    FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private CollectionReference usersCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_out);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("Users");

        setStatusBar();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("deviceToken", "");

            usersCollection.document(mAuth.getCurrentUser().getUid()).update(tokenMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mAuth.signOut();

                            goToLogIn();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(LogOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

            }
        }, 500);

    }


    public void setStatusBar() {

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

    }


    public void goToLogIn() {

        Intent intentLogIn = new Intent(LogOut.this, LogIn.class);
        startActivity(intentLogIn);
        finish();

    }


}
