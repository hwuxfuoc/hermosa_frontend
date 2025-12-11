package com.example.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.models.OrderHistoryResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private final Context context;
    private final List<OrderHistoryResponse.OrderItem> orderList;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("vi"));

    public OrderHistoryAdapter(Context context, List<OrderHistoryResponse.OrderItem> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder h, int position) {
        OrderHistoryResponse.OrderItem order = orderList.get(position);

        // Mã đơn + ngày
        h.tvOrderID.setText(order.getOrderID() != null ? order.getOrderID() : "#ORD-???");
        h.tvOrderDate.setText(dateFormat.format(new Date(order.getCreateAt())));

        // Trạng thái
        String status = order.getStatus();
        h.tvOrderStatus.setText(getStatusText(status));
        h.tvOrderStatus.setBackgroundResource(getStatusBgRes(status));

        // Tổng tiền
        h.tvOrderTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", order.getFinalTotal()));

        // Phương thức thanh toán
        String payment = order.getPaymentMethod();
        h.tvPaymentMethodLabel.setText(payment != null && !payment.isEmpty() ? payment : "Tiền mặt");

        // Danh sách sản phẩm tóm tắt
        List<OrderHistoryResponse.OrderItem.ProductItem> products = order.getProducts();
        if (products != null && !products.isEmpty()) {
            StringBuilder summary = new StringBuilder();
            int totalQty = 0;

            int maxShow = Math.min(3, products.size());
            for (int i = 0; i < maxShow; i++) {
                OrderHistoryResponse.OrderItem.ProductItem p = products.get(i);
                if (i > 0) summary.append(", ");
                summary.append(p.getName()).append(" (x").append(p.getQuantity()).append(")");
                totalQty += p.getQuantity();
            }
            if (products.size() > 3) summary.append("...");

            h.tvProductSummary.setText(summary.toString());
            h.tvItemCount.setText(totalQty + " món");

            // Ảnh sản phẩm đầu tiên
            String imgUrl = products.get(0).getImage();
            Glide.with(context)
                    .load(imgUrl)
                    .placeholder(R.drawable.placeholder_food)  // Đảm bảo bạn đã tạo drawable này
                    .error(R.drawable.ic_launcher_foreground)
                    .into(h.imgProductThumb);
        } else {
            h.tvProductSummary.setText("Không có sản phẩm");
            h.tvItemCount.setText("0 món");
            h.imgProductThumb.setImageResource(R.drawable.cake_strawberry_cheese);
        }

        // Nút Xem chi tiết (tạm thời dùng Toast vì chưa có OrderDetailActivity)
        h.btnDetail.setOnClickListener(v -> {
            Toast.makeText(context, "Chi tiết đơn hàng: " + order.getOrderID(), Toast.LENGTH_LONG).show();

            // Khi bạn tạo xong OrderDetailActivity, bỏ comment đoạn sau:
            // Intent intent = new Intent(context, OrderDetailActivity.class);
            // intent.putExtra("orderID", order.getOrderID());
            // context.startActivity(intent);
        });

        // Nút Mua lại (tạm thời thông báo)
        h.btnReorder.setOnClickListener(v -> {
            Toast.makeText(context, "Tính năng mua lại đang phát triển", Toast.LENGTH_SHORT).show();
            // TODO: Implement thêm lại sản phẩm vào giỏ hàng
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderID, tvOrderDate, tvOrderStatus;
        TextView tvProductSummary, tvItemCount, tvPaymentMethodLabel, tvOrderTotal;
        ImageView imgProductThumb;
        Button btnDetail, btnReorder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderID = itemView.findViewById(R.id.tvOrderID);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvProductSummary = itemView.findViewById(R.id.tvProductSummary);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvPaymentMethodLabel = itemView.findViewById(R.id.tvPaymentMethodLabel);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            imgProductThumb = itemView.findViewById(R.id.imgProductThumb);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnReorder = itemView.findViewById(R.id.btnReorder);
        }
    }

    // Helper: Chuyển trạng thái tiếng Anh → tiếng Việt
    private String getStatusText(String status) {
        if (status == null) return "Không rõ";
        return switch (status.toLowerCase()) {
            case "pending" -> "Chờ xác nhận";
            case "confirmed" -> "Đã xác nhận";
            case "preparing" -> "Đang chuẩn bị";
            case "delivering" -> "Đang giao";
            case "done", "completed" -> "Hoàn thành";
            case "cancelled", "canceled" -> "Đã hủy";
            default -> status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        };
    }

    // Helper: Background theo trạng thái
    private int getStatusBgRes(String status) {
        if (status == null) return R.drawable.bg_status_pending;
        return switch (status.toLowerCase()) {
            case "pending" -> R.drawable.bg_status_pending;
            case "done", "completed" -> R.drawable.bg_status_done;
            case "cancelled", "canceled" -> R.drawable.bg_status_cancelled;
            default -> R.drawable.bg_status_pending;
        };
    }
}