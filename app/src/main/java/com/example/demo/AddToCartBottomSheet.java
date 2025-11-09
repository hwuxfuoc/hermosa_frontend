package com.example.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;  // ĐÚNG RỒI!

import com.example.demo.models.Product;

public class AddToCartBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCT = "product";
    private Product product;

    public static AddToCartBottomSheet newInstance(Product product) {
        AddToCartBottomSheet fragment = new AddToCartBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
        setCancelable(true);

        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId;

        // FIX: XỬ LÝ NULL AN TOÀN
        String category = product.getCategory();
        if (category == null) {
            category = "cake"; // mặc định
        }

        switch (category.toLowerCase()) {
            case "drink":
                layoutId = R.layout.layout_add_item_drink;
                break;
            case "food":
                layoutId = R.layout.layout_add_item_food;
                break;
            default:
                layoutId = R.layout.layout_add_item_cake;
                break;
        }

        View view = inflater.inflate(layoutId, container, false);

        TextView tvTitle = view.findViewById(R.id.text_title_edit);
        ImageView imgProduct = view.findViewById(R.id.image_product_cart);
        TextView tvName = view.findViewById(R.id.text_name_cart);
        TextView tvPrice = view.findViewById(R.id.text_price_cart);
        Button btnAdd = view.findViewById(R.id.button_add_to_cart);

        tvTitle.setText("Thêm " + product.getName());
        imgProduct.setImageResource(product.getImageResId());
        tvName.setText(product.getName());
        tvPrice.setText(product.getPrice());

        btnAdd.setOnClickListener(v -> {
            addToCart(product);
            Toast.makeText(getContext(), product.getName() + " đã thêm vào giỏ!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    private void addToCart(Product newProduct) {
        for (Product p : ProductData.cartList) {
            if (p.getName().equals(newProduct.getName())) {
                p.setQuantity(p.getQuantity() + 1);
                return;
            }
        }
        newProduct.setQuantity(1);
        ProductData.cartList.add(newProduct);
    }
}