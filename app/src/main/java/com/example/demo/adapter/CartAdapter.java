package com.example.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.model.CartItem;
import com.example.demo.model.CommonResponse;
import com.example.demo.model.MenuResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    private final List<CartItem> items;
    private final Context context;
    private final String userID;
    private final OnAction listener;

    // Interface chính – FragmentCart sẽ implement
    public interface OnAction {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onDelete(CartItem item);
        void onToggleSelect(CartItem item, boolean selected);
        void onDataChanged();
    }

    public CartAdapter(Context context, List<CartItem> items, String userID, OnAction listener) {
        this.context = context;
        this.items = items;
        this.userID = userID;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartItem item = items.get(position); // ĐÚNG KIỂU!

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(String.format("%,d đ", item.getSubtotal()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Load ảnh từ URL đã có trong CartItem (được set từ FragmentCart)
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    //.placeholder(R.drawable.placeholder)
                    .into(holder.img);
        }

        // Checkbox
        holder.cbSelect.setChecked(item.isSelected());
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            listener.onToggleSelect(item, isChecked);
        });

        // Nút tăng/giảm/xóa
        holder.btnMinus.setOnClickListener(v -> listener.onDecrease(item));
        holder.btnPlus.setOnClickListener(v -> listener.onIncrease(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));

        // Background color
        holder.itemView.setBackgroundColor(item.getColor());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice, tvQuantity;
        ImageButton btnMinus, btnPlus, btnDelete;
        CheckBox cbSelect;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.text_name_cart);
            tvPrice = v.findViewById(R.id.text_price_cart);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            btnMinus = v.findViewById(R.id.button_minus);
            btnPlus = v.findViewById(R.id.button_plus);
            btnDelete = v.findViewById(R.id.btnDelete);
            cbSelect = v.findViewById(R.id.cbSelect);
        }
    }
}