package com.example.demo.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.adapters.ProductAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        loadFavoritesFromApi();

        return view;
    }

    public void reloadFavorites() {
        loadFavoritesFromApi();
    }

    private void loadFavoritesFromApi() {
        favoriteList.clear();

        Map<String, ?> allEntries = prefs.getAll();
        List<String> favoriteIds = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof Boolean && (Boolean) entry.getValue()) {
                favoriteIds.add(entry.getKey());
            }
        }

        Log.d("FAV", "Yêu thích IDs: " + favoriteIds);

        if (favoriteIds.isEmpty()) {
            showEmptyState();
            return;
        }

        ApiClient.getClient().create(ApiService.class)
                .getAllProducts()
                .enqueue(new Callback<MenuResponse>() {
                    @Override
                    public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<MenuResponse.MenuItem> allItems = response.body().getData();
                            List<Product> newList = new ArrayList<>();

                            for (MenuResponse.MenuItem item : allItems) {
                                if (favoriteIds.contains(item.getId())) {
                                    Product product = Product.fromMenuItem(item);
                                    newList.add(product);
                                }
                            }

                            SharedPreferences.Editor editor = prefs.edit();
                            for (String id : favoriteIds) {
                                boolean exists = newList.stream().anyMatch(p -> p.getId().equals(id));
                                if (!exists) {
                                    editor.remove(id);
                                    Log.w("FAV", "Đã xóa món không tồn tại: " + id);
                                }
                            }
                            editor.apply();

                            favoriteList.clear();
                            favoriteList.addAll(newList);
                            adapter.updateList(favoriteList);
                            updateUI();

                            Log.d("FAV", "Load thành công: " + favoriteList.size() + " món");
                        } else {
                            Toast.makeText(requireContext(), "Lỗi server", Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(Call<MenuResponse> call, Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        }
                        showEmptyState();
                    }
                });
    }

    private void updateUI() {
        if (favoriteList.isEmpty()) {
            showEmptyState();
        } else {
            recyclerProducts.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        recyclerProducts.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    // XÓA HOÀN TOÀN onResume() → TRÁNH GỌI LẠI API
    // @Override
    // public void onResume() { ... } → ĐÃ BỊ XÓA
}