package com.jeofferson.onclas.PackageOthers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeofferson.onclas.PackageActivities.CommentActivity;
import com.jeofferson.onclas.PackageActivities.MainActivity;
import com.jeofferson.onclas.PackageActivities.Updates;
import com.jeofferson.onclas.R;

public class TestNotification extends AppCompatActivity {


    private FirebaseFirestore db;
    private CollectionReference postsCollection;
    private CollectionReference commentsCollection;
    private CollectionReference replyHoldersCollection;

    private String notificationType;
    private String destinationPostHolder;
    private String destinationPost;
    private String destinationCommentHolder;
    private String destinationComment;
    private String destinationReplyHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_test_notification);

        notificationType = getIntent().getStringExtra("notificationType");
        destinationPostHolder = getIntent().getStringExtra("destinationPostHolder");
        destinationPost = getIntent().getStringExtra("destinationPost");
        destinationCommentHolder = getIntent().getStringExtra("destinationCommentHolder");
        destinationComment = getIntent().getStringExtra("destinationComment");
        destinationReplyHolder = getIntent().getStringExtra("destinationReplyHolder");

        db = FirebaseFirestore.getInstance();
        postsCollection = db.collection("Posts");
        commentsCollection = db.collection("Comments");
        replyHoldersCollection = db.collection("ReplyHolders");

        checkIfPostExists();

    }


    public void checkIfPostExists() {

        postsCollection.document(destinationPost).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {

                            checkIfCommentExists();

                        } else {

                            showMayBeenDeletedDialog();

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

//                        Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void checkIfCommentExists() {

        commentsCollection.document(destinationComment).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {

                            switch (notificationType) {

                                case "Comment Notification":

                                    goToStudentForum(destinationPostHolder, destinationPost, false, true);
                                    break;

                                case "Reply Notification":

                                    checkIfReplyExists();
                                    break;

                            }

                        } else {

                            showMayBeenDeletedDialog();

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

//                        Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void checkIfReplyExists() {

        replyHoldersCollection.document(destinationReplyHolder).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {

                            goToCommentActivity(destinationCommentHolder, destinationComment, false, true);

                        } else {

                            showMayBeenDeletedDialog();

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

//                        Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void showMayBeenDeletedDialog() {

        new AlertDialog.Builder(TestNotification.this)
                .setTitle(getResources().getString(R.string.oops))
                .setMessage(getResources().getString(R.string.mayBeenDeleted))
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        goToMain();

                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {

                        goToMain();

                    }
                })
                .show();

    }


    public void goToStudentForum(String postHolderId, String postId, boolean willComment, boolean isFromNotifications) {

        Intent intentStudentForum = new Intent(TestNotification.this, Updates.class);
        intentStudentForum.putExtra("postHolderId", postHolderId);
        intentStudentForum.putExtra("postId", postId);
        intentStudentForum.putExtra("willComment", willComment);
        intentStudentForum.putExtra("isFromNotifications", isFromNotifications);
        startActivity(intentStudentForum);
        finish();

    }


    public void goToCommentActivity(String commentHolderId, String commentId, boolean willComment, boolean isFromNotifications) {

        Intent intentCommentActivity = new Intent(TestNotification.this, CommentActivity.class);
        intentCommentActivity.putExtra("commentHolderId", commentHolderId);
        intentCommentActivity.putExtra("commentId", commentId);
        intentCommentActivity.putExtra("willComment", willComment);
        intentCommentActivity.putExtra("isFromNotifications", isFromNotifications);
        startActivity(intentCommentActivity);
        finish();

    }


    public void goToMain() {

        Intent intentMain = new Intent(TestNotification.this, MainActivity.class);
        startActivity(intentMain);

    }


}