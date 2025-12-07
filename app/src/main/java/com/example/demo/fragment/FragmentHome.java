package com.example.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.adapters.BestSellerAdapter;
import com.example.demo.adapters.ProductAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.Product;
import com.example.demo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHome extends Fragment {

    private TextView tvUsername;
    private RecyclerView recyclerProducts, recyclerBestSeller;
    private ProductAdapter productAdapter;
    private BestSellerAdapter bestSellerAdapter;
    private ApiService apiService;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> currentProductList = new ArrayList<>();
    private List<Product> bestSellerList = new ArrayList<>();

    private TextView tabCake, tabDrink, tabFood;
    private String currentCategory = "cake";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Ánh xạ
        tvUsername = view.findViewById(R.id.username_text);
        recyclerProducts = view.findViewById(R.id.recycler_products);
        recyclerBestSeller = view.findViewById(R.id.recycler_best_seller);
        SearchView searchView = view.findViewById(R.id.search_view);
        tabCake = view.findViewById(R.id.tab_cake);
        tabDrink = view.findViewById(R.id.tab_drink);
        tabFood = view.findViewById(R.id.tab_food);

        updateUserInfo();
        setupRecyclerViews();
        setupTabs();
        setupSearch(searchView);

        // GỌI API LẤY TẤT CẢ SẢN PHẨM
        if (allProducts.isEmpty()) {
            loadAllProducts();
        }

        return view;
    }

    private void setupRecyclerViews() {
        // Best Seller - ngang
        recyclerBestSeller.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bestSellerAdapter = new BestSellerAdapter(requireContext(), bestSellerList);
        recyclerBestSeller.setAdapter(bestSellerAdapter);

        // Danh sách chính - grid 2 cột
        recyclerProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(requireContext(), currentProductList);
        recyclerProducts.setAdapter(productAdapter);
    }

    private void loadAllProducts() {
        apiService.getAllProducts().enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    List<MenuResponse.MenuItem> menuItems = response.body().getData();

                    allProducts.clear();
                    for (MenuResponse.MenuItem item : menuItems) {
                        Product p = Product.fromMenuItem(item);
                        allProducts.add(p);
                    }

                    // Cập nhật Best Seller
                    bestSellerList.clear();
                    bestSellerList.addAll(allProducts.subList(0, Math.min(10, allProducts.size())));
                    bestSellerAdapter.notifyDataSetChanged();

                    filterByCategory("cake");
                    requireView().post(() -> {
                        resetTabs();
                        tabCake.setBackgroundResource(R.drawable.tab_selected_bg);
                        tabCake.setTextColor(0xFFEB4341);
                    });
                } else {
                    Toast.makeText(requireContext(), "Lỗi server: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterByCategory(String category) {
        currentCategory = category.toLowerCase();
        currentProductList.clear();

        for (Product p : allProducts) {
            String normalizedCat = Product.normalizeCategory(p.getCategory());
            if (currentCategory.equals(normalizedCat)) {
                currentProductList.add(p);
            }
        }

        productAdapter.updateList(currentProductList);
    }

    private void setupTabs() {
        View.OnClickListener listener = v -> {
            resetTabs();
            TextView tab = (TextView) v;
            tab.setBackgroundResource(R.drawable.tab_selected_bg);
            tab.setTextColor(0xFFEB4341);

            if (v.getId() == R.id.tab_drink) filterByCategory("drink");
            else if (v.getId() == R.id.tab_food) filterByCategory("food");
            else filterByCategory("cake");
        };

        tabCake.setOnClickListener(listener);
        tabDrink.setOnClickListener(listener);
        tabFood.setOnClickListener(listener);
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

    private void setupSearch(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        List<Product> filtered = new ArrayList<>();
        String query = text.toLowerCase().trim();

        if (query.isEmpty()) {
            filterByCategory(currentCategory);
            return;
        }

        for (Product p : allProducts) {
            String normalizedCat = Product.normalizeCategory(p.getCategory());
            boolean matchCategory = currentCategory.equals(normalizedCat);
            boolean matchName = p.getName().toLowerCase().contains(query);

            if (matchCategory && matchName) {
                filtered.add(p);
            }
        }
        productAdapter.updateList(filtered);
    }

    private void updateUserInfo() {
        String userName = SessionManager.getUserName(requireContext());
        tvUsername.setText(userName != null && !userName.isEmpty() ? userName : "Khách");
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserInfo();
        if (allProducts.isEmpty()) {
            loadAllProducts();
        }
    }
}
