package com.example.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.example.demo.models.OrderHistoryResponse;
import com.example.demo.utils.SessionManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentOrderHistory extends Fragment {

    private RecyclerView recyclerOrderHistory;
    private ProgressBar progressBar;
    private View layoutEmptyOrder;
    private ImageButton btnBack; // Nếu bạn vẫn muốn nút back (tùy chọn, trong fragment thường không cần)

    private OrderHistoryAdapter adapter;
    private ArrayList<OrderHistoryResponse.OrderItem> orderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_order_history, container, false); // Dùng lại XML của bạn

        initViews(view);
        setupRecyclerView();
        // Không cần btnBack nếu dùng trong Bottom Navigation
        // Nếu vẫn muốn giữ nút back (ví dụ quay về Home), thì uncomment dòng dưới
        // setupBackButton();

        loadOrderHistory();

        return view;
    }

    private void initViews(View view) {
        recyclerOrderHistory = view.findViewById(R.id.recyclerOrderHistory);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyOrder = view.findViewById(R.id.layoutEmptyOrder);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(requireContext(), orderList);
        recyclerOrderHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerOrderHistory.setAdapter(adapter);
    }

    // Nếu muốn giữ nút back (không khuyến khích trong Bottom Nav)
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void loadOrderHistory() {
        String userID = SessionManager.getUserID(requireContext());
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
            showEmpty();
            return;
        }

        showLoading(true);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getOrderHistory(userID).enqueue(new Callback<OrderHistoryResponse>() {
            @Override
            public void onResponse(Call<OrderHistoryResponse> call, Response<OrderHistoryResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && "Success".equalsIgnoreCase(response.body().getStatus())) {
                    orderList.clear();
                    if (response.body().getData() != null) {
                        orderList.addAll(response.body().getData());
                    }
                    adapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        showEmpty();
                    } else {
                        recyclerOrderHistory.setVisibility(View.VISIBLE);
                        layoutEmptyOrder.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(requireContext(), "Không tải được lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
                    showEmpty();
                }
            }

            @Override
            public void onFailure(Call<OrderHistoryResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmpty();
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerOrderHistory.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showEmpty() {
        layoutEmptyOrder.setVisibility(View.VISIBLE);
        recyclerOrderHistory.setVisibility(View.GONE);
    }

    // Tùy chọn: refresh khi quay lại tab
    @Override
    public void onResume() {
        super.onResume();
        // Nếu muốn refresh mỗi khi vào tab
        // loadOrderHistory();
    }
}