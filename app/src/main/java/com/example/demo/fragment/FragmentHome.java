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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.adapters.BestSellerAdapter;
import com.example.demo.adapters.ProductAdapter;
import com.example.demo.ProductData;
import com.example.demo.R;
import com.example.demo.models.Product;
import com.google.android.gms.analytics.ecommerce.Product;

import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends Fragment {

    private RecyclerView recyclerProducts, recyclerBestSeller;
    private ProductAdapter productAdapter;
    private BestSellerAdapter bestSellerAdapter;
    private List<Product> currentProductList;

    private TextView tabCake, tabDrink, tabFood;
    private String currentCategory = "cake"; // Biến theo dõi category hiện tại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ
        recyclerProducts = view.findViewById(R.id.recycler_products);
        recyclerBestSeller = view.findViewById(R.id.recycler_best_seller);
        SearchView searchView = view.findViewById(R.id.search_view);
        tabCake = view.findViewById(R.id.tab_cake);
        tabDrink = view.findViewById(R.id.tab_drink);
        tabFood = view.findViewById(R.id.tab_food);

        // Khởi tạo dữ liệu
        ProductData.initializeData();

        // === BEST SELLER - ngang ===
        List<Product> bestSellerList = new ArrayList<>();
        bestSellerList.add(ProductData.getAllProducts().get(0));  // Strawberry Cheese
        bestSellerList.add(ProductData.getAllProducts().get(1));  // Yellow Lemon
        bestSellerList.add(ProductData.getAllProducts().get(8));  // Strawberry Smooth
        bestSellerList.add(ProductData.getAllProducts().get(26)); // Sandwich

        bestSellerAdapter = new BestSellerAdapter(requireContext(), bestSellerList);
        recyclerBestSeller.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerBestSeller.setAdapter(bestSellerAdapter);

        // === DANH SÁCH CHÍNH - grid 2 cột ===
        currentProductList = ProductData.getProductsByCategory(currentCategory);
        productAdapter = new ProductAdapter(requireContext(), currentProductList);
        recyclerProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerProducts.setAdapter(productAdapter);

        // === TAB ===
        setupTabs();

        // === SEARCH ===
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { filter(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { filter(newText); return true; }
        });

        return view;
    }

    private void setupTabs() {
        View.OnClickListener listener = v -> {
            resetTabs();
            TextView tab = (TextView) v;
            tab.setBackgroundResource(R.drawable.tab_selected_bg);
            tab.setTextColor(0xFFEB4341);

            if (v.getId() == R.id.tab_drink) currentCategory = "drink";
            else if (v.getId() == R.id.tab_food) currentCategory = "food";
            else currentCategory = "cake";

            currentProductList = ProductData.getProductsByCategory(currentCategory);
            productAdapter.updateList(currentProductList);
        };
        tabCake.setOnClickListener(listener);
        tabDrink.setOnClickListener(listener);
        tabFood.setOnClickListener(listener);

        tabCake.performClick(); // default
    }

    private void resetTabs() {
        int gray = 0xFF9E9E9E;
        tabCake.setBackgroundResource(R.drawable.tab_unselected_bg);
        tabDrink.setBackgroundResource(R.drawable.tab_unselected_bg);
        tabFood.setBackgroundResource(R.drawable.tab_unselected_bg);
        tabCake.setTextColor(gray);
        tabDrink.setTextColor(gray);
        tabFood.setTextColor(gray);
    }

    private void filter(String text) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : ProductData.getProductsByCategory(currentCategory)) {
            if (p.getName().toLowerCase().contains(text.toLowerCase())) filtered.add(p);
        }
        productAdapter.updateList(filtered);
    }
}