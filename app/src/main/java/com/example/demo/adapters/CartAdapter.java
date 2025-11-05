package com.example.demo.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public interface OnAction {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onDelete(CartItem item);
        void onToggleSelect(CartItem item, boolean selected);
    }

    private final Context ctx;
    private final List<CartItem> list;
    private final OnAction action;

    public CartAdapter(Context ctx, List<CartItem> list, OnAction action) {
        this.ctx = ctx;
        this.list = list;
        this.action = action;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.cart_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartItem item = list.get(position);
        holder.tvName.setText(item.getName());
        holder.tvSubtotal.setText(String.format("%,d VND/pc", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // ✅ Load ảnh (có thể dùng link từ BE nếu có)
        /*if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(ctx).load(item.getImageUrl()).into(holder.img);
        } else {
            Glide.with(ctx).load(R.drawable.logo_app).into(holder.img);
        }*/

        // ✅ Set màu nền cho CardView (không dùng setBackgroundColor)
        int colorRes = getBackgroundColor(item);
        holder.itemBackground.setCardBackgroundColor(ContextCompat.getColor(ctx, colorRes));

        // Các nút hành động
        holder.btnPlus.setOnClickListener(v -> action.onIncrease(item));
        holder.btnMinus.setOnClickListener(v -> action.onDecrease(item));
        holder.btnDelete.setOnClickListener(v -> action.onDelete(item));

        // Checkbox chọn sản phẩm
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(true);
        holder.cbSelect.setOnCheckedChangeListener((b, checked) -> action.onToggleSelect(item, checked));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Xác định màu nền theo tên sản phẩm (so khớp với colors.xml bạn gửi)
     */
    private int getBackgroundColor(CartItem item) {
        String name = item.getName().toLowerCase();

        // === Đồ uống ===
        if (name.contains("strawberry") && name.contains("smoothie"))
            return R.color.smoothie_strawberry;
        if (name.contains("caramel"))
            return R.color.smoothie_caramel;
        if (name.contains("oreo"))
            return R.color.smoothie_oreo;
        if (name.contains("blueberry"))
            return R.color.smoothie_blueberry;
        if (name.contains("matcha") || name.contains("latte"))
            return R.color.matcha_green;
        if (name.contains("milk tea") || name.contains("chocolate milk tea"))
            return R.color.choco_milk_tea;
        if (name.contains("black ice coffee"))
            return R.color.coffee_black_ice;
        if (name.contains("milk coffee"))
            return R.color.coffee_milk;
        if (name.contains("hot coffee"))
            return R.color.coffee_hot;
        if (name.contains("green tea"))
            return R.color.tea_green;
        if (name.contains("guava"))
            return R.color.tea_guava;
        if (name.contains("longan"))
            return R.color.tea_longan;

        // === Bánh ===
        if (name.contains("strawberry cheese"))
            return R.color.cake_strawberry_donut;
        if (name.contains("yellow lemon"))
            return R.color.cake_lemon;
        if (name.contains("blueberry cheese"))
            return R.color.cake_blueberry;
        if (name.contains("tiramisu"))
            return R.color.cake_tiramisu_chocolate;
        if (name.contains("classic matcha"))
            return R.color.cake_tiramisu_matcha;
        if (name.contains("eclair"))
            return R.color.cake_eclair;
        if (name.contains("truffle"))
            return R.color.cake_truffle;
        if (name.contains("opera"))
            return R.color.cake_opera;
        if (name.contains("donut") && name.contains("strawberry"))
            return R.color.cake_strawberry_donut;
        if (name.contains("donut") && name.contains("matcha"))
            return R.color.cake_matcha_donut;
        if (name.contains("egg tart"))
            return R.color.cake_egg_tart;
        if (name.contains("macarons"))
            return R.color.cake_macarons;
        if (name.contains("croissant") && name.contains("chocolate"))
            return R.color.cake_chocolate_croissant;
        if (name.contains("croissant"))
            return R.color.cake_croissant;

        // Mặc định
        return R.color.white;
    }

    // ================== ViewHolder ==================
    public static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvSubtotal, tvQuantity;
        ImageButton btnPlus, btnMinus, btnDelete;
        CheckBox cbSelect;
        CardView itemBackground; // CardView bọc ngoài

        public VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvSubtotal = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            itemBackground = itemView.findViewById(R.id.itemBackground);
        }
    }
}
