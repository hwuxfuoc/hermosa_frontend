/*
package com.example.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.description.DescriptionCake;
import com.example.demo.description.DescriptionDrink;
import com.example.demo.description.DescriptionFood;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NextItemAdapter extends RecyclerView.Adapter<NextItemAdapter.ViewHolder> {

    private Context context;
    private List<Product> list;

    public NextItemAdapter(Context context, List<Product> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommended_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = list.get(position);

        // Mới vào chỉ có ID, chưa có tên -> Hiện ID tạm hoặc "Đang tải..."
        String displayName = (p.getName() != null) ? p.getName() : "Món ngon...";
        holder.tvName.setText(displayName);

        // Format giá (nếu có)
        if (p.getPrice() != null) {
            try {
                long price = Long.parseLong(p.getPrice().replaceAll("[^0-9]", ""));
                holder.tvPrice.setText(String.format("%,d VND/pc", price));
            } catch (Exception e) { holder.tvPrice.setText("Let's try"); }
        }

        if (p.getImageUrl() == null || p.getImageUrl().isEmpty()) {
            String searchID = (p.getProductID() != null) ? p.getProductID() : p.getId();

            if (searchID != null) {
                ApiClient.getClient().create(ApiService.class).getProductDetail(searchID)
                        .enqueue(new Callback<MenuResponse.SingleProductResponse>() {
                            @Override
                            public void onResponse(Call<MenuResponse.SingleProductResponse> call, Response<MenuResponse.SingleProductResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                    MenuResponse.MenuItem detail = response.body().getData();

                                    // Cập nhật lại data vào object Product trong list
                                    p.setName(detail.getName());
                                    p.setPrice(String.valueOf(detail.getPrice()));
                                    p.setImageUrl(detail.getPicture());
                                    p.setCategory(detail.getCategory());
                                    p.setProductID(detail.getProductID()); // Lưu lại ID chuẩn

                                    // Update UI ngay lập tức
                                    holder.tvName.setText(detail.getName());
                                    holder.tvPrice.setText(String.format("%,d đ", detail.getPrice()));
                                    Glide.with(context).load(detail.getPicture()).placeholder(R.drawable.placeholder_food).into(holder.imgProduct);
                                }
                            }
                            @Override
                            public void onFailure(Call<MenuResponse.SingleProductResponse> call, Throwable t) {}
                        });
            }
        } else {
            // Nếu đã có ảnh rồi thì load luôn
            Glide.with(context).load(p.getImageUrl()).placeholder(R.drawable.placeholder_food).into(holder.imgProduct);
        }

        // Click sự kiện
        holder.itemView.setOnClickListener(v -> {
            Class<?> cls;
            String cat = p.getCategory() != null ? p.getCategory().toLowerCase() : "cake";
            if (cat.contains("drink")) cls = DescriptionDrink.class;
            else if (cat.contains("food")) cls = DescriptionFood.class;
            else cls = DescriptionCake.class;

            Intent i = new Intent(context, cls);
            i.putExtra("product", p);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.image_product_cart);
            tvName = itemView.findViewById(R.id.text_name_cart);
            tvPrice = itemView.findViewById(R.id.text_price_cart);
        }
    }
}*/
package com.example.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.description.DescriptionCake;
import com.example.demo.description.DescriptionDrink;
import com.example.demo.description.DescriptionFood;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NextItemAdapter extends RecyclerView.Adapter<NextItemAdapter.ViewHolder> {

    private Context context;
    private List<Product> list;

    public NextItemAdapter(Context context, List<Product> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommended_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = list.get(position);

        // Mới vào chỉ có ID, chưa có tên -> Hiện ID tạm hoặc "Đang tải..."
        String displayName = (p.getName() != null) ? p.getName() : "Đang tải...";
        holder.tvName.setText(displayName);

        // Format giá (nếu có sẵn)
        if (p.getPrice() != null) {
            try {
                long price = Long.parseLong(p.getPrice().replaceAll("[^0-9]", ""));
                holder.tvPrice.setText(String.format("%,d VND/pc", price));
            } catch (Exception e) {
                holder.tvPrice.setText("Updating...");
            }
        }

        String searchID = (p.getProductID() != null) ? p.getProductID() : p.getId();

        if (searchID != null) {
            ApiClient.getClient().create(ApiService.class).getProductDetail(searchID)
                    .enqueue(new Callback<MenuResponse.SingleProductResponse>() {
                        @Override
                        public void onResponse(Call<MenuResponse.SingleProductResponse> call, Response<MenuResponse.SingleProductResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                MenuResponse.MenuItem detail = response.body().getData();

                                // 1. CẬP NHẬT VIEW CƠ BẢN
                                holder.tvName.setText(detail.getName());
                                holder.tvPrice.setText(String.format("%,d VND/pc", detail.getPrice()));

                                Glide.with(context)
                                        .load(detail.getPicture())
                                        .placeholder(R.drawable.placeholder_food)
                                        .into(holder.imgProduct);

                                // 2. CẬP NHẬT LƯỢT BÁN (MỚI THÊM)
                                /*if (holder.tvSold != null) {
                                    // Lấy sumofRatings hoặc totalSold tùy model của bạn
                                    int count = detail.getSumofRatings();
                                    holder.tvSold.setText("Đã bán " + formatCount(count));
                                    holder.tvSold.setVisibility(View.VISIBLE);
                                }*/

                                // 3. CẬP NHẬT NGƯỢC LẠI VÀO OBJECT PRODUCT (để khi click có dữ liệu)
                                p.setName(detail.getName());
                                p.setPrice(String.valueOf(detail.getPrice()));
                                p.setImageUrl(detail.getPicture());
                                p.setCategory(detail.getCategory());
                                p.setProductID(detail.getProductID());
                            }
                        }

                        @Override
                        public void onFailure(Call<MenuResponse.SingleProductResponse> call, Throwable t) {
                            // API lỗi thì giữ nguyên hoặc load ảnh placeholder
                            if (p.getImageUrl() != null) {
                                Glide.with(context).load(p.getImageUrl()).into(holder.imgProduct);
                            }
                        }
                    });
        } else {
            // Trường hợp có sẵn ảnh (ít xảy ra ở NextItem)
            if (p.getImageUrl() != null) {
                Glide.with(context).load(p.getImageUrl()).into(holder.imgProduct);
            }
        }

        // Click sự kiện chuyển trang
        holder.itemView.setOnClickListener(v -> {
            Class<?> cls;
            String cat = p.getCategory() != null ? p.getCategory().toLowerCase() : "cake";
            if (cat.contains("drink")) cls = DescriptionDrink.class;
            else if (cat.contains("food")) cls = DescriptionFood.class;
            else cls = DescriptionCake.class;

            Intent i = new Intent(context, cls);
            i.putExtra("product", p);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(i);
        });
    }

    // Hàm format số lượng: 1200 -> 1.2K
    private String formatCount(int count) {
        if (count < 1000) return String.valueOf(count);
        return String.format("%.1fK+", count / 1000.0);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice;
        TextView tvSold; // KHAI BÁO TV SOLD

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.image_product_cart);
            tvName = itemView.findViewById(R.id.text_name_cart);
            tvPrice = itemView.findViewById(R.id.text_price_cart);


            /*tvSold = itemView.findViewById(R.id.tvSold);*/
        }
    }
}