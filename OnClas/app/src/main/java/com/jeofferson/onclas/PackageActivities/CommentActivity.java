package com.jeofferson.onclas.PackageActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jeofferson.onclas.PackageAdapters.ReplyAdapter;
import com.jeofferson.onclas.PackageForms.EditComment;
import com.jeofferson.onclas.PackageObjectModel.Comment;
import com.jeofferson.onclas.PackageObjectModel.GeneralSearchItem;
import com.jeofferson.onclas.PackageObjectModel.Liker;
import com.jeofferson.onclas.PackageObjectModel.NotificationHolder;
import com.jeofferson.onclas.PackageObjectModel.ReplyHolder;
import com.jeofferson.onclas.PackageOthers.BottomSheetDialogComment;
import com.jeofferson.onclas.PackageOthers.BottomSheetDialogReply;
import com.jeofferson.onclas.PackageOthers.MyRecyclerView;
import com.jeofferson.onclas.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity implements BottomSheetDialogComment.BottomSheetDialogListener, BottomSheetDialogReply.BottomSheetDialogListener {


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 10;
    static final int REQUEST_CODE_PHONE_GALLERY_COMMENT_PICTURE = 11;

    private FirebaseAuth mAuth;

    private StorageReference mStorageRef;
    private StorageReference replyPicturePath;

    private FirebaseFirestore db;
    private CollectionReference generalSearchListCollection;
    private CollectionReference commentHoldersCollection;
    private CollectionReference commentsCollection;
    private CollectionReference commentLikersCollection;
    private CollectionReference replyHoldersCollection;
    private CollectionReference notificationsCollection;

    private GeneralSearchItem generalSearchItem;

    private String postHolderId;
    private String postId;
    private String commentHolderId;
    private String commentId;
    private boolean willComment;
    private boolean isFromNotifications;

    private Comment comment;

    private ReplyHolder replyHolder;
    private String replyOnId;
    private String replier1Id;
    private String replier2Id;
    private String replierPicture;
    private String replier1Name;
    private String replier2Name;
    private String replyContent;
    private @ServerTimestamp Date dateCreated;
    private String replyPicture;

    private int currentReplyPosition;

    private NotificationHolder notificationHolder;
    private String notificationType;
    private String notificationContent;

    private List<ReplyHolder> replyHolderList = new ArrayList<>();
    private ReplyAdapter replyAdapter;
    private Query replyHoldersQuery;

    private boolean areReplyHoldersFirstTimeLoaded;

    // Views of Comment
    private CircleImageView commentActivityCircleImageViewDisplayCommenterPicture;
    private TextView commentActivityTextViewDisplayCommenterFullName;
    private ImageView commentActivityImageViewEditPost;
    private TextView commentActivityTextViewDateCreated;
    private TextView commentActivityTextViewDisplayCommenterFullType;
    private TextView commentActivityTextViewContent;
    private ImageView commentActivityImageViewCommentPicture;
    private ImageView commentActivityButtonLike;
    private TextView commentActivityTextViewNumOfLikes;
    private TextView commentActivityTextViewNumOfReplies;
    private ImageView commentActivityTextViewReply;

    // Replies RecyclerView
    private MyRecyclerView commentActivityMyRecyclerViewReplies;

    // Views of Replies
    private RelativeLayout commentActivityRelativeLayoutReplyPicture;
    private ImageView commentActivityImageViewReplyPicture;
    private ImageView commentActivityImageViewRemoveReplyPicture;
    private CircleImageView commentActivityCircleImageViewDisplayReplierPicture;
    private EditText commentActivityEditTextReplyContent;
    private ImageView commentActivityButtonAddReplyPicture;
    private ProgressBar commentActivityProgressBar;
    private ImageView commentActivityButtonReply;

    private Uri chosenReplyPictureUri = null;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_activity);

        mAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        db = FirebaseFirestore.getInstance();
        generalSearchListCollection = db.collection("GeneralSearchList");
        commentHoldersCollection = db.collection("CommentHolders");
        commentsCollection = db.collection("Comments");
        commentLikersCollection = db.collection("CommentLikers");
        replyHoldersCollection = db.collection("ReplyHolders");
        notificationsCollection = db.collection("Notifications");

        setUpToolbar();

        postHolderId = getIntent().getStringExtra("postHolderId");
        postId = getIntent().getStringExtra("postId");
        commentHolderId = getIntent().getStringExtra("commentHolderId");
        commentId = getIntent().getStringExtra("commentId");
        willComment = getIntent().getBooleanExtra("willComment", false);
        isFromNotifications = getIntent().getBooleanExtra("isFromNotifications", false);

        commentActivityCircleImageViewDisplayCommenterPicture = findViewById(R.id.commentActivityCircleImageViewDisplayCommenterPicture);
        commentActivityTextViewDisplayCommenterFullName = findViewById(R.id.commentActivityTextViewDisplayCommenterFullName);
        commentActivityImageViewEditPost = findViewById(R.id.commentActivityImageViewEditPost);
        commentActivityTextViewDateCreated = findViewById(R.id.commentActivityTextViewDateCreated);
        commentActivityTextViewDisplayCommenterFullType = findViewById(R.id.commentActivityTextViewDisplayCommenterFullType);
        commentActivityTextViewContent = findViewById(R.id.commentActivityTextViewContent);
        commentActivityImageViewCommentPicture = findViewById(R.id.commentActivityImageViewCommentPicture);
        commentActivityButtonLike = findViewById(R.id.commentActivityButtonLike);
        commentActivityTextViewNumOfLikes = findViewById(R.id.commentActivityTextViewNumOfLikes);
        commentActivityTextViewNumOfReplies = findViewById(R.id.commentActivityTextViewNumOfReplies);
        commentActivityTextViewReply = findViewById(R.id.commentActivityTextViewReply);

        commentActivityMyRecyclerViewReplies = findViewById(R.id.commentActivityMyRecyclerViewReplies);

        commentActivityRelativeLayoutReplyPicture = findViewById(R.id.commentActivityRelativeLayoutReplyPicture);
        commentActivityImageViewReplyPicture = findViewById(R.id.commentActivityImageViewReplyPicture);
        commentActivityImageViewRemoveReplyPicture = findViewById(R.id.commentActivityImageViewRemoveReplyPicture);
        commentActivityCircleImageViewDisplayReplierPicture = findViewById(R.id.commentActivityCircleImageViewDisplayReplierPicture);
        commentActivityEditTextReplyContent = findViewById(R.id.commentActivityEditTextReplyContent);
        commentActivityButtonAddReplyPicture = findViewById(R.id.commentActivityButtonAddReplyPicture);
        commentActivityProgressBar = findViewById(R.id.commentActivityProgressBar);
        commentActivityButtonReply = findViewById(R.id.commentActivityButtonReply);

        generalSearchListCollection
                .whereEqualTo("itemId", mAuth.getCurrentUser().getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                generalSearchItem = queryDocumentSnapshot.toObject(GeneralSearchItem.class);

                                initializeViews();

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(CommentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_toolbar_main, menu);

        return true;

    }


    public void setUpToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.comment));

    }


    public void initializeViews() {

        commentsCollection.document(commentId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        comment = documentSnapshot.toObject(Comment.class);
                        Log.e("hey", "" + commentId);
                        comment.setObjectId(documentSnapshot.getId());

                        Glide.with(CommentActivity.this).load(comment.getCommenterPicture()).placeholder(R.drawable.placeholder).into(commentActivityCircleImageViewDisplayCommenterPicture);
                        commentActivityTextViewDisplayCommenterFullName.setText(comment.getCommenter1Name());
                        commentActivityTextViewDisplayCommenterFullType.setText(comment.getCommenter2Name());
                        setDateCreated(comment.getDateCreated(), commentActivityTextViewDateCreated);

                        // user display picture when replying...
                        Glide.with(CommentActivity.this).load(mAuth.getCurrentUser().getPhotoUrl()).placeholder(R.drawable.placeholder).into(commentActivityCircleImageViewDisplayReplierPicture);

                        // checks if it's the current user's own comment...
                        if (mAuth.getCurrentUser().getUid().equals(comment.getCommenter1Id())) {

                            commentActivityImageViewEditPost.setVisibility(View.VISIBLE);

                        } else {

                            commentActivityImageViewEditPost.setVisibility(View.GONE);

                        }

                        // checks if there's no content...
                        if (comment.getCommentContent().trim().isEmpty()) {

                            commentActivityTextViewContent.setVisibility(View.GONE);

                        } else {

                            commentActivityTextViewContent.setVisibility(View.VISIBLE);
                            commentActivityTextViewContent.setText(comment.getCommentContent());

                        }

                        // checks if there's no picture...
                        if (comment.getCommentPicture().equals("NA")) {

                            commentActivityImageViewCommentPicture.setVisibility(View.GONE);

                        } else {

                            Glide.with(CommentActivity.this).load(comment.getCommentPicture()).placeholder(R.drawable.placeholder).into(commentActivityImageViewCommentPicture);
                            commentActivityImageViewCommentPicture.setVisibility(View.VISIBLE);

                        }

                        // checks if the user came here to reply or not
                        if (willComment) {

                            commentActivityEditTextReplyContent.requestFocus();

                            InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                        }

                        commentLikersCollection
                                .whereEqualTo("likedItemId", comment.getObjectId())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                        // sets the initial number of likes...
                                        final int numOfLikes = queryDocumentSnapshots.size();
                                        commentActivityTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                        // sets the initial state of the like button...
                                        if (!queryDocumentSnapshots.isEmpty()) {

                                            List<String> likers = new ArrayList<>();

                                            for (QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots) {

                                                Liker liker = queryDocumentSnapshot.toObject(Liker.class);
                                                likers.add(liker.getItemId());

                                            }

                                            if (likers.contains(mAuth.getCurrentUser().getUid())) {

                                                turnToLiked(commentActivityButtonLike);

                                            } else {

                                                turnToLike(commentActivityButtonLike);

                                            }

                                        } else {

                                            turnToLike(commentActivityButtonLike);

                                        }

                                        replyHoldersCollection
                                                .whereEqualTo("replyOnId", comment.getObjectId())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                        // sets the initial number of replies...
                                                        int numOfReplies = queryDocumentSnapshots.size();
                                                        commentActivityTextViewNumOfReplies.setText(String.valueOf(numOfReplies));

                                                        setUpAdapter();

                                                        startRetrievingReplyHoldersFromDatabase();

                                                        // when the profile picture of the one who posted is clicked...
                                                        commentActivityCircleImageViewDisplayCommenterPicture.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                goToUserActivity(comment.getCommenter1Id());

                                                            }
                                                        });

                                                        // when the full name of the one who posted is clicked...
                                                        commentActivityTextViewDisplayCommenterFullName.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                goToUserActivity(comment.getCommenter1Id());

                                                            }
                                                        });

                                                        // when the down arrow for editing the comment is clicked...
                                                        commentActivityImageViewEditPost.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                BottomSheetDialogComment BottomSheetDialogComment = new BottomSheetDialogComment();
                                                                BottomSheetDialogComment.show(getSupportFragmentManager(), "My Bottom Sheet");

                                                            }
                                                        });

                                                        // when the full type of the one who posted is clicked...
                                                        commentActivityTextViewDisplayCommenterFullType.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                goToUserActivity(comment.getCommenter2Id());

                                                            }
                                                        });

                                                        // when the post picture is clicked...
                                                        commentActivityImageViewCommentPicture.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                goToFullScreenImage(Uri.parse(comment.getCommentPicture()));

                                                            }
                                                        });

                                                        // when the number of likes is clicked...
                                                        commentActivityTextViewNumOfLikes.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                goToGeneralSearch("commentLikers", comment.getObjectId());

                                                            }
                                                        });

                                                        // when the like button is clicked...
                                                        commentActivityButtonLike.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                // to prevent the user from spamming the like button, the like button will be disabled for 1 second after click...
                                                                commentActivityButtonLike.setEnabled(false);
                                                                Handler handler = new Handler();
                                                                handler.postDelayed(new Runnable() {
                                                                    public void run() {

                                                                        commentActivityButtonLike.setEnabled(true);

                                                                    }
                                                                }, 1000);

                                                                if (commentActivityButtonLike.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_like).getConstantState()) {

                                                                    turnToLiked(commentActivityButtonLike);

                                                                } else {

                                                                    turnToLike(commentActivityButtonLike);

                                                                }

                                                                commentLikersCollection
                                                                        .whereEqualTo("likedItemId", comment.getObjectId())
                                                                        .get()
                                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                                                if (!queryDocumentSnapshots.isEmpty()) {

                                                                                    List<String> likers = new ArrayList<>();

                                                                                    for (QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots) {

                                                                                        Liker liker = queryDocumentSnapshot.toObject(Liker.class);
                                                                                        likers.add(liker.getItemId());

                                                                                    }

                                                                                    if (likers.contains(mAuth.getCurrentUser().getUid())) {

                                                                                        turnToLike(commentActivityButtonLike);

                                                                                        int numOfLikes = Integer.valueOf(commentActivityTextViewNumOfLikes.getText().toString());
                                                                                        numOfLikes--;
                                                                                        commentActivityTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                                        removeUserFromPostLikers(comment, commentActivityButtonLike, commentActivityTextViewNumOfLikes);

                                                                                    } else {

                                                                                        turnToLiked(commentActivityButtonLike);

                                                                                        int numOfLikes = Integer.valueOf(commentActivityTextViewNumOfLikes.getText().toString());
                                                                                        numOfLikes++;
                                                                                        commentActivityTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                                        addUserToCommentLikers(comment, commentActivityButtonLike, commentActivityTextViewNumOfLikes);

                                                                                    }

                                                                                } else {

                                                                                    turnToLiked(commentActivityButtonLike);

                                                                                    int numOfLikes = Integer.valueOf(commentActivityTextViewNumOfLikes.getText().toString());
                                                                                    numOfLikes++;
                                                                                    commentActivityTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                                    addUserToCommentLikers(comment, commentActivityButtonLike, commentActivityTextViewNumOfLikes);

                                                                                }

                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {

                                                                                Toast.makeText(CommentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });

                                                            }
                                                        });

                                                        // when the reply button is clicked...
                                                        commentActivityTextViewReply.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                                                            }
                                                        });

                                                        // when the newly attached picture to comment is clicked...
                                                        commentActivityImageViewReplyPicture.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                setUpReadExternalStoragePermission(REQUEST_CODE_PHONE_GALLERY_COMMENT_PICTURE);

                                                            }
                                                        });

                                                        // when the button for attaching picture to comment is clicked...
                                                        commentActivityButtonAddReplyPicture.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                setUpReadExternalStoragePermission(REQUEST_CODE_PHONE_GALLERY_COMMENT_PICTURE);

                                                            }
                                                        });

                                                        commentActivityImageViewRemoveReplyPicture.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                chosenReplyPictureUri = null;

                                                                commentActivityRelativeLayoutReplyPicture.setVisibility(View.GONE);

                                                            }
                                                        });

                                                        // when the comment button is clicked...
                                                        commentActivityButtonReply.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                addReplyPictureToStorage();

                                                            }
                                                        });

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        Toast.makeText(CommentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(CommentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(CommentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void setDateCreated(Date date, TextView textView) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        textView.setText(simpleDateFormat.format(date));

    }


    public void turnToLiked(ImageView imageView) {

        imageView.setImageResource(R.drawable.ic_liked);

    }


    public void addUserToCommentLikers(final Comment comment, final ImageView imageView, final TextView textView) {

        final Liker liker = new Liker(generalSearchItem.getItemId(), generalSearchItem.getItemPicture(), generalSearchItem.getItemName(), generalSearchItem.getType(), generalSearchItem.getDepartments(), generalSearchItem.getYear(), generalSearchItem.getFullType(), generalSearchItem.getItemType(), comment.getObjectId());

        commentLikersCollection.add(liker).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if (task.isSuccessful()) {

                    commentLikersCollection
                            .whereEqualTo("likedItemId", comment.getObjectId())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    int numOfLikes = queryDocumentSnapshots.size();
                                    textView.setText(String.valueOf(numOfLikes));

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    turnToLike(imageView);

                                    int numOfLikes = Integer.valueOf(textView.getText().toString());
                                    numOfLikes--;
                                    textView.setText(String.valueOf(numOfLikes));

                                    Toast.makeText(CommentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                } else {

                    turnToLike(imageView);

                    int numOfLikes = Integer.valueOf(textView.getText().toString());
                    numOfLikes--;
                    textView.setText(String.valueOf(numOfLikes));

                    Toast.makeText(CommentActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    public void turnToLike(ImageView imageView) {

        imageView.setImageResource(R.drawable.ic_like);

    }


    public void removeUserFromPostLikers(final Comment comment, final ImageView imageView, final TextView textView) {

        commentLikersCollection
                .whereEqualTo("likedItemId", comment.getObjectId())
                .whereEqualTo("itemId", mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            WriteBatch writeBatch = db.batch();

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                writeBatch.delete(commentLikersCollection.document(queryDocumentSnapshot.getId()));

                            }

                            writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        commentLikersCollection
                                                .whereEqualTo("likedItemId", comment.getObjectId())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                        int numOfLikes = queryDocumentSnapshots.size();
                                                        textView.setText(String.valueOf(numOfLikes));

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        turnToLiked(imageView);

                                                        int numOfLikes = Integer.valueOf(textView.getText().toString());
                                                        numOfLikes++;
                                                        textView.setText(String.valueOf(numOfLikes));

                                                        Toast.makeText(CommentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    } else {

                                        turnToLiked(imageView);

                                        int numOfLikes = Integer.valueOf(textView.getText().toString());
                                        numOfLikes++;
                                        textView.setText(String.valueOf(numOfLikes));

                                        Toast.makeText(CommentActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        turnToLiked(imageView);

                        int numOfLikes = Integer.valueOf(textView.getText().toString());
                        numOfLikes++;
                        textView.setText(String.valueOf(numOfLikes));

                        Toast.makeText(CommentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void setUpAdapter() {

        replyHolderList = new ArrayList<>();
        replyAdapter = new ReplyAdapter(replyHolderList, postHolderId, postId, commentHolderId, commentId);

        commentActivityMyRecyclerViewReplies = findViewById(R.id.commentActivityMyRecyclerViewReplies);

        commentActivityMyRecyclerViewReplies.setHasFixedSize(true);
        commentActivityMyRecyclerViewReplies.setLayoutManager(new LinearLayoutManager(this));

        commentActivityMyRecyclerViewReplies.setAdapter(replyAdapter);
        ViewCompat.setNestedScrollingEnabled(commentActivityMyRecyclerViewReplies, false);

    }


    public void startRetrievingReplyHoldersFromDatabase() {

        areReplyHoldersFirstTimeLoaded = true;

        replyHoldersQuery = replyHoldersCollection
                .whereEqualTo("replyOnId", comment.getObjectId())
                .orderBy("dateCreated");

        getReplyHolders();

    }


    public void getReplyHolders() {

        replyHoldersQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {

                    Log.e("My Error: ", e.getMessage());
                    return;

                }

                if (!queryDocumentSnapshots.isEmpty()) {

                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                        switch (documentChange.getType()) {

                            case ADDED:

                                ReplyHolder replyHolder = documentChange.getDocument().toObject(ReplyHolder.class);
                                replyHolder.setObjectId(documentChange.getDocument().getId());

                                if (areReplyHoldersFirstTimeLoaded) {

                                    replyHolderList.add(replyHolder);
                                    replyAdapter.notifyDataSetChanged();

                                } else {

                                    replyHolderList.add(replyHolder);
                                    replyAdapter.notifyItemInserted(replyHolderList.size() - 1);

                                }

                                break;

                        }

                    }
                    areReplyHoldersFirstTimeLoaded = false;

                }

            }
        });

    }


    public void startDeleteComment(final String commentId, final String typeOfEdit) {

        final ProgressDialog progressDialog;

        switch (typeOfEdit) {

            case "deleteComment":
                progressDialog = ProgressDialog.show(CommentActivity.this, getResources().getString(R.string.pleaseWait), getResources().getString(R.string.deletingComment), true);
                break;

            case "deleteReply":
                progressDialog = ProgressDialog.show(CommentActivity.this, getResources().getString(R.string.pleaseWait), getResources().getString(R.string.deletingReply), true);
                break;

            default:
                progressDialog = ProgressDialog.show(CommentActivity.this, getResources().getString(R.string.pleaseWait), getResources().getString(R.string.deletingComment), true);

        }

        WriteBatch writeBatch = db.batch();

        switch (typeOfEdit) {

            case "deleteComment":
                writeBatch.delete(commentHoldersCollection.document(commentHolderId));
                writeBatch.delete(commentsCollection.document(commentId));
                break;

            case "deleteReply":
                writeBatch.delete(replyHoldersCollection.document(commentId));
                break;

        }

        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    progressDialog.dismiss();

                    switch (typeOfEdit) {

                        case "deleteComment":
                            goToStudentForum(postHolderId, postId, false, false);
                            break;

                        case "deleteReply":
                            replyHolderList.remove(currentReplyPosition);
                            int numOfReplies = replyHolderList.size();
                            commentActivityTextViewNumOfReplies.setText(String.valueOf(numOfReplies));
                            replyAdapter.notifyItemRemoved(currentReplyPosition);
                            break;

                    }

                } else {

                    progressDialog.dismiss();
                    Toast.makeText(CommentActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    public void setUpReadExternalStoragePermission(int myRequestCode) {

        if (Build.VERSION.SDK_INT >= 22) {

            if (ContextCompat.checkSelfPermission(CommentActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(CommentActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Toast.makeText(CommentActivity.this, "Permission Required", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(CommentActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                } else {

                    ActivityCompat.requestPermissions(CommentActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                }

            } else {

                openGallery(myRequestCode);

            }

        } else {

            openGallery(myRequestCode);

        }

    }


    public void openGallery(int myRequestCode) {

        Intent intentPhoneGallery = new Intent(Intent.ACTION_GET_CONTENT);
        intentPhoneGallery.setType("image/*");
        startActivityForResult(intentPhoneGallery, myRequestCode);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PHONE_GALLERY_COMMENT_PICTURE && resultCode == RESULT_OK && data != null) {

            chosenReplyPictureUri = data.getData();
            commentActivityImageViewReplyPicture.setImageURI(chosenReplyPictureUri);

            commentActivityRelativeLayoutReplyPicture.setVisibility(View.VISIBLE);

            commentActivityEditTextReplyContent.setError(null);

        }

    }


    public void addReplyPictureToStorage() {

        replyContent = commentActivityEditTextReplyContent.getText().toString().trim();

        if (!replyContent.isEmpty() || chosenReplyPictureUri != null) {

            commentActivityButtonReply.setVisibility(View.GONE);
            commentActivityProgressBar.setVisibility(View.VISIBLE);

            if (chosenReplyPictureUri != null) {

                replyPicturePath = mStorageRef.child("replyPicture").child(System.currentTimeMillis() + ".jpg");
                replyPicturePath.putFile(chosenReplyPictureUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            replyPicturePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    if (task.isSuccessful()) {

                                        replyPicture = task.getResult().toString();
                                        addReplyToDatabase();

                                    } else {

                                        commentActivityProgressBar.setVisibility(View.GONE);
                                        commentActivityButtonReply.setVisibility(View.VISIBLE);
                                        Toast.makeText(CommentActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                        } else {

                            commentActivityProgressBar.setVisibility(View.GONE);
                            commentActivityButtonReply.setVisibility(View.VISIBLE);
                            Toast.makeText(CommentActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            } else {

                replyPicture = "NA";
                addReplyToDatabase();

            }

        } else {

            commentActivityEditTextReplyContent.setError(getResources().getString(R.string.required));

        }

    }


    public void addReplyToDatabase() {

        replyOnId = comment.getObjectId();
        replier1Id = mAuth.getCurrentUser().getUid();
        replier2Id = mAuth.getCurrentUser().getUid();
        replierPicture = mAuth.getCurrentUser().getPhotoUrl().toString();
        replier1Name = mAuth.getCurrentUser().getDisplayName();
        replier2Name = generalSearchItem.getFullType();

        String replyUid = String.valueOf(System.currentTimeMillis());

        replyHolder = new ReplyHolder(replyOnId, replier1Id, replier2Id, replierPicture, replier1Name, replier2Name, replyContent, dateCreated, replyPicture);

        notificationType = "Reply Notification";
        notificationContent = "replied to your comment";

        notificationHolder = new NotificationHolder(mAuth.getCurrentUser().getUid(), comment.getCommenter1Id(), mAuth.getCurrentUser().getPhotoUrl().toString(), mAuth.getCurrentUser().getDisplayName(), comment.getCommenterPicture(), comment.getCommenter1Name(), notificationType, notificationContent, replyContent, dateCreated, postHolderId, postId, commentHolderId, comment.getObjectId(), replyUid);

        WriteBatch writeBatch = db.batch();

        writeBatch.set(replyHoldersCollection.document(replyUid), replyHolder);

        if (!mAuth.getCurrentUser().getUid().equals(comment.getCommenter1Id())) {

            writeBatch.set(notificationsCollection.document(), notificationHolder);

        }

        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    int numOfReplies = replyHolderList.size();
                    commentActivityTextViewNumOfReplies.setText(String.valueOf(numOfReplies));

                    commentActivityProgressBar.setVisibility(View.GONE);
                    commentActivityButtonReply.setVisibility(View.VISIBLE);

                    commentActivityRelativeLayoutReplyPicture.setVisibility(View.GONE);
                    chosenReplyPictureUri = null;
                    replyPicture = "NA";
                    commentActivityEditTextReplyContent.setText("");

                    replyAdapter.notifyDataSetChanged();

                    InputMethodManager inputMethodManager = (InputMethodManager) CommentActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(CommentActivity.this.getCurrentFocus().getWindowToken(), 0);

                } else {

                    commentActivityProgressBar.setVisibility(View.GONE);
                    commentActivityButtonReply.setVisibility(View.VISIBLE);
                    Toast.makeText(CommentActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    @Override
    public void onBackPressed() {

        if (isFromNotifications) {

            goToMain();

        } else {

            super.onBackPressed();

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

    @Override
    public void onButtonClicked(String text) {

        switch (text) {

            case "Edit Comment":
                goToEditComment("editComment");
                break;

            case "Delete Comment":

                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.deleteCommentQuestionMark))
                        .setMessage(getResources().getString(R.string.youCanEdit))
                        .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                startDeleteComment(comment.getObjectId(), "deleteComment");

                            }

                        })
                        .setNeutralButton(getResources().getString(R.string.edit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                goToEditComment("editComment");

                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), null)
                        .show();

                break;

        }


    }


    @Override
    public void onButtonClicked(String text, int position, final ReplyHolder replyHolder) {

        currentReplyPosition = position;

        switch (text) {

            case "Edit Reply":
                goToEditComment("editReply", replyHolder);
                break;

            case "Delete Reply":

                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.deleteReplyQuestionMark))
                        .setMessage(getResources().getString(R.string.youCanEdit))
                        .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                startDeleteComment(replyHolder.getObjectId(), "deleteReply");

                            }

                        })
                        .setNeutralButton(getResources().getString(R.string.edit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                goToEditComment("editReply", replyHolder);

                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), null)
                        .show();

                break;

        }

    }


    public void goToFullScreenImage(Uri uri) {

        Intent intentFullScreenImage = new Intent(CommentActivity.this, FullScreenImage.class);
        intentFullScreenImage.setData(uri);
        startActivity(intentFullScreenImage);

    }


    public void goToGeneralSearch(String type, String postId) {

        Intent intentGeneralSearch = new Intent(CommentActivity.this, GeneralSearch.class);
        intentGeneralSearch.putExtra("type", type);
        intentGeneralSearch.putExtra("commentId", postId);
        startActivity(intentGeneralSearch);

    }


    public void goToStudentForum(String postHolderId, String postId, boolean willComment, boolean isFromNotifications) {

        Intent intentStudentForum = new Intent(CommentActivity.this, Updates.class);

        intentStudentForum.putExtra("postHolderId", postHolderId);
        intentStudentForum.putExtra("postId", postId);
        intentStudentForum.putExtra("willComment", willComment);
        intentStudentForum.putExtra("isFromNotifications", isFromNotifications);

        intentStudentForum.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentStudentForum);
        finish();

    }


    public void goToUserActivity(String userId) {

        Intent intentUserActivity = new Intent(CommentActivity.this, UserActivity.class);
        intentUserActivity.putExtra("userId", userId);
        startActivity(intentUserActivity);

    }


    public void goToEditComment(String typeOfEdit) {

        Intent intentEditComment = new Intent(CommentActivity.this, EditComment.class);

        intentEditComment.putExtra("typeOfEdit", typeOfEdit);
        intentEditComment.putExtra("postHolderId", postHolderId);
        intentEditComment.putExtra("postId", postId);
        intentEditComment.putExtra("commentHolderId", commentHolderId);
        intentEditComment.putExtra("commentId", commentId);
        intentEditComment.putExtra("commentHolder2Id", commentHolderId);
        intentEditComment.putExtra("comment2Id", commentId);
        intentEditComment.putExtra("displayCommenterPicture", comment.getCommenterPicture());
        intentEditComment.putExtra("displayCommenterFullName", comment.getCommenter1Name());
        intentEditComment.putExtra("displayCommenterFullType", comment.getCommenter2Name());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        intentEditComment.putExtra("dateCreated", simpleDateFormat.format(comment.getDateCreated()));

        intentEditComment.putExtra("commentContent", comment.getCommentContent());
        intentEditComment.putExtra("commentPicture", comment.getCommentPicture());

        startActivity(intentEditComment);

    }


    public void goToEditComment(String typeOfEdit, ReplyHolder replyHolder) {

        Intent intentEditComment = new Intent(CommentActivity.this, EditComment.class);

        intentEditComment.putExtra("typeOfEdit", typeOfEdit);
        intentEditComment.putExtra("postHolderId", postHolderId);
        intentEditComment.putExtra("postId", postId);
        intentEditComment.putExtra("commentHolderId", commentHolderId);
        intentEditComment.putExtra("commentId", commentId);
        intentEditComment.putExtra("commentHolder2Id", replyHolder.getObjectId());
        intentEditComment.putExtra("comment2Id", replyHolder.getObjectId());
        intentEditComment.putExtra("displayCommenterPicture", replyHolder.getReplierPicture());
        intentEditComment.putExtra("displayCommenterFullName", replyHolder.getReplier1Name());
        intentEditComment.putExtra("displayCommenterFullType", replyHolder.getReplier2Name());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        intentEditComment.putExtra("dateCreated", simpleDateFormat.format(replyHolder.getDateCreated()));

        intentEditComment.putExtra("commentContent", replyHolder.getReplyContent());
        intentEditComment.putExtra("commentPicture", replyHolder.getReplyPicture());

        startActivity(intentEditComment);

    }


    public void goToCommentActivity(String commentHolderId, String commentId, boolean willComment, boolean isFromNotifications) {

        Intent intentCommentActivity = new Intent(CommentActivity.this, CommentActivity.class);

        intentCommentActivity.putExtra("postHolderId", postHolderId);
        intentCommentActivity.putExtra("postId", postId);
        intentCommentActivity.putExtra("commentHolderId", commentHolderId);
        intentCommentActivity.putExtra("commentId", commentId);
        intentCommentActivity.putExtra("willComment", willComment);
        intentCommentActivity.putExtra("isFromNotifications", isFromNotifications);

        startActivity(intentCommentActivity);

    }


    public void goToMain() {

        Intent intentMain = new Intent(CommentActivity.this, MainActivity.class);
        startActivity(intentMain);

    }


    @Override
    public void onResume() {
        super.onResume();

        if (replyAdapter != null) {

            replyAdapter.notifyDataSetChanged();

        }

    }


    @Override
    protected void onPause() {
        super.onPause();

        // close soft keyboard when leaving activity...
        InputMethodManager inputMethodManager = (InputMethodManager) CommentActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(CommentActivity.this.getCurrentFocus().getWindowToken(), 0);

    }


}
