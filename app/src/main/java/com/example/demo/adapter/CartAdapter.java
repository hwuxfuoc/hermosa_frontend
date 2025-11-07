package com.example.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.demo.R;
import com.example.demo.model.Product;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public interface OnAction {
        void onIncrease(Product item);
        void onDecrease(Product item);
        void onDelete(Product item);
        void onToggleSelect(Product item, boolean selected);
        void onEdit(Product item);
    }

    private final Context context;
    private final List<Product> list;
    private final OnAction action;
    private boolean isEditMode = false;

    public CartAdapter(Context context, List<Product> list, OnAction action) {
        this.context = context;
        this.list = list;
        this.action = action;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }

    @Override
    public int getItemViewType(int position) {
        Product item = list.get(position);
        String category = item.getCategory();

        if (isEditMode) {
            switch (category) {
                case "drink":
                    return R.layout.cart_item_edit_drink;
                case "food":
                    return R.layout.cart_item_edit_food;
                default:
                    return R.layout.cart_item_edit_cake;
            }
        } else {
            switch (category) {
                case "drink":
                    return R.layout.cart_item_select_drink;
                case "food":
                    return R.layout.cart_item_select_food;
                default:
                    return R.layout.cart_item_select_cake;
            }
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(viewType, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product item = list.get(position);

        holder.textName.setText(item.getName());
        holder.textPrice.setText(String.format("%,d VND", item.getPrice()));
        holder.textQuantity.setText(String.valueOf(item.getQuantity()));
        holder.imageProduct.setImageResource(item.getImageResId());

        // Checkbox chọn (chỉ có ở select mode)
        if (holder.checkBoxSelect != null) {
            holder.checkBoxSelect.setChecked(item.isSelected()); // DÙNG isSelected() – ĐÃ CÓ TRONG Product.java
            holder.checkBoxSelect.setOnCheckedChangeListener((btn, isChecked) ->
                    action.onToggleSelect(item, isChecked));
        }

        holder.buttonPlus.setOnClickListener(v -> action.onIncrease(item));
        holder.buttonMinus.setOnClickListener(v -> action.onDecrease(item));
        holder.buttonDelete.setOnClickListener(v -> action.onDelete(item));

        // Click item → mở edit (chỉ khi edit mode)
        if (isEditMode) {
            holder.itemView.setOnClickListener(v -> action.onEdit(item));
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ==================== ViewHolder ====================
    static class VH extends RecyclerView.ViewHolder {
        TextView textName, textPrice, textQuantity;
        ImageView imageProduct;
        ImageButton buttonPlus, buttonMinus, buttonDelete;
        CheckBox checkBoxSelect;

        VH(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textPrice = itemView.findViewById(R.id.text_price);
            textQuantity = itemView.findViewById(R.id.text_quantity);
            imageProduct = itemView.findViewById(R.id.image_product);
            buttonPlus = itemView.findViewById(R.id.button_plus);
            buttonMinus = itemView.findViewById(R.id.button_minus);
            buttonDelete = itemView.findViewById(R.id.button_delete);
            checkBoxSelect = itemView.findViewById(R.id.check_box_select);
        }
    }
}