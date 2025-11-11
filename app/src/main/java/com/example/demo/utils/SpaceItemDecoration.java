package com.example.demo.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int space; // Khoảng cách (px)

    public SpaceItemDecoration(int spaceInPx) {
        this.space = spaceInPx;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        // Thêm khoảng cách bên phải cho mỗi item (ngoại trừ item cuối)
        outRect.right = space;

        // Nếu muốn khoảng cách đều (trái và phải), thêm dòng này:
        // outRect.left = space;

        // Loại bỏ khoảng cách phải cho item cuối để tránh khoảng trống dư thừa
        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
            outRect.right = 0;
        }
    }
}