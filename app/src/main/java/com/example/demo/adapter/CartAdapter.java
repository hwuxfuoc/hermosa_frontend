package com.example.demo.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.model.CartItem;
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
        // layout bạn dùng: item_cart_cake (theo bạn gửi)
        View v = LayoutInflater.from(ctx).inflate(R.layout.fragment_cart, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartItem item = list.get(position);

        holder.tvName.setText(item.getName());
        holder.tvSubtotal.setText(String.format("%,d VND/pc", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // 1) Load ảnh: ưu tiên imageUrl nếu có, ngược lại dùng drawable từ imageName
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(ctx).load(item.getImageUrl()).into(holder.img);
        } else {
            int resId = item.getDrawableResId(ctx);
            holder.img.setImageResource(resId);
        }

        // 2) Dùng màu từ item.resolveColor(ctx)
        int bgColor = item.resolveColor(ctx);
        holder.itemBackground.setCardBackgroundColor(bgColor);

        // 3) Các nút hành động
        holder.btnPlus.setOnClickListener(v -> action.onIncrease(item));
        holder.btnMinus.setOnClickListener(v -> action.onDecrease(item));
        holder.btnDelete.setOnClickListener(v -> action.onDelete(item));

        // 4) Checkbox: set listener an toàn tránh recycled listener
        holder.cbSelect.setOnCheckedChangeListener(null);
        // mặc định chọn (nếu bạn muốn mặc định true). Nếu muốn dựa vào dữ liệu, thêm trường boolean trong CartItem
        holder.cbSelect.setChecked(true);
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> action.onToggleSelect(item, isChecked));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================== ViewHolder ==================
    public static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvSubtotal, tvQuantity;
        ImageButton btnPlus, btnMinus, btnDelete;
        CheckBox cbSelect;
        CardView itemBackground;

        public VH(@NonNull View itemView) {
            super(itemView);
            // IDs theo xml bạn gửi (cart_item_select_cake / item_cart_cake)
            // nếu bạn có id khác trong layout, hãy đổi tương ứng
            img = itemView.findViewById(R.id.image_product);      // xml: image_product
            if (img == null) img = itemView.findViewById(R.id.image_product); // fallback nếu id khác

            tvName = itemView.findViewById(R.id.text_name);       // xml: text_name
            if (tvName == null) tvName = itemView.findViewById(R.id.text_name);

            tvSubtotal = itemView.findViewById(R.id.text_price);  // xml: text_price
            if (tvSubtotal == null) tvSubtotal = itemView.findViewById(R.id.text_price);

            tvQuantity = itemView.findViewById(R.id.text_quantity); // xml: text_quantity
            if (tvQuantity == null) tvQuantity = itemView.findViewById(R.id.text_quantity);

            btnPlus = itemView.findViewById(R.id.button_plus);    // xml
            if (btnPlus == null) btnPlus = itemView.findViewById(R.id.button_plus);

            btnMinus = itemView.findViewById(R.id.button_minus);  // xml
            if (btnMinus == null) btnMinus = itemView.findViewById(R.id.button_minus);

            btnDelete = itemView.findViewById(R.id.button_delete); // xml
            if (btnDelete == null) btnDelete = itemView.findViewById(R.id.button_delete);

            cbSelect = itemView.findViewById(R.id.check_box_select); // xml
            if (cbSelect == null) cbSelect = itemView.findViewById(R.id.check_box_select);

            itemBackground = itemView.findViewById(R.id.item_background); // xml: item_background (card)
            if (itemBackground == null) itemBackground = itemView.findViewById(R.id.item_background);
        }
    }
}
