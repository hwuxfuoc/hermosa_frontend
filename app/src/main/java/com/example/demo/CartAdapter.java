package com.example.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // ✅ THÊM: Import thư viện Glide
import com.example.demo.model.Product;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<Product> cartList;
    // ✅ KHUYẾN KHÍCH: Thêm interface để giao tiếp với Activity
    private OnCartActionListener listener;

    // ✅ KHUYẾN KHÍCH: Sửa constructor để nhận interface
    public CartAdapter(Context context, List<Product> cartList, OnCartActionListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_cake, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartList.get(position);

        // ✅ SỬA 1: Sử dụng getter để lấy dữ liệu
        holder.textName.setText(product.getName());

        // ✅ SỬA 3: Chuyển giá trị số thành chuỗi và định dạng tiền tệ
        // Giả sử getPrice() trả về một số (int, double, float)
        holder.textPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", product.getPrice()));

        // ✅ KHUYẾN KHÍCH: Dùng Glide để tải ảnh từ URL thay vì resource ID
        // Glide.with(context).load(product.getImageUrl()).into(holder.imageProduct);
        // Nếu vẫn dùng resourceId:
        holder.imageProduct.setImageResource(product.getImageResId());


        holder.quantityInput.setText(String.valueOf(product.getQuantity()));

        // Nút + tăng số lượng
        holder.buttonPlus.setOnClickListener(v -> {
            int qty = product.getQuantity() + 1;
            product.setQuantity(qty);
            holder.quantityInput.setText(String.valueOf(qty));
            if (listener != null) listener.onItemQuantityChanged(product); // ✅ Thông báo cho Activity
        });

        // Nút - giảm số lượng
        holder.buttonMinus.setOnClickListener(v -> {
            int qty = product.getQuantity();
            if (qty > 1) {
                qty--;
                product.setQuantity(qty);
                holder.quantityInput.setText(String.valueOf(qty));
                if (listener != null) listener.onItemQuantityChanged(product); // ✅ Thông báo cho Activity
            }
        });

        // Xử lý sự kiện xóa item
        holder.buttonRemove.setOnClickListener(v -> {
            // ✅ KHUYẾN KHÍCH: Không tự xóa trong Adapter, hãy báo cho Activity/Fragment
            if (listener != null) {
                listener.onItemRemoved(product, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    // ViewHolder cho CartAdapter
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textName, textPrice;
        ImageButton buttonRemove;
        Button buttonPlus;
        Button buttonMinus;
        EditText quantityInput;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.image_product_cart);
            textName = itemView.findViewById(R.id.text_name_cart);
            textPrice = itemView.findViewById(R.id.text_price_cart);
            buttonRemove = itemView.findViewById(R.id.button_remove_cart);

            // ✅ SỬA 2: Ánh xạ các view còn thiếu
            buttonPlus = itemView.findViewById(R.id.button_plus);
            buttonMinus = itemView.findViewById(R.id.button_minus);
            quantityInput = itemView.findViewById(R.id.quantity_input);
        }
    }

    // ✅ KHUYẾN KHÍCH: Định nghĩa interface để giao tiếp ngược lại
    public interface OnCartActionListener {
        void onItemRemoved(Product product, int position);
        void onItemQuantityChanged(Product product);
    }
}
