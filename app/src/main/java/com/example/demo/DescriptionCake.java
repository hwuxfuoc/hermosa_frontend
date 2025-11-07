package com.example.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.models.Product;

public class DescriptionCake extends AppCompatActivity {

    private Product selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.description_cake);

        ImageView imageView = findViewById(R.id.cake_image);
        TextView textName = findViewById(R.id.cake_name);
        TextView textPrice = findViewById(R.id.cake_price);
        TextView textDescription = findViewById(R.id.cake_description);
        ImageView arrowIcon = findViewById(R.id.icon_return_arrow);
        ImageButton favoriteIcon = findViewById(R.id.icon_favorite);
        Button addToCartButton = findViewById(R.id.button_add_to_cart);

        // Lấy dữ liệu từ Intent
        String name = getIntent().getStringExtra("name");
        String price = getIntent().getStringExtra("price");
        int imageResId = getIntent().getIntExtra("imageResId", 0);
        String description = getIntent().getStringExtra("description");
        String category = getIntent().getStringExtra("category");

        selectedProduct = new Product(name, price, imageResId, 0, description, category);

        // Gán dữ liệu vào View
        textName.setText(name);
        textPrice.setText(price);
        if (imageResId != 0) imageView.setImageResource(imageResId);
        textDescription.setText(description != null && !description.isEmpty() ? description : "No description available.");

        arrowIcon.setOnClickListener(v -> finish());

        // Xử lý yêu thích
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        final boolean[] isFavorite = {prefs.getBoolean(name, false)};
        favoriteIcon.setImageResource(isFavorite[0] ? R.drawable.icon_favorite_fill : R.drawable.icon_favorite_empty);
        favoriteIcon.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            if (isFavorite[0]) {
                favoriteIcon.setImageResource(R.drawable.icon_favorite_empty);
                editor.remove(name);
                Toast.makeText(this, name + " removed from favorites", Toast.LENGTH_SHORT).show();
            } else {
                favoriteIcon.setImageResource(R.drawable.icon_favorite_fill);
                editor.putBoolean(name, true);
                Toast.makeText(this, name + " added to favorites", Toast.LENGTH_SHORT).show();
            }
            editor.apply();
            isFavorite[0] = !isFavorite[0];
        });
    }
}

