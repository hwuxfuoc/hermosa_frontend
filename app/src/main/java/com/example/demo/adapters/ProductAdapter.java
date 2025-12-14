package com.example.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.AddToCartBottomSheet;
import com.example.demo.R;
import com.example.demo.description.DescriptionCake;
import com.example.demo.description.DescriptionDrink;
import com.example.demo.description.DescriptionFood;
import com.example.demo.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = new ArrayList<>(productList != null ? productList : new ArrayList<>());
        Log.d("HOME", "Adapter khởi tạo, kích thước ban đầu: " + this.productList.size());
    }

    @Override
    public int getItemViewType(int position) {
        Product product = productList.get(position);
        switch (product.getCategory()) {
            case "drink": return 1;
            case "food": return 2;
            default: return 0;
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        switch (viewType) {
            case 1: layoutId = R.layout.item_product_drink; break;
            case 2: layoutId = R.layout.item_product_food; break;
            default: layoutId = R.layout.item_product_cake; break;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.textName.setText(product.getName());
        holder.textPrice.setText(formatPrice(product.getPrice()));

        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(getPlaceholderDrawable(product.getCategory()))
                    .error(R.drawable.placeholder_cake)
                    .fallback(R.drawable.placeholder_cake)
                    .into(holder.imageProduct);
        } else {
            Glide.with(context)
                    .load(R.drawable.placeholder_cake)
                    .into(holder.imageProduct);
            Log.w("HOME", "URL hình ảnh null hoặc rỗng cho sản phẩm: " + product.getName());
        }

        // Bấm item → mở chi tiết
        holder.itemView.setOnClickListener(v -> {
            Class<?> cls = switch (product.getCategory().toLowerCase()) {
                case "drink" -> DescriptionDrink.class;
                case "food" -> DescriptionFood.class;
                default -> DescriptionCake.class;
            };

            Intent i = new Intent(context, cls);
            i.putExtra("product", product);
            context.startActivity(i);
        });

        // Bấm +
        holder.buttonPlus.setOnClickListener(v -> {
            AddToCartBottomSheet bottomSheet = AddToCartBottomSheet.newInstance(product);
            bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "AddToCart");
        });
    }
    private int getPlaceholderDrawable(String category) {
        return switch (category.toLowerCase()) {
            case "drink" -> R.drawable.placeholder_drink;
            case "food" -> R.drawable.placeholder_food;
            default -> R.drawable.placeholder_cake;
        };
    }

    private String formatPrice(String price) {
        try {
            long p = Long.parseLong(price.replaceAll("[^0-9]", ""));
            return String.format("₫%,d", p);
        } catch (Exception e) {
            return "₫" + price;
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public void updateList(List<Product> newList) {
        if (newList != null) {
            productList.clear();
            productList.addAll(newList);
            Log.d("HOME", "Adapter cập nhật, kích thước mới: " + productList.size());
            notifyDataSetChanged();
        } else {
            Log.w("HOME", "Danh sách mới null, không cập nhật");
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice;
        ImageView imageProduct;
        Button buttonPlus;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textPrice = itemView.findViewById(R.id.text_price);
            imageProduct = itemView.findViewById(R.id.image_product);
            buttonPlus = itemView.findViewById(R.id.button_plus);
        }
    }
}