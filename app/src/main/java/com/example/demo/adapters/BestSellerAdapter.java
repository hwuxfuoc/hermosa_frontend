package com.example.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.AddToCartBottomSheet;
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

public class BestSellerAdapter extends RecyclerView.Adapter<BestSellerAdapter.ViewHolder> {

    private Context context;
    private List<Product> list;
    private static final String TAG = "BestSellerAdapter";

    public BestSellerAdapter(Context context, List<Product> list) {
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

        // 1. Hiển thị Tên
        holder.tvName.setText(p.getName() != null ? p.getName() : "Sản phẩm");

        // 2. Xử lý Giá
        long price = 0;
        if (p.getPrice() != null) {
            String priceStr = p.getPrice().toString().trim().replaceAll("[^0-9]", "");
            try {
                price = Long.parseLong(priceStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Lỗi parse giá: " + e.getMessage());
            }
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(price) + " VND/pc");

        // 3. Sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            Class<?> cls;
            String cat = p.getCategory() != null ? p.getCategory().toLowerCase() : "";
            if (cat.contains("drink")) cls = DescriptionDrink.class;
            else if (cat.contains("food") || cat.contains("lunch")) cls = DescriptionFood.class;
            else cls = DescriptionCake.class;

            Intent i = new Intent(context, cls);
            i.putExtra("product", p);
            context.startActivity(i);
        });

        // 4. Sự kiện nút Cộng
        holder.btnPlus.setOnClickListener(v -> {
            if (context instanceof AppCompatActivity) {
                AddToCartBottomSheet sheet = AddToCartBottomSheet.newInstance(p);
                sheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "AddToCart");
            }
        });

        // 5. GỌI API LẤY CHI TIẾT (Màu, Ảnh, Lượt bán)
        String productID = (p.getProductID() != null) ? p.getProductID() : p.getId();

        if (productID != null && !productID.isEmpty()) {
            ApiClient.getClient().create(ApiService.class)
                    .getProductDetail(productID)
                    .enqueue(new Callback<MenuResponse.SingleProductResponse>() {
                        @Override
                        public void onResponse(Call<MenuResponse.SingleProductResponse> call, Response<MenuResponse.SingleProductResponse> res) {
                            if (res.isSuccessful() && res.body() != null && res.body().getData() != null) {
                                MenuResponse.MenuItem detail = res.body().getData();

                                // --- A. CẬP NHẬT LƯỢT BÁN (MỚI THÊM) ---
                                /*if (holder.tvSold != null) {
                                    // Giả sử API trả về sumofRatings hoặc totalSold.
                                    // Bạn thay đổi getter này cho khớp với model MenuItem của bạn.
                                    int soldCount = detail.getSumofRatings();
                                    holder.tvSold.setText("Đã bán " + formatCount(soldCount));
                                    holder.tvSold.setVisibility(View.VISIBLE);
                                }*/

                                // --- B. XỬ LÝ ẢNH ---
                                String finalUrl = (detail.getPicture() != null && !detail.getPicture().isEmpty())
                                        ? detail.getPicture() : p.getImageUrl();

                                if (finalUrl != null && !finalUrl.isEmpty()) {
                                    Glide.with(context).load(finalUrl)
                                            .placeholder(R.drawable.placeholder_food)
                                            .error(R.drawable.placeholder_food)
                                            .into(holder.imgProduct);
                                    p.setImageUrl(finalUrl);
                                } else {
                                    loadFallbackImage(holder.imgProduct, p);
                                }

                                // --- C. XỬ LÝ MÀU NỀN ---
                                String hex = detail.getBackgroundHexacode();
                                if (hex != null && !hex.isEmpty()) {
                                    try {
                                        String cleanHex = hex.trim().replace("#", "");
                                        int color = Integer.parseInt(cleanHex, 16) | 0xFF000000;
                                        holder.bg_item_color.setBackgroundColor(color);
                                        p.setColor(color);
                                    } catch (Exception e) {
                                        setFallbackColor(holder.bg_item_color, p);
                                    }
                                } else {
                                    setFallbackColor(holder.bg_item_color, p);
                                }

                            } else {
                                // Fallback nếu API lỗi logic
                                loadFallbackImage(holder.imgProduct, p);
                                setFallbackColor(holder.bg_item_color, p);
                                if (holder.tvSold != null) holder.tvSold.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onFailure(Call<MenuResponse.SingleProductResponse> call, Throwable t) {
                            // Fallback nếu lỗi mạng
                            loadFallbackImage(holder.imgProduct, p);
                            setFallbackColor(holder.bg_item_color, p);
                            if (holder.tvSold != null) holder.tvSold.setVisibility(View.GONE);
                        }
                    });
        } else {
            loadFallbackImage(holder.imgProduct, p);
            setFallbackColor(holder.bg_item_color, p);
            if (holder.tvSold != null) holder.tvSold.setVisibility(View.GONE);
        }
    }

    // --- CÁC HÀM HỖ TRỢ ---

    // Hàm format số lượng: 1200 -> 1.2K
    private String formatCount(int count) {
        if (count < 1000) return String.valueOf(count);
        return String.format("%.1fK+", count / 1000.0);
    }

    private void loadFallbackImage(ImageView img, Product p) {
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            Glide.with(context).load(p.getImageUrl()).placeholder(R.drawable.placeholder_food).into(img);
        } else {
            img.setImageResource(R.drawable.placeholder_food);
        }
    }

    private void setFallbackColor(LinearLayout layout, Product p) {
        if (layout == null) return;
        if (p.getColor() != 0) {
            layout.setBackgroundColor(p.getColor());
            return;
        }
        String cat = p.getCategory() != null ? p.getCategory().toLowerCase() : "";
        int color;
        if (cat.contains("drink")) color = Color.parseColor("#A71317");
        else if (cat.contains("food")) color = Color.parseColor("#388E3C");
        else color = Color.parseColor("#F1BCBC");
        layout.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void updateList(List<Product> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice;
        TextView tvSold; // KHAI BÁO MỚI
        Button btnPlus;
        LinearLayout bg_item_color;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.image_product_cart);
            tvName = itemView.findViewById(R.id.text_name_cart);
            tvPrice = itemView.findViewById(R.id.text_price_cart);
            bg_item_color = itemView.findViewById(R.id.bg_item_color);
            btnPlus = itemView.findViewById(R.id.button_plus);
            tvSold = itemView.findViewById(R.id.tvSold);
        }
    }
}