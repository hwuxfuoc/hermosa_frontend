package com.example.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.AddToCartBottomSheet;
import com.example.demo.R;
import com.example.demo.description.DescriptionCake;
import com.example.demo.description.DescriptionDrink;
import com.example.demo.description.DescriptionFood;
import com.example.demo.models.Product;

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

        holder.imgProduct.setImageResource(product.getImageResId());
        holder.tvName.setText(product.getName());
        /*holder.tvPrice.setText(product.getPrice());*/
        String url=product.getImageUrl();
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.imgProduct);
        // holder.tvSold.setText("Đã bán 18K+"); // Nếu có ID text_sold trong XML, thêm vào
        int colorHex=product.getColor();
        if(colorHex!=0){
            holder.bg_item_color.setBackgroundColor(colorHex);
        }

        holder.itemView.setOnClickListener(v -> {
            Class<?> cls = switch (product.getCategory()) {
                case "drink" -> DescriptionDrink.class;
                case "food" -> DescriptionFood.class;
                default -> DescriptionCake.class;
            };

            Intent i = new Intent(context, cls);
            i.putExtra("product", product);
            context.startActivity(i);
        });

        // Bấm btn + → mở BottomSheet
        holder.btnPlus.setOnClickListener(v -> {
            AddToCartBottomSheet sheet = AddToCartBottomSheet.newInstance(product);
            sheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "AddToCart");
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice; // tvSold nếu có
        Button btnPlus;
        LinearLayout bg_item_color;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.image_product_cart);
            tvName = itemView.findViewById(R.id.text_name_cart);
            tvPrice = itemView.findViewById(R.id.text_price_cart);
            bg_item_color=itemView.findViewById(R.id.bg_item_color);
            btnPlus = itemView.findViewById(R.id.button_plus);

        }
    }
}