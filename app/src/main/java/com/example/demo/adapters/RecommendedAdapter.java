package com.example.demo.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.models.Product;
import java.util.List;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.VH> {

    private List<Product> list;
    private Context context;
    private OnAddToCartListener listener;

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    public RecommendedAdapter(List<Product> list, OnAddToCartListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_recommended_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = list.get(position);
        holder.tvName.setText(p.getName());
        holder.tvPrice.setText(p.getPrice() + " VND/pc");
        Glide.with(context).load(p.getImageUrl()).into(holder.img);
        holder.leftLayout.setBackgroundTintList(android.content.res.ColorStateList.valueOf(p.getColor()));
        holder.btnAdd.setOnClickListener(v -> listener.onAddToCart(p));
    }

    @Override public int getItemCount() { return list.size(); }

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
}