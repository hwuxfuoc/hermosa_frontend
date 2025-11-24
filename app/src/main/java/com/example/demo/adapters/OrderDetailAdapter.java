package com.example.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.demo.R;
import com.example.demo.models.CartItem; // Sử dụng CartItem từ model cũ của bạn
import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private List<CartItem> list;

    public OrderDetailAdapter(List<CartItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = list.get(position);
        // Giả sử CartItem có các field: quantity, name (hoặc productID -> name)
        holder.tvQuantity.setText(item.getQuantity() + "x");

        // Lưu ý: Nếu CartItem chỉ có productID, bạn cần map sang tên.
        // Nhưng thường trong Order đã lưu snapshot tên món (như file be carts.js dòng 69)
        holder.tvDishName.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuantity, tvDishName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDishName = itemView.findViewById(R.id.tvProductName);
        }
    }
}