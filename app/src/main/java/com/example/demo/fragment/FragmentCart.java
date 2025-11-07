package com.example.demo.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.adapters.CartAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CartItem;
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentCart extends Fragment implements CartAdapter.OnAction {

    private RecyclerView rv;
    private TextView tvTotal;
    private CartAdapter adapter;
    private List<CartItem> items = new ArrayList<>();
    private ApiService api;
    private String userID;

    // Lưu trạng thái các item được chọn
    private Map<String, Boolean> selectedItems = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.recyclerViewCart);
        tvTotal = view.findViewById(R.id.tvTotal);

        api = ApiClient.getClient().create(ApiService.class);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userID = prefs.getString("USER_ID", null);

        if (userID == null) {
            Toast.makeText(requireContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new CartAdapter(requireContext(), items, this);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        loadCart();
    }

    private void loadCart() {
        api.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    CartResponse.CartData data = response.body().getData();
                    items.clear();
                    if (data.getItems() != null) items.addAll(data.getItems());
                    adapter.notifyDataSetChanged();

                    tvTotal.setText(String.format("Tổng: %,d đ", data.getTotalMoney()));

                    // Đặt mặc định tất cả là chọn
                    selectedItems.clear();
                    for (CartItem item : items) {
                        selectedItems.put(item.getId(), true);
                    }
                } else {
                    Toast.makeText(requireContext(), "Không có dữ liệu giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e("API", "loadCart", t);
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================== CartAdapter.OnAction ==================

    @Override
    public void onIncrease(CartItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());

        api.increaseItem(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    item.setQuantity(item.getQuantity() + 1);
                    adapter.notifyDataSetChanged();
                    loadCart(); // tải lại tổng tiền
                } else {
                    Toast.makeText(requireContext(), "Không tăng được số lượng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDecrease(CartItem item) {
        if (item.getQuantity() <= 1) {
            Toast.makeText(requireContext(), "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());

        api.decreaseItem(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    item.setQuantity(item.getQuantity() - 1);
                    adapter.notifyDataSetChanged();
                    loadCart();
                } else {
                    Toast.makeText(requireContext(), "Không giảm được số lượng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDelete(CartItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());

        api.deleteItem(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    items.remove(item);
                    adapter.notifyDataSetChanged();
                    loadCart();
                } else {
                    Toast.makeText(requireContext(), "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //trang bo sung kiem tra lai
    @Override
    public void onDataChanged() {
        double selectedTotal = 0;
        if (items != null) {
            for (CartItem item : items) {
                if (item.isSelected()) { // Chỉ cộng tiền item nào được tick
                    selectedTotal += item.getPrice() * item.getQuantity();
                }
            }
        }
        // Cập nhật TextView tổng tiền (và nút Mua hàng)
        tvTotal.setText(String.format("Tổng: %,.0f đ", selectedTotal));
        // TODO: Cập nhật nút Mua hàng
        // btnPurchase.setText(String.format("Mua hàng (%,.0f đ)", selectedTotal));
    }
    @Override
    public void onToggleSelect(CartItem item, boolean selected) {
        selectedItems.put(item.getId(), selected);
        updateTotal();
    }

    private void updateTotal() {
        int total = 0;
        for (CartItem item : items) {
            if (selectedItems.getOrDefault(item.getId(), false)) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        tvTotal.setText(String.format("Tổng: %,d đ", total));
    }
}
