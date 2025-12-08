package com.example.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.R;
import com.example.demo.models.Product;
import com.example.demo.models.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewItemAdapter extends RecyclerView.Adapter<ReviewItemAdapter.ViewHolder> {

    private List<Product> products;
    private List<Review> reviews = new ArrayList<>(); // Lưu reviews để submit

    public ReviewItemAdapter(List<Product> products) {
        this.products = products;
        // Khởi tạo reviews rỗng cho từng product
        for (Product p : products) {
            reviews.add(new Review(p.getProductID(), 0, ""));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_product, parent, false); // XML item trong RecyclerView
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        Review review = reviews.get(position);

        holder.tvProductName.setText(product.getName());
        Glide.with(holder.itemView.getContext()).load(product.getImageUrl()).into(holder.imgProduct);

        holder.rbProductRating.setRating(review.getRating());
        holder.etComment.setText(review.getComment());

        // Listener để cập nhật review
        holder.rbProductRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> review.setRating(rating));
        holder.etComment.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) review.setComment(holder.etComment.getText().toString());
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public List<Review> getReviews() {
        // Cập nhật cuối cùng trước khi submit
        return reviews;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        RatingBar rbProductRating;
        EditText etComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            rbProductRating = itemView.findViewById(R.id.rbProductRating);
            etComment = itemView.findViewById(R.id.etComment);
        }
    }
}