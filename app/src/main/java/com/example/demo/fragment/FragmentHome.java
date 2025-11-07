package com.example.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.ProductAdapter;
import com.example.demo.ProductData;
import com.example.demo.R;
import com.example.demo.Product;

import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private SearchView searchView;

    private TextView tabNearby, tabBestseller, tabRating;
    private String currentTab = "nearby";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ view
        recyclerView = view.findViewById(R.id.recycler_products);
        searchView = view.findViewById(R.id.search_view);
        /*tabNearby = view.findViewById(R.id.tab_nearby);
        tabBestseller = view.findViewById(R.id.tab_bestseller);
        tabRating = view.findViewById(R.id.tab_rating);*/

        // Dữ liệu
        ProductData.initializeData();
        allProducts = ProductData.getProductsByCategory("cake"); // ví dụ mặc định
        filteredProducts = new ArrayList<>(allProducts);

        adapter = new ProductAdapter(getContext(), filteredProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

        // Sự kiện chọn tab
       /* setupTabs();*/

        // Tìm kiếm
        setupSearch();

        return view;
    }

    /*private void setupTabs() {
        View.OnClickListener listener = v -> {
            resetTabs();

            if (v.getId() == R.id.tab_nearby) {
                currentTab = "nearby";
                tabNearby.setTextColor(0xFFEB4341);
                tabNearby.setBackgroundResource(R.drawable.tab_selected_bg);
                // TODO: load dữ liệu theo tab "Gần tôi"
            } else if (v.getId() == R.id.tab_bestseller) {
                currentTab = "bestseller";
                tabBestseller.setTextColor(0xFFEB4341);
                tabBestseller.setBackgroundResource(R.drawable.tab_selected_bg);
                // TODO: load dữ liệu theo tab "Bán chạy"
            } else if (v.getId() == R.id.tab_rating) {
                currentTab = "rating";
                tabRating.setTextColor(0xFFEB4341);
                tabRating.setBackgroundResource(R.drawable.tab_selected_bg);
                // TODO: load dữ liệu theo tab "Đánh giá"
            }
        };

        tabNearby.setOnClickListener(listener);
        tabBestseller.setOnClickListener(listener);
        tabRating.setOnClickListener(listener);
    }*/

    /*private void resetTabs() {
        tabNearby.setTextColor(0xFF9E9E9E);
        tabBestseller.setTextColor(0xFF9E9E9E);
        tabRating.setTextColor(0xFF9E9E9E);

        tabNearby.setBackgroundResource(R.drawable.tab_unselected_bg);
        tabBestseller.setBackgroundResource(R.drawable.tab_unselected_bg);
        tabRating.setBackgroundResource(R.drawable.tab_unselected_bg);
    }*/

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
}
