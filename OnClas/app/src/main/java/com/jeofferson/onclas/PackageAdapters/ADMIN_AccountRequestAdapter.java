package com.jeofferson.onclas.PackageAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.jeofferson.onclas.PackageObjectModel.AdminAccountRequestHolder;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ADMIN_AccountRequestAdapter extends RecyclerView.Adapter<ADMIN_AccountRequestAdapter.ADMIN_AccountRequestViewHolder> {


    private FirebaseFirestore db;
    private CollectionReference adminAccountRequestsCollection;

    private Context context;
    private List<AdminAccountRequestHolder> adminAccountRequestList = new ArrayList<>();


    public ADMIN_AccountRequestAdapter() {}
    public ADMIN_AccountRequestAdapter(List<AdminAccountRequestHolder> adminAccountRequestList) {
        this.adminAccountRequestList = adminAccountRequestList;
    }


    @NonNull
    @Override
    public ADMIN_AccountRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_admin_account_request_holder, parent, false);

        db = FirebaseFirestore.getInstance();
        adminAccountRequestsCollection = db.collection("AdminAccountRequests");

        return new ADMIN_AccountRequestAdapter.ADMIN_AccountRequestViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull final ADMIN_AccountRequestViewHolder holder, int position) {

        final AdminAccountRequestHolder adminAccountRequestHolder = adminAccountRequestList.get(position);

        String userActionDescription = adminAccountRequestHolder.getUserFullName() + " requests to create an account as a " + adminAccountRequestHolder.getFullType();

        Glide.with(context).load(adminAccountRequestHolder.getUserPicture()).placeholder(R.drawable.placeholder).into(holder.adminAccountRequestHolderCircleImageViewUserPicture);
        holder.adminAccountRequestHolderTextViewUserFullName.setText(adminAccountRequestHolder.getUserFullName());
        holder.adminAccountRequestHolderTextViewUserActionDescription.setText(userActionDescription);
        holder.adminAccountRequestHolderButtonAllow.setVisibility(View.VISIBLE);
        holder.adminAccountRequestHolderButtonDecline.setVisibility(View.VISIBLE);

        holder.adminAccountRequestHolderButtonAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.adminAccountRequestHolderButtonAllow.setVisibility(View.GONE);
                holder.adminAccountRequestHolderButtonDecline.setVisibility(View.GONE);

                holder.adminAccountRequestHolderTextViewAllowedDeclined.setText(context.getResources().getString(R.string.processing));
                holder.adminAccountRequestHolderTextViewAllowedDeclined.setTextColor(context.getResources().getColor(R.color.darkTextSelected));
                holder.adminAccountRequestHolderTextViewAllowedDeclined.setVisibility(View.VISIBLE);

                allowAccountRequest(adminAccountRequestHolder.getUserReference(), adminAccountRequestHolder.getGeneralSearchItemReference(), adminAccountRequestsCollection.document(adminAccountRequestHolder.getObjectId()), holder.adminAccountRequestHolderButtonAllow, holder.adminAccountRequestHolderButtonDecline, holder.adminAccountRequestHolderTextViewAllowedDeclined);

            }
        });

        holder.adminAccountRequestHolderButtonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.adminAccountRequestHolderButtonAllow.setVisibility(View.GONE);
                holder.adminAccountRequestHolderButtonDecline.setVisibility(View.GONE);

                holder.adminAccountRequestHolderTextViewAllowedDeclined.setText(context.getResources().getString(R.string.processing));
                holder.adminAccountRequestHolderTextViewAllowedDeclined.setTextColor(context.getResources().getColor(R.color.darkTextSelected));
                holder.adminAccountRequestHolderTextViewAllowedDeclined.setVisibility(View.VISIBLE);

                declineAccountRequest(adminAccountRequestHolder.getUserReference(), adminAccountRequestHolder.getGeneralSearchItemReference(), adminAccountRequestsCollection.document(adminAccountRequestHolder.getObjectId()), holder.adminAccountRequestHolderButtonAllow, holder.adminAccountRequestHolderButtonDecline, holder.adminAccountRequestHolderTextViewAllowedDeclined);

            }
        });

    }


    @Override
    public int getItemCount() {
        return adminAccountRequestList.size();
    }


    public class ADMIN_AccountRequestViewHolder extends RecyclerView.ViewHolder {

        CircleImageView adminAccountRequestHolderCircleImageViewUserPicture;
        TextView adminAccountRequestHolderTextViewUserFullName;
        TextView adminAccountRequestHolderTextViewUserActionDescription;
        Button adminAccountRequestHolderButtonAllow;
        Button adminAccountRequestHolderButtonDecline;
        TextView adminAccountRequestHolderTextViewAllowedDeclined;

        public ADMIN_AccountRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            adminAccountRequestHolderCircleImageViewUserPicture = itemView.findViewById(R.id.adminAccountRequestHolderCircleImageViewUserPicture);
            adminAccountRequestHolderTextViewUserFullName = itemView.findViewById(R.id.adminAccountRequestHolderTextViewUserFullName);
            adminAccountRequestHolderTextViewUserActionDescription = itemView.findViewById(R.id.adminAccountRequestHolderTextViewUserActionDescription);
            adminAccountRequestHolderButtonAllow = itemView.findViewById(R.id.adminAccountRequestHolderButtonAllow);
            adminAccountRequestHolderButtonDecline = itemView.findViewById(R.id.adminAccountRequestHolderButtonDecline);
            adminAccountRequestHolderTextViewAllowedDeclined = itemView.findViewById(R.id.adminAccountRequestHolderTextViewAllowedDeclined);

        }

    }


    public void allowAccountRequest(DocumentReference userReference, DocumentReference generalSearchItemReference, DocumentReference adminAccountRequestDocumentReference, final Button buttonAllow, final Button buttonDecline, final TextView textViewAllowedDeclined) {

        WriteBatch writeBatch = db.batch();

        writeBatch.update(userReference, "authorized", true);
        writeBatch.update(generalSearchItemReference, "authorized", true);
        writeBatch.update(adminAccountRequestDocumentReference, "decided", true);

        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    textViewAllowedDeclined.setText(context.getResources().getString(R.string.allowed));
                    textViewAllowedDeclined.setTextColor(context.getResources().getColor(R.color.darkTextDisabled));

                } else {

                    textViewAllowedDeclined.setVisibility(View.GONE);

                    buttonAllow.setVisibility(View.VISIBLE);
                    buttonDecline.setVisibility(View.VISIBLE);

                    Toast.makeText(context, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    public void declineAccountRequest(DocumentReference userReference, DocumentReference generalSearchItemReference, DocumentReference adminAccountRequestDocumentReference, final Button buttonAllow, final Button buttonDecline, final TextView textViewAllowedDeclined) {

        WriteBatch writeBatch = db.batch();

        writeBatch.update(userReference, "authorized", false);
        writeBatch.update(userReference, "fullType", "NA");
        writeBatch.update(generalSearchItemReference, "authorized", false);
        writeBatch.update(adminAccountRequestDocumentReference, "decided", true);

        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    textViewAllowedDeclined.setText(context.getResources().getString(R.string.declined));
                    textViewAllowedDeclined.setTextColor(context.getResources().getColor(R.color.darkTextDisabled));

                } else {

                    textViewAllowedDeclined.setVisibility(View.GONE);

                    buttonAllow.setVisibility(View.VISIBLE);
                    buttonDecline.setVisibility(View.VISIBLE);

                    Toast.makeText(context, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


}
