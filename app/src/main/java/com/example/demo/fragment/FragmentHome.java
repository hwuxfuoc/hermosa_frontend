package com.example.demo.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.demo.AddToCartBottomSheet;
import com.example.demo.ConfirmOrderActivity;
import com.example.demo.R;
import com.example.demo.adapters.BannerAdapter;
import com.example.demo.adapters.BestSellerAdapter;
import com.example.demo.adapters.ProductAdapter;
import com.example.demo.adapters.RecommendedAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.Product;
import com.example.demo.models.TopSellingResponse;
import com.example.demo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHome extends Fragment {

    private TextView tvUsername;
    private RecyclerView recyclerProducts, recyclerBestSeller, recyclerRecommend;
    private ProductAdapter productAdapter;
    private BestSellerAdapter bestSellerAdapter;
    private RecommendedAdapter recommendedAdapter;
    private ApiService apiService;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> currentProductList = new ArrayList<>();
    private List<Product> bestSellerList = new ArrayList<>();
    private List<Product> recommendList = new ArrayList<>();

    private ImageView tabCake, tabDrink, tabFood, tabFavorite;
    private String currentCategory = "cake";
    private SharedPreferences favoritePrefs;

    private void initFavoritePrefs() {
        favoritePrefs = requireContext().getSharedPreferences("favorites", 0);
    }

    private void loadFavoriteProducts() {
        currentProductList.clear();

        Map<String, ?> allEntries = favoritePrefs.getAll();
        List<String> favoriteIds = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof Boolean && (Boolean) entry.getValue()) {
                favoriteIds.add(entry.getKey());
            }
        }

        if (favoriteIds.isEmpty()) {
            productAdapter.updateList(currentProductList);
            return;
        }

        // Lấy tất cả sản phẩm từ allProducts, lọc theo ID yêu thích
        for (Product p : allProducts) {
            if (favoriteIds.contains(p.getId())) {
                currentProductList.add(p);
            }
        }

        productAdapter.updateList(currentProductList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Ánh xạ
        recyclerProducts = view.findViewById(R.id.recycler_products);
        recyclerBestSeller = view.findViewById(R.id.recycler_best_seller);
        recyclerRecommend = view.findViewById(R.id.recycler_recommend);
        SearchView searchView = view.findViewById(R.id.search_view);
        ViewPager2 bannerViewPager = view.findViewById(R.id.bannerViewPager);
        CircleIndicator3 indicator = view.findViewById(R.id.bannerIndicator);

        tabCake = view.findViewById(R.id.tab_cake);
        tabDrink = view.findViewById(R.id.tab_drink);
        tabFood = view.findViewById(R.id.tab_food);
        tabFavorite = view.findViewById(R.id.tab_favorite);

        setupRecyclerViews();
        setupTabs();
        setupSearch(searchView);
        initFavoritePrefs();

        // GỌI API LẤY TẤT CẢ SẢN PHẨM
        if (allProducts.isEmpty()) {
            loadAllProducts();
        }
        loadTopSelling();

        List<Integer> banners = new ArrayList<>();
        banners.add(R.drawable.banner1);
        banners.add(R.drawable.banner2);
        banners.add(R.drawable.banner3);
        banners.add(R.drawable.banner4);

        BannerAdapter adapter = new BannerAdapter(banners);
        bannerViewPager.setAdapter(adapter);
        indicator.setViewPager(bannerViewPager);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int current = bannerViewPager.getCurrentItem();
                bannerViewPager.setCurrentItem((current + 1) % banners.size(), true);
                handler.postDelayed(this, 3000); // 3 giây
            }
        };
        handler.postDelayed(runnable, 3000);

        return view;
    }

    private void loadTopSelling() {
        apiService.getTopSelling().enqueue(new Callback<TopSellingResponse>() {
            @Override
            public void onResponse(Call<TopSellingResponse> call, Response<TopSellingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("Success".equals(response.body().getStatus())) {
                        List<Product> topProducts = response.body().getData();
                        if (topProducts == null) topProducts = new ArrayList<>();

                        bestSellerList.clear();
                        bestSellerList.addAll(topProducts);
                        bestSellerAdapter.notifyDataSetChanged(); // hoặc dùng updateList() nếu có

                        Log.d("BestSeller", "Loaded " + topProducts.size() + " best sellers");
                    }
                }
            }

            @Override
            public void onFailure(Call<TopSellingResponse> call, Throwable t) {
                Log.e("BestSeller", "Failed: " + t.getMessage());
                Toast.makeText(requireContext(), "Không tải được Best Seller", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerViews() {
        // Best Seller - ngang
        recyclerBestSeller.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bestSellerAdapter = new BestSellerAdapter(requireContext(), bestSellerList);
        recyclerBestSeller.setAdapter(bestSellerAdapter);

        // Recommend - ngang
        recyclerRecommend.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new RecommendedAdapter(requireContext(), recommendList, product -> {
            AddToCartBottomSheet sheet = AddToCartBottomSheet.newInstance(product);
            sheet.show(((AppCompatActivity) requireContext()).getSupportFragmentManager(), "AddToCart");
        });
        recyclerRecommend.setAdapter(recommendedAdapter);

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

                    // Cập nhật Recommend
                    recommendList.clear();
                    recommendList.addAll(allProducts.subList(0, Math.min(10, allProducts.size())));
                    recommendedAdapter.notifyDataSetChanged();

                    filterByCategory("cake");
                    requireView().post(() -> {
                        resetTabs();
                        tabCake.setBackgroundResource(R.drawable.button_cake);
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

        if (currentCategory.equals("favorite")) {
            loadFavoriteProducts();
        } else {
            for (Product p : allProducts) {
                String normalizedCat = Product.normalizeCategory(p.getCategory());
                if (currentCategory.equals(normalizedCat)) {
                    currentProductList.add(p);
                }
            }
            productAdapter.updateList(currentProductList);
        }
    }

    private void setupTabs() {
        View.OnClickListener listener = v -> {
            resetTabs();

            int tabId = v.getId();
            if (tabId == R.id.tab_cake) {
                tabCake.setBackgroundResource(R.drawable.button_cake);
                filterByCategory("cake");
            } else if (tabId == R.id.tab_drink) {
                tabDrink.setBackgroundResource(R.drawable.button_drink);
                filterByCategory("drink");
            } else if (tabId == R.id.tab_food) {
                tabFood.setBackgroundResource(R.drawable.button_food);
                filterByCategory("food");
            } else if (tabId == R.id.tab_favorite) {
                tabFavorite.setBackgroundResource(R.drawable.button_favorite);
                filterByCategory("favorite");
            }
            else filterByCategory("cake");
        };

        tabCake.setOnClickListener(listener);
        tabDrink.setOnClickListener(listener);
        tabFood.setOnClickListener(listener);
        tabFavorite.setOnClickListener(listener);
    }

    private void resetTabs() {
        tabCake.setBackgroundResource(R.drawable.button_cake);
        tabDrink.setBackgroundResource(R.drawable.button_drink);
        tabFood.setBackgroundResource(R.drawable.button_food);
        tabFavorite.setBackgroundResource(R.drawable.button_favorite);
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


    @Override
    public void onResume() {
        super.onResume();
        if (currentCategory.equals("favorite")) {
            filterByCategory("favorite");
        }
        if (allProducts.isEmpty()) {
            loadAllProducts();
        }
    }
}