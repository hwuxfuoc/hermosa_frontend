package com.example.demo.description;

import android.widget.Button;

import com.example.demo.AddToCartBottomSheet;
import com.example.demo.R;

public class DescriptionCake extends BaseDescriptionActivity {
    @Override
    protected int getLayoutResId() { return R.layout.description_cake; }
    @Override
    protected int getImageViewId() { return R.id.cake_image; }
    @Override
    protected int getNameTextViewId() { return R.id.cake_name; }
    @Override
    protected int getPriceTextViewId() { return R.id.cake_price; }
    @Override
    protected int getDescriptionTextViewId() { return R.id.cake_description; }

}