/*
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
}*/
package com.example.demo.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

    private List<Product> productList;
    private List<Review> reviewList; // Lưu kết quả đánh giá

    /*public ReviewItemAdapter(List<Product> productList) {
        this.productList = productList;
        this.reviewList = new ArrayList<>();
        // Khởi tạo review rỗng cho mỗi sản phẩm để tránh null
        for (Product p : productList) {
            Review r = new Review();
            r.setProductID(p.getProductID() != null ? p.getProductID() : p.getId());
            r.setRating(5); // Mặc định 5 sao
            r.setComment("");
            reviewList.add(r);
        }
    }*/
    // Trong ReviewItemAdapter.java

    // Nhớ import android.util.Log;

    public ReviewItemAdapter(List<Product> productList) {
        this.productList = productList;
        this.reviewList = new ArrayList<>();

        // 1. Kiểm tra danh sách đầu vào
        if (productList == null) {
            Log.e("DEBUG_REVIEW", "❌ LỖI NGHIÊM TRỌNG: productList truyền vào Adapter bị NULL!");
            return;
        } else if (productList.isEmpty()) {
            Log.e("DEBUG_REVIEW", "⚠️ CẢNH BÁO: productList truyền vào bị RỖNG (Size = 0).");
        } else {
            Log.d("DEBUG_REVIEW", "✅ Adapter nhận được: " + productList.size() + " sản phẩm.");
        }

        // 2. Duyệt vòng lặp và log chi tiết từng món
        for (int i = 0; i < productList.size(); i++) {
            Product p = productList.get(i);

            // Lấy thông tin để log
            String name = p.getName();
            String id = p.getProductID();
            String url = p.getImageUrl();

            // LOG QUAN TRỌNG: Kiểm tra xem URL ảnh có bị null không
            Log.d("DEBUG_REVIEW", "🔎 Item [" + i + "]: " + name
                    + " | ID: " + id
                    + " | URL Ảnh: " + (url == null ? "NULL (Lỗi ở đây!)" : url));

            // --- Logic khởi tạo Review của bạn ---
            Review r = new Review();

            String pid = p.getProductID();
            if (pid == null || pid.isEmpty()) {
                pid = p.getId();
                Log.w("DEBUG_REVIEW", "   -> ID chính bị thiếu, dùng ID phụ: " + pid);
            }

            r.setProductID(pid);
            r.setRating(5);
            r.setComment("");
            reviewList.add(r);
        }
    }

    public List<Review> getReviews() {
        return reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        Review review = reviewList.get(position);

        holder.tvProductName.setText(product.getName());

        // Load ảnh (giả sử Product có getImageUrl)
        if (product.getImageUrl() != null) {
            Glide.with(holder.itemView.getContext()).load(product.getImageUrl()).into(holder.imgProduct);
        }

        // Listener cho RatingBar
        holder.rbProductRating.setOnRatingBarChangeListener(null); // Xóa listener cũ
        holder.rbProductRating.setRating(review.getRating());
        holder.rbProductRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            review.setRating(rating);
        });

        // Listener cho EditText Comment
        holder.etProductComment.removeTextChangedListener(holder.textWatcher); // Xóa watcher cũ
        holder.etProductComment.setText(review.getComment());

        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                review.setComment(s.toString());
            }
        };
        holder.etProductComment.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        RatingBar rbProductRating;
        EditText etProductComment;
        TextWatcher textWatcher; // Giữ tham chiếu để remove

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            rbProductRating = itemView.findViewById(R.id.rbProductRating);
            etProductComment = itemView.findViewById(R.id.etComment);
        }
    }
}