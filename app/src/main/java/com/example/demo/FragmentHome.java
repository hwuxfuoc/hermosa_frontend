package com.example.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter; // Adapter
    private List<Product> allProducts; // Tất cả sản phẩm (hiện tại theo category)
    private List<Product> filteredProducts; // Danh sách sau khi lọc tìm kiếm
    private Button button_cake, button_drink, button_food; // 3 nút danh mục
    private SearchView searchView; // Thanh tìm kiếm
    private static final int COLOR_SELECTED = 0xFFEB4341; // Màu nền khi nút được chọn
    private static final int COLOR_UNSELECTED = 0xFFBDBDBD; // Màu nền khi nút không được chọn
    private String currentCategory = "cake"; // Danh mục hiện tại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ
        recyclerView = view.findViewById(R.id.recycler_products);
        button_cake = view.findViewById(R.id.button_cake);
        button_drink = view.findViewById(R.id.button_drink);
        button_food = view.findViewById(R.id.button_food);
        searchView = view.findViewById(R.id.search_view);

        ProductData.initializeData();

        // Load mặc định
        allProducts = ProductData.getProductsByCategory("cake");
        filteredProducts = new ArrayList<>(allProducts);

        adapter = new ProductAdapter(getContext(), filteredProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

        // Nút danh mục
        button_cake.setOnClickListener(v -> loadCategory("cake", button_cake));
        button_drink.setOnClickListener(v -> loadCategory("drink", button_drink));
        button_food.setOnClickListener(v -> loadCategory("food", button_food));

        // Tìm kiếm theo tên sản phẩm
        setupSearch();

        return view;
    }

    private void setupSearch() {
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    private void filterList(String text) {
        filteredProducts.clear();

        if (text.isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            String lowerText = text.toLowerCase();
            for (Product product : allProducts) {
                if (product.getName().toLowerCase().contains(lowerText)) {
                    filteredProducts.add(product);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void loadCategory(String category, Button selectedButton) {
        currentCategory = category;

        allProducts = ProductData.getProductsByCategory(category);
        filteredProducts.clear();
        filteredProducts.addAll(allProducts);
        adapter.notifyDataSetChanged();

        setActiveButton(selectedButton);
        searchView.setQuery("", false); // reset thanh tìm kiếm
        searchView.clearFocus();
    }

    private void setActiveButton(Button activeButton) {
        button_cake.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_UNSELECTED));
        button_drink.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_UNSELECTED));
        button_food.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_UNSELECTED));

        activeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_SELECTED));
    }
}
