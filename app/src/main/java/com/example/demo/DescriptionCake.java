package com.example.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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
        ImageView arrowIcon = findViewById(R.id.arrow_icon);
        ImageButton favoriteIcon = findViewById(R.id.favorite_icon);
        EditText quantityInput = findViewById(R.id.quantity_input);
        Button addToCartButton = findViewById(R.id.button_add_to_cart);
        Button plusButton = findViewById(R.id.button_plus);
        Button minusButton = findViewById(R.id.button_minus);

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
        final boolean[] isFavorite = { prefs.getBoolean(name, false) };
        favoriteIcon.setImageResource(isFavorite[0] ? R.drawable.favorite_icon_fill : R.drawable.favorite_icon_empty);
        favoriteIcon.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            if (isFavorite[0]) {
                favoriteIcon.setImageResource(R.drawable.favorite_icon_empty);
                editor.remove(name);
                Toast.makeText(this, name + " removed from favorites", Toast.LENGTH_SHORT).show();
            } else {
                favoriteIcon.setImageResource(R.drawable.favorite_icon_fill);
                editor.putBoolean(name, true);
                Toast.makeText(this, name + " added to favorites", Toast.LENGTH_SHORT).show();
            }
            editor.apply();
            isFavorite[0] = !isFavorite[0];
        });

        // Nút + / -
        plusButton.setOnClickListener(v -> {
            int quantity = getQuantity(quantityInput);
            quantity++;
            quantityInput.setText(String.valueOf(quantity));
        });

        minusButton.setOnClickListener(v -> {
            int quantity = getQuantity(quantityInput);
            if (quantity > 1) {
                quantity--;
                quantityInput.setText(String.valueOf(quantity));
            }
        });

        // Add to cart
        addToCartButton.setOnClickListener(v -> {
            int quantity = getQuantity(quantityInput);
            addToCart(selectedProduct, quantity);
        });
    }

    // Hàm tiện ích đọc số lượng
    private int getQuantity(EditText et) {
        int quantity = 1;
        try {
            String input = et.getText().toString().trim();
            if (!input.isEmpty()) quantity = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            quantity = 1;
        }
        return quantity;
    }

    private void addToCart(Product product, int quantity) {
        for (Product p : ProductData.cartList) {
            if (p.getName().equals(product.getName())) {
                p.setQuantity(p.getQuantity() + quantity);
                Toast.makeText(this, "Updated quantity in cart!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        product.setQuantity(quantity);
        ProductData.cartList.add(product);
        Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show();
    }
}

