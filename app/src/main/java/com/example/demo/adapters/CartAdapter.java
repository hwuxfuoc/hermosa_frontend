/*
// CartAdapter.java
// This adapter handles displaying cart items in both CartFragment (with checkboxes for selection) and ConfirmOrderActivity (without checkboxes, for confirmation).
// It supports edit mode for deletion and confirm mode to hide checkboxes.
// Logic is synced with backend: updates (increase/decrease/delete) call API endpoints from carts.js.
// Colors are set based on category for UI consistency.
// Bổ sung: Màu background riêng cho từng sản phẩm, lấy từ ProductData.java (tra cứu theo name hoặc productID)

package com.example.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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

        // Bổ sung: Màu background riêng cho từng sản phẩm, tra cứu từ ProductData.java
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
            int pos = holder.getAdapterPosition(); // Fix lint warning: Use getAdapterPosition()
            if (pos == RecyclerView.NO_POSITION) return;

            CartResponse.CartItem currentItem = items.get(pos);
            if (currentItem.getQuantity() > 1) {
                update(currentItem.getId(), false); // Call /update-decrease from carts.js
            } else {
                delete(currentItem.getId()); // Call /delete from carts.js
            }
        });

        // Handle plus button: Increase quantity
        holder.btnPlus.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition(); // Fix lint warning
            if (pos == RecyclerView.NO_POSITION) return;

            update(items.get(pos).getId(), true); // Call /update-increase from carts.js
        });

        // Handle delete button: Remove item
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition(); // Fix lint warning
            if (pos == RecyclerView.NO_POSITION) return;

            delete(items.get(pos).getId()); // Call /delete from carts.js
            items.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, items.size()); // FIX hiệu ứng mượt
            if (listener != null) listener.onCartUpdated(); // Notify to refresh total/UI
        });

        // Handle checkbox change (only in Cart mode, not Confirm)
        if (!isConfirmMode && checkListener != null) {
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = holder.getAdapterPosition(); // Fix lint warning
                if (pos == RecyclerView.NO_POSITION) return;

                items.get(pos).setSelected(isChecked); // Update selection state (local, for UI filtering)
                checkListener.onUpdateTotal(); // Update total based on selected items
            });
        }
    }

    // Bổ sung: Hàm tra cứu màu background riêng từ ProductData.java (dựa trên name và category)
    // Bổ sung: Màu riêng theo tên sản phẩm (từ ProductData)
    private int getColorByProduct(String name, String category) {
        List<Product> allProducts = ProductData.getAllProducts();
        for (Product p : allProducts) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p.getColor(); // Màu riêng của từng món (ưu tiên cao nhất)
            }
        }
        // Nếu không có trong ProductData → dùng màu theo category
        return getCategoryColor(category);
    }

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
    private void delete(String itemId) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", itemId);
        ApiClient.getClient().create(ApiService.class).deleteItem(body)
                .enqueue(new Callback<CommonResponse>() {
                    @Override public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                        if (r.isSuccessful() && listener != null) listener.onCartUpdated();
                    }
                    @Override public void onFailure(Call<CommonResponse> call, Throwable t) {}
                });
    }

    @Override public int getItemCount() { return items.size(); }

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
// CartAdapter.java
// This adapter handles displaying cart items in both CartFragment (with checkboxes for selection) and ConfirmOrderActivity (without checkboxes, for confirmation).
// It supports edit mode for deletion and confirm mode to hide checkboxes.
// Logic is synced with backend: updates (increase/decrease/delete) call API endpoints from carts.js.
// Colors are set based on category for UI consistency.
// Bổ sung: Màu background riêng cho từng sản phẩm, lấy từ ProductData.java (tra cứu theo name hoặc productID)

package com.example.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
                delete(currentItem.getId()); // Call /delete
            }
        });

        // Handle plus button: Increase quantity
        holder.btnPlus.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            update(items.get(pos).getId(), true); // Call /update-increase
        });

        // Handle delete button: Remove item
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            delete(items.get(pos).getId());
            items.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, items.size());
            if (listener != null) listener.onCartUpdated();
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
    private void delete(String itemId) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", itemId);
        ApiClient.getClient().create(ApiService.class).deleteItem(body)
                .enqueue(new Callback<CommonResponse>() {
                    @Override public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                        if (r.isSuccessful() && listener != null) listener.onCartUpdated();
                    }
                    @Override public void onFailure(Call<CommonResponse> call, Throwable t) {}
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
}