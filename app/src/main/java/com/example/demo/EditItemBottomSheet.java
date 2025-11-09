package com.example.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.adapters.CheckboxAdapter;
import com.example.demo.models.CheckboxItem;
import com.example.demo.models.Product;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class EditItemBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCT = "product";
    private Product product;

    public static EditItemBottomSheet newInstance(Product product) {
        EditItemBottomSheet fragment = new EditItemBottomSheet();
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
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        int layoutId;
        switch (product.getCategory()) { // ĐÃ SỬA: dùng getCategory() thay vì getType()
            case "drink":
                layoutId = R.layout.layout_edit_item_drink;
                break;
            case "food":
                layoutId = R.layout.layout_edit_item_food;
                break;
            default:
                layoutId = R.layout.layout_edit_item_cake;
                break;
        }

        View view = inflater.inflate(layoutId, container, false);

        TextView tvTitle = view.findViewById(R.id.text_title_edit);
        ImageView img = view.findViewById(R.id.image_product);
        TextView tvName = view.findViewById(R.id.text_name);
        TextView tvPrice = view.findViewById(R.id.text_price);
        TextView tvQty = view.findViewById(R.id.text_quantity);
        ImageButton btnMinus = view.findViewById(R.id.button_minus);
        ImageButton btnPlus = view.findViewById(R.id.button_plus);
        ImageButton btnDelete = view.findViewById(R.id.button_delete);
        RecyclerView rvOptions = view.findViewById(R.id.recycler_checkboxes);
        Button btnSave = view.findViewById(R.id.button_add_to_cart);

        tvTitle.setText("Sửa " + product.getName());
        img.setImageResource(product.getImageResId());
        tvName.setText(product.getName());
        tvPrice.setText(String.format("%,d VND", product.getPrice()));
        tvQty.setText(String.valueOf(product.getQuantity()));

        // Demo options (size/topping)
        List<CheckboxItem> options = new ArrayList<>();
        options.add(new CheckboxItem("Size S", false));
        options.add(new CheckboxItem("Size M", true));
        options.add(new CheckboxItem("Size L", false));
        options.add(new CheckboxItem("Thêm đá", false));
        options.add(new CheckboxItem("Ít đường", false));

        CheckboxAdapter adapter = new CheckboxAdapter(options);
        rvOptions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOptions.setAdapter(adapter);

        btnPlus.setOnClickListener(v -> {
            product.setQuantity(product.getQuantity() + 1);
            tvQty.setText(String.valueOf(product.getQuantity()));
        });

        btnMinus.setOnClickListener(v -> {
            if (product.getQuantity() > 1) {
                product.setQuantity(product.getQuantity() - 1);
                tvQty.setText(String.valueOf(product.getQuantity()));
            }
        });

        btnDelete.setOnClickListener(v -> {
            ProductData.cartList.remove(product);
            Toast.makeText(getContext(), "Đã xóa khỏi giỏ hàng!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        btnSave.setOnClickListener(v -> {
            // TODO: Lưu size/topping từ checkbox vào product nếu cần
            Toast.makeText(getContext(), "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }
}