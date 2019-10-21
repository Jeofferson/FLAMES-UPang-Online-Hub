package com.jeofferson.onclas.PackageAdapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.jeofferson.onclas.PackageActivities.FullScreenImage;
import com.jeofferson.onclas.PackageObjectModel.UserPhoto;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.List;

public class UserPhotoAdapter extends RecyclerView.Adapter<UserPhotoAdapter.UserPhotoViewHolder> {


    private Context context;
    private Resources resources;

    private List<UserPhoto> userPhotoList = new ArrayList<>();

    private boolean isVertical;
    private String userId;
    private String from;
    private boolean isLandscape;


    public UserPhotoAdapter() {}
    public UserPhotoAdapter(List<UserPhoto> userPhotoList, boolean isVertical, String userId, String from, boolean isLandscape) {
        this.userPhotoList = userPhotoList;
        this.isVertical = isVertical;
        this.userId = userId;
        this.from = from;
        this.isLandscape = isLandscape;
    }


    @NonNull
    @Override
    public UserPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        resources = context.getResources();

        View view;

        if (isLandscape) {

            if (isVertical) {

                view = LayoutInflater.from(context).inflate(R.layout.list_user_photo_2, parent, false);

            } else {

                view = LayoutInflater.from(context).inflate(R.layout.list_user_photo_horizontal_scrolling_2, parent, false);

            }

        } else {

            if (isVertical) {

                view = LayoutInflater.from(context).inflate(R.layout.list_user_photo, parent, false);

            } else {

                view = LayoutInflater.from(context).inflate(R.layout.list_user_photo_horizontal_scrolling, parent, false);

            }

        }

        return new UserPhotoAdapter.UserPhotoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull UserPhotoViewHolder holder, int position) {

        UserPhoto userPhoto = userPhotoList.get(position);

        Glide.with(context).load(userPhoto.getUserPhotoDownloadUrl()).apply(new RequestOptions().override(600, 600)).into(holder.userPhotoImageView);

    }


    @Override
    public int getItemCount() {

        return userPhotoList.size();

    }


    public class UserPhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView userPhotoImageView;

        public UserPhotoViewHolder(@NonNull final View itemView) {
            super(itemView);

            userPhotoImageView = itemView.findViewById(R.id.userPhotoImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    UserPhoto userPhoto = userPhotoList.get(getAdapterPosition());
                    goToFullScreenImage(Uri.parse(userPhoto.getUserPhotoDownloadUrl()), userPhoto.getObjectId());

                }
            });

        }

    }


    public void goToFullScreenImage(Uri uri, String userPhotoId) {

        Intent intentFullScreenImage = new Intent(context, FullScreenImage.class);

        intentFullScreenImage.setData(uri);
        intentFullScreenImage.putExtra("userPhotoId", userPhotoId);
        intentFullScreenImage.putExtra("from", from);
        intentFullScreenImage.putExtra("userId", userId);

        context.startActivity(intentFullScreenImage);

    }


}
