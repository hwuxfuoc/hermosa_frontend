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
import com.example.demo.models.FavoriteListResponse;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.Product;
import com.example.demo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentFavorite extends Fragment {
    private ApiService apiService;

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

        apiService = ApiClient.getClient().create(ApiService.class);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        loadFavoritesFromApi();

        return view;
    }

    public void reloadFavorites() {
        loadFavoritesFromApi();
    }

    private void loadFavoritesFromApi() {
        String userID = SessionManager.getUserID(requireContext());

        if (userID == null || userID.isEmpty() || "unknown".equals(userID)) {
            showEmptyState();
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getFavoriteList(userID).enqueue(new Callback<FavoriteListResponse>() {
            @Override
            public void onResponse(Call<FavoriteListResponse> call, Response<FavoriteListResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && "Success".equals(response.body().getStatus())) {

                    List<MenuResponse.MenuItem> items = response.body().getData();
                    favoriteList.clear();
                    for (MenuResponse.MenuItem item : items) {
                        favoriteList.add(Product.fromMenuItem(item));
                    }
                    adapter.updateList(favoriteList);
                    updateUI();
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<FavoriteListResponse> call, Throwable t) {
                showEmptyState();
                Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
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