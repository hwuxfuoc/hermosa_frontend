package com.example.demo.description;

import android.widget.Button;

import com.example.demo.AddToCartBottomSheet;
import com.example.demo.R;

public class DescriptionFood extends BaseDescriptionActivity {
    @Override
    protected int getLayoutResId() { return R.layout.description_food; }
    @Override
    protected int getImageViewId() { return R.id.food_image; }
    @Override
    protected int getNameTextViewId() { return R.id.food_name; }
    @Override
    protected int getPriceTextViewId() { return R.id.food_price; }
    @Override
    protected int getDescriptionTextViewId() { return R.id.food_description; }

    @Override
    protected void setupAddToCart() {
        Button btnAdd = findViewById(R.id.button_add_to_cart);
        btnAdd.setOnClickListener(v -> {
            AddToCartBottomSheet sheet = AddToCartBottomSheet.newInstance(product);
            sheet.show(getSupportFragmentManager(), "AddToCart");
        });
    }
}