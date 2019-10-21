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
import com.jeofferson.onclas.PackageAdapters.CommentAdapter;
import com.jeofferson.onclas.PackageForms.EditPost;
import com.jeofferson.onclas.PackageObjectModel.Comment;
import com.jeofferson.onclas.PackageObjectModel.CommentHolder;
import com.jeofferson.onclas.PackageObjectModel.GeneralSearchItem;
import com.jeofferson.onclas.PackageObjectModel.Liker;
import com.jeofferson.onclas.PackageObjectModel.NotificationHolder;
import com.jeofferson.onclas.PackageObjectModel.Post;
import com.jeofferson.onclas.PackageOthers.BottomSheetDialogUpdate;
import com.jeofferson.onclas.PackageOthers.MyRecyclerView;
import com.jeofferson.onclas.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Updates extends AppCompatActivity implements BottomSheetDialogUpdate.BottomSheetDialogListener {


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 8;
    static final int REQUEST_CODE_PHONE_GALLERY_COMMENT_PICTURE = 9;

    private FirebaseAuth mAuth;

    private StorageReference mStorageRef;
    private StorageReference commentPicturePath;

    private FirebaseFirestore db;
    private CollectionReference generalSearchListCollection;
    private CollectionReference newsfeedCollection;
    private CollectionReference postsCollection;
    private CollectionReference postLikersCollection;
    private CollectionReference commentHoldersCollection;
    private CollectionReference commentsCollection;
    private CollectionReference notificationsCollection;

    private GeneralSearchItem generalSearchItem;

    private String postHolderId;
    private String postId;
    private boolean willComment;
    private boolean isFromNotifications;

    private Post post;

    private CommentHolder commentHolder;
    private Comment comment;
    private String commentOnId;
    private String commenter1Id;
    private String commenter2Id;
    private String commenterPicture;
    private String commenter1Name;
    private String commenter2Name;
    private String commentContent;
    private @ServerTimestamp Date dateCreated;
    private String commentPicture;

    private NotificationHolder notificationHolder;
    private String notificationType;
    private String notificationContent;

    private List<CommentHolder> commentHolderList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private Query commentHoldersQuery;

    private boolean areCommentHoldersFirstTimeLoaded;

    // Views of Post
    private CircleImageView studentForumCircleImageViewDisplayPosterPicture;
    private TextView studentForumTextViewDisplayPosterFullName;
    private ImageView studentForumImageViewEditPost;
    private TextView studentForumTextViewDisplayPosterFullType;
    private TextView studentForumTextViewDateCreated;
    private TextView studentForumTextViewContent;
    private ImageView studentForumImageViewPostPicture;
    private TextView studentForumTextViewNumOfLikes;
    private TextView studentForumTextViewNumOfComments;
    private ImageView studentForumButtonLike;

    // Comments RecyclerView
    private MyRecyclerView studentForumMyRecyclerViewComments;

    // Views of Comments
    private RelativeLayout studentForumRelativeLayoutCommentPicture;
    private ImageView studentForumImageViewCommentPicture;
    private ImageView studentForumImageViewRemoveCommentPicture;
    private CircleImageView studentForumCircleImageViewDisplayCommenterPicture;
    private EditText studentForumEditTextCommentContent;
    private ImageView studentForumButtonAddCommentPicture;
    private ProgressBar studentForumProgressBar;
    private ImageView studentForumButtonComment;

    private Uri chosenCommentPictureUri = null;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updates);

        mAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        db = FirebaseFirestore.getInstance();
        generalSearchListCollection = db.collection("GeneralSearchList");
        newsfeedCollection = db.collection("Newsfeed");
        postsCollection = db.collection("Posts");
        postLikersCollection = db.collection("PostLikers");
        commentHoldersCollection = db.collection("CommentHolders");
        commentsCollection = db.collection("Comments");
        notificationsCollection = db.collection("Notifications");

        setUpToolbar();

        postHolderId = getIntent().getStringExtra("postHolderId");
        postId = getIntent().getStringExtra("postId");
        willComment = getIntent().getBooleanExtra("willComment", false);
        isFromNotifications = getIntent().getBooleanExtra("isFromNotifications", false);

        studentForumCircleImageViewDisplayPosterPicture = findViewById(R.id.studentForumCircleImageViewDisplayPosterPicture);
        studentForumTextViewDisplayPosterFullName = findViewById(R.id.studentForumTextViewDisplayPosterFullName);
        studentForumImageViewEditPost = findViewById(R.id.studentForumImageViewEditPost);
        studentForumTextViewDisplayPosterFullType = findViewById(R.id.studentForumTextViewDisplayPosterFullType);
        studentForumTextViewDateCreated = findViewById(R.id.studentForumTextViewDateCreated);
        studentForumTextViewContent = findViewById(R.id.studentForumTextViewContent);
        studentForumImageViewPostPicture = findViewById(R.id.studentForumImageViewPostPicture);
        studentForumTextViewNumOfLikes = findViewById(R.id.studentForumTextViewNumOfLikes);
        studentForumTextViewNumOfComments = findViewById(R.id.studentForumTextViewNumOfComments);
        studentForumButtonLike = findViewById(R.id.studentForumButtonLike);

        studentForumMyRecyclerViewComments = findViewById(R.id.studentForumMyRecyclerViewComments);

        studentForumRelativeLayoutCommentPicture = findViewById(R.id.studentForumRelativeLayoutCommentPicture);
        studentForumImageViewCommentPicture = findViewById(R.id.studentForumImageViewCommentPicture);
        studentForumImageViewRemoveCommentPicture = findViewById(R.id.studentForumImageViewRemoveCommentPicture);
        studentForumCircleImageViewDisplayCommenterPicture = findViewById(R.id.studentForumCircleImageViewDisplayCommenterPicture);
        studentForumEditTextCommentContent = findViewById(R.id.studentForumEditTextCommentContent);
        studentForumButtonAddCommentPicture = findViewById(R.id.studentForumButtonAddCommentPicture);
        studentForumProgressBar = findViewById(R.id.studentForumProgressBar);
        studentForumButtonComment = findViewById(R.id.studentForumButtonComment);

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

                        Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

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
        getSupportActionBar().setTitle(getResources().getString(R.string.post));

    }


    public void initializeViews() {

        postsCollection.document(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        post = documentSnapshot.toObject(Post.class);
                        post.setObjectId(documentSnapshot.getId());

                        Glide.with(Updates.this).load(post.getPosterPicture()).placeholder(R.drawable.placeholder).into(studentForumCircleImageViewDisplayPosterPicture);
                        studentForumTextViewDisplayPosterFullName.setText(post.getPoster1Name());
                        studentForumTextViewDisplayPosterFullType.setText(post.getPoster2Name());
                        setDateCreated(post.getDateCreated(), studentForumTextViewDateCreated);

                        // user display picture when commenting...
                        Glide.with(Updates.this).load(mAuth.getCurrentUser().getPhotoUrl()).placeholder(R.drawable.placeholder).into(studentForumCircleImageViewDisplayCommenterPicture);

                        // checks if it's the current user's own post...
                        if (mAuth.getCurrentUser().getUid().equals(post.getPoster1Id())) {

                            studentForumImageViewEditPost.setVisibility(View.VISIBLE);

                        } else {

                            studentForumImageViewEditPost.setVisibility(View.GONE);

                        }

                        // checks if there's no content...
                        if (post.getPostContent().trim().isEmpty()) {

                            studentForumTextViewContent.setVisibility(View.GONE);

                        } else {

                            studentForumTextViewContent.setVisibility(View.VISIBLE);
                            studentForumTextViewContent.setText(post.getPostContent());

                        }

                        // checks if there's no picture...
                        if (post.getPostPicture().equals("NA")) {

                            studentForumImageViewPostPicture.setVisibility(View.GONE);

                        } else {

                            Glide.with(Updates.this).load(post.getPostPicture()).placeholder(R.drawable.placeholder).into(studentForumImageViewPostPicture);
                            studentForumImageViewPostPicture.setVisibility(View.VISIBLE);

                        }

                        // checks if the user came here to comment or not
                        if (willComment) {

                            studentForumEditTextCommentContent.requestFocus();

                            InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                        }

                        postLikersCollection
                                .whereEqualTo("likedItemId", post.getObjectId())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                        // sets the initial number of likes...
                                        int numOfLikes = queryDocumentSnapshots.size();
                                        studentForumTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                        // sets the initial state of the like button...
                                        if (!queryDocumentSnapshots.isEmpty()) {

                                            List<String> likers = new ArrayList<>();

                                            for (QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots) {

                                                Liker liker = queryDocumentSnapshot.toObject(Liker.class);
                                                likers.add(liker.getItemId());

                                            }

                                            if (likers.contains(mAuth.getCurrentUser().getUid())) {

                                                turnToLiked(studentForumButtonLike);

                                            } else {

                                                turnToLike(studentForumButtonLike);

                                            }

                                        } else {

                                            turnToLike(studentForumButtonLike);

                                        }

                                        commentHoldersCollection
                                                .whereEqualTo("commentOnId", post.getObjectId())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                        // sets the initial number of comments...
                                                        int numOfComments = queryDocumentSnapshots.size();
                                                        studentForumTextViewNumOfComments.setText(numOfComments > 1 ? String.format(getResources().getString(R.string.numOfComments), numOfComments) : String.format(getResources().getString(R.string.numOfComment), numOfComments));

                                                        setUpAdapter();

                                                        startRetrievingCommentHoldersFromDatabase();

                                                        // when the profile picture of the one who posted is clicked...
                                                        studentForumCircleImageViewDisplayPosterPicture.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                goToUserActivity(post.getPoster1Id());

                                                            }
                                                        });

                                                        // when the full name of the one who posted is clicked...
                                                        studentForumTextViewDisplayPosterFullName.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                goToUserActivity(post.getPoster1Id());

                                                            }
                                                        });

                                                        // when the down arrow for editing the post is clicked...
                                                        studentForumImageViewEditPost.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                BottomSheetDialogUpdate bottomSheetDialogUpdate = new BottomSheetDialogUpdate();
                                                                bottomSheetDialogUpdate.show(getSupportFragmentManager(), "My Bottom Sheet");

                                                            }
                                                        });

                                                        // when the full type of the one who posted is clicked...
                                                        studentForumTextViewDisplayPosterFullType.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                goToUserActivity(post.getPoster2Id());

                                                            }
                                                        });

                                                        // when the post picture is clicked...
                                                        studentForumImageViewPostPicture.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                goToFullScreenImage(Uri.parse(post.getPostPicture()));

                                                            }
                                                        });

                                                        // when the number of likes is clicked...
                                                        studentForumTextViewNumOfLikes.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                goToGeneralSearch("postLikers", post.getObjectId());

                                                            }
                                                        });

                                                        // when the like button is clicked...
                                                        studentForumButtonLike.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                // to prevent the user from spamming the like button, the like button will be disabled for 1 second after click...
                                                                studentForumButtonLike.setEnabled(false);
                                                                Handler handler = new Handler();
                                                                handler.postDelayed(new Runnable() {
                                                                    public void run() {

                                                                        studentForumButtonLike.setEnabled(true);

                                                                    }
                                                                }, 1000);

                                                                if (studentForumButtonLike.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_like).getConstantState()) {

                                                                    turnToLiked(studentForumButtonLike);

                                                                } else {

                                                                    turnToLike(studentForumButtonLike);

                                                                }

                                                                postLikersCollection
                                                                        .whereEqualTo("likedItemId", post.getObjectId())
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

                                                                                        turnToLike(studentForumButtonLike);

                                                                                        int numOfLikes = Integer.valueOf(studentForumTextViewNumOfLikes.getText().toString());
                                                                                        numOfLikes--;
                                                                                        studentForumTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                                        removeUserFromPostLikers(post, studentForumButtonLike, studentForumTextViewNumOfLikes);

                                                                                    } else {

                                                                                        turnToLiked(studentForumButtonLike);

                                                                                        int numOfLikes = Integer.valueOf(studentForumTextViewNumOfLikes.getText().toString());
                                                                                        numOfLikes++;
                                                                                        studentForumTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                                        addUserToPostLikers(post, studentForumButtonLike, studentForumTextViewNumOfLikes);

                                                                                    }

                                                                                } else {

                                                                                    turnToLiked(studentForumButtonLike);

                                                                                    int numOfLikes = Integer.valueOf(studentForumTextViewNumOfLikes.getText().toString());
                                                                                    numOfLikes++;
                                                                                    studentForumTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                                    addUserToPostLikers(post, studentForumButtonLike, studentForumTextViewNumOfLikes);

                                                                                }

                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {

                                                                                Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });

                                                            }
                                                        });

                                                        // when the newly attached picture to comment is clicked...
                                                        studentForumImageViewCommentPicture.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                setUpReadExternalStoragePermission(REQUEST_CODE_PHONE_GALLERY_COMMENT_PICTURE);

                                                            }
                                                        });

                                                        // when the button for attaching picture to comment is clicked...
                                                        studentForumButtonAddCommentPicture.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                setUpReadExternalStoragePermission(REQUEST_CODE_PHONE_GALLERY_COMMENT_PICTURE);

                                                            }
                                                        });

                                                        studentForumImageViewRemoveCommentPicture.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                chosenCommentPictureUri = null;

                                                                studentForumRelativeLayoutCommentPicture.setVisibility(View.GONE);

                                                            }
                                                        });

                                                        // when the comment button is clicked...
                                                        studentForumButtonComment.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                addCommentPictureToStorage();

                                                            }
                                                        });

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

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


    public void addUserToPostLikers(final Post post, final ImageView imageView, final TextView textView) {

        final Liker liker = new Liker(generalSearchItem.getItemId(), generalSearchItem.getItemPicture(), generalSearchItem.getItemName(), generalSearchItem.getType(), generalSearchItem.getDepartments(), generalSearchItem.getYear(), generalSearchItem.getFullType(), generalSearchItem.getItemType(), post.getObjectId());

        postLikersCollection.add(liker).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if (task.isSuccessful()) {

                    postLikersCollection
                            .whereEqualTo("likedItemId", post.getObjectId())
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

                                    Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                } else {

                    turnToLike(imageView);

                    int numOfLikes = Integer.valueOf(textView.getText().toString());
                    numOfLikes--;
                    textView.setText(String.valueOf(numOfLikes));

                    Toast.makeText(Updates.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    public void turnToLike(ImageView imageView) {

        imageView.setImageResource(R.drawable.ic_like);

    }


    public void removeUserFromPostLikers(final Post post, final ImageView imageView, final TextView textView) {

        postLikersCollection
                .whereEqualTo("likedItemId", post.getObjectId())
                .whereEqualTo("itemId", mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            WriteBatch writeBatch = db.batch();

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                writeBatch.delete(postLikersCollection.document(queryDocumentSnapshot.getId()));

                            }

                            writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        postLikersCollection
                                                .whereEqualTo("likedItemId", post.getObjectId())
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

                                                        Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    } else {

                                        turnToLiked(imageView);

                                        int numOfLikes = Integer.valueOf(textView.getText().toString());
                                        numOfLikes++;
                                        textView.setText(String.valueOf(numOfLikes));

                                        Toast.makeText(Updates.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

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

                        Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void setUpAdapter() {

        commentHolderList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentHolderList, postHolderId, postId);

        studentForumMyRecyclerViewComments = findViewById(R.id.studentForumMyRecyclerViewComments);

        studentForumMyRecyclerViewComments.setHasFixedSize(true);
        studentForumMyRecyclerViewComments.setLayoutManager(new LinearLayoutManager(this));

        studentForumMyRecyclerViewComments.setAdapter(commentAdapter);
        ViewCompat.setNestedScrollingEnabled(studentForumMyRecyclerViewComments, false);

    }


    public void startRetrievingCommentHoldersFromDatabase() {

        areCommentHoldersFirstTimeLoaded = true;

        commentHoldersQuery = commentHoldersCollection
                .whereEqualTo("commentOnId", post.getObjectId())
                .orderBy("dateCreated", Query.Direction.DESCENDING);

        getCommentHolders();

    }


    public void getCommentHolders() {

        commentHoldersQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
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

                                CommentHolder commentHolder = documentChange.getDocument().toObject(CommentHolder.class);
                                commentHolder.setObjectId(documentChange.getDocument().getId());

                                if (areCommentHoldersFirstTimeLoaded) {

                                    commentHolderList.add(commentHolder);
                                    commentAdapter.notifyDataSetChanged();

                                } else {

                                    commentHolderList.add(0, commentHolder);
                                    commentAdapter.notifyItemInserted(0);

                                }

                                break;

                        }

                    }
                    areCommentHoldersFirstTimeLoaded = false;

                }

            }
        });

    }


    public void startDeletePost(String postId) {

        final ProgressDialog progressDialog = ProgressDialog.show(Updates.this, getResources().getString(R.string.pleaseWait), getResources().getString(R.string.deletingPost), true);

        WriteBatch writeBatch = db.batch();

        writeBatch.delete(newsfeedCollection.document(postHolderId));
        writeBatch.delete(postsCollection.document(postId));

        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    progressDialog.dismiss();
                    goToMain();

                } else {

                    progressDialog.dismiss();
                    Toast.makeText(Updates.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    public void setUpReadExternalStoragePermission(int myRequestCode) {

        if (Build.VERSION.SDK_INT >= 22) {

            if (ContextCompat.checkSelfPermission(Updates.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(Updates.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Toast.makeText(Updates.this, "Permission Required", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(Updates.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                } else {

                    ActivityCompat.requestPermissions(Updates.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

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

            chosenCommentPictureUri = data.getData();
            studentForumImageViewCommentPicture.setImageURI(chosenCommentPictureUri);

            studentForumRelativeLayoutCommentPicture.setVisibility(View.VISIBLE);

            studentForumEditTextCommentContent.setError(null);

        }

    }


    public void addCommentPictureToStorage() {

        commentContent = studentForumEditTextCommentContent.getText().toString().trim();

        if (!commentContent.isEmpty() || chosenCommentPictureUri != null) {

            studentForumButtonComment.setVisibility(View.GONE);
            studentForumProgressBar.setVisibility(View.VISIBLE);

            if (chosenCommentPictureUri != null) {

                commentPicturePath = mStorageRef.child("commentPicture").child(System.currentTimeMillis() + ".jpg");
                commentPicturePath.putFile(chosenCommentPictureUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            commentPicturePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    if (task.isSuccessful()) {

                                        commentPicture = task.getResult().toString();
                                        addCommentToDatabase();

                                    } else {

                                        studentForumProgressBar.setVisibility(View.GONE);
                                        studentForumButtonComment.setVisibility(View.VISIBLE);
                                        Toast.makeText(Updates.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                        } else {

                            studentForumProgressBar.setVisibility(View.GONE);
                            studentForumButtonComment.setVisibility(View.VISIBLE);
                            Toast.makeText(Updates.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            } else {

                commentPicture = "NA";
                addCommentToDatabase();

            }

        } else {

            studentForumEditTextCommentContent.setError(getResources().getString(R.string.required));

        }

    }


    public void addCommentToDatabase() {

        commentOnId = post.getObjectId();
        commenter1Id = mAuth.getCurrentUser().getUid();
        commenter2Id = mAuth.getCurrentUser().getUid();
        commenterPicture = mAuth.getCurrentUser().getPhotoUrl().toString();
        commenter1Name = mAuth.getCurrentUser().getDisplayName();
        commenter2Name = generalSearchItem.getFullType();

        String commentUid = String.valueOf(System.currentTimeMillis());
        String commentHolderUid = commentUid + "holder";

        comment = new Comment(commentOnId, commenter1Id, commenter2Id, commenterPicture, commenter1Name, commenter2Name, commentContent, dateCreated, commentPicture);
        commentHolder = new CommentHolder(commentOnId, commentUid, commenter1Id, commenter2Id, commenterPicture, commenter1Name, commenter2Name, commentContent, dateCreated, commentPicture);

        notificationType = "Comment Notification";
        notificationContent = "commented on your post";

        notificationHolder = new NotificationHolder(mAuth.getCurrentUser().getUid(), post.getPoster1Id(), mAuth.getCurrentUser().getPhotoUrl().toString(), mAuth.getCurrentUser().getDisplayName(), post.getPosterPicture(), post.getPoster1Name(), notificationType, notificationContent, commentContent, dateCreated, postHolderId, post.getObjectId(), commentHolderUid, commentUid, "");

        WriteBatch writeBatch = db.batch();

        writeBatch.set(commentsCollection.document(commentUid), comment);
        writeBatch.set(commentHoldersCollection.document(commentHolderUid), commentHolder);

        if (!mAuth.getCurrentUser().getUid().equals(post.getPoster1Id())) {

            writeBatch.set(notificationsCollection.document(), notificationHolder);

        }

        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    int numOfComments = commentHolderList.size();
                    studentForumTextViewNumOfComments.setText(numOfComments > 1 ? String.format(getResources().getString(R.string.numOfComments), numOfComments) : String.format(getResources().getString(R.string.numOfComment), numOfComments));

                    studentForumProgressBar.setVisibility(View.GONE);
                    studentForumButtonComment.setVisibility(View.VISIBLE);

                    studentForumRelativeLayoutCommentPicture.setVisibility(View.GONE);
                    chosenCommentPictureUri = null;
                    commentPicture = "NA";
                    studentForumEditTextCommentContent.setText("");

                    commentAdapter.notifyDataSetChanged();

                    InputMethodManager inputMethodManager = (InputMethodManager) Updates.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(Updates.this.getCurrentFocus().getWindowToken(), 0);

                } else {

                    studentForumProgressBar.setVisibility(View.GONE);
                    studentForumButtonComment.setVisibility(View.VISIBLE);
                    Toast.makeText(Updates.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

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

            case "Edit Post":
                goToEditPost();
                break;

            case "Delete Post":

                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.deletePostQuestionMark))
                        .setMessage(getResources().getString(R.string.youCanEdit))
                        .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                startDeletePost(post.getObjectId());

                            }

                        })
                        .setNeutralButton(getResources().getString(R.string.edit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                goToEditPost();

                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), null)
                        .show();

                break;

        }

    }


    public void goToFullScreenImage(Uri uri) {

        Intent intentFullScreenImage = new Intent(Updates.this, FullScreenImage.class);
        intentFullScreenImage.setData(uri);
        startActivity(intentFullScreenImage);

    }


    public void goToGeneralSearch(String type, String postId) {

        Intent intentGeneralSearch = new Intent(Updates.this, GeneralSearch.class);
        intentGeneralSearch.putExtra("type", type);
        intentGeneralSearch.putExtra("postId", postId);
        startActivity(intentGeneralSearch);

    }


    public void goToUserActivity(String userId) {

        Intent intentUserActivity = new Intent(Updates.this, UserActivity.class);
        intentUserActivity.putExtra("userId", userId);
        startActivity(intentUserActivity);

    }


    public void goToEditPost() {

        Intent intentEditPost = new Intent(Updates.this, EditPost.class);

        intentEditPost.putExtra("postHolderId", postHolderId);
        intentEditPost.putExtra("postId", postId);
        intentEditPost.putExtra("displayPosterPicture", post.getPosterPicture());
        intentEditPost.putExtra("displayPosterFullName", post.getPoster1Name());
        intentEditPost.putExtra("displayPosterFullType", post.getPoster2Name());
        intentEditPost.putExtra("postContent", post.getPostContent());
        intentEditPost.putExtra("postPicture", post.getPostPicture());

        startActivity(intentEditPost);

    }


    public void goToMain() {

        Intent intentMain = new Intent(Updates.this, MainActivity.class);
        startActivity(intentMain);
        finish();

    }


    @Override
    public void onResume() {
        super.onResume();

        if (commentAdapter != null) {

            commentAdapter.notifyDataSetChanged();

        }

    }


    @Override
    protected void onPause() {
        super.onPause();

        // close soft keyboard when leaving activity...
        InputMethodManager inputMethodManager = (InputMethodManager) Updates.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(Updates.this.getCurrentFocus().getWindowToken(), 0);

    }


}
