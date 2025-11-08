package com.example.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.AddToCartBottomSheet;
import com.example.demo.R;
import com.example.demo.description.DescriptionCake;
import com.example.demo.description.DescriptionDrink;
import com.example.demo.description.DescriptionFood;
import com.example.demo.models.Product;

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
        Product product = productList.get(position);
        holder.textName.setText(product.getName());
        holder.textPrice.setText(product.getPrice());
        holder.imageProduct.setImageResource(product.getImageResId());
        holder.viewTopBar.setBackgroundColor(product.getColor());
        holder.buttonPlus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(product.getColor()));

        // Bấm itemView → mở Description
        holder.itemView.setOnClickListener(v -> {
            Class<?> cls;
            switch (product.getCategory()) {
                case "drink":
                    cls = DescriptionDrink.class;
                    break;
                case "food":
                    cls = DescriptionFood.class;
                    break;
                default:
                    cls = DescriptionCake.class;
                    break;
            }

            Intent i = new Intent(context, cls);
            i.putExtra("name", product.getName());
            i.putExtra("price", product.getPrice());
            i.putExtra("imageResId", product.getImageResId());
            i.putExtra("description", product.getDescription());
            i.putExtra("category", product.getCategory());
            context.startActivity(i);
        });

        // Bấm button + → mở BottomSheet trực tiếp
        holder.buttonPlus.setOnClickListener(v -> {
            AddToCartBottomSheet bottomSheet = AddToCartBottomSheet.newInstance(product);
            bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "AddToCart");
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