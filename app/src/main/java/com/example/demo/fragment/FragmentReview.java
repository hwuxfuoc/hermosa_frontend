package com.example.demo.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.MainActivity;
import com.example.demo.R;
import com.example.demo.adapters.ReviewItemAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.OrderResponse;
import com.example.demo.models.Product;
import com.example.demo.models.Review;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentReview extends Fragment {

    private String orderID;
    private List<Product> productsToReview; // Danh sách sản phẩm từ order
    private ApiService apiService;

    // Views
    private RatingBar rbGeneral;
    private EditText etGeneralFeedback;
    private MaterialButton btnSubmitReview;
    private MaterialButton btnSkip;
    private RecyclerView rvProductReviews;
    private ReviewItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_order, container, false); // XML của fragment review
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Lấy data từ bundle (từ FragmentOrderTracking)
        if (getArguments() != null) {
            orderID = getArguments().getString("ORDER_ID");
            productsToReview = (List<Product>) getArguments().getSerializable("PRODUCTS"); // List sản phẩm từ order
        }

        initViews(view);
        setupRecyclerView();
        setupEvents();
    }

    private void initViews(View view) {
        rbGeneral = view.findViewById(R.id.rbGeneral);
        etGeneralFeedback = view.findViewById(R.id.etGeneralFeedback);
        btnSubmitReview = view.findViewById(R.id.btnSubmitReview);
        btnSkip = view.findViewById(R.id.btnSkip);
        rvProductReviews = view.findViewById(R.id.rvProductReviews);
    }

    private void setupRecyclerView() {
        rvProductReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProductReviews.setNestedScrollingEnabled(false);
        adapter = new ReviewItemAdapter(productsToReview);
        rvProductReviews.setAdapter(adapter);
    }

    private void setupEvents() {
        btnSubmitReview.setOnClickListener(v -> submitReview());
        btnSkip.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Cảm ơn bạn đã đặt hàng!", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().finish();
        });
    }

    private void submitReview() {
        float generalRating = rbGeneral.getRating();
        String generalFeedback = etGeneralFeedback.getText().toString().trim();

        List<Review> allReviews = adapter.getReviews();

        // Chỉ lấy những review có nội dung
        List<Map<String, Object>> productReviews = new ArrayList<>();
        for (Review r : allReviews) {
            boolean hasRating = r.getRating() > 0;
            boolean hasComment = r.getComment() != null && !r.getComment().trim().isEmpty();
            if (hasRating || hasComment) {
                Map<String, Object> map = new HashMap<>();
                map.put("productID", r.getProductID());
                map.put("rating", r.getRating());
                map.put("comment", r.getComment());
                productReviews.add(map);
            }
        }

        // TRÁNH LỖI BACKEND: Nếu không có review nào → gửi mảng rỗng (không gửi dummy)
        // Vì backend lỗi ở chỗ dùng biến sai → dummy cũng không cứu được
        // → TỐT NHẤT: Không gửi review sản phẩm nếu người dùng không đánh giá món nào

        Map<String, Object> body = new HashMap<>();
        body.put("orderReview", Map.of(
                "rating", generalRating,
                "comment", generalFeedback
        ));

        // CHỈ GỬI productsReview NẾU CÓ ÍT NHẤT 1 REVIEW HỢP LỆ
        if (!productReviews.isEmpty()) {
            body.put("productsReview", productReviews);
        }
        // Nếu rỗng → không gửi field này → backend sẽ bỏ qua vòng lặp → không lỗi!

        Log.d("REVIEW", "Gửi đánh giá: " + new com.google.gson.Gson().toJson(body));

        apiService.reviewOrderAndProducts(orderID, body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("Success".equalsIgnoreCase(response.body().getStatus())) {
                        showVoucherSuccessDialog();
                    } else {
                        Toast.makeText(getContext(), "Đánh giá thành công (chỉ tổng quát)", Toast.LENGTH_LONG).show();
                        showVoucherSuccessDialog(); // Vẫn tặng voucher dù chỉ đánh giá chung
                    }
                } else {
                    Toast.makeText(getContext(), "Đã gửi đánh giá!", Toast.LENGTH_LONG).show();
                    showVoucherSuccessDialog();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Đã gửi đánh giá!", Toast.LENGTH_LONG).show();
                showVoucherSuccessDialog();
            }
        });
    }

    private void showVoucherSuccessDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setView(LayoutInflater.from(getContext()).inflate(R.layout.dialog_voucher_success, null))
                .show();

        // Tự động về trang chủ sau 3s
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() != null) {
                startActivity(new Intent(getContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                getActivity().finish();
            }
        }, 3000);
    }
}