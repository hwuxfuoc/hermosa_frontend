package com.example.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.models.Voucher;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    private Context context;
    private List<Voucher> list;
    private OnVoucherClickListener listener;
    private Voucher selectedVoucher = null;

    public interface OnVoucherClickListener {
        void onVoucherClick(Voucher voucher);
    }

    public VoucherAdapter(Context context, List<Voucher> list, OnVoucherClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void setSelectedVoucher(Voucher voucher) {
        this.selectedVoucher = voucher;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Voucher voucher = list.get(position);

        holder.tvVoucherTitle.setText(voucher.getDiscountDisplay());

        String minSpend = String.format("%,.0f", voucher.getMinPurchaseAmount());
        holder.tvVoucherDesc.setText("Đơn hàng từ " + minSpend + " VND");

        String dateStr = formatDate(voucher.getValidTo());
        holder.tvExpiry.setText("HSD: " + dateStr);

        if (selectedVoucher != null && selectedVoucher.getVoucherCode().equals(voucher.getVoucherCode())) {
            holder.btnUseVoucher.setText("Đã chọn");
            holder.btnUseVoucher.setBackgroundColor(context.getResources().getColor(R.color.smoothie_strawberry));
            holder.btnUseVoucher.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.cardContainer.setStrokeColor(context.getResources().getColor(R.color.smoothie_strawberry));
            holder.cardContainer.setStrokeWidth(3);
        } else {
            holder.btnUseVoucher.setText("Dùng");
            holder.btnUseVoucher.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.btnUseVoucher.setTextColor(0xFFB71C1C);
            holder.cardContainer.setStrokeColor(0xFFB71C1C);
            holder.cardContainer.setStrokeWidth(1);
        }

        View.OnClickListener clickAction = v -> listener.onVoucherClick(voucher);

        holder.btnUseVoucher.setOnClickListener(clickAction);
        holder.itemView.setOnClickListener(clickAction);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = inputFormat.parse(isoDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvVoucherTitle, tvVoucherDesc, tvExpiry, tvExpireTag;
        MaterialButton btnUseVoucher;
        com.google.android.material.card.MaterialCardView cardContainer;
        RelativeLayout layoutLeftColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID
            tvVoucherTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvVoucherDesc = itemView.findViewById(R.id.tvVoucherDesc);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            tvExpireTag = itemView.findViewById(R.id.tvExpireTag);
            btnUseVoucher = itemView.findViewById(R.id.btnUseVoucher);
            cardContainer = itemView.findViewById(R.id.cardContainer);
            layoutLeftColor = itemView.findViewById(R.id.layoutLeftColor);
        }
    }
}