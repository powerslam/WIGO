package com.capstone.whereigo;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// RecyclerView에 padding(간격) 넣으려고 쓴 클래스
public class ItemSpacingDecoration extends RecyclerView.ItemDecoration {

    private final int space;

    public ItemSpacingDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        outRect.bottom = space;
    }
}
