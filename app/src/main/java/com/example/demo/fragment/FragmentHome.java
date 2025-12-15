package com.example.demo.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.demo.adapters.AlsoLikeAdapter;
import com.example.demo.adapters.AlsoViewAdapter;
import com.example.demo.adapters.BannerAdapter;
import com.example.demo.adapters.BestSellerAdapter;
import com.example.demo.adapters.ProductAdapter;
import com.example.demo.adapters.RecommendedAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.FavoriteListResponse;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.Product;
import com.example.demo.models.RecommendationResponse;
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
    private RecyclerView recyclerProducts, recyclerBestSeller;
    private RecyclerView recyclerAlsoLike, recyclerAlsoView;
    private ProductAdapter productAdapter;
    private BestSellerAdapter bestSellerAdapter;
    private AlsoLikeAdapter alsoLikeAdapter;
    private AlsoViewAdapter alsoViewAdapter;

    private ApiService apiService;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> currentProductList = new ArrayList<>();
    private List<Product> bestSellerList = new ArrayList<>();
    private List<Product> alsoLikeList = new ArrayList<>();
    private List<Product> alsoViewList = new ArrayList<>();

    private ImageView tabCake, tabDrink, tabFood, tabFavorite;
    private String currentCategory = "cake";
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Ánh xạ
        recyclerProducts = rootView.findViewById(R.id.recycler_products);
        recyclerBestSeller = rootView.findViewById(R.id.recycler_best_seller);
        recyclerAlsoLike = rootView.findViewById(R.id.recycler_alsolike);
        recyclerAlsoView = rootView.findViewById(R.id.recycler_alsoview);

        SearchView searchView = rootView.findViewById(R.id.search_view);
        ViewPager2 bannerViewPager = rootView.findViewById(R.id.bannerViewPager);
        CircleIndicator3 indicator = rootView.findViewById(R.id.bannerIndicator);

        tabCake = rootView.findViewById(R.id.tab_cake);
        tabDrink = rootView.findViewById(R.id.tab_drink);
        tabFood = rootView.findViewById(R.id.tab_food);
        tabFavorite = rootView.findViewById(R.id.tab_favorite);

        setupRecyclerViews();
        setupTabs();
        setupSearch(searchView);

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

        return rootView;
    }

    private void setupRecyclerViews() {
        // Best Seller - ngang
        recyclerBestSeller.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bestSellerAdapter = new BestSellerAdapter(requireContext(), bestSellerList);
        recyclerBestSeller.setAdapter(bestSellerAdapter);

        // Also Like
        recyclerAlsoLike.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        alsoLikeAdapter = new AlsoLikeAdapter(requireContext(), alsoLikeList, this::showAddToCartSheet);
        recyclerAlsoLike.setAdapter(alsoLikeAdapter);

        // Also View
        recyclerAlsoView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        alsoViewAdapter = new AlsoViewAdapter(requireContext(), alsoViewList, this::showAddToCartSheet);
        recyclerAlsoView.setAdapter(alsoViewAdapter);

        // Danh sách chính - grid 2 cột
        recyclerProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(requireContext(), currentProductList);
        recyclerProducts.setAdapter(productAdapter);
    }

    private void showAddToCartSheet(Product product) {
        AddToCartBottomSheet sheet = AddToCartBottomSheet.newInstance(product);
        sheet.show(((AppCompatActivity) requireContext()).getSupportFragmentManager(), "AddToCart");
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

    private void loadRecommendations() {
        String userID = SessionManager.getUserID(requireContext());

        LinearLayout alsoLikeContainer = rootView.findViewById(R.id.recycler_alsolike_container);
        LinearLayout alsoViewContainer = rootView.findViewById(R.id.recycler_alsoview_container);

        if (userID == null || userID.isEmpty() || "unknown".equals(userID)) {
            alsoLikeContainer.setVisibility(View.GONE);
            alsoViewContainer.setVisibility(View.GONE);
            return;
        }

        // Also Like
        apiService.getAlsoLike(userID).enqueue(new Callback<RecommendationResponse>() {
            @Override
            public void onResponse(Call<RecommendationResponse> call, Response<RecommendationResponse> response) {
                Log.d("Recommend", "alsoLike RAW RESPONSE: " + response.body());

                if (response.isSuccessful() && response.body() != null
                        && "Success".equals(response.body().getStatus())
                        && response.body().getData() != null
                        && response.body().getData().getData() != null
                        && !response.body().getData().getData().isEmpty()) {

                    alsoLikeList.clear();
                    alsoLikeList.addAll(response.body().getData().getData());

                    Log.d("Recommend", "alsoLike loaded: " + alsoLikeList.size() + " items");
                    for (Product p : alsoLikeList) {
                        Log.d("Recommend", "alsoLike item: name=" + p.getName() +
                                ", price=" + p.getPrice() +
                                ", imageUrl=" + p.getImageUrl() +
                                ", productID=" + p.getProductID());
                    }

                    alsoLikeAdapter.notifyDataSetChanged();
                    alsoLikeContainer.setVisibility(View.VISIBLE);
                } else {
                    alsoLikeContainer.setVisibility(View.GONE);
                    Log.w("Recommend", "alsoLike: No data or failed status");
                }
            }

            @Override
            public void onFailure(Call<RecommendationResponse> call, Throwable t) {
                alsoLikeContainer.setVisibility(View.GONE);
                Log.e("Recommend", "alsoLike API failed: " + t.getMessage());
            }
        });

// Tương tự cho alsoView
        apiService.getAlsoView(userID).enqueue(new Callback<RecommendationResponse>() {
            @Override
            public void onResponse(Call<RecommendationResponse> call, Response<RecommendationResponse> response) {
                Log.d("Recommend", "alsoView RAW RESPONSE: " + response.body());

                if (response.isSuccessful() && response.body() != null
                        && "Success".equals(response.body().getStatus())
                        && response.body().getData() != null
                        && response.body().getData().getData() != null
                        && !response.body().getData().getData().isEmpty()) {

                    alsoViewList.clear();
                    alsoViewList.addAll(response.body().getData().getData());

                    Log.d("Recommend", "alsoView loaded: " + alsoViewList.size() + " items");
                    for (Product p : alsoViewList) {
                        Log.d("Recommend", "alsoView item: name=" + p.getName() +
                                ", price=" + p.getPrice() +
                                ", imageUrl=" + p.getImageUrl() +
                                ", productID=" + p.getProductID());
                    }

                    alsoViewAdapter.notifyDataSetChanged();
                    alsoViewContainer.setVisibility(View.VISIBLE);
                } else {
                    alsoViewContainer.setVisibility(View.GONE);
                    Log.w("Recommend", "alsoView: No data or failed status");
                }
            }

            @Override
            public void onFailure(Call<RecommendationResponse> call, Throwable t) {
                alsoViewContainer.setVisibility(View.GONE);
                Log.e("Recommend", "alsoView API failed: " + t.getMessage());
            }
        });
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

                    loadRecommendations();

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

        if (category.equals("favorite")) {
            String userID = SessionManager.getUserID(requireContext());
            if (userID == null || userID.isEmpty() || "unknown".equals(userID)) {
                currentProductList.clear();
                productAdapter.updateList(currentProductList);
                Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            apiService.getFavoriteList(userID).enqueue(new Callback<FavoriteListResponse>() {
                @Override
                public void onResponse(Call<FavoriteListResponse> call, Response<FavoriteListResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentProductList.clear();
                        for (MenuResponse.MenuItem item : response.body().getData()) {
                            currentProductList.add(Product.fromMenuItem(item));
                        }
                        productAdapter.updateList(currentProductList);
                    }
                }

                @Override
                public void onFailure(Call<FavoriteListResponse> call, Throwable t) {
                    currentProductList.clear();
                    productAdapter.updateList(currentProductList);
                }
            });
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