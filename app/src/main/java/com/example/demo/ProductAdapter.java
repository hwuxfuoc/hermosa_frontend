package com.example.demo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public int getItemViewType(int position) {
        Product product = productList.get(position);
        switch (product.getCategory()) {
            case "drink": return 1;
            case "food": return 2;
            default: return 0; // cake
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
        Product product = productList.get(position); // Lấy sản phẩm tại vị trí hiện tại
        holder.textName.setText(product.getName()); // Đặt tên sản phẩm vào TextView
        holder.textPrice.setText(product.getPrice()); // Đặt giá sản phẩm vào TextView
        holder.imageProduct.setImageResource(product.getImageResId()); // Đặt ảnh sản phẩm vào ImageView
        holder.viewTopBar.setBackgroundColor(product.getColor()); // Đặt màu nền cho viewTopBar
        holder.buttonPlus.setBackgroundColor(product.getColor()); // Đặt màu nền cho buttonPlus
        holder.buttonPlus.setOnClickListener(v -> {
            Intent intent;

            switch (product.getCategory()) {
                case "drink":
                    intent = new Intent(context, DescriptionDrink.class);
                    break;
                case "food":
                    intent = new Intent(context, DescriptionFood.class);
                    break;
                default:
                    intent = new Intent(context, DescriptionCake.class);
                    break;
            }

            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageResId", product.getImageResId());
            intent.putExtra("description", product.getDescription());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList){
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice;
        ImageView imageProduct;
        Button buttonPlus;
        View viewTopBar;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textPrice = itemView.findViewById(R.id.text_price);
            imageProduct = itemView.findViewById(R.id.image_product);
            buttonPlus = itemView.findViewById(R.id.button_plus);
            viewTopBar = itemView.findViewById(R.id.view_top_bar);
        }
    }
}
