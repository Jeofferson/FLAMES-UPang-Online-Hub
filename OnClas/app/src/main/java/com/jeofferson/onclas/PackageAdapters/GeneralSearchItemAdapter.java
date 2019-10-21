package com.jeofferson.onclas.PackageAdapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jeofferson.onclas.PackageActivities.UserActivity;
import com.jeofferson.onclas.PackageObjectModel.GeneralSearchItem;
import com.jeofferson.onclas.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GeneralSearchItemAdapter extends RecyclerView.Adapter<GeneralSearchItemAdapter.GeneralSearchItemViewHolder> {


    private Context context;

    private List<GeneralSearchItem> generalSearchItemList = new ArrayList<>();


    public GeneralSearchItemAdapter() {}
    public GeneralSearchItemAdapter(List<GeneralSearchItem> generalSearchItemList) {
        this.generalSearchItemList = generalSearchItemList;
    }


    @NonNull
    @Override
    public GeneralSearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_general_search_item, parent, false);

        return new GeneralSearchItemViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull final GeneralSearchItemViewHolder holder, int position) {

        GeneralSearchItem generalSearchItem = generalSearchItemList.get(position);

        Glide.with(context).load(generalSearchItem.getItemPicture()).placeholder(R.drawable.placeholder).into(holder.generalSearchItemCircleImageViewItemPicture);
        holder.generalSearchItemTextViewItemName.setText(generalSearchItem.getItemName());
        holder.generalSearchItemTextViewFullType.setText(generalSearchItem.getFullType());

    }


    @Override
    public int getItemCount() {

        return generalSearchItemList.size();

    }


    public class GeneralSearchItemViewHolder extends RecyclerView.ViewHolder {

        CircleImageView generalSearchItemCircleImageViewItemPicture;
        TextView generalSearchItemTextViewItemName;
        TextView generalSearchItemTextViewFullType;

        public GeneralSearchItemViewHolder(@NonNull View itemView) {
            super(itemView);

            generalSearchItemCircleImageViewItemPicture = itemView.findViewById(R.id.generalSearchItemCircleImageViewItemPicture);
            generalSearchItemTextViewItemName = itemView.findViewById(R.id.generalSearchItemTextViewItemName);
            generalSearchItemTextViewFullType = itemView.findViewById(R.id.generalSearchItemTextViewFullType);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    goToUserActivity(0, generalSearchItemList.get(getAdapterPosition()).getItemId());

                }
            });

        }

    }


    public void goToUserActivity(int initialTabIndex, String userId) {

        Intent intentUserActivity = new Intent(context, UserActivity.class);
        intentUserActivity.putExtra("initialTabIndex", initialTabIndex);
        intentUserActivity.putExtra("userId", userId);
        context.startActivity(intentUserActivity);

    }


}
