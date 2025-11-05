package com.example.demo;

import android.annotation.SuppressLint;
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
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<Product> cartList;

    public CartAdapter(Context context, List<Product> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item mới của giỏ hàng
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartList.get(position);
        holder.textName.setText(product.name);
        holder.textPrice.setText(product.getPrice()); // nếu price là String
        holder.imageProduct.setImageResource(product.imageResId);

        // Hiển thị số lượng hiện tại
        holder.quantityInput.setText(String.valueOf(product.getQuantity()));

        // Nút + tăng số lượng
        holder.buttonPlus.setOnClickListener(v -> {
            int qty = product.getQuantity() + 1;
            product.setQuantity(qty);
            holder.quantityInput.setText(String.valueOf(qty));
            // TODO: cập nhật tổng tiền nếu cần
        });

        // Nút - giảm số lượng
        holder.buttonMinus.setOnClickListener(v -> {
            int qty = product.getQuantity();
            if (qty > 1) { // không cho giảm <1
                qty--;
                product.setQuantity(qty);
                holder.quantityInput.setText(String.valueOf(qty));
            }
            // TODO: cập nhật tổng tiền nếu cần
        });

        // Xử lý sự kiện xóa item
        holder.buttonRemove.setOnClickListener(v -> {
            // Xóa item khỏi danh sách
            cartList.remove(position);
            // Thông báo cho adapter về sự thay đổi
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartList.size());
            // TODO: Cập nhật lại tổng tiền nếu có
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

        @SuppressLint("WrongViewCast")
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.image_product);
            textName = itemView.findViewById(R.id.tvName);
            textPrice = itemView.findViewById(R.id.tvPrice);
            buttonRemove = itemView.findViewById(R.id.btnDelete);
            buttonPlus = itemView.findViewById(R.id.btnPlus);
            buttonMinus = itemView.findViewById(R.id.btnMinus);
            quantityInput = itemView.findViewById(R.id.tvQuantity);
        }
    }
}
