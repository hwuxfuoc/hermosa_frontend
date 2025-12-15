/*
package com.example.demo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.demo.R;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.VH> {

    private final List<Product> list;
    private final OnAddToCartListener listener;
    private final Context context;

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    public RecommendedAdapter(Context context, List<Product> list, OnAddToCartListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_recommended_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = list.get(position);
        holder.tvName.setText(p.getName() != null ? p.getName() : "Tên sản phẩm không có");

        // Xử lý giá an toàn và format VND (ví dụ: 85.000 VND/pc)
        long price = 0;
        if (p.getPrice() != null) {
            String priceStr = p.getPrice().toString().trim().replaceAll("[^0-9]", "");
            try {
                price = Long.parseLong(priceStr);
            } catch (NumberFormatException e) {
                Log.e("RecommendedAdapter", "Lỗi parse giá: " + e.getMessage());
            }
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(price) + " VND/pc");

        // Set listener cho nút "+"
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(p);
            }
        });

        Log.d("RecommendedAdapter", "Vị trí: " + position + ", ProductID: " + (p.getProductID() != null ? p.getProductID() : "null"));

        if (p.getProductID() != null && !p.getProductID().isEmpty()) {
            ApiClient.getClient().create(ApiService.class)
                    .getProductDetail(p.getProductID())
                    .enqueue(new Callback<MenuResponse.SingleProductResponse>() {
                        @Override
                        public void onResponse(Call<MenuResponse.SingleProductResponse> call, Response<MenuResponse.SingleProductResponse> res) {
                            if (res.isSuccessful() && res.body() != null && res.body().getData() != null) {
                                MenuResponse.MenuItem detail = res.body().getData();
                                Log.d("RecommendedAdapter", "API thành công - Ảnh: " + detail.getPicture() + ", Màu hex: " + detail.getBackgroundHexacode());


                                if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
                                    Glide.with(context)
                                            .load(p.getImageUrl())
                                            .placeholder(R.drawable.placeholder_food)
                                            .error(R.drawable.placeholder_food)
                                            .into(holder.img);
                                    Log.d("RecommendedAdapter", "Load ảnh từ backend: " + p.getImageUrl());
                                } else {
                                    Log.w("RecommendedAdapter", "URL ảnh backend null/empty, dùng fallback");
                                    loadFallbackImage(holder.img, p);
                                }

                                String hex = detail.getBackgroundHexacode();
                                LinearLayout parentLayout = (LinearLayout) ((CardView) holder.itemView).getChildAt(0);
                                if (hex != null && !hex.isEmpty()) {
                                    try {
                                        if (!hex.matches("^#?[0-9A-Fa-f]{6}$")) {
                                            throw new IllegalArgumentException("Định dạng hex không hợp lệ: " + hex);
                                        }
                                        String cleanHex = hex.trim().replace("#", "");
                                        int color = Integer.parseInt(cleanHex, 16) | 0xFF000000;
                                        parentLayout.setBackgroundColor(color);
                                        Log.d("RecommendedAdapter", "Set màu nền thành công: #" + cleanHex);
                                    } catch (Exception e) {
                                        Log.e("RecommendedAdapter", "Lỗi parse hex: " + e.getMessage() + ", Hex: " + hex);
                                        setFallbackColor(parentLayout, p);
                                    }
                                } else {
                                    Log.w("RecommendedAdapter", "Hex null/empty, dùng fallback");
                                    setFallbackColor(parentLayout, p);
                                }
                            } else {
                                Log.e("RecommendedAdapter", "API response không hợp lệ, code: " + res.code() + ", dùng fallback");
                                loadFallbackImage(holder.img, p);
                                LinearLayout parentLayout = (LinearLayout) ((CardView) holder.itemView).getChildAt(0);
                                setFallbackColor(parentLayout, p);
                            }
                        }

                        @Override
                        public void onFailure(Call<MenuResponse.SingleProductResponse> call, Throwable t) {
                            Log.e("RecommendedAdapter", "API thất bại: " + t.getMessage() + ", dùng fallback");
                            loadFallbackImage(holder.img, p);
                            LinearLayout parentLayout = (LinearLayout) ((CardView) holder.itemView).getChildAt(0);
                            setFallbackColor(parentLayout, p);
                        }
                    });
        } else {
            Log.w("RecommendedAdapter", "ProductID không hợp lệ, dùng fallback");
            loadFallbackImage(holder.img, p);
            LinearLayout parentLayout = (LinearLayout) ((CardView) holder.itemView).getChildAt(0);
            setFallbackColor(parentLayout, p);
        }
    }

    private void loadFallbackImage(ImageView img, Product p) {
        Glide.with(context).load(p.getImageUrl()).into(img);
        Log.d("RecommendedAdapter", "Load ảnh fallback: " + p.getImageUrl());
    }

    private void setFallbackColor(LinearLayout layout, Product p) {
        layout.setBackgroundColor(p.getColor());
        Log.d("RecommendedAdapter", "Set màu fallback: " + Integer.toHexString(p.getColor()));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice;
        Button btnAdd;
        LinearLayout leftLayout;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.image_product_cart);
            tvName = v.findViewById(R.id.text_name_cart);
            tvPrice = v.findViewById(R.id.text_price_cart);
            btnAdd = v.findViewById(R.id.button_plus);
            leftLayout = v.findViewById(R.id.left_color_layout);
        }
    }
}*/
package com.example.demo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.VH> {

    private final List<Product> list;
    private final OnAddToCartListener listener;
    private final Context context;

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    public RecommendedAdapter(Context context, List<Product> list, OnAddToCartListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_recommended_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = list.get(position);
        holder.tvName.setText(p.getName() != null ? p.getName() : "Tên sản phẩm không có");

        // Xử lý giá an toàn và format VND
        long price = 0;
        if (p.getPrice() != null) {
            String priceStr = p.getPrice().toString().trim().replaceAll("[^0-9]", "");
            try {
                price = Long.parseLong(priceStr);
            } catch (NumberFormatException e) {
                Log.e("RecommendedAdapter", "Lỗi parse giá: " + e.getMessage());
            }
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(price) + " VND/pc");

        // Set listener cho nút "+"
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(p);
            }
        });

        // GỌI API LẤY CHI TIẾT SẢN PHẨM (Màu, Ảnh, Lượt bán)
        if (p.getProductID() != null && !p.getProductID().isEmpty()) {
            ApiClient.getClient().create(ApiService.class)
                    .getProductDetail(p.getProductID())
                    .enqueue(new Callback<MenuResponse.SingleProductResponse>() {
                        @Override
                        public void onResponse(Call<MenuResponse.SingleProductResponse> call, Response<MenuResponse.SingleProductResponse> res) {
                            if (res.isSuccessful() && res.body() != null && res.body().getData() != null) {
                                MenuResponse.MenuItem detail = res.body().getData();

                                // --- 1. HIỂN THỊ LƯỢT BÁN (MỚI THÊM) ---
                                /*if (holder.tvSold != null) {
                                    int count = detail.getSumofRatings(); // Hoặc getTotalSold() tùy model
                                    holder.tvSold.setText("Đã bán " + formatCount(count));
                                    holder.tvSold.setVisibility(View.VISIBLE);
                                }*/

                                // --- 2. HIỂN THỊ ẢNH ---
                                if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
                                    Glide.with(context)
                                            .load(p.getImageUrl())
                                            .placeholder(R.drawable.placeholder_food)
                                            .error(R.drawable.placeholder_food)
                                            .into(holder.img);
                                } else {
                                    loadFallbackImage(holder.img, p);
                                }

                                // --- 3. HIỂN THỊ MÀU NỀN ---
                                String hex = detail.getBackgroundHexacode();
                                LinearLayout parentLayout = (LinearLayout) ((CardView) holder.itemView).getChildAt(0);
                                if (hex != null && !hex.isEmpty()) {
                                    try {
                                        String cleanHex = hex.trim().replace("#", "");
                                        int color = Integer.parseInt(cleanHex, 16) | 0xFF000000;
                                        parentLayout.setBackgroundColor(color);
                                    } catch (Exception e) {
                                        setFallbackColor(parentLayout, p);
                                    }
                                } else {
                                    setFallbackColor(parentLayout, p);
                                }
                            } else {
                                // API lỗi logic -> Ẩn lượt bán, dùng màu/ảnh fallback
                                /*if (holder.tvSold != null) holder.tvSold.setVisibility(View.GONE);*/
                                loadFallbackImage(holder.img, p);
                                LinearLayout parentLayout = (LinearLayout) ((CardView) holder.itemView).getChildAt(0);
                                setFallbackColor(parentLayout, p);
                            }
                        }

                        @Override
                        public void onFailure(Call<MenuResponse.SingleProductResponse> call, Throwable t) {
                            // Mạng lỗi -> Ẩn lượt bán
                            /*if (holder.tvSold != null) holder.tvSold.setVisibility(View.GONE);*/
                            loadFallbackImage(holder.img, p);
                            LinearLayout parentLayout = (LinearLayout) ((CardView) holder.itemView).getChildAt(0);
                            setFallbackColor(parentLayout, p);
                        }
                    });
        } else {
            /*if (holder.tvSold != null) holder.tvSold.setVisibility(View.GONE);*/
            loadFallbackImage(holder.img, p);
            LinearLayout parentLayout = (LinearLayout) ((CardView) holder.itemView).getChildAt(0);
            setFallbackColor(parentLayout, p);
        }
    }

    // Hàm format số lượng: 1200 -> 1.2K
    private String formatCount(int count) {
        if (count < 1000) return String.valueOf(count);
        return String.format("%.1fK+", count / 1000.0);
    }

    private void loadFallbackImage(ImageView img, Product p) {
        Glide.with(context).load(p.getImageUrl()).into(img);
    }

    private void setFallbackColor(LinearLayout layout, Product p) {
        layout.setBackgroundColor(p.getColor());
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice;
        /*TextView tvSold;*/ // KHAI BÁO MỚI
        Button btnAdd;
        LinearLayout leftLayout;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.image_product_cart);
            tvName = v.findViewById(R.id.text_name_cart);
            tvPrice = v.findViewById(R.id.text_price_cart);
            btnAdd = v.findViewById(R.id.button_plus);
            leftLayout = v.findViewById(R.id.left_color_layout);

            // ÁNH XẠ MỚI (ID này đã có trong item_recommended_product.xml từ các bước trước)
            /*tvSold = v.findViewById(R.id.tvSold);*/
        }
    }
}