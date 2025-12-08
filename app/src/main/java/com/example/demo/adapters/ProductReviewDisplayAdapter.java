package com.example.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.models.Review;

import java.util.List;

public class ProductReviewDisplayAdapter extends RecyclerView.Adapter<ProductReviewDisplayAdapter.ViewHolder> {

    private List<Review> reviews;

    public ProductReviewDisplayAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_display, parent, false); // Tạo XML mới cho item hiển thị (text only, no edit)
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvUserName.setText(review.getUserName() != null ? review.getUserName() : "Khách hàng");
        holder.tvComment.setText(review.getComment());
        holder.rbRating.setRating(review.getRating());
        holder.tvDate.setText(review.getDate() != null ? review.getDate() : "");
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvComment, tvDate;
        RatingBar rbRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvComment = itemView.findViewById(R.id.tvComment);
            rbRating = itemView.findViewById(R.id.rbRating);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}