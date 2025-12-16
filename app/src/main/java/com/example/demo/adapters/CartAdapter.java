package com.example.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.MenuResponse;
import com.example.demo.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    private List<CartResponse.CartItem> items;
    private Context context;
    private String userID;
    private OnCartUpdateListener listener;
    private OnItemCheckListener checkListener;
    private boolean isEditMode = false;
    private boolean isConfirmMode = false;

    // Interface để báo cho Fragment biết cần update
    public interface OnCartUpdateListener { void onCartUpdated(); }
    public interface OnItemCheckListener { void onUpdateTotal(); }

    // Constructor cho CartFragment
    public CartAdapter(List<CartResponse.CartItem> items, Context context, OnCartUpdateListener listener) {
        this.items = items;
        this.context = context;
        this.userID = SessionManager.getUserID(context);
        this.listener = listener;
    }

    // Constructor cho ConfirmOrderActivity
    public CartAdapter(List<CartResponse.CartItem> items, String userID, OnCartUpdateListener listener) {
        this.items = items;
        this.context = null;
        this.userID = userID;
        this.listener = listener;
    }

    public boolean isEditMode() { return isEditMode; }
    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    public void setConfirmMode(boolean confirmMode) {
        isConfirmMode = confirmMode;
        notifyDataSetChanged();
    }

    public void setCheckListener(OnItemCheckListener checkListener) {
        this.checkListener = checkListener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        View v = LayoutInflater.from(ctx).inflate(R.layout.cart_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartResponse.CartItem item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(String.format("%,d VND", item.getSubtotal()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Checkbox
        holder.cbSelect.setVisibility(isConfirmMode ? View.GONE : View.VISIBLE);
        if (!isConfirmMode) {
            // Gỡ listener trước khi set trạng thái để tránh trigger sai
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(item.isSelected());
        }

        // Nút xóa
        holder.btnDelete.setVisibility(isEditMode || isConfirmMode ? View.VISIBLE : View.GONE);

        // GỌI API LẤY CHI TIẾT SẢN PHẨM → ẢNH + MÀU NỀN TỪ BACKEND
        ApiClient.getClient().create(ApiService.class)
                .getProductDetail(item.getProductID())
                .enqueue(new Callback<MenuResponse.SingleProductResponse>() {
                    @Override
                    public void onResponse(Call<MenuResponse.SingleProductResponse> call, Response<MenuResponse.SingleProductResponse> res) {
                        if (res.isSuccessful() && res.body() != null && res.body().getData() != null) {
                            MenuResponse.MenuItem product = res.body().getData();

                            // Load ảnh
                            if (holder.itemView.getContext() != null) {
                                Glide.with(holder.itemView.getContext())
                                        .load(product.getPicture())
                                        .placeholder(R.drawable.cake_strawberry_cheese)
                                        .into(holder.imgProduct);
                            }

                            String hex = product.getBackgroundHexacode();
                            if (hex != null && !hex.isEmpty()) {
                                try {
                                    int color = parseHexColor(hex);
                                    holder.itemBackground.setCardBackgroundColor(color);
                                } catch (Exception e) {
                                    holder.itemBackground.setCardBackgroundColor(0xFFF0BCBC);
                                }
                            } else {
                                holder.itemBackground.setCardBackgroundColor(0xFFF0BCBC);
                            }
                        } else {
                            holder.itemBackground.setCardBackgroundColor(0xFFF0BCBC);
                        }
                    }

                    @Override
                    public void onFailure(Call<MenuResponse.SingleProductResponse> call, Throwable t) {
                        holder.itemBackground.setCardBackgroundColor(0xFFF0BCBC);
                    }
                });

        // Nút trừ
        holder.btnMinus.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            CartResponse.CartItem currentItem = items.get(pos);
            if (currentItem.getQuantity() > 1) {
                update(currentItem.getId(), false);
            } else {
                delete(currentItem.getId(), holder.itemView.getContext());
            }
        });

        // Nút cộng
        holder.btnPlus.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            update(items.get(pos).getId(), true);
        });

        // Nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            delete(items.get(pos).getId(), holder.itemView.getContext());
        });

        // Xử lý sự kiện Checkbox
        if (!isConfirmMode) {
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    // 1. Cập nhật model
                    items.get(pos).setSelected(isChecked);

                    // 2. Báo cho Fragment tính lại tổng tiền
                    if (checkListener != null) {
                        checkListener.onUpdateTotal();
                    }
                }
            });
        }
    }

    // CHUYỂN HEX "F1BCBC" → int color (0xFFF1BCBC)
    private int parseHexColor(String hex) {
        if (hex == null || hex.isEmpty()) return 0xFFF0BCBC;
        hex = hex.trim().replace("#", "");
        if (hex.length() == 6) {
            return 0xFF000000 | Integer.parseInt(hex, 16);
        }
        return 0xFFF0BCBC; // fallback
    }

    // Cập nhật số lượng
    private void update(String itemId, boolean increase) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", itemId);

        Call<CommonResponse> call = increase ?
                ApiClient.getClient().create(ApiService.class).increaseItem(body) :
                ApiClient.getClient().create(ApiService.class).decreaseItem(body);

        call.enqueue(new Callback<CommonResponse>() {
            @Override public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                if (r.isSuccessful() && listener != null) listener.onCartUpdated();
            }
            @Override public void onFailure(Call<CommonResponse> call, Throwable t) {}
        });
    }

    // Xóa món
    private void delete(String itemId, Context context) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", itemId);

        ApiClient.getClient().create(ApiService.class).deleteItem(body)
                .enqueue(new Callback<CommonResponse>() {
                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                        if (r.isSuccessful()) {
                            int pos = -1;
                            for (int i = 0; i < items.size(); i++) {
                                if (items.get(i).getId().equals(itemId)) {
                                    pos = i;
                                    break;
                                }
                            }
                            if (pos != -1) {
                                items.remove(pos);
                                notifyItemRemoved(pos);
                                if (!items.isEmpty()) {
                                    notifyItemRangeChanged(pos, items.size());
                                }
                            }
                            if (listener != null) listener.onCartUpdated();
                        } else {
                            Toast.makeText(context, "Xóa thất bại: " + r.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {
                        Toast.makeText(context, "Lỗi mạng khi xóa", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        ImageView imgProduct;
        TextView tvName, tvPrice, tvQuantity;
        ImageView btnMinus, btnPlus, btnDelete;
        CardView itemBackground;

        VH(@NonNull View v) {
            super(v);
            cbSelect = v.findViewById(R.id.cbSelect);
            imgProduct = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnDelete = v.findViewById(R.id.btnDelete);
            itemBackground = v.findViewById(R.id.itemBackground);
        }
    }
}