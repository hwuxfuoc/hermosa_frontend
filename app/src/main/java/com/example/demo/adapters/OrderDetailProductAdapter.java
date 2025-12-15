package com.example.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.models.OrderDetailResponse;
import java.text.DecimalFormat;
import java.util.List;

public class OrderDetailProductAdapter extends RecyclerView.Adapter<OrderDetailProductAdapter.ViewHolder> {

    private List<OrderDetailResponse.ProductItem> list;

    public OrderDetailProductAdapter(List<OrderDetailResponse.ProductItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetailResponse.ProductItem item = list.get(position);

        // 1. Số lượng
        holder.tvQuantity.setText(item.getQuantity() + "x");

        // 2. Tên món (Dữ liệu JSON CÓ trường này nên sẽ hiện OK)
        if (item.getName() != null) {
            holder.tvProductName.setText(item.getName());
        } else {
            holder.tvProductName.setText("Món #" + item.getProductID());
        }

        // 3. Giá tiền (Format đ)
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvPrice.setText(formatter.format(item.getPrice()) + " đ");

        // 4. Ảnh (Dữ liệu JSON KHÔNG CÓ nên sẽ hiện ảnh lỗi/placeholder)
        // Bạn có thể dùng ảnh tĩnh nếu chưa fix được Backend
        Glide.with(holder.itemView.getContext())
                .load(item.getPicture())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuantity, tvProductName, tvPrice;
        ImageView imgProduct;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct); // Đảm bảo ID này có trong item_product_detail.xml
        }
    }
}