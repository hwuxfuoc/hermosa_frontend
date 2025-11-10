package com.example.demo.description;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.models.Product;

public abstract class BaseDescriptionActivity extends AppCompatActivity {

    protected Product product;
    private ImageButton btnFav;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        // Lấy product từ Intent
        product = (Product) getIntent().getSerializableExtra("product");

        // FIX: Nếu null → tạo mới từ extras (tránh crash)
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

        // Kiểm tra product có productID không
        if (product.getProductID() == null || product.getProductID().equals("UNKNOWN")) {
            product.setProductID("TEMP_" + System.currentTimeMillis()); // Backup ID
        }

        // Ánh xạ
        ImageView imgProduct = findViewById(getImageViewId());
        TextView tvName = findViewById(getNameTextViewId());
        TextView tvPrice = findViewById(getPriceTextViewId());
        TextView tvDesc = findViewById(getDescriptionTextViewId());
        ImageButton btnBack = findViewById(R.id.icon_return_arrow);
        btnFav = findViewById(R.id.icon_favorite);

        // Gán dữ liệu
        tvName.setText(product.getName());
        tvPrice.setText("₫" + formatPrice(product.getPrice()));
        tvDesc.setText(product.getDescription() != null ? product.getDescription() : "Đang cập nhật...");

        // Load ảnh
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this).load(product.getImageUrl()).into(imgProduct);
        } else if (product.getImageResId() != 0) {
            imgProduct.setImageResource(product.getImageResId());
        }

        btnBack.setOnClickListener(v -> finish());

        // === FAVORITE SIÊU ỔN ĐỊNH – KHÔNG LỖI FINAL ===
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        String productId = product.getProductID();

// Khởi tạo trạng thái ban đầu
        btnFav.setImageResource(
                prefs.getBoolean(productId, false)
                        ? R.drawable.icon_favorite_fill
                        : R.drawable.icon_favorite_empty
        );

        btnFav.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            boolean isFav = prefs.getBoolean(productId, false);

            if (isFav) {
                editor.remove(productId);
                btnFav.setImageResource(R.drawable.icon_favorite_empty);
                Toast.makeText(this, product.getName() + " đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                editor.putBoolean(productId, true);
                btnFav.setImageResource(R.drawable.icon_favorite_fill);
                Toast.makeText(this, product.getName() + " đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            }
            editor.apply();
        });

        setupAddToCart();
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

    // Abstract methods
    protected abstract int getLayoutResId();
    protected abstract int getImageViewId();
    protected abstract int getNameTextViewId();
    protected abstract int getPriceTextViewId();
    protected abstract int getDescriptionTextViewId();
    protected abstract void setupAddToCart();
}