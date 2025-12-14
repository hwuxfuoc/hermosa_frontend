/*
package com.example.demo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.models.OrderHistoryResponse;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderHistoryResponse.HistoryItem> historyList;

    // 1. KHAI BÁO LISTENER ĐỂ GỌI SỰ KIỆN RA NGOÀI
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onReorderClick(List<OrderHistoryResponse.ProductQuantity> products);
        void onDetailClick(String orderID);
    }

    // 2. SỬA CONSTRUCTOR ĐỂ NHẬN 3 THAM SỐ (Context, List, Listener)
    public OrderHistoryAdapter(Context context, List<OrderHistoryResponse.HistoryItem> historyList, OnOrderActionListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
    }

    public void setData(List<OrderHistoryResponse.HistoryItem> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderHistoryResponse.HistoryItem item = historyList.get(position);
        if (item == null || item.getOrderInfo() == null) return;

        OrderHistoryResponse.OrderInfo info = item.getOrderInfo();

        // --- GÁN DỮ LIỆU ---
        holder.tvOrderID.setText("OrderID" + info.getOrderID());

        // Xử lý ngày tháng
        String rawDate = info.getDate();
        if(rawDate != null && rawDate.length() > 10) {
            rawDate = rawDate.replace("T", " ").substring(0, 16);
        }
        holder.tvOrderDate.setText(rawDate);

        // Xử lý trạng thái & màu sắc
        String status = info.getStatus();
        holder.tvOrderStatus.setText(status);

        GradientDrawable bgShape = (GradientDrawable) holder.tvOrderStatus.getBackground();
        if(bgShape == null) {
            bgShape = new GradientDrawable();
            bgShape.setCornerRadius(8f);
            holder.tvOrderStatus.setBackground(bgShape);
        }

        if ("done".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)) {
            bgShape.setColor(Color.parseColor("#4CAF50")); // Xanh
            holder.tvOrderStatus.setText("Hoàn thành");
        } else if ("cancelled".equalsIgnoreCase(status) || "failed".equalsIgnoreCase(status)) {
            bgShape.setColor(Color.parseColor("#F44336")); // Đỏ
            holder.tvOrderStatus.setText("Đã hủy");
        } else {
            bgShape.setColor(Color.parseColor("#FF9800")); // Cam
            holder.tvOrderStatus.setText("Đang xử lý");
        }

        // Tiền & Thanh toán
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvOrderTotal.setText(formatter.format(info.getTotalPrice()));

        String paymentMethod = info.getPaymentMethod();
        holder.tvPaymentMethodLabel.setText(paymentMethod != null ? paymentMethod.toUpperCase() : "TIỀN MẶT");

        // Xử lý list sản phẩm
        List<OrderHistoryResponse.ProductDetail> pics = item.getPictures();
        List<OrderHistoryResponse.ProductQuantity> prods = item.getProducts();

        int totalItems = (prods != null) ? prods.size() : 0;
        holder.tvItemCount.setText(totalItems + " món");

        if (pics != null && !pics.isEmpty()) {
            Glide.with(context)
                    .load(pics.get(0).getImage())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgProductThumb);

            StringBuilder summary = new StringBuilder();
            if (prods != null) {
                for (OrderHistoryResponse.ProductQuantity pq : prods) {
                    String name = "Món ăn";
                    for (OrderHistoryResponse.ProductDetail pd : pics) {
                        if (pd.getProductID().equals(pq.getProductID())) {
                            name = pd.getName();
                            break;
                        }
                    }
                    summary.append(name).append(" (x").append(pq.getQuantity()).append("), ");
                }
            }
            String resultText = summary.toString();
            if (resultText.endsWith(", ")) {
                resultText = resultText.substring(0, resultText.length() - 2);
            }
            holder.tvProductSummary.setText(resultText);
        } else {
            holder.tvProductSummary.setText("Đang cập nhật...");
        }

        // 3. BẮT SỰ KIỆN NÚT MUA LẠI
        holder.btnReorder.setOnClickListener(v -> {
            if (listener != null && item.getProducts() != null) {
                // Gọi Interface để Fragment xử lý
                listener.onReorderClick(item.getProducts());
            }
        });

        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) {
                // Truyền OrderID ra ngoài Fragment để gọi API
                listener.onDetailClick(info.getOrderID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderID, tvOrderDate, tvOrderStatus, tvProductSummary, tvItemCount, tvPaymentMethodLabel, tvOrderTotal;
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
}*/
package com.example.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderHistoryResponse.HistoryItem> historyList;
    private OnOrderActionListener listener;

    // Interface giao tiếp với Fragment
    /*public interface OnOrderActionListener {
        void onReorderClick(List<OrderHistoryResponse.ProductQuantity> products);
        void onDetailClick(String orderID);
    }*/
    public interface OnOrderActionListener {
        void onReorderClick(List<OrderHistoryResponse.ProductQuantity> products);
        // Thêm tham số thứ 2 là List ảnh
        void onDetailClick(String orderID, List<OrderHistoryResponse.ProductDetail> pictures);
    }

    public OrderHistoryAdapter(Context context, List<OrderHistoryResponse.HistoryItem> historyList, OnOrderActionListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
    }

    public void setData(List<OrderHistoryResponse.HistoryItem> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderHistoryResponse.HistoryItem item = historyList.get(position);
        if (item == null || item.getOrderInfo() == null) return;

        OrderHistoryResponse.OrderInfo info = item.getOrderInfo();

        // --- GÁN DỮ LIỆU ---
        holder.tvOrderID.setText("OrderID: " + info.getOrderID()); // Thêm dấu # cho đẹp

        // Ngày tháng
        String rawDate = info.getDate();
        if(rawDate != null && rawDate.length() > 10) {
            rawDate = rawDate.replace("T", " ").substring(0, 16);
        }
        holder.tvOrderDate.setText(rawDate);

        // Trạng thái & Màu
        String status = info.getStatus();
        GradientDrawable bgShape = (GradientDrawable) holder.tvOrderStatus.getBackground();
        // Cần mutate() để không bị đổi màu tất cả các item khác
        bgShape = (GradientDrawable) bgShape.mutate();

        if ("done".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
            bgShape.setColor(Color.parseColor("#4CAF50")); // Xanh
            holder.tvOrderStatus.setText("Hoàn thành");
        } else if ("cancelled".equalsIgnoreCase(status)) {
            bgShape.setColor(Color.parseColor("#F44336")); // Đỏ
            holder.tvOrderStatus.setText("Đã hủy");
        } else {
            bgShape.setColor(Color.parseColor("#FF9800")); // Cam
            holder.tvOrderStatus.setText("Đang xử lý");
        }
        holder.tvOrderStatus.setBackground(bgShape);

        // Tiền tệ
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvOrderTotal.setText(formatter.format(info.getTotalPrice()));

        holder.tvPaymentMethodLabel.setText(info.getPaymentMethod() != null ? info.getPaymentMethod().toUpperCase() : "TIỀN MẶT");

        // Xử lý ảnh và tên món
        List<OrderHistoryResponse.ProductDetail> pics = item.getPictures();
        List<OrderHistoryResponse.ProductQuantity> prods = item.getProducts();

        holder.tvItemCount.setText((prods != null ? prods.size() : 0) + " món");

        if (pics != null && !pics.isEmpty()) {
            // Load ảnh đầu tiên
            Glide.with(context)
                    .load(pics.get(0).getImage())
                    .placeholder(R.drawable.ic_launcher_background) // Cần có ảnh này trong drawable
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgProductThumb);

            // Ghép tên các món
            StringBuilder summary = new StringBuilder();
            if (prods != null) {
                for (OrderHistoryResponse.ProductQuantity pq : prods) {
                    String name = "Món ăn";
                    for (OrderHistoryResponse.ProductDetail pd : pics) {
                        if (pd.getProductID().equals(pq.getProductID())) {
                            name = pd.getName();
                            break;
                        }
                    }
                    summary.append(name).append(" (x").append(pq.getQuantity()).append("), ");
                }
            }
            String resultText = summary.toString();
            if (resultText.endsWith(", ")) {
                resultText = resultText.substring(0, resultText.length() - 2);
            }
            holder.tvProductSummary.setText(resultText);
        } else {
            holder.tvProductSummary.setText("Đang cập nhật...");
        }

        // --- XỬ LÝ CLICK ---
        holder.btnReorder.setOnClickListener(v -> {
            if (listener != null) listener.onReorderClick(item.getProducts());
        });

        /*holder.btnDetail.setOnClickListener(v -> {
            Log.d("OrderAdapter", "Click Detail. ID: " + info.getOrderID()); // Kiểm tra Logcat
            if (listener != null) {
                if (info.getOrderID() != null) {
                    listener.onDetailClick(info.getOrderID());
                } else {
                    Toast.makeText(context, "Lỗi: ID đơn hàng rỗng", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) {
                // Truyền cả OrderID và List Pictures
                listener.onDetailClick(info.getOrderID(), item.getPictures());
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderID, tvOrderDate, tvOrderStatus, tvProductSummary, tvItemCount, tvPaymentMethodLabel, tvOrderTotal;
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
}