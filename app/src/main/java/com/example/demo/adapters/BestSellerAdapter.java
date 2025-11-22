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
import com.bumptech.glide.Glide;
import java.util.List;

public class BestSellerAdapter extends RecyclerView.Adapter<BestSellerAdapter.ViewHolder> {

    private Context context;
    private List<Product> list;

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
        Product product = list.get(position);

        // SỬA TẠI ĐÂY: DÙNG GLIDE + URL THAY VÌ setImageResource
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_cake)
                .error(R.drawable.placeholder_cake)
                .into(holder.imgProduct);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(formatPrice(product.getPrice()) + " ₫");

        // Bấm item → mở Description đúng loại
        holder.itemView.setOnClickListener(v -> {
            String cat = Product.normalizeCategory(product.getCategory());
            Class<?> cls = switch (cat) {
                case "drink" -> DescriptionDrink.class;
                case "food"  -> DescriptionFood.class;
                default      -> DescriptionCake.class;
            };
            Intent i = new Intent(context, cls);
            i.putExtra("product", product);
            context.startActivity(i);
        });

        // Bấm +
        holder.btnPlus.setOnClickListener(v -> {
            AddToCartBottomSheet sheet = AddToCartBottomSheet.newInstance(product);
            sheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "AddToCart");
        });
    }
    private String formatPrice(String price) {
        try {
            long p = Long.parseLong(price.replaceAll("[^0-9]", ""));
            return String.format("%,d", p);
        } catch (Exception e) {
            return price;
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice; // tvSold nếu có
        Button btnPlus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.image_product_cart);
            tvName = itemView.findViewById(R.id.text_name_cart);
            tvPrice = itemView.findViewById(R.id.text_price_cart);
            // tvSold = itemView.findViewById(R.id.text_sold); // Thêm nếu có
            btnPlus = itemView.findViewById(R.id.button_plus);
        }
    }
}