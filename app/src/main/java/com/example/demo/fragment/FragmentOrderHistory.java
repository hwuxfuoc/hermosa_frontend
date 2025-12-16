/*
package com.example.demo.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.adapters.OrderHistoryAdapter;
import com.example.demo.api.ApiClient; // Quan trọng
import com.example.demo.api.ApiService;
import com.example.demo.models.OrderHistoryResponse;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentOrderHistory extends Fragment {

    private RecyclerView rcvOrderHistory;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyOrder;
    private ImageButton btnBack;
    private OrderHistoryAdapter adapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);
        initViews(view);

        // Khởi tạo Adapter
        setupRecyclerView();

        // Gọi API
        callApiGetOrderHistory();

        return view;
    }

    private void initViews(View view) {
        rcvOrderHistory = view.findViewById(R.id.recyclerOrderHistory);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyOrder = view.findViewById(R.id.layoutEmptyOrder);
        btnBack = view.findViewById(R.id.btnBack);

        // --- THÊM DÒNG NÀY ĐỂ KHẮC PHỤC LỖI NULL ---
        // ApiClient.getClient() trả về Retrofit, cần .create() để ra ApiService
        apiService = ApiClient.getClient().create(ApiService.class);
        // --------------------------------------------

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void setupRecyclerView() {
        rcvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        // Khởi tạo adapter với list rỗng
        adapter = new OrderHistoryAdapter(getContext(), new ArrayList<>());
        rcvOrderHistory.setAdapter(adapter);
    }

    private void callApiGetOrderHistory() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmptyOrder.setVisibility(View.GONE);

        // Lấy UserID (Thay bằng logic lấy ID thật của bạn)
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString("USER_ID", "690d583c81db5d6a42009e02");

        // GỌI API QUA RETROFIT CLIENT (Tránh lỗi Null)
        apiService.getOrderHistory(userID).enqueue(new Callback<OrderHistoryResponse>() {
            @Override
            public void onResponse(Call<OrderHistoryResponse> call, Response<OrderHistoryResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    OrderHistoryResponse res = response.body();

                    if ("Success".equalsIgnoreCase(res.getStatus())) {
                        List<OrderHistoryResponse.HistoryItem> listData = res.getData();

                        if (listData == null || listData.isEmpty()) {
                            layoutEmptyOrder.setVisibility(View.VISIBLE);
                            rcvOrderHistory.setVisibility(View.GONE);
                        } else {
                            layoutEmptyOrder.setVisibility(View.GONE);
                            rcvOrderHistory.setVisibility(View.VISIBLE);
                            // Cập nhật dữ liệu vào Adapter
                            adapter.setData(listData);
                        }
                    } else {
                        layoutEmptyOrder.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi server phản hồi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderHistoryResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}*/
package com.example.demo.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.adapters.OrderHistoryAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.OrderDetailResponse;
import com.example.demo.models.OrderHistoryResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentOrderHistory extends Fragment {

    private RecyclerView rcvOrderHistory;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyOrder;
    private ImageButton btnBack;

    private OrderHistoryAdapter adapter;
    private ApiService apiService;
    private String currentUserID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        // 1. Khởi tạo các View và Biến
        initViews(view);

        // 2. Cài đặt RecyclerView và Sự kiện Mua lại
        setupRecyclerView();

        // 3. Gọi API lấy dữ liệu lịch sử
        callApiGetOrderHistory();

        return view;
    }

    private void initViews(View view) {
        rcvOrderHistory = view.findViewById(R.id.recyclerOrderHistory);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyOrder = view.findViewById(R.id.layoutEmptyOrder);
        btnBack = view.findViewById(R.id.btnBack);

        // --- KHỞI TẠO API SERVICE (QUAN TRỌNG ĐỂ TRÁNH LỖI NULL) ---
        apiService = ApiClient.getClient().create(ApiService.class);

        // --- LẤY USER ID TỪ SHAREDPREFERENCES ---
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        // ID mặc định để test nếu chưa đăng nhập: 690d583c81db5d6a42009e02
        currentUserID = sharedPreferences.getString("USER_ID", "690d583c81db5d6a42009e02");

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rcvOrderHistory.setLayoutManager(layoutManager);

        adapter = new OrderHistoryAdapter(getContext(), new ArrayList<>(), new OrderHistoryAdapter.OnOrderActionListener() {
            @Override
            public void onReorderClick(List<OrderHistoryResponse.ProductQuantity> products) {
                processReorder(products);
            }

            @Override
            // Sửa hàm này nhận thêm tham số pictures
            public void onDetailClick(String orderID, List<OrderHistoryResponse.ProductDetail> pictures) {
                openOrderDetail(orderID, pictures);
            }
        });
        rcvOrderHistory.setAdapter(adapter);
    }

    // Cập nhật hàm mở màn hình chi tiết
    private void openOrderDetail(String orderID, List<OrderHistoryResponse.ProductDetail> pictures) {
        FragmentOrderDetail detailFragment = new FragmentOrderDetail();

        Bundle args = new Bundle();
        args.putString("ORDER_ID", orderID);

        // Truyền list ảnh sang (ép kiểu về Serializable)
        if (pictures != null) {
            args.putSerializable("PICTURES_LIST", (Serializable) pictures);
        }

        detailFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment) // Kiểm tra lại ID này trong activity_main.xml
                    .addToBackStack(null)
                    .commit();
        }
    }
    // Trong FragmentOrderHistory.java

    // Sửa lại hàm này
    private void openOrderDetail(String orderID) {
        // 1. Tạo Fragment Chi tiết
        FragmentOrderDetail detailFragment = new FragmentOrderDetail();

        // 2. Đóng gói OrderID vào Bundle để gửi sang
        Bundle args = new Bundle();
        args.putString("ORDER_ID", orderID);
        detailFragment.setArguments(args);

        // 3. Thực hiện chuyển màn hình
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null) // Để bấm Back quay lại được danh sách
                    .commit();
        }
    }

    // --- LOGIC XỬ LÝ MUA LẠI ---
    private void processReorder(List<OrderHistoryResponse.ProductQuantity> products) {
        if (products == null || products.isEmpty()) {
            Toast.makeText(getContext(), "Đơn hàng không có sản phẩm nào", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Đang thêm " + products.size() + " món vào giỏ...", Toast.LENGTH_SHORT).show();

        // Duyệt qua từng sản phẩm trong đơn cũ và gọi API thêm vào giỏ
        for (OrderHistoryResponse.ProductQuantity product : products) {
            callApiAddToCart(product.getProductID(), product.getQuantity());
        }
    }

    private void callApiAddToCart(String productID, int quantity) {
        // Tạo body request
        Map<String, Object> body = new HashMap<>();
        body.put("userID", currentUserID);
        body.put("productID", productID);
        body.put("quantity", quantity);

        apiService.addToCart(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                // API chạy ngầm, nếu thành công thì tốt, không cần thông báo quá nhiều gây phiền
                if (!response.isSuccessful()) {
                    // Có thể log lỗi ra console nếu cần: Log.e("CartError", "Failed to add item");
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                // Lỗi kết nối
            }
        });
    }
    // ---------------------------

    private void callApiGetOrderHistory() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmptyOrder.setVisibility(View.GONE);

        apiService.getOrderHistory(currentUserID).enqueue(new Callback<OrderHistoryResponse>() {
            @Override
            public void onResponse(Call<OrderHistoryResponse> call, Response<OrderHistoryResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    OrderHistoryResponse res = response.body();

                    // Kiểm tra status từ Backend trả về
                    if ("Success".equalsIgnoreCase(res.getStatus())) {
                        List<OrderHistoryResponse.HistoryItem> listData = res.getData();

                        if (listData == null || listData.isEmpty()) {
                            layoutEmptyOrder.setVisibility(View.VISIBLE);
                            rcvOrderHistory.setVisibility(View.GONE);
                        } else {
                            layoutEmptyOrder.setVisibility(View.GONE);
                            rcvOrderHistory.setVisibility(View.VISIBLE);
                            // Đổ dữ liệu vào Adapter
                            adapter.setData(listData);
                        }
                    } else {
                        // Trường hợp thất bại logic (ví dụ: User không tồn tại)
                        layoutEmptyOrder.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi phản hồi từ server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderHistoryResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}