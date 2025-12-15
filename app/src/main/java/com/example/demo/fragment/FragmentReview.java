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
    private List<Product> productsToReview;
    private ApiService apiService;

    private RatingBar rbGeneral;
    private EditText etGeneralFeedback;
    private MaterialButton btnSubmitReview;
    private MaterialButton btnSkip;
    private RecyclerView rvProductReviews;
    private ReviewItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient().create(ApiService.class);

        if (getArguments() != null) {
            orderID = getArguments().getString("ORDER_ID");
            ArrayList<Product> temp = (ArrayList<Product>) getArguments().getSerializable("PRODUCTS");
            if (temp != null) {
                productsToReview = new ArrayList<>(temp);
            }
        }

        // BẮT BUỘC KIỂM TRA
        if (orderID == null || productsToReview == null || productsToReview.isEmpty()) {
            Toast.makeText(getContext(), "Không có dữ liệu đơn hàng để đánh giá", Toast.LENGTH_LONG).show();
            if (getActivity() != null) {
                getActivity().finish(); // hoặc quay về
            }
            return;
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
        String generalFeedback = etGeneralFeedback.getText().toString().trim();

        List<Review> allReviews = adapter.getReviews();

        List<Map<String, Object>> productReviews = new ArrayList<>();
        for (Review r : allReviews) {
            boolean hasRating = r.getRating() > 0;
            boolean hasComment = r.getComment() != null && !r.getComment().trim().isEmpty();

            if (hasRating || hasComment) {
                Map<String, Object> map = new HashMap<>();
                map.put("productID", r.getProductID());
                map.put("rating", r.getRating());
                map.put("comment", r.getComment() != null ? r.getComment() : "");
                productReviews.add(map);
            }
        }

        // ĐÚNG ĐỊNH DẠNG BODY THEO BACKEND
        Map<String, Object> body = new HashMap<>();

        // orderReview là STRING (chỉ comment chung), không phải object
        body.put("orderReview", generalFeedback);

        // Chỉ gửi productsReview nếu có ít nhất 1 sản phẩm được đánh giá
        if (!productReviews.isEmpty()) {
            body.put("productsReview", productReviews);
        }

        // Nếu người dùng không nhập gì cho đơn hàng và không đánh giá sản phẩm nào → có thể cho phép bỏ qua
        if (generalFeedback.isEmpty() && productReviews.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đánh giá ít nhất một sản phẩm hoặc để lại nhận xét cho đơn hàng", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("REVIEW", "Body gửi đi: " + new com.google.gson.Gson().toJson(body));

        apiService.reviewOrderAndProducts(orderID, body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("Success".equalsIgnoreCase(response.body().getStatus())) {
                        Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_LONG).show();
                        showVoucherSuccessDialog();
                    } else {
                        Toast.makeText(getContext(), "Đánh giá thất bại: " + response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi server: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showVoucherSuccessDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setView(LayoutInflater.from(getContext()).inflate(R.layout.dialog_voucher_success, null))
                .show();

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() != null) {
                startActivity(new Intent(getContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                getActivity().finish();
            }
        }, 3000);
    }
}