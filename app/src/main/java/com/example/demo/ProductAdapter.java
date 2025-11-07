package com.example.demo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// ✅ BƯỚC 1: THÊM 2 DÒNG IMPORT
import com.bumptech.glide.Glide;
import java.util.Locale;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public int getItemViewType(int position) {
        Product product = productList.get(position);
        switch (product.getCategory()) {
            case "drink": return 1;
            case "food": return 2;
            default: return 0; // cake
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        switch (viewType) {
            case 1: layoutId = R.layout.item_product_drink; break;
            case 2: layoutId = R.layout.item_product_food; break;
            default: layoutId = R.layout.item_product_cake; break;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position); // Lấy sản phẩm tại vị trí hiện tại
        holder.textName.setText(product.getName()); // Đặt tên sản phẩm vào TextView

        // ✅ BƯỚC 2: SỬA LỖI HIỂN THỊ GIÁ
        // (Code này sẽ parse giá "55000" thành "55.000 VND")
        try {
            double priceValue = Double.parseDouble(product.getPrice());
            holder.textPrice.setText(String.format(Locale.getDefault(), "%,.0f VND", priceValue));
        } catch (Exception e) {
            holder.textPrice.setText(product.getPrice()); // Hiển thị gốc nếu lỗi
        }

        // ✅ BƯỚC 3: SỬA LỖI TẢI ẢNH (Quan trọng nhất)
        // XÓA DÒNG NÀY: holder.imageProduct.setImageResource(product.getImageResId());
        // THAY BẰNG GLIDE:
        Glide.with(context)
                .load(product.getImageResId()) // Tải ID (Glide tự xử lý nếu ID = 0)
                .placeholder(R.drawable.logo_app) // Ảnh chờ
                .error(R.drawable.logo_app)       // Ảnh lỗi (nếu ID = 0 hoặc lỗi)
                .into(holder.imageProduct);


        // (Các dòng còn lại giữ nguyên, chúng đã đúng)
        holder.viewTopBar.setBackgroundColor(product.getColor());
        holder.buttonPlus.setBackgroundColor(product.getColor());
        holder.buttonPlus.setOnClickListener(v -> {
            Intent intent;

            switch (product.getCategory()) {
                case "drink":
                    intent = new Intent(context, DescriptionDrink.class);
                    break;
                case "food":
                    intent = new Intent(context, DescriptionFood.class);
                    break;
                default:
                    intent = new Intent(context, DescriptionCake.class);
                    break;
            }

            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageResId", product.getImageResId());
            intent.putExtra("description", product.getDescription());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList){
        productList.clear();
        productList.addAll(newList);

        // 💡 LƯU Ý TỐI ƯU:
        // Dùng notifyDataSetChanged() rất chậm và gây giật.
        // Bạn nên tìm hiểu về "ListAdapter" và "DiffUtil"
        // để tối ưu hóa hàm này, app sẽ mượt hơn khi lọc.
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice;
        ImageView imageProduct;
        Button buttonPlus;
        View viewTopBar;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textPrice = itemView.findViewById(R.id.text_price);
            imageProduct = itemView.findViewById(R.id.image_product);
            buttonPlus = itemView.findViewById(R.id.button_plus);
            viewTopBar = itemView.findViewById(R.id.view_top_bar);
        }
    }
}