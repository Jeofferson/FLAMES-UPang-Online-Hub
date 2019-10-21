package com.jeofferson.onclas.PackageOthers;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jeofferson.onclas.R;

public class BottomSheetDialogUpdate extends BottomSheetDialogFragment {


    private BottomSheetDialogListener bottomSheetDialogListener;

    private LinearLayout bottomSheetDialogUpdateLinearLayoutEditPost;
    private LinearLayout bottomSheetDialogUpdateLinearLayoutDeletePost;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog_update, container, false);

        bottomSheetDialogUpdateLinearLayoutEditPost = view.findViewById(R.id.bottomSheetDialogUpdateLinearLayoutEditPost);
        bottomSheetDialogUpdateLinearLayoutDeletePost = view.findViewById(R.id.bottomSheetDialogUpdateLinearLayoutDeletePost);

        bottomSheetDialogUpdateLinearLayoutEditPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheetDialogListener.onButtonClicked("Edit Post");
                dismiss();

            }
        });

        bottomSheetDialogUpdateLinearLayoutDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheetDialogListener.onButtonClicked("Delete Post");
                dismiss();

            }
        });

        return view;
    }


    public interface BottomSheetDialogListener {

        void onButtonClicked(String text);

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {

            bottomSheetDialogListener = (BottomSheetDialogListener) context;

        } catch (ClassCastException e) {

            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");

        }

    }


}
