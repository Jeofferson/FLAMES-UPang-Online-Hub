package com.jeofferson.onclas.PackageAdapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.jeofferson.onclas.PackageActivities.CommentActivity;
import com.jeofferson.onclas.PackageActivities.GeneralSearch;
import com.jeofferson.onclas.PackageActivities.UserActivity;
import com.jeofferson.onclas.PackageObjectModel.CommentHolder;
import com.jeofferson.onclas.PackageObjectModel.GeneralSearchItem;
import com.jeofferson.onclas.PackageObjectModel.Liker;
import com.jeofferson.onclas.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {


    private FirebaseAuth mAuth;
    private String currentUserId;

    private FirebaseFirestore db;
    private CollectionReference generalSearchListCollection;
    private CollectionReference commentHoldersCollection;
    private CollectionReference commentsCollection;
    private CollectionReference commentLikersCollection;
    private CollectionReference replyHoldersCollection;

    private GeneralSearchItem generalSearchItem;

    private Context context;
    private Resources resources;

    private List<CommentHolder> commentHolderList = new ArrayList<>();

    private String postHolderId;
    private String postId;


    public CommentAdapter() {}
    public CommentAdapter(List<CommentHolder> commentHolderList, String postHolderId, String postId) {
        this.commentHolderList = commentHolderList;
        this.postHolderId = postHolderId;
        this.postId = postId;
    }


    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        resources = context.getResources();

        View view = LayoutInflater.from(context).inflate(R.layout.list_comment_holder, parent, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        db = FirebaseFirestore.getInstance();
        generalSearchListCollection = db.collection("GeneralSearchList");
        commentHoldersCollection = db.collection("CommentHolders");
        commentsCollection = db.collection("Comments");
        commentLikersCollection = db.collection("CommentLikers");
        replyHoldersCollection = db.collection("ReplyHolders");

        generalSearchListCollection
                .whereEqualTo("itemId", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                generalSearchItem = queryDocumentSnapshot.toObject(GeneralSearchItem.class);

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

        return new CommentViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final CommentViewHolder holder, int position) {

        final CommentHolder commentHolder = commentHolderList.get(position);

        // sets the profile picture, name, and full type of the one who posted...
        Glide.with(context).load(commentHolder.getCommenterPicture()).placeholder(R.drawable.placeholder).into(holder.commentHolderCircleImageViewDisplayCommenterPicture);
        holder.commentHolderTextViewDisplayCommenterFullName.setText(commentHolder.getCommenter1Name());
        holder.commentHolderTextViewDisplayCommenterFullType.setText(commentHolder.getCommenter2Name());

        setDateCreated(commentHolder.getDateCreated(), holder.commentHolderTextViewDateCreated);

        // checks if there's no content...
        if (commentHolder.getCommentContent().trim().isEmpty()) {

            holder.commentHolderTextViewContent.setVisibility(View.GONE);

        } else {

            holder.commentHolderTextViewContent.setVisibility(View.VISIBLE);
            holder.commentHolderTextViewContent.setText(commentHolder.getCommentContent());

        }

        // checks if there's no picture...
        if (commentHolder.getCommentPicture().equals("NA")) {

            holder.commentHolderImageViewCommentPicture.setVisibility(View.GONE);

        } else {

            holder.commentHolderImageViewCommentPicture.setVisibility(View.VISIBLE);
            Glide.with(context).load(commentHolder.getCommentPicture()).placeholder(R.drawable.placeholder).into(holder.commentHolderImageViewCommentPicture);

        }

        commentLikersCollection
                .whereEqualTo("likedItemId", commentHolder.getCommentId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        // sets the initial number of likes...
                        int numOfLikes = queryDocumentSnapshots.size();
                        holder.commentHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                        // sets the initial state of the like button...
                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<String> likers = new ArrayList<>();

                            for (QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots) {

                                Liker liker = queryDocumentSnapshot.toObject(Liker.class);
                                likers.add(liker.getItemId());

                            }

                            if (likers.contains(currentUserId)) {

                                turnToLiked(holder.commentHolderButtonLike);

                            } else {

                                turnToLike(holder.commentHolderButtonLike);

                            }

                        } else {

                            turnToLike(holder.commentHolderButtonLike);

                        }

                        replyHoldersCollection
                                .whereEqualTo("replyOnId", commentHolder.getCommentId())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                        // sets the initial number of replies...
                                        int numOfReplies = queryDocumentSnapshots.size();
                                        holder.commentHolderTextViewNumOfReplies.setText(String.valueOf(numOfReplies));

                                        // when the profile picture of the one who posted is clicked...
                                        holder.commentHolderCircleImageViewDisplayCommenterPicture.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToUserActivity(commentHolder.getCommenter1Id());

                                            }
                                        });

                                        // when the full name of the one who posted is clicked...
                                        holder.commentHolderTextViewDisplayCommenterFullName.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToUserActivity(commentHolder.getCommenter1Id());

                                            }
                                        });

                                        // when the full type of the one who posted is clicked...
                                        holder.commentHolderTextViewDisplayCommenterFullType.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToUserActivity(commentHolder.getCommenter2Id());

                                            }
                                        });

                                        // when the number of likes is clicked...
                                        holder.commentHolderTextViewNumOfLikes.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToGeneralSearch("commentLikers", commentHolder.getCommentId());

                                            }
                                        });

                                        // when the like button is clicked...
                                        holder.commentHolderButtonLike.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                // to prevent the user from spamming the like button, the like button will be disabled for 1 second after click...
                                                holder.commentHolderButtonLike.setEnabled(false);
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    public void run() {

                                                        holder.commentHolderButtonLike.setEnabled(true);

                                                    }
                                                }, 1000);

                                                if (holder.commentHolderButtonLike.getDrawable().getConstantState() == resources.getDrawable(R.drawable.ic_like).getConstantState()) {

                                                    turnToLiked(holder.commentHolderButtonLike);

                                                } else {

                                                    turnToLike(holder.commentHolderButtonLike);

                                                }

                                                commentLikersCollection
                                                        .whereEqualTo("likedItemId", commentHolder.getCommentId())
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

                                                                    if (likers.contains(currentUserId)) {

                                                                        turnToLike(holder.commentHolderButtonLike);

                                                                        int numOfLikes = Integer.valueOf(holder.commentHolderTextViewNumOfLikes.getText().toString());
                                                                        numOfLikes--;
                                                                        holder.commentHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                        removeUserFromCommentLikers(commentHolder, holder.commentHolderButtonLike, holder.commentHolderTextViewNumOfLikes);

                                                                    } else {

                                                                        turnToLiked(holder.commentHolderButtonLike);

                                                                        int numOfLikes = Integer.valueOf(holder.commentHolderTextViewNumOfLikes.getText().toString());
                                                                        numOfLikes++;
                                                                        holder.commentHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                        addUserToCommentLikers(commentHolder, holder.commentHolderButtonLike, holder.commentHolderTextViewNumOfLikes);

                                                                    }

                                                                } else {

                                                                    turnToLiked(holder.commentHolderButtonLike);

                                                                    int numOfLikes = Integer.valueOf(holder.commentHolderTextViewNumOfLikes.getText().toString());
                                                                    numOfLikes++;
                                                                    holder.commentHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                    addUserToCommentLikers(commentHolder, holder.commentHolderButtonLike, holder.commentHolderTextViewNumOfLikes);

                                                                }

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {

                                                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                            }
                                                        });

                                            }
                                        });

                                        // when the reply button is clicked...
                                        holder.commentHolderTextViewReply.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToCommentActivity(commentHolder.getObjectId(), commentHolder.getCommentId(), true, false);

                                            }
                                        });

                                        // when the number of replies is clicked...
                                        holder.commentHolderTextViewNumOfReplies.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToCommentActivity(commentHolder.getObjectId(), commentHolder.getCommentId(), false, false);

                                            }
                                        });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    @Override
    public int getItemCount() {
        return commentHolderList.size();
    }


    public class CommentViewHolder extends RecyclerView.ViewHolder {

        CircleImageView commentHolderCircleImageViewDisplayCommenterPicture;
        TextView commentHolderTextViewDisplayCommenterFullName;
        TextView commentHolderTextViewDateCreated;
        TextView commentHolderTextViewDisplayCommenterFullType;
        TextView commentHolderTextViewContent;
        ImageView commentHolderImageViewCommentPicture;
        ImageView commentHolderButtonLike;
        TextView commentHolderTextViewNumOfLikes;
        TextView commentHolderTextViewNumOfReplies;
        ImageView commentHolderTextViewReply;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            commentHolderCircleImageViewDisplayCommenterPicture = itemView.findViewById(R.id.commentHolderCircleImageViewDisplayCommenterPicture);
            commentHolderTextViewDisplayCommenterFullName = itemView.findViewById(R.id.commentHolderTextViewDisplayCommenterFullName);
            commentHolderTextViewDateCreated = itemView.findViewById(R.id.commentHolderTextViewDateCreated);
            commentHolderTextViewDisplayCommenterFullType = itemView.findViewById(R.id.commentHolderTextViewDisplayCommenterFullType);
            commentHolderTextViewContent = itemView.findViewById(R.id.commentHolderTextViewContent);
            commentHolderImageViewCommentPicture = itemView.findViewById(R.id.commentHolderImageViewCommentPicture);
            commentHolderButtonLike = itemView.findViewById(R.id.commentHolderButtonLike);
            commentHolderTextViewNumOfLikes = itemView.findViewById(R.id.commentHolderTextViewNumOfLikes);
            commentHolderTextViewNumOfReplies = itemView.findViewById(R.id.commentHolderTextViewNumOfReplies);
            commentHolderTextViewReply = itemView.findViewById(R.id.commentHolderTextViewReply);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    CommentHolder commentHolder = commentHolderList.get(getAdapterPosition());
                    goToCommentActivity(commentHolder.getObjectId(), commentHolder.getCommentId(), false, false);

                }
            });

        }

    }


    public void setDateCreated(Date date, TextView textView) {

        if (date != null) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            textView.setText(simpleDateFormat.format(date));

        } else {

            textView.setText(context.getResources().getString(R.string.now));

        }

    }


    public void turnToLiked(ImageView imageView) {

        imageView.setImageResource(R.drawable.ic_liked);

    }


    public void addUserToCommentLikers(final CommentHolder commentHolder, final ImageView imageView, final TextView textView) {

        final Liker liker = new Liker(generalSearchItem.getItemId(), generalSearchItem.getItemPicture(), generalSearchItem.getItemName(), generalSearchItem.getType(), generalSearchItem.getDepartments(), generalSearchItem.getYear(), generalSearchItem.getFullType(), generalSearchItem.getItemType(), commentHolder.getCommentId());

        commentLikersCollection.add(liker).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if (task.isSuccessful()) {

                    commentLikersCollection
                            .whereEqualTo("likedItemId", commentHolder.getCommentId())
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

                                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                } else {

                    turnToLike(imageView);

                    int numOfLikes = Integer.valueOf(textView.getText().toString());
                    numOfLikes--;
                    textView.setText(String.valueOf(numOfLikes));

                    Toast.makeText(context, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    public void turnToLike(ImageView imageView) {

        imageView.setImageResource(R.drawable.ic_like);

    }


    public void removeUserFromCommentLikers(final CommentHolder commentHolder, final ImageView imageView, final TextView textView) {

        commentLikersCollection
                .whereEqualTo("likedItemId", commentHolder.getCommentId())
                .whereEqualTo("itemId", currentUserId)
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
                                                .whereEqualTo("likedItemId", commentHolder.getCommentId())
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

                                                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    } else {

                                        turnToLiked(imageView);

                                        int numOfLikes = Integer.valueOf(textView.getText().toString());
                                        numOfLikes++;
                                        textView.setText(String.valueOf(numOfLikes));

                                        Toast.makeText(context, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

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

                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void goToCommentActivity(String commentHolderId, String commentId, boolean willComment, boolean isFromNotifications) {

        Intent intentCommentActivity = new Intent(context, CommentActivity.class);

        intentCommentActivity.putExtra("postHolderId", postHolderId);
        intentCommentActivity.putExtra("postId", postId);
        intentCommentActivity.putExtra("commentHolderId", commentHolderId);
        intentCommentActivity.putExtra("commentId", commentId);
        intentCommentActivity.putExtra("willComment", willComment);
        intentCommentActivity.putExtra("isFromNotifications", isFromNotifications);

        context.startActivity(intentCommentActivity);

    }


    public void goToGeneralSearch(String type, String commentId) {

        Intent intentGeneralSearch = new Intent(context, GeneralSearch.class);
        intentGeneralSearch.putExtra("type", type);
        intentGeneralSearch.putExtra("commentId", commentId);
        context.startActivity(intentGeneralSearch);

    }


    public void goToUserActivity(String userId) {

        Intent intentUserActivity = new Intent(context, UserActivity.class);
        intentUserActivity.putExtra("userId", userId);
        context.startActivity(intentUserActivity);

    }


}
