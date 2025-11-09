package com.example.demo.description;

import android.widget.Button;

import com.example.demo.AddToCartBottomSheet;
import com.example.demo.R;

public class DescriptionDrink extends BaseDescriptionActivity {
    @Override
    protected int getLayoutResId() { return R.layout.description_drink; }
    @Override
    protected int getImageViewId() { return R.id.drink_image; }
    @Override
    protected int getNameTextViewId() { return R.id.drink_name; }
    @Override
    protected int getPriceTextViewId() { return R.id.drink_price; }
    @Override
    protected int getDescriptionTextViewId() { return R.id.drink_description; }

}