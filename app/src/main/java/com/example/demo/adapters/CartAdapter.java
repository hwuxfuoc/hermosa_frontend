/*
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
import com.example.demo.ProductData; // Bổ sung: Import để lấy màu background riêng
import com.example.demo.models.Product; // Bổ sung: Để tra cứu Product
import com.example.demo.utils.SessionManager;
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
    private OnItemCheckListener checkListener;
    private boolean isEditMode = false;
    private boolean isConfirmMode = false;

    public interface OnCartUpdateListener { void onCartUpdated(); }
    public interface OnItemCheckListener { void onUpdateTotal(); }

    // Constructor dùng trong CartFragment (lấy userID từ Session)
    public CartAdapter(List<CartResponse.CartItem> items, Context context, OnCartUpdateListener listener) {
        this.items = items;
        this.context = context;
        this.userID = SessionManager.getUserID(context);
        this.listener = listener;
    }

    // Constructor dùng trong ConfirmOrderActivity (có userID rõ ràng)
    public CartAdapter(List<CartResponse.CartItem> items, String userID, OnCartUpdateListener listener) {
        this.items = items;
        this.context = null;
        this.userID = userID;
        this.listener = listener;
    }

    // Public getter for isEditMode (to fix private access error)
    public boolean isEditMode() {
        return isEditMode;
    }

    // Enable/disable edit mode (shows delete button in Cart)
    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    // Enable/disable confirm mode (hides checkboxes in ConfirmOrderActivity)
    public void setConfirmMode(boolean confirmMode) {
        isConfirmMode = confirmMode;
        notifyDataSetChanged();
    }

    // Public setter cho checkListener
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
            holder.cbSelect.setChecked(item.isSelected());
        }

        // Delete button
        holder.btnDelete.setVisibility(isEditMode || isConfirmMode ? View.VISIBLE : View.GONE);

        // Bổ sung: Màu background riêng cho từng sản phẩm
        // Ưu tiên: Tìm theo tên sản phẩm trong ProductData → nếu không có → dùng màu category
        int color = getColorByProduct(item.getName(), item.getCategory());
        holder.itemBackground.setCardBackgroundColor(color);

        // Load ảnh từ backend using /menu/product endpoint (from menu.js)
        ApiClient.getClient().create(ApiService.class)
                .getProductDetail(item.getProductID())
                .enqueue(new Callback<MenuResponse.SingleProductResponse>() {
                    @Override
                    public void onResponse(Call<MenuResponse.SingleProductResponse> call, Response<MenuResponse.SingleProductResponse> res) {
                        if (res.isSuccessful() && res.body() != null && res.body().getData() != null) {
                            Glide.with(holder.itemView.getContext())
                                    .load(res.body().getData().getPicture())
                                    .placeholder(R.drawable.cake_strawberry_cheese)
                                    .into(holder.imgProduct);
                        }
                    }
                    @Override public void onFailure(Call<MenuResponse.SingleProductResponse> call, Throwable t) {}
                });

        // Handle minus button: Decrease quantity or delete if quantity reaches 1
        holder.btnMinus.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            CartResponse.CartItem currentItem = items.get(pos);
            if (currentItem.getQuantity() > 1) {
                update(currentItem.getId(), false); // Call /update-decrease
            } else {
                // FIX: Truyền thêm Context vào delete()
                delete(currentItem.getId(), holder.itemView.getContext());
            }
        });

        // Handle plus button: Increase quantity
        holder.btnPlus.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            update(items.get(pos).getId(), true); // Call /update-increase
        });

        // Handle delete button: Remove item → CHỈ GỌI API, KHÔNG ĐỘNG GÌ LOCAL
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            delete(items.get(pos).getId(), holder.itemView.getContext()); // Truyền context
        });

        // Handle checkbox change (only in Cart mode, not Confirm)
        if (!isConfirmMode && checkListener != null) {
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                items.get(pos).setSelected(isChecked);
                checkListener.onUpdateTotal();
            });
        }
    }

    // Bổ sung: Hàm tra cứu màu background riêng từ ProductData.java
    // Ưu tiên: Tìm theo tên sản phẩm (chính xác)
    // Nếu không tìm thấy → dùng màu theo category
    private int getColorByProduct(String name, String category) {
        if (name == null) return getCategoryColor(category);

        List<Product> allProducts = ProductData.getAllProducts();
        for (Product p : allProducts) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                return p.getColor(); // Trả về màu riêng của sản phẩm
            }
        }
        // Không tìm thấy → dùng màu theo category
        return getCategoryColor(category);
    }

    // Hàm giữ nguyên: Màu theo category (fallback)
    private int getCategoryColor(String category) {
        if (category == null) return 0xFFF0BCBC;
        String cat = category.toLowerCase();
        if ("cake".equals(cat)) return 0xFFF1BCBC;
        if ("drink".equals(cat)) return 0xFFFF6B6B;
        if ("food".equals(cat)) return 0xFFE1B55C;
        return 0xFFF0BCBC;
    }

    // Update quantity via backend API
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

    // Delete item via backend API
    private void delete(String itemId, Context context) {  // Thêm tham số Context
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

                                // FIX: Chỉ gọi notifyItemRangeChanged nếu còn item
                                if (!items.isEmpty()) {
                                    notifyItemRangeChanged(pos, items.size());
                                }
                                // Nếu list rỗng → không cần notify gì thêm
                            }

                            if (listener != null) {
                                listener.onCartUpdated(); // Activity sẽ reload từ API → xử lý list rỗng
                            }
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
        return items != null ? items.size() : 0; // An toàn khi items == null
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        ImageView imgProduct;
        TextView tvName, tvPrice, tvQuantity;
        ImageButton btnMinus, btnPlus, btnDelete;
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
}*/
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
                            Glide.with(holder.itemView.getContext())
                                    .load(product.getPicture())
                                    .placeholder(R.drawable.cake_strawberry_cheese)
                                    .into(holder.imgProduct);

                            // LẤY MÀU NỀN TỪ backgroundHexacode (backend)
                            String hex = product.getBackgroundHexacode();
                            if (hex != null && !hex.isEmpty()) {
                                try {
                                    int color = parseHexColor(hex);
                                    holder.itemBackground.setCardBackgroundColor(color);
                                } catch (Exception e) {
                                    holder.itemBackground.setCardBackgroundColor(0xFFF0BCBC); // fallback
                                }
                            } else {
                                holder.itemBackground.setCardBackgroundColor(0xFFF0BCBC); // fallback
                            }
                        } else {
                            // Nếu API lỗi → fallback
                            holder.itemBackground.setCardBackgroundColor(0xFFF0BCBC);
                        }
                    }

                    @Override
                    public void onFailure(Call<MenuResponse.SingleProductResponse> call, Throwable t) {
                        // Lỗi mạng → fallback
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

        // Checkbox thay đổi
        if (!isConfirmMode && checkListener != null) {
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                items.get(pos).setSelected(isChecked);
                checkListener.onUpdateTotal();
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
        ImageButton btnMinus, btnPlus, btnDelete;
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