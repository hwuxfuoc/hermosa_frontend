package com.example.demo.description;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.adapters.ProductReviewDisplayAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.fragment.FragmentFavorite;
import com.example.demo.models.Product;
import com.example.demo.models.Review;
import com.example.demo.models.ReviewResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseDescriptionActivity extends AppCompatActivity {

    protected Product product;
    private ImageButton btnFav;

    private ApiService apiService;
    private RecyclerView rvProductReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        apiService = ApiClient.getClient().create(ApiService.class);

        product = (Product) getIntent().getSerializableExtra("product");

        if (product == null) {
            String productID = getIntent().getStringExtra("productID");
            String name = getIntent().getStringExtra("name");
            String price = getIntent().getStringExtra("price");
            int imageResId = getIntent().getIntExtra("imageResId", 0);
            String description = getIntent().getStringExtra("description");
            String category = getIntent().getStringExtra("category");

            product = new Product(name, price, imageResId, 0, description, category);
            if (productID != null) {
                product.setProductID(productID);
            }
        }

        if (product.getProductID() == null || product.getProductID().equals("UNKNOWN")) {
            product.setProductID("TEMP_" + System.currentTimeMillis());
        }

        ImageView imgProduct = findViewById(getImageViewId());
        TextView tvName = findViewById(getNameTextViewId());
        TextView tvPrice = findViewById(getPriceTextViewId());
        TextView tvDesc = findViewById(getDescriptionTextViewId());
        ImageButton btnBack = findViewById(R.id.icon_return_arrow);
        btnFav = findViewById(R.id.icon_favorite);

        rvProductReviews = findViewById(R.id.rvProductReviews);
        if (rvProductReviews != null) {
            rvProductReviews.setLayoutManager(new LinearLayoutManager(this));
            rvProductReviews.setNestedScrollingEnabled(false);
            rvProductReviews.setAdapter(new ProductReviewDisplayAdapter(new ArrayList<>())); // khởi tạo rỗng
        }

        tvName.setText(product.getName());
        tvPrice.setText(formatPrice(product.getPrice()) + " đ");
        tvDesc.setText(product.getDescription() != null ? product.getDescription() : "Đang cập nhật...");

        // Load ảnh
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this).load(product.getImageUrl()).into(imgProduct);
        } else if (product.getImageResId() != 0) {
            imgProduct.setImageResource(product.getImageResId());
        }

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);

        String productId = product.getId();
        if (productId == null || productId.isEmpty()) {
            productId = product.getProductID();
        }
        final String favoriteKey = productId;

        btnFav.setImageResource(
                prefs.getBoolean(favoriteKey, false)
                        ? R.drawable.icon_favorite_fill
                        : R.drawable.icon_favorite_empty
        );

        btnFav.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            boolean isFav = prefs.getBoolean(favoriteKey, false);

            if (isFav) {
                editor.remove(favoriteKey);
                btnFav.setImageResource(R.drawable.icon_favorite_empty);
                Toast.makeText(this, product.getName() + " đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                editor.putBoolean(favoriteKey, true);
                btnFav.setImageResource(R.drawable.icon_favorite_fill);
                Toast.makeText(this, product.getName() + " đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            }
            editor.apply();

            // RELOAD TAB YÊU THÍCH NẾU ĐANG MỞ
            Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (frag instanceof FragmentFavorite) {
                ((FragmentFavorite) frag).reloadFavorites();
            }
        });

        setupAddToCart();
    }

    private void loadProductReviews(String productID) {
        if (rvProductReviews == null || apiService == null) return;

        apiService.getProductReviews(productID).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getReviews() != null) {
                    rvProductReviews.setAdapter(new ProductReviewDisplayAdapter(response.body().getReviews()));
                } else {
                    rvProductReviews.setAdapter(new ProductReviewDisplayAdapter(new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                // Backend chưa có hoặc lỗi mạng → vẫn để trống, không crash
                rvProductReviews.setAdapter(new ProductReviewDisplayAdapter(new ArrayList<>()));
            }
        });
    }

    private String formatPrice(String price) {
        if (price == null || price.isEmpty()) return "0";
        try {
            long p = Long.parseLong(price.replaceAll("[^0-9]", ""));
            return String.format("%,d", p);
        } catch (Exception e) {
            return price;
        }
    }

    protected abstract int getLayoutResId();
    protected abstract int getImageViewId();
    protected abstract int getNameTextViewId();
    protected abstract int getPriceTextViewId();
    protected abstract int getDescriptionTextViewId();
    protected abstract void setupAddToCart();
}