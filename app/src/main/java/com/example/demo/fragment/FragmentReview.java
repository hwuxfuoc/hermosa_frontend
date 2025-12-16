package com.example.demo.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
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
import com.example.demo.models.MenuResponse;
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
    private View line1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_order, container, false); // XML của fragment review
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient().create(ApiService.class);

        if (getArguments() != null) {
            orderID = getArguments().getString("ORDER_ID");
            productsToReview = (List<Product>) getArguments().getSerializable("PRODUCTS");
        }

        initViews(view);
        setupRecyclerView();
        setupEvents();
        loadImagesFromApi();
        line1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
    }
    private void loadImagesFromApi() {
        // Kiểm tra danh sách rỗng thì thôi
        if (productsToReview == null || productsToReview.isEmpty()) return;

        // Duyệt qua từng sản phẩm trong danh sách
        for (int i = 0; i < productsToReview.size(); i++) {
            Product product = productsToReview.get(i);
            final int position = i; // Lưu vị trí để cập nhật lại giao diện dòng này

            // Chỉ gọi API nếu chưa có ảnh hoặc link ảnh bị lỗi (không có http)
            if (product.getImageUrl() == null || product.getImageUrl().isEmpty() || !product.getImageUrl().startsWith("http")) {

                String pid = product.getProductID(); // Lấy mã sản phẩm (ví dụ: C01)

                // Gọi API lấy chi tiết sản phẩm
                apiService.getProductDetail(pid).enqueue(new Callback<MenuResponse.SingleProductResponse>() {
                    @Override
                    public void onResponse(Call<MenuResponse.SingleProductResponse> call, Response<MenuResponse.SingleProductResponse> response) {
                        // Kiểm tra Fragment còn sống không (tránh crash)
                        if (!isAdded() || getContext() == null) return;

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                            // Lấy dữ liệu từ Server trả về (MenuItem)
                            MenuResponse.MenuItem itemFromServer = response.body().getData();

                            // Lấy link ảnh chuẩn
                            String realImageUrl = itemFromServer.getPicture(); // Hoặc getImageUrl() tùy model MenuItem của bạn

                            // Cập nhật vào Product hiện tại
                            product.setImageUrl(realImageUrl);

                            // Báo cho Adapter biết dòng này đã có ảnh mới -> Vẽ lại
                            if (adapter != null) {
                                adapter.notifyItemChanged(position);
                            }

                            Log.d("REVIEW_IMG", "Đã tải ảnh cho " + product.getName() + ": " + realImageUrl);
                        }
                    }

                    @Override
                    public void onFailure(Call<MenuResponse.SingleProductResponse> call, Throwable t) {
                    }
                });
            }
        }
    }

    private void initViews(View view) {
        rbGeneral = view.findViewById(R.id.rbGeneral);
        etGeneralFeedback = view.findViewById(R.id.etGeneralFeedback);
        btnSubmitReview = view.findViewById(R.id.btnSubmitReview);
        btnSkip = view.findViewById(R.id.btnSkip);
        rvProductReviews = view.findViewById(R.id.rvProductReviews);
        line1 = view.findViewById(R.id.line1);
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
        // 1. Lấy dữ liệu từ giao diện
        float generalRating = rbGeneral.getRating();
        String generalFeedback = etGeneralFeedback.getText().toString().trim();

        // 2. Chuẩn bị danh sách đánh giá sản phẩm (giữ nguyên logic cũ)
        List<Review> allReviews = adapter.getReviews();
        List<Map<String, Object>> productReviews = new ArrayList<>();

        for (Review r : allReviews) {
            // Chỉ lấy những sản phẩm có đánh giá hoặc có comment
            if (r.getRating() > 0 || (r.getComment() != null && !r.getComment().isEmpty())) {
                Map<String, Object> map = new HashMap<>();
                map.put("productID", r.getProductID());
                map.put("rating", r.getRating());
                map.put("comment", r.getComment());
                productReviews.add(map);
            }
        }

        // 3. Chuẩn bị Body để gửi (SỬA ĐOẠN NÀY)
        Map<String, Object> body = new HashMap<>();

        // --- LOGIC MỚI: GỘP SAO VÀ COMMENT THÀNH STRING ---
        // Vì Backend chỉ nhận String cho "orderReview", ta nối chuỗi lại.
        // Ví dụ kết quả: "5.0 sao - Nhân viên nhiệt tình"
        String finalOrderReviewString;

        if (generalFeedback.isEmpty()) {
            // Nếu khách không viết gì, chỉ lấy số sao
            finalOrderReviewString = generalRating + " sao.";
        } else {
            // Nếu có viết, nối cả hai
            finalOrderReviewString = generalRating + " sao - " + generalFeedback;
        }

        body.put("orderReview", finalOrderReviewString);

        if (!productReviews.isEmpty()) {
            body.put("productsReview", productReviews);
        }

        Log.d("REVIEW_SUBMIT", "Body: " + new com.google.gson.Gson().toJson(body));

        // 4. Gọi API
        apiService.reviewOrderAndProducts(orderID, body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showVoucherSuccessDialog();
                } else {
                    Toast.makeText(getContext(), "Gửi đánh giá: " + response.message(), Toast.LENGTH_SHORT).show();
                    showVoucherSuccessDialog();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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