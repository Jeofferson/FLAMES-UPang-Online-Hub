package com.jeofferson.onclas.PackageAdapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeofferson.onclas.PackageActivities.CommentActivity;
import com.jeofferson.onclas.PackageActivities.Updates;
import com.jeofferson.onclas.PackageObjectModel.NotificationHolder;
import com.jeofferson.onclas.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {


    private Context context;
    private Resources resources;

    private FirebaseFirestore db;
    private CollectionReference postsCollection;
    private CollectionReference commentsCollection;
    private CollectionReference replyHoldersCollection;

    private List<NotificationHolder> notificationHolderList = new ArrayList<>();


    public NotificationAdapter(List<NotificationHolder> notificationHolderList) {
        this.notificationHolderList = notificationHolderList;
    }


    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        resources = context.getResources();

        View view = LayoutInflater.from(context).inflate(R.layout.list_notification_holder, parent, false);

        db = FirebaseFirestore.getInstance();
        postsCollection = db.collection("Posts");
        commentsCollection = db.collection("Comments");
        replyHoldersCollection = db.collection("ReplyHolders");

        return new NotificationAdapter.NotificationViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {

        NotificationHolder notificationHolder = notificationHolderList.get(position);

        Glide.with(context).load(notificationHolder.getFromPicture()).placeholder(R.drawable.placeholder).into(holder.notificationHolderCircleImageViewFromPicture);

        String bodyMessage;

//        Log.e("hey", notificationHolder.getNotificationSpecificContent() + " <-");

        if (notificationHolder.getNotificationSpecificContent().isEmpty()) {

            bodyMessage = notificationHolder.getFromName() + " " + notificationHolder.getNotificationContent() + ".";

        } else {

            bodyMessage = notificationHolder.getFromName() + " " + notificationHolder.getNotificationContent() + ": \"" + notificationHolder.getNotificationSpecificContent() + "\"";

        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(bodyMessage);
        spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, notificationHolder.getFromName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.notificationHolderTextViewNotificationContent.setText(spannableStringBuilder);

        setDateCreated(notificationHolder.getDateCreated(), holder.notificationHolderTextViewDateCreated);

    }


    @Override
    public int getItemCount() {
        return notificationHolderList.size();
    }


    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        CircleImageView notificationHolderCircleImageViewFromPicture;
        TextView notificationHolderTextViewNotificationContent;
        TextView notificationHolderTextViewDateCreated;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            notificationHolderCircleImageViewFromPicture = itemView.findViewById(R.id.notificationHolderCircleImageViewFromPicture);
            notificationHolderTextViewNotificationContent = itemView.findViewById(R.id.notificationHolderTextViewNotificationContent);
            notificationHolderTextViewDateCreated = itemView.findViewById(R.id.notificationHolderTextViewDateCreated);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    NotificationHolder notificationHolder = notificationHolderList.get(getAdapterPosition());

                    checkIfPostExists(notificationHolder);

                }
            });

        }

    }


    public void checkIfPostExists(final NotificationHolder notificationHolder) {

        postsCollection.document(notificationHolder.getDestinationPost()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {

                            checkIfCommentExists(notificationHolder);

                        } else {

                            showMayBeenDeletedDialog();

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

//                                            Toast.makeText(Updates.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void checkIfCommentExists(final NotificationHolder notificationHolder) {

        commentsCollection.document(notificationHolder.getDestinationComment()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {

                            switch (notificationHolder.getNotificationType()) {

                                case "Comment Notification":

                                    goToStudentForum(notificationHolder.getDestinationPostHolder(), notificationHolder.getDestinationPost(), false, false);
                                    break;

                                case "Reply Notification":

                                    checkIfReplyExists(notificationHolder);
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


    public void checkIfReplyExists(final NotificationHolder notificationHolder) {

        replyHoldersCollection.document(notificationHolder.getDestinationReplyHolder()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {

                            goToCommentActivity(notificationHolder.getDestinationCommentHolder(), notificationHolder.getDestinationComment(), false, false);

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


    public void setDateCreated(Date date, TextView textView) {

        if (date != null) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            textView.setText(simpleDateFormat.format(date));

        } else {

            textView.setText(context.getResources().getString(R.string.now));

        }

    }


    public void showMayBeenDeletedDialog() {

        new AlertDialog.Builder(context)
                .setTitle(resources.getString(R.string.oops))
                .setMessage(resources.getString(R.string.mayBeenDeleted))
                .setPositiveButton(resources.getString(R.string.ok), null)
                .show();

    }


    public void goToStudentForum(String postHolderId, String postId, boolean willComment, boolean isFromNotifications) {

        Intent intentStudentForum = new Intent(context, Updates.class);
        intentStudentForum.putExtra("postHolderId", postHolderId);
        intentStudentForum.putExtra("postId", postId);
        intentStudentForum.putExtra("willComment", willComment);
        intentStudentForum.putExtra("isFromNotifications", isFromNotifications);
        context.startActivity(intentStudentForum);

    }


    public void goToCommentActivity(String commentHolderId, String commentId, boolean willComment,  boolean isFromNotifications) {

        Intent intentCommentActivity = new Intent(context, CommentActivity.class);
        intentCommentActivity.putExtra("commentHolderId", commentHolderId);
        intentCommentActivity.putExtra("commentId", commentId);
        intentCommentActivity.putExtra("willComment", willComment);
        intentCommentActivity.putExtra("isFromNotifications", isFromNotifications);
        context.startActivity(intentCommentActivity);

    }


}
