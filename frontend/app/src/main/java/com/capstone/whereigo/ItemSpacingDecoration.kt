package com.capstone.whereigo

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// Recycle view 에 padding 넣으려고 쓴거
class ItemSpacingDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = space
    }
}
