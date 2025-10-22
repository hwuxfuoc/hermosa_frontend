package com.example.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentCart extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter adapter; // SỬA: Đổi từ ProductAdapter sang CartAdapter

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recycler_cart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // SỬA: Sử dụng CartAdapter mới
        adapter = new CartAdapter(getContext(), ProductData.getCartList()); // Sử dụng hàm getter an toàn
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload cart khi fragment hiển thị lại
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            // TODO: Cập nhật lại tổng tiền ở đây
        }
    }
}
