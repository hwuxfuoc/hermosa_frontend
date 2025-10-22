package com.example.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> favoriteList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        recyclerView = view.findViewById(R.id.recycler_favorite);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        loadFavorites();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites(); // cập nhật mỗi lần quay lại
    }

    private void loadFavorites() {
        favoriteList.clear();
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("favorites", getContext().MODE_PRIVATE);

        for (Product p : ProductData.getAllProducts()) {
            if (prefs.getBoolean(p.getName(), false)) {
                favoriteList.add(p);
            }
        }

        // tạo adapter theo constructor (Context, List<Product>)
        adapter = new ProductAdapter(getContext(), favoriteList);
        recyclerView.setAdapter(adapter);
    }
}
