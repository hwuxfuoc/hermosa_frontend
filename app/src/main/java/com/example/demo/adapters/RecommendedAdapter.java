package com.example.demo.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// ✅ BƯỚC 1: Thêm 2 import này
import com.bumptech.glide.Glide;
import java.util.Locale;

import com.example.demo.Product;
import com.example.demo.R;

import java.util.List;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.VH> {

    private Context context;
    private final List<Product> list;
    private final OnAddToCartListener listener;

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    public RecommendedAdapter(List<Product> list, OnAddToCartListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) context = parent.getContext();
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_recommended_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = list.get(position);
        if (p == null) return;

        // --- Set dữ liệu ---
        holder.tvName.setText(p.getName());

        // ✅ TỐI ƯU HIỂN THỊ GIÁ (Tránh lỗi định dạng)
        try {
            double priceValue = Double.parseDouble(p.getPrice());
            holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f VND/pc", priceValue));
        } catch (NumberFormatException e) {
            holder.tvPrice.setText(p.getPrice()); // Hiển thị gốc nếu không parse được
        }

        // ✅ BƯỚC 2: SỬA LỖI HIỂN THỊ ẢNH
        // XÓA DÒNG NÀY: holder.img.setImageResource(p.getImageResId());
        // THAY BẰNG GLIDE:
        Glide.with(context)
                .load(p.getImageResId()) // Tải ID từ drawable (Glide tự xử lý nếu ID = 0)
                .placeholder(R.drawable.logo_app) // Ảnh hiển thị trong lúc chờ
                .error(R.drawable.logo_app)       // Ảnh hiển thị nếu tải lỗi (hoặc ID = 0)
                .into(holder.img);                // Nơi hiển thị ảnh

        // 🎨 Đổi màu phần nền trái (Giữ nguyên logic của bạn)
        int mainColor = p.getColor();
        holder.leftLayout.setBackgroundTintList(ColorStateList.valueOf(mainColor));

        // 🎨 Đổi màu nút "+" (Giữ nguyên logic của bạn)
        holder.btnAdd.setBackgroundTintList(ColorStateList.valueOf(mainColor));

        // Khi nhấn "+"
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(p);
                Toast.makeText(context, "Đã thêm " + p.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // --- ViewHolder ---
    public static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice;
        Button btnAdd;
        LinearLayout leftLayout; // phần nền bên trái có ảnh

        public VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.image_product_cart);
            tvName = itemView.findViewById(R.id.text_name_cart);
            tvPrice = itemView.findViewById(R.id.text_price_cart);
            btnAdd = itemView.findViewById(R.id.button_plus);
            leftLayout = itemView.findViewById(R.id.left_color_layout); // (ID này phải có trong XML)
        }
    }
}