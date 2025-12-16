
package com.example.demo.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.adapters.OrderDetailProductAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.OrderDetailResponse;
import com.example.demo.models.OrderHistoryResponse;

import java.text.DecimalFormat; // Dùng cái này để format #,###
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentOrderDetail extends Fragment {

    private String orderID;
    private ApiService apiService;
    private String currentUserID;

    private TextView tvOrderID, tvStatus, tvDate, tvFinalTotal, tvDetailAddress, tvShippingFee, tvDetailUserName;
    private RecyclerView rcvProducts;
    private ImageButton btnBack;
    private Button btnReorder;
    private OrderDetailResponse.OrderInfo currentOrderInfo;
    private List<OrderHistoryResponse.ProductDetail> passedPictures;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_detail, container, false);
        if (getArguments() != null) {
            orderID = getArguments().getString("ORDER_ID");

            // 1. NHẬN DANH SÁCH ẢNH TỪ BUNDLE
            try {
                passedPictures = (List<OrderHistoryResponse.ProductDetail>) getArguments().getSerializable("PICTURES_LIST");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initViews(view);
        callApiGetDetail();
        return view;
    }

    private void initViews(View view) {
        apiService = ApiClient.getClient().create(ApiService.class);
        SharedPreferences prefs = getActivity().getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        currentUserID = prefs.getString("USER_ID", "");

        tvOrderID = view.findViewById(R.id.tvDetailOrderID);
        tvStatus = view.findViewById(R.id.tvDetailStatus);
        tvDate = view.findViewById(R.id.tvDetailDate);
        tvFinalTotal = view.findViewById(R.id.tvFinalTotal);
        tvDetailAddress = view.findViewById(R.id.tvDetailAddress);
        tvShippingFee = view.findViewById(R.id.tvShippingFee);
        tvDetailUserName = view.findViewById(R.id.tvDetailUserName); // Tên người nhận

        rcvProducts = view.findViewById(R.id.rcvDetailProducts);
        rcvProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        btnBack = view.findViewById(R.id.btnBackDetail);
        /*btnReorder = view.findViewById(R.id.btnDetailReorder);*/

        btnBack.setOnClickListener(v -> { if (getActivity() != null) getActivity().onBackPressed(); });
        // btnReorder.setOnClickListener(...) // Logic mua lại
    }

    private void callApiGetDetail() {
        // Gọi đúng tên hàm trong ApiService (getOrderDetail3 hoặc getOrderDetail)
        apiService.getOrderDetail3(orderID).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderDetailResponse res = response.body();
                    if ("Success".equalsIgnoreCase(res.getStatus())) {
                        currentOrderInfo = res.getData();
                        updateUI(currentOrderInfo);
                    }
                }
            }
            @Override
            public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(OrderDetailResponse.OrderInfo info) {
        tvOrderID.setText(info.getOrderID());
        tvStatus.setText(info.getStatus());

        // Xử lý ngày
        String date = info.getDate();
        if(date != null && date.contains("T")) {
            date = date.replace("T", " ").substring(0, 16);
        }
        tvDate.setText(date);

        // --- 2. SỬA FORMAT TIỀN TỆ (Dùng DecimalFormat) ---
        DecimalFormat formatter = new DecimalFormat("#,###");

        tvFinalTotal.setText(formatter.format(info.getFinalTotal()) + " đ");

        String address = info.getDeliverAddress();
        tvDetailAddress.setText(address != null ? address : "Chưa có địa chỉ");

        // Phí ship (API trả về int, cần format)
        int fee = info.getDeliveryFee();
        tvShippingFee.setText(formatter.format(fee) + " đ");

        // --- 3. GHÉP ẢNH VÀO LIST SẢN PHẨM ---
        if (info.getProducts() != null) {

            // Duyệt qua từng sản phẩm trong đơn chi tiết
            for (OrderDetailResponse.ProductItem item : info.getProducts()) {
                // Tìm ảnh tương ứng trong list đã nhận (passedPictures)
                String imgUrl = findImageForProduct(item.getProductID());

                // Nếu tìm thấy thì set vào item
                if (imgUrl != null) {
                    item.setPicture(imgUrl);
                }
            }

            // Đưa list đã có ảnh vào Adapter
            OrderDetailProductAdapter adapter = new OrderDetailProductAdapter(info.getProducts());
            rcvProducts.setAdapter(adapter);
        }
    }

    // Hàm phụ trợ để tìm link ảnh theo ProductID
    private String findImageForProduct(String productID) {
        if (passedPictures == null || productID == null) return null;

        for (OrderHistoryResponse.ProductDetail pic : passedPictures) {
            // So sánh ProductID
            if (productID.equals(pic.getProductID())) {
                return pic.getImage();
            }
        }
        return null;
    }
}
