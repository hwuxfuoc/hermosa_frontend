package com.example.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.demo.models.MapboxSuggestionResponse;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    private List<MapboxSuggestionResponse.SuggestionItem> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MapboxSuggestionResponse.SuggestionItem item);
    }

    public SuggestionAdapter(List<MapboxSuggestionResponse.SuggestionItem> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout mặc định đơn giản của Android để hiển thị dòng chữ
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MapboxSuggestionResponse.SuggestionItem item = list.get(position);
        holder.tvName.setText(item.name); // Hiển thị tên địa điểm đầy đủ

        // Bắt sự kiện khi chọn 1 địa chỉ
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(android.R.id.text1);
        }
    }

    // Hàm cập nhật dữ liệu mới khi gõ phím
    public void updateData(List<MapboxSuggestionResponse.SuggestionItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }
}