package com.example.demo.description;

import android.util.Log;
import android.widget.Button;

import com.example.demo.AddToCartBottomSheet;
import com.example.demo.R;

public class DescriptionCake extends BaseDescriptionActivity {

    @Override
    protected int getLayoutResId() { return R.layout.description_cake; }

    @Override
    protected int getImageViewId() { return R.id.imgProduct; }

    @Override
    protected int getNameTextViewId() { return R.id.tvName; }

    @Override
    protected int getPriceTextViewId() { return R.id.tvPrice; }

    @Override
    protected int getDescriptionTextViewId() { return R.id.tvDescription; }

    @Override
    protected void setupAddToCart() {
        Button btn = findViewById(R.id.btn_add_to_cart);
        if (btn != null && product != null) {
            btn.setOnClickListener(v -> {
                Log.d("DESCRIPTION", "Mở BottomSheet từ Description | ProductID: " + product.getProductID());
                AddToCartBottomSheet bottomSheet = AddToCartBottomSheet.newInstance(product);
                bottomSheet.show(getSupportFragmentManager(), "AddToCart");
            });
        }
    }
}