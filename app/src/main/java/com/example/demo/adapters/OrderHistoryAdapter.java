package com.example.demo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public OrderHistoryAdapter(Context context, List<OrderHistoryResponse.HistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
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

        holder.tvOrderID.setText("Mã: " + info.getOrderID());
        holder.tvDate.setText(info.getDate());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvTotalPrice.setText(formatter.format(info.getTotalPrice()));

        holder.tvStatus.setText(info.getStatus());
        if ("Completed".equalsIgnoreCase(info.getStatus()) || "Success".equalsIgnoreCase(info.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else if ("Failed".equalsIgnoreCase(info.getStatus())) {
            holder.tvStatus.setTextColor(Color.RED);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
        }

        List<OrderHistoryResponse.ProductDetail> pics = item.getPictures();
        List<OrderHistoryResponse.ProductQuantity> prods = item.getProducts();

        if (pics != null && !pics.isEmpty()) {
            Glide.with(context).load(pics.get(0).getImage()).into(holder.imgProductThumb);

            StringBuilder summary = new StringBuilder();

            if (prods != null) {
                for (OrderHistoryResponse.ProductQuantity pq : prods) {
                    // Tìm tên món tương ứng trong mảng pictures
                    String name = "Món lạ";
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
            holder.imgProductThumb.setImageResource(R.drawable.ic_launcher_background); // Ảnh mặc định
            holder.tvProductSummary.setText("Không có thông tin chi tiết");
        }
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderID, tvDate, tvStatus, tvTotalPrice, tvProductSummary;
        ImageView imgProductThumb;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderID = itemView.findViewById(R.id.tvOrderID);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvProductSummary = itemView.findViewById(R.id.tvProductSummary);
            imgProductThumb = itemView.findViewById(R.id.imgProductThumb);
        }
    }
}