package com.jeofferson.onclas.PackageOthers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MyRecyclerView extends RecyclerView {


    private List<View> viewsWhenEmpty = Collections.emptyList();


    private AdapterDataObserver adapterDataObserver = new AdapterDataObserver() {

        @Override
        public void onChanged() {

            toggleViews();

        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {

            toggleViews();

        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {

            toggleViews();

        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {

            toggleViews();

        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {

            toggleViews();

        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {

            toggleViews();

        }

    };

    private void toggleViews() {

        if (getAdapter() != null && !viewsWhenEmpty.isEmpty()) {

            if (getAdapter().getItemCount() == 0) {

                setVisibility(View.GONE);

                Util.showViews(viewsWhenEmpty);

            } else {

                setVisibility(View.VISIBLE);

                Util.hideViews(viewsWhenEmpty);

            }

        }

    }


    public MyRecyclerView(@NonNull Context context) {
        super(context);
    }


    public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);

        if (adapter != null) {

            adapter.registerAdapterDataObserver(adapterDataObserver);

        }

        adapterDataObserver.onChanged();

    }


    public void showIfEmpty(View ...views) {

        viewsWhenEmpty = Arrays.asList(views);

    }


}
