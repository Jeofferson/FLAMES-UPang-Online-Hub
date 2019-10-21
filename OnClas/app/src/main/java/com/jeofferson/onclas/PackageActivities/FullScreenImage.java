package com.jeofferson.onclas.PackageActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.jeofferson.onclas.R;
import com.jsibbold.zoomage.ZoomageView;

public class FullScreenImage extends AppCompatActivity {


    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private CollectionReference userPhotosCollection;

    private Uri uri;
    private String userId;
    private String userPhotoId;
    private String from;

    private RelativeLayout fullScreenImageRelativeLayoutMainImage;
    private ZoomageView fullScreenImageZoomageView;

    private RelativeLayout fullScreenImageRelativeLayoutCustomTab;
    private ImageView fullScreenImageImageViewDeletePicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        userPhotosCollection = db.collection("UserPhotos");

        setStatusBar();

        uri = getIntent().getData();
        userId = getIntent().getStringExtra("userId");

        fullScreenImageRelativeLayoutMainImage = findViewById(R.id.fullScreenImageRelativeLayoutMainImage);
        fullScreenImageZoomageView = findViewById(R.id.fullScreenImageZoomageView);

        fullScreenImageRelativeLayoutCustomTab = findViewById(R.id.fullScreenImageRelativeLayoutCustomTab);
        fullScreenImageImageViewDeletePicture = findViewById(R.id.fullScreenImageImageViewDeletePicture);

        if (userId != null) {

            if (userId.equals(mAuth.getCurrentUser().getUid())) {

                fullScreenImageRelativeLayoutCustomTab.setVisibility(View.VISIBLE);

            } else {

                fullScreenImageRelativeLayoutCustomTab.setVisibility(View.GONE);

            }

        } else {

            fullScreenImageRelativeLayoutCustomTab.setVisibility(View.GONE);

        }

        Glide.with(FullScreenImage.this).load(uri).into(fullScreenImageZoomageView);

        fullScreenImageImageViewDeletePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(FullScreenImage.this)
                        .setTitle(getResources().getString(R.string.deletePhoto))
                        .setMessage(getResources().getString(R.string.confirmDeletePhotoMessage))
                        .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final ProgressDialog progressDialog = ProgressDialog.show(FullScreenImage.this, getResources().getString(R.string.pleaseWait), getResources().getString(R.string.deletingPhoto), true);

                                userPhotoId = getIntent().getStringExtra("userPhotoId");

                                WriteBatch writeBatch = db.batch();
                                writeBatch.delete(userPhotosCollection.document(userPhotoId));
                                writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            from = getIntent().getStringExtra("from");

                                            progressDialog.dismiss();

                                            switch (from) {

                                                case "userActivity":
                                                    goToUserActivity(userId);
                                                    break;

                                                case "userPhotos":
                                                    goToUserPhotos(userId);
                                                    break;

                                            }

                                        } else {

                                            Toast.makeText(FullScreenImage.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                        }

                                    }
                                });

                            }

                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), null)
                        .show();
                
            }
        });

    }


    public void setStatusBar() {

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

    }


    public void goToUserPhotos(String userId) {

        Intent intentUserPhotos = new Intent(FullScreenImage.this, UserPhotos.class);
        intentUserPhotos.putExtra("userId", userId);
        intentUserPhotos.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentUserPhotos);
        finish();

    }


    public void goToUserActivity(String userId) {

        Intent intentUserActivity = new Intent(FullScreenImage.this, UserActivity.class);
        intentUserActivity.putExtra("userId", userId);
        intentUserActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentUserActivity);
        finish();

    }


}
