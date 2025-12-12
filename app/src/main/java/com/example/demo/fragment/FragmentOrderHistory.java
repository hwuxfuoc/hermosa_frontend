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
}