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
import com.jeofferson.onclas.PackageObjectModel.ReplyHolder;
import com.jeofferson.onclas.R;

public class BottomSheetDialogReply extends BottomSheetDialogFragment {


    private BottomSheetDialogReply.BottomSheetDialogListener bottomSheetDialogListener;

    private LinearLayout bottomSheetDialogReplyLinearLayoutEditReply;
    private LinearLayout bottomSheetDialogReplyLinearLayoutDeleteReply;

    private int position;
    private ReplyHolder replyHolder;


    public BottomSheetDialogReply(int position, ReplyHolder replyHolder) {
        this.position = position;
        this.replyHolder = replyHolder;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog_reply, container, false);

        bottomSheetDialogReplyLinearLayoutEditReply = view.findViewById(R.id.bottomSheetDialogReplyLinearLayoutEditReply);
        bottomSheetDialogReplyLinearLayoutDeleteReply = view.findViewById(R.id.bottomSheetDialogReplyLinearLayoutDeleteReply);

        bottomSheetDialogReplyLinearLayoutEditReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheetDialogListener.onButtonClicked("Edit Reply", position, replyHolder);
                dismiss();

            }
        });

        bottomSheetDialogReplyLinearLayoutDeleteReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheetDialogListener.onButtonClicked("Delete Reply", position, replyHolder);
                dismiss();

            }
        });

        return view;
    }


    public interface BottomSheetDialogListener {

        void onButtonClicked(String text, int position, ReplyHolder replyHolder);

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {

            bottomSheetDialogListener = (BottomSheetDialogReply.BottomSheetDialogListener) context;

        } catch (ClassCastException e) {

            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");

        }

    }


}
