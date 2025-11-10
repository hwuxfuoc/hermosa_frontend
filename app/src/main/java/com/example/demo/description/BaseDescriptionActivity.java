package com.example.demo.description;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.R;
import com.example.demo.models.Product;

public abstract class BaseDescriptionActivity extends AppCompatActivity {

    protected Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        // Lấy dữ liệu từ Intent
        String productID = getIntent().getStringExtra("productID");
        String name = getIntent().getStringExtra("name");
        String price = getIntent().getStringExtra("price");
        int imageResId = getIntent().getIntExtra("imageResId", 0);
        String description = getIntent().getStringExtra("description");
        String category = getIntent().getStringExtra("category");

        product = new Product(name, price, imageResId, 0, description, category);

        // Ánh xạ chung
        ImageView imgProduct = findViewById(getImageViewId());
        TextView tvName = findViewById(getNameTextViewId());
        TextView tvPrice = findViewById(getPriceTextViewId());
        TextView tvDesc = findViewById(getDescriptionTextViewId());
        ImageButton btnBack = findViewById(R.id.icon_return_arrow);
        ImageButton btnFav = findViewById(R.id.icon_favorite);

        // Gán dữ liệu
        tvName.setText(name);
        tvPrice.setText(price);
        if (imageResId != 0) imgProduct.setImageResource(imageResId);
        tvDesc.setText(description);

        btnBack.setOnClickListener(v -> finish());

        // Favorite - dùng tên sản phẩm làm key
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        boolean[] isFav = {prefs.getBoolean(name, false)}; // Dùng mảng để biến thành effectively final
        btnFav.setImageResource(isFav[0] ? R.drawable.icon_favorite_fill : R.drawable.icon_favorite_empty);

        btnFav.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            if (isFav[0]) {
                btnFav.setImageResource(R.drawable.icon_favorite_empty);
                editor.remove(name);
                Toast.makeText(this, name + " đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                btnFav.setImageResource(R.drawable.icon_favorite_fill);
                editor.putBoolean(name, true);
                Toast.makeText(this, name + " đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            }
            editor.apply();
            isFav[0] = !isFav[0]; // Cập nhật giá trị trong mảng
        });

        // Setup nút Add to Cart (mở BottomSheet)
        setupAddToCart();
    }

    // Các method abstract để mỗi class con định nghĩa
    protected abstract int getLayoutResId();
    protected abstract int getImageViewId();
    protected abstract int getNameTextViewId();
    protected abstract int getPriceTextViewId();
    protected abstract int getDescriptionTextViewId();

    // Override ở class con để setOnClick cho nút add_to_cart
    protected void setupAddToCart() {
        // Mặc định không làm gì
    }
}