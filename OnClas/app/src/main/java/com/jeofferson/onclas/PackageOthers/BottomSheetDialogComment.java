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

public class BottomSheetDialogComment extends BottomSheetDialogFragment {


    private BottomSheetDialogComment.BottomSheetDialogListener bottomSheetDialogListener;

    private LinearLayout bottomSheetDialogCommentLinearLayoutEditComment;
    private LinearLayout bottomSheetDialogCommentLinearLayoutDeleteComment;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog_comment, container, false);

        bottomSheetDialogCommentLinearLayoutEditComment = view.findViewById(R.id.bottomSheetDialogCommentLinearLayoutEditComment);
        bottomSheetDialogCommentLinearLayoutDeleteComment = view.findViewById(R.id.bottomSheetDialogCommentLinearLayoutDeleteComment);

        bottomSheetDialogCommentLinearLayoutEditComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheetDialogListener.onButtonClicked("Edit Comment");
                dismiss();

            }
        });

        bottomSheetDialogCommentLinearLayoutDeleteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheetDialogListener.onButtonClicked("Delete Comment");
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

            bottomSheetDialogListener = (BottomSheetDialogComment.BottomSheetDialogListener) context;

        } catch (ClassCastException e) {

            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");

        }

    }


}
