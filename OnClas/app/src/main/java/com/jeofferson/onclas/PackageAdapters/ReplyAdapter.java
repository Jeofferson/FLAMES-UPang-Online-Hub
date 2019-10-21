package com.jeofferson.onclas.PackageAdapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
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
import com.jeofferson.onclas.PackageActivities.FullScreenImage;
import com.jeofferson.onclas.PackageActivities.GeneralSearch;
import com.jeofferson.onclas.PackageActivities.UserActivity;
import com.jeofferson.onclas.PackageObjectModel.GeneralSearchItem;
import com.jeofferson.onclas.PackageObjectModel.Liker;
import com.jeofferson.onclas.PackageObjectModel.ReplyHolder;
import com.jeofferson.onclas.PackageOthers.BottomSheetDialogReply;
import com.jeofferson.onclas.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {


    private FirebaseAuth mAuth;
    private String currentUserId;

    private FirebaseFirestore db;
    private CollectionReference generalSearchListCollection;
    private CollectionReference replyHoldersCollection;
    private CollectionReference replyLikersCollection;

    private GeneralSearchItem generalSearchItem;

    private Context context;
    private Resources resources;

    private List<ReplyHolder> replyHolderList = new ArrayList<>();

    private String postHolderId;
    private String postId;
    private String commentHolderId;
    private String commentId;


    public ReplyAdapter() {}
    public ReplyAdapter(List<ReplyHolder> replyHolderList, String postHolderId, String postId, String commentHolderId, String commentId) {
        this.replyHolderList = replyHolderList;
        this.postHolderId = postHolderId;
        this.postId = postId;
        this.commentHolderId = commentHolderId;
        this.commentId = commentId;
    }


    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        resources = context.getResources();

        View view = LayoutInflater.from(context).inflate(R.layout.list_reply_holder, parent, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        db = FirebaseFirestore.getInstance();
        generalSearchListCollection = db.collection("GeneralSearchList");
        replyHoldersCollection = db.collection("ReplyHolders");
        replyLikersCollection = db.collection("ReplyLikers");

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

        return new ReplyAdapter.ReplyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ReplyViewHolder holder, final int position) {

        final ReplyHolder replyHolder = replyHolderList.get(position);

        // sets the profile picture, name, and full type of the one who posted...
        Glide.with(context).load(replyHolder.getReplierPicture()).placeholder(R.drawable.placeholder).into(holder.replyHolderCircleImageViewDisplayReplierPicture);
        holder.replyHolderTextViewDisplayReplierFullName.setText(replyHolder.getReplier1Name());
        holder.replyHolderTextViewDisplayReplierFullType.setText(replyHolder.getReplier2Name());

        setDateCreated(replyHolder.getDateCreated(), holder.replyHolderTextViewDateCreated);

        // checks if it's the current user's own comment...
        if (mAuth.getCurrentUser().getUid().equals(replyHolder.getReplier1Id())) {

            holder.replyHolderImageViewEditReply.setVisibility(View.VISIBLE);

        } else {

            holder.replyHolderImageViewEditReply.setVisibility(View.GONE);

        }

        // checks if there's no content...
        if (replyHolder.getReplyContent().trim().isEmpty()) {

            holder.replyHolderTextViewContent.setVisibility(View.GONE);

        } else {

            holder.replyHolderTextViewContent.setVisibility(View.VISIBLE);
            holder.replyHolderTextViewContent.setText(replyHolder.getReplyContent());

        }

        // checks if there's no picture...
        if (replyHolder.getReplyPicture().equals("NA")) {

            holder.replyHolderImageViewReplyPicture.setVisibility(View.GONE);

        } else {

            holder.replyHolderImageViewReplyPicture.setVisibility(View.VISIBLE);
            Glide.with(context).load(replyHolder.getReplyPicture()).placeholder(R.drawable.placeholder).into(holder.replyHolderImageViewReplyPicture);

        }

        replyLikersCollection
                .whereEqualTo("likedItemId", replyHolder.getObjectId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        // sets the initial number of likes...
                        int numOfLikes = queryDocumentSnapshots.size();
                        holder.replyHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                        // sets the initial state of the like button...
                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<String> likers = new ArrayList<>();

                            for (QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots) {

                                Liker liker = queryDocumentSnapshot.toObject(Liker.class);
                                likers.add(liker.getItemId());

                            }

                            if (likers.contains(currentUserId)) {

                                turnToLiked(holder.replyHolderButtonLike);

                            } else {

                                turnToLike(holder.replyHolderButtonLike);

                            }

                        } else {

                            turnToLike(holder.replyHolderButtonLike);

                        }

                        // when the profile picture of the one who posted is clicked...
                        holder.replyHolderCircleImageViewDisplayReplierPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                goToUserActivity(replyHolder.getReplier1Id());

                            }
                        });

                        // when the full name of the one who posted is clicked...
                        holder.replyHolderTextViewDisplayReplierFullName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                goToUserActivity(replyHolder.getReplier1Id());

                            }
                        });

                        // when the down arrow for editing the comment is clicked...
                        holder.replyHolderImageViewEditReply.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                BottomSheetDialogReply BottomSheetDialogReply = new BottomSheetDialogReply(position, replyHolder);
                                BottomSheetDialogReply.show(((FragmentActivity) context).getSupportFragmentManager(), "My Bottom Sheet");

                            }
                        });

                        // when the full type of the one who posted is clicked...
                        holder.replyHolderTextViewDisplayReplierFullType.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                goToUserActivity(replyHolder.getReplier2Id());

                            }
                        });

                        // when the post picture is clicked...
                        holder.replyHolderImageViewReplyPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                goToFullScreenImage(Uri.parse(replyHolder.getReplyPicture()));

                            }
                        });

                        // when the number of likes is clicked...
                        holder.replyHolderTextViewNumOfLikes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                goToGeneralSearch("replyLikers", replyHolder.getObjectId());

                            }
                        });

                        // when the like button is clicked...
                        holder.replyHolderButtonLike.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                // to prevent the user from spamming the like button, the like button will be disabled for 1 second after click...
                                holder.replyHolderButtonLike.setEnabled(false);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {

                                        holder.replyHolderButtonLike.setEnabled(true);

                                    }
                                }, 1000);

                                if (holder.replyHolderButtonLike.getDrawable().getConstantState() == resources.getDrawable(R.drawable.ic_like).getConstantState()) {

                                    turnToLiked(holder.replyHolderButtonLike);

                                } else {

                                    turnToLike(holder.replyHolderButtonLike);

                                }

                                replyLikersCollection
                                        .whereEqualTo("likedItemId", replyHolder.getObjectId())
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

                                                        turnToLike(holder.replyHolderButtonLike);

                                                        int numOfLikes = Integer.valueOf(holder.replyHolderTextViewNumOfLikes.getText().toString());
                                                        numOfLikes--;
                                                        holder.replyHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                        removeUserFromCommentLikers(replyHolder, holder.replyHolderButtonLike, holder.replyHolderTextViewNumOfLikes);

                                                    } else {

                                                        turnToLiked(holder.replyHolderButtonLike);

                                                        int numOfLikes = Integer.valueOf(holder.replyHolderTextViewNumOfLikes.getText().toString());
                                                        numOfLikes++;
                                                        holder.replyHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                        addUserToCommentLikers(replyHolder, holder.replyHolderButtonLike, holder.replyHolderTextViewNumOfLikes);

                                                    }

                                                } else {

                                                    turnToLiked(holder.replyHolderButtonLike);

                                                    int numOfLikes = Integer.valueOf(holder.replyHolderTextViewNumOfLikes.getText().toString());
                                                    numOfLikes++;
                                                    holder.replyHolderTextViewNumOfLikes.setText(String.valueOf(numOfLikes));

                                                    addUserToCommentLikers(replyHolder, holder.replyHolderButtonLike, holder.replyHolderTextViewNumOfLikes);

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

        return replyHolderList.size();

    }


    public class ReplyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView replyHolderCircleImageViewDisplayReplierPicture;
        TextView replyHolderTextViewDisplayReplierFullName;
        ImageView replyHolderImageViewEditReply;
        TextView replyHolderTextViewDateCreated;
        TextView replyHolderTextViewDisplayReplierFullType;
        TextView replyHolderTextViewContent;
        ImageView replyHolderImageViewReplyPicture;
        ImageView replyHolderButtonLike;
        TextView replyHolderTextViewNumOfLikes;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);

            replyHolderCircleImageViewDisplayReplierPicture = itemView.findViewById(R.id.replyHolderCircleImageViewDisplayReplierPicture);
            replyHolderTextViewDisplayReplierFullName = itemView.findViewById(R.id.replyHolderTextViewDisplayReplierFullName);
            replyHolderImageViewEditReply = itemView.findViewById(R.id.replyHolderImageViewEditReply);
            replyHolderTextViewDateCreated = itemView.findViewById(R.id.replyHolderTextViewDateCreated);
            replyHolderTextViewDisplayReplierFullType = itemView.findViewById(R.id.replyHolderTextViewDisplayReplierFullType);
            replyHolderTextViewContent = itemView.findViewById(R.id.replyHolderTextViewContent);
            replyHolderImageViewReplyPicture = itemView.findViewById(R.id.replyHolderImageViewReplyPicture);
            replyHolderButtonLike = itemView.findViewById(R.id.replyHolderButtonLike);
            replyHolderTextViewNumOfLikes = itemView.findViewById(R.id.replyHolderTextViewNumOfLikes);

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


    public void addUserToCommentLikers(final ReplyHolder replyHolder, final ImageView imageView, final TextView textView) {

        final Liker liker = new Liker(generalSearchItem.getItemId(), generalSearchItem.getItemPicture(), generalSearchItem.getItemName(), generalSearchItem.getType(), generalSearchItem.getDepartments(), generalSearchItem.getYear(), generalSearchItem.getFullType(), generalSearchItem.getItemType(), replyHolder.getObjectId());

        replyLikersCollection.add(liker).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if (task.isSuccessful()) {

                    replyLikersCollection
                            .whereEqualTo("likedItemId", replyHolder.getObjectId())
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


    public void removeUserFromCommentLikers(final ReplyHolder replyHolder, final ImageView imageView, final TextView textView) {

        replyLikersCollection
                .whereEqualTo("likedItemId", replyHolder.getObjectId())
                .whereEqualTo("itemId", currentUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            WriteBatch writeBatch = db.batch();

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {

                                writeBatch.delete(replyLikersCollection.document(queryDocumentSnapshot.getId()));

                            }

                            writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        replyLikersCollection
                                                .whereEqualTo("likedItemId", replyHolder.getObjectId())
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


    public void goToFullScreenImage(Uri uri) {

        Intent intentFullScreenImage = new Intent(context, FullScreenImage.class);
        intentFullScreenImage.setData(uri);
        context.startActivity(intentFullScreenImage);

    }


    public void goToGeneralSearch(String type, String commentId) {

        Intent intentGeneralSearch = new Intent(context, GeneralSearch.class);
        intentGeneralSearch.putExtra("type", type);
        intentGeneralSearch.putExtra("replyId", commentId);
        context.startActivity(intentGeneralSearch);

    }


    public void goToUserActivity(String userId) {

        Intent intentUserActivity = new Intent(context, UserActivity.class);
        intentUserActivity.putExtra("userId", userId);
        context.startActivity(intentUserActivity);

    }


}
