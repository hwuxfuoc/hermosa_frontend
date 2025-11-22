package com.example.demo.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.ProductData;
import com.example.demo.R;
import com.example.demo.adapters.ProductAdapter;
import com.example.demo.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FragmentFavorite extends Fragment {

    private RecyclerView recyclerProducts;
    private LinearLayout layoutEmpty;
    private ProductAdapter adapter;
    private List<Product> favoriteList = new ArrayList<>();
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerProducts = view.findViewById(R.id.recycler_products);
        layoutEmpty = view.findViewById(R.id.tv_empty_cart);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        prefs = requireContext().getSharedPreferences("favorites", 0);

        recyclerProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new ProductAdapter(requireContext(), favoriteList);
        recyclerProducts.setAdapter(adapter);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        loadFavoritesFromPrefs();

        return view;
    }

    private void loadFavoritesFromPrefs() {
        favoriteList.clear();
        Map<String, ?> allEntries = prefs.getAll();

        // LẤY DANH SÁCH TẤT CẢ 28 MÓN CHỈ 1 LẦN
        List<Product> allProducts = ProductData.getAllProducts();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof Boolean && (Boolean) entry.getValue()) {
                String productId = entry.getKey();

                // TỰ ĐỘNG TÌM TRONG 28 MÓN – KHÔNG CẦN SWITCH CASE
                Product product = allProducts.stream()
                        .filter(p -> productId.equals(p.getProductID()))
                        .findFirst()
                        .orElse(null);

                if (product != null) {
                    favoriteList.add(product);
                }
            }
        }

        adapter.updateList(favoriteList);

        if (favoriteList.isEmpty()) {
            recyclerProducts.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerProducts.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}