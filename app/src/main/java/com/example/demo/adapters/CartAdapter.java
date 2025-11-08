package com.example.demo.adapters;

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
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.MenuResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    private List<CartResponse.CartItem> items;
    private Context context;
    private String userID;
    private OnCartUpdateListener listener;

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    public CartAdapter(List<CartResponse.CartItem> items, String userID, OnCartUpdateListener listener) {
        this.items = items;
        this.userID = userID;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart_confirm, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartResponse.CartItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(String.format("%,d VND", item.getSubtotal()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Load ảnh từ backend
        ApiClient.getClient().create(ApiService.class)
                .getProductDetail(item.getProductID())
                .enqueue(new Callback<MenuResponse.SingleProductResponse>() {
                    @Override
                    public void onResponse(Call<MenuResponse.SingleProductResponse> call, Response<MenuResponse.SingleProductResponse> res) {
                        if (res.isSuccessful() && res.body() != null && res.body().getData() != null) {
                            Glide.with(context).load(res.body().getData().getPicture()).into(holder.img);
                        }
                    }
                    @Override public void onFailure(Call<MenuResponse.SingleProductResponse> call, Throwable t) {}
                });

        holder.btnMinus.setOnClickListener(v -> update(item.getId(), false));
        holder.btnPlus.setOnClickListener(v -> update(item.getId(), true));
        holder.btnDelete.setOnClickListener(v -> delete(item.getId()));
    }

    private void update(String itemId, boolean increase) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", itemId);

        Call<CommonResponse> call = increase ?
                ApiClient.getClient().create(ApiService.class).increaseItem(body) :
                ApiClient.getClient().create(ApiService.class).decreaseItem(body);

        call.enqueue(new Callback<CommonResponse>() {
            @Override public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                if (r.isSuccessful()) listener.onCartUpdated();
            }
            @Override public void onFailure(Call<CommonResponse> call, Throwable t) {}
        });
    }

    private void delete(String itemId) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", itemId);
        ApiClient.getClient().create(ApiService.class).deleteItem(body)
                .enqueue(new Callback<CommonResponse>() {
                    @Override public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                        if (r.isSuccessful()) listener.onCartUpdated();
                    }
                    @Override public void onFailure(Call<CommonResponse> call, Throwable t) {}
                });
    }

    @Override public int getItemCount() { return items.size(); }

    public interface OnAction {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onDelete(CartItem item);
        void onDataChanged();
        void onToggleSelect(CartResponse.CartItem item, boolean selected);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice, tvQuantity;
        ImageButton btnMinus, btnPlus, btnDelete;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.text_name_cart);
            tvPrice = v.findViewById(R.id.text_price_cart);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            btnMinus = v.findViewById(R.id.button_minus);
            btnPlus = v.findViewById(R.id.button_plus);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
