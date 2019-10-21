package com.jeofferson.onclas.PackageAdapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
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
import com.jeofferson.onclas.PackageActivities.GeneralSearch;
import com.jeofferson.onclas.PackageActivities.Updates;
import com.jeofferson.onclas.PackageActivities.UserActivity;
import com.jeofferson.onclas.PackageObjectModel.GeneralSearchItem;
import com.jeofferson.onclas.PackageObjectModel.Liker;
import com.jeofferson.onclas.PackageObjectModel.PostHolder;
import com.jeofferson.onclas.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {


    private FirebaseAuth mAuth;
    private String currentUserId;

    private FirebaseFirestore db;
    private CollectionReference generalSearchListCollection;
    private CollectionReference newsfeedCollection;
    private CollectionReference postsCollection;
    private CollectionReference postLikersCollection;
    private CollectionReference commentHoldersCollection;

    private GeneralSearchItem generalSearchItem;

    private Context context;
    private Resources resources;

    private List<PostHolder> postHolderList = new ArrayList<>();


    public PostAdapter() {}
    public PostAdapter(List<PostHolder> postHolderList) {
        this.postHolderList = postHolderList;
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        resources = context.getResources();

        View view = LayoutInflater.from(context).inflate(R.layout.list_post_holder, parent, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        db = FirebaseFirestore.getInstance();
        generalSearchListCollection = db.collection("GeneralSearchList");
        newsfeedCollection = db.collection("Newsfeed");
        postsCollection = db.collection("Posts");
        postLikersCollection = db.collection("PostLikers");
        commentHoldersCollection = db.collection("CommentHolders");

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

        return new PostViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, final int position) {

        final PostHolder postHolder = postHolderList.get(position);

        // sets the profile picture, name, and full type of the one who posted...
        Glide.with(context).load(postHolder.getPosterPicture()).placeholder(R.drawable.placeholder).into(holder.postHolderCircleImageViewDisplayPosterPicture);
        holder.postHolderTextViewDisplayPosterFullName.setText(postHolder.getPoster1Name());
        holder.postHolderTextViewDisplayPosterFullType.setText(postHolder.getPoster2Name());

        setDateCreated(postHolder.getDateCreated(), holder.postHolderTextViewDateCreated);

        // checks if there's no content...
        if (postHolder.getPostContent().trim().isEmpty()) {

            holder.postHolderTextViewContent.setVisibility(View.GONE);

        } else {

            holder.postHolderTextViewContent.setVisibility(View.VISIBLE);
            holder.postHolderTextViewContent.setText(postHolder.getPostContent());

        }

        // checks if there's no picture...
        if (postHolder.getPostPicture().equals("NA")) {

            holder.postHolderImageViewPostPicture.setVisibility(View.GONE);

        } else {

            holder.postHolderImageViewPostPicture.setVisibility(View.VISIBLE);
            Glide.with(context).load(postHolder.getPostPicture()).placeholder(R.drawable.placeholder).into(holder.postHolderImageViewPostPicture);

        }

        postLikersCollection
                .whereEqualTo("likedItemId", postHolder.getPostId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        // sets the initial number of likes...
                        int numOfLikes = queryDocumentSnapshots.size();
                        holder.postHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                        // sets the initial state of the like button...
                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<String> likers = new ArrayList<>();

                            for (QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots) {

                                Liker liker = queryDocumentSnapshot.toObject(Liker.class);
                                likers.add(liker.getItemId());

                            }

                            if (likers.contains(currentUserId)) {

                                turnToLiked(holder.postHolderButtonLike);

                            } else {

                                turnToLike(holder.postHolderButtonLike);

                            }

                        } else {

                            turnToLike(holder.postHolderButtonLike);

                        }

                        commentHoldersCollection
                                .whereEqualTo("commentOnId", postHolder.getPostId())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                        // sets the initial number of comments...
                                        int numOfComments = queryDocumentSnapshots.size();
                                        holder.postHolderTextViewNumOfComments.setText(String.valueOf(numOfComments));

                                        // when the profile picture of the one who posted is clicked...
                                        holder.postHolderCircleImageViewDisplayPosterPicture.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToUserActivity(postHolder.getPoster1Id());

                                            }
                                        });

                                        // when the full name of the one who posted is clicked...
                                        holder.postHolderTextViewDisplayPosterFullName.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToUserActivity(postHolder.getPoster1Id());

                                            }
                                        });

                                        // when the full type of the one who posted is clicked...
                                        holder.postHolderTextViewDisplayPosterFullType.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToUserActivity(postHolder.getPoster2Id());

                                            }
                                        });

                                        // when the number of likes is clicked...
                                        holder.postHolderTextViewNumOfLikes.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToGeneralSearch("postLikers", postHolder.getPostId());

                                            }
                                        });

                                        // when the like button is clicked...
                                        holder.postHolderButtonLike.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                // to prevent the user from spamming the like button, the like button will be disabled for 1 second after click...
                                                holder.postHolderButtonLike.setEnabled(false);
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    public void run() {

                                                    holder.postHolderButtonLike.setEnabled(true);

                                                    }
                                                }, 1000);

                                                if (holder.postHolderButtonLike.getDrawable().getConstantState() == resources.getDrawable(R.drawable.ic_like).getConstantState()) {

                                                    turnToLiked(holder.postHolderButtonLike);

                                                } else {

                                                    turnToLike(holder.postHolderButtonLike);

                                                }

                                                postLikersCollection
                                                        .whereEqualTo("likedItemId", postHolder.getPostId())
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

                                                                        turnToLike(holder.postHolderButtonLike);

                                                                        int numOfLikes = Integer.valueOf(holder.postHolderTextViewNumOfLikes.getText().toString());
                                                                        numOfLikes--;
                                                                        holder.postHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                        removeUserFromPostLikers(postHolder, holder.postHolderButtonLike, holder.postHolderTextViewNumOfLikes);

                                                                    } else {

                                                                        turnToLiked(holder.postHolderButtonLike);

                                                                        int numOfLikes = Integer.valueOf(holder.postHolderTextViewNumOfLikes.getText().toString());
                                                                        numOfLikes++;
                                                                        holder.postHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                        addUserToPostLikers(postHolder, holder.postHolderButtonLike, holder.postHolderTextViewNumOfLikes);

                                                                    }

                                                                } else {

                                                                    turnToLiked(holder.postHolderButtonLike);

                                                                    int numOfLikes = Integer.valueOf(holder.postHolderTextViewNumOfLikes.getText().toString());
                                                                    numOfLikes++;
                                                                    holder.postHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                                    addUserToPostLikers(postHolder, holder.postHolderButtonLike, holder.postHolderTextViewNumOfLikes);

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

                                        // when the comment button is clicked...
                                        holder.postHolderButtonComment.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                goToStudentForum(postHolder.getObjectId(), postHolder.getPostId(), true, false);

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

        return postHolderList.size();

    }


    public class PostViewHolder extends RecyclerView.ViewHolder {

        CircleImageView postHolderCircleImageViewDisplayPosterPicture;
        TextView postHolderTextViewDisplayPosterFullName;
        TextView postHolderTextViewDisplayPosterFullType;
        TextView postHolderTextViewDateCreated;
        TextView postHolderTextViewContent;
        ImageView postHolderImageViewPostPicture;
        ImageView postHolderButtonLike;
        ImageView postHolderButtonComment;
        TextView postHolderTextViewNumOfLikes;
        TextView postHolderTextViewNumOfComments;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            postHolderCircleImageViewDisplayPosterPicture = itemView.findViewById(R.id.postHolderCircleImageViewDisplayPosterPicture);
            postHolderTextViewDisplayPosterFullName = itemView.findViewById(R.id.postHolderTextViewDisplayPosterFullName);
            postHolderTextViewDisplayPosterFullType = itemView.findViewById(R.id.postHolderTextViewDisplayPosterFullType);
            postHolderTextViewDateCreated = itemView.findViewById(R.id.postHolderTextViewDateCreated);
            postHolderTextViewContent = itemView.findViewById(R.id.postHolderTextViewContent);
            postHolderImageViewPostPicture = itemView.findViewById(R.id.postHolderImageViewPostPicture);
            postHolderButtonLike = itemView.findViewById(R.id.postHolderButtonLike);
            postHolderButtonComment = itemView.findViewById(R.id.postHolderButtonComment);
            postHolderTextViewNumOfLikes = itemView.findViewById(R.id.postHolderTextViewNumOfLikes);
            postHolderTextViewNumOfComments = itemView.findViewById(R.id.postHolderTextViewNumOfComments);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PostHolder postHolder = postHolderList.get(getAdapterPosition());

                    switch (postHolder.getPostType()) {

                        case "Student Updates":
                        case "School Updates":
                            goToStudentForum(postHolder.getObjectId(), postHolder.getPostId(), false, false);
                            break;

                    }

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


    public void addUserToPostLikers(final PostHolder postHolder, final ImageView imageView, final TextView textView) {

        final Liker liker = new Liker(generalSearchItem.getItemId(), generalSearchItem.getItemPicture(), generalSearchItem.getItemName(), generalSearchItem.getType(), generalSearchItem.getDepartments(), generalSearchItem.getYear(), generalSearchItem.getFullType(), generalSearchItem.getItemType(), postHolder.getPostId());

        postLikersCollection.add(liker).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if (task.isSuccessful()) {

                    postLikersCollection
                            .whereEqualTo("likedItemId", postHolder.getPostId())
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


    public void removeUserFromPostLikers(final PostHolder postHolder, final ImageView imageView, final TextView textView) {

        postLikersCollection
                .whereEqualTo("likedItemId", postHolder.getPostId())
                .whereEqualTo("itemId", currentUserId)
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
                                                .whereEqualTo("likedItemId", postHolder.getPostId())
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


    public void goToStudentForum(String postHolderId, String postId, boolean willComment, boolean isFromNotifications) {

        Intent intentStudentForum = new Intent(context, Updates.class);
        intentStudentForum.putExtra("postHolderId", postHolderId);
        intentStudentForum.putExtra("postId", postId);
        intentStudentForum.putExtra("willComment", willComment);
        intentStudentForum.putExtra("isFromNotifications", isFromNotifications);
        context.startActivity(intentStudentForum);

    }


    public void goToGeneralSearch(String type, String postId) {

        Intent intentGeneralSearch = new Intent(context, GeneralSearch.class);
        intentGeneralSearch.putExtra("type", type);
        intentGeneralSearch.putExtra("postId", postId);
        context.startActivity(intentGeneralSearch);

    }


    public void goToUserActivity(String userId) {

        Intent intentUserActivity = new Intent(context, UserActivity.class);
        intentUserActivity.putExtra("userId", userId);
        context.startActivity(intentUserActivity);

    }


}
