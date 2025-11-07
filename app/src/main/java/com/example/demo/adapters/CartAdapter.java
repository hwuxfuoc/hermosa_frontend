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

    /**
     * Interface để giao tiếp với Activity.
     * Đã bổ sung onDataChanged để báo Activity tính lại tổng tiền.
     */
    public interface OnAction {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onDelete(CartItem item);
        void onToggleSelect(CartItem item, boolean selected);
        void onDataChanged(); // Báo cho Activity/Fragment tính lại tổng tiền
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
        if (item == null) return;

        holder.tvName.setText(item.getName());

        // Đã sửa (dùng "%,.0f" cho double)
        holder.tvSubtotal.setText(String.format("%,.0f VND/pc", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // ✅ Load ảnh (Giữ nguyên logic Glide của bạn)
        int resId = item.getDrawableResId(ctx); // Dùng hàm getDrawableResId
        if (resId != R.drawable.logo_app) {
            Glide.with(ctx).load(resId).into(holder.img);
        } else {
            Glide.with(ctx).load(R.drawable.logo_app).into(holder.img);
        }

        // =========================================================
        // SỬA LOGIC MÀU TẠI ĐÂY
        // =========================================================

        // 1. Lấy màu (dạng int) trực tiếp từ CartItem
        int colorValue = item.getColor();

        if (colorValue != 0) {
            // 2. Dùng trực tiếp con số int màu này
            holder.itemBackground.setCardBackgroundColor(colorValue);
        } else {
            // 3. (Dự phòng) Dùng màu trắng nếu không có màu
            holder.itemBackground.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.white));
        }

        // =========================================================

        // Các nút hành động (Đã bổ sung onDataChanged)
        holder.btnPlus.setOnClickListener(v -> {
            if (action != null) {
                action.onIncrease(item);
                action.onDataChanged(); // Báo Activity tính lại tiền
            }
        });
        holder.btnMinus.setOnClickListener(v -> {
            if (action != null) {
                action.onDecrease(item);
                action.onDataChanged(); // Báo Activity tính lại tiền
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (action != null) {
                action.onDelete(item);
                action.onDataChanged(); // Báo Activity tính lại tiền
            }
        });

        // Checkbox chọn sản phẩm (Đã sửa logic)
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(item.isSelected()); // Lấy trạng thái từ model

        holder.cbSelect.setOnCheckedChangeListener((b, checked) -> {
            if (action != null) {
                action.onToggleSelect(item, checked);
                action.onDataChanged(); // Báo Activity tính lại tiền
            }
        });
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    /**
     * BỔ SUNG: Phương thức clearItems() mà ConfirmOrderActivity cần.
     */
    public void clearItems() {
        if (list != null) {
            int itemCount = list.size();
            list.clear();
            notifyItemRangeRemoved(0, itemCount);
        }
    }

    // =========================================================
    // XÓA HÀM getBackgroundColor(CartItem item)
    // (Toàn bộ khối if-else dài 70 dòng đã bị xóa)
    // =========================================================


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