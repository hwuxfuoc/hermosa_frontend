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
import com.example.demo.adapter.CartAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.model.CartItem;
import com.example.demo.model.CartResponse;
import com.example.demo.model.CommonResponse;

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

    public FragmentCart() {
        // required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // inflate layout fragment_cart.xml (tên layout của bạn)
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // init views using the inflated view
        rv = view.findViewById(R.id.recyclerViewCart);
        tvTotal = view.findViewById(R.id.tvTotal); // đảm bảo id này có trong fragment_cart.xml

        api = ApiClient.getClient().create(ApiService.class);

        // SharedPreferences trong Fragment: dùng requireActivity().getSharedPreferences(...)
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userID = prefs.getString("USER_ID", null);

        if (userID == null) {
            Toast.makeText(requireContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            // nếu muốn chuyển sang activity Login: startActivity(...)
            // hoặc tắt fragment: popBackStack() tuỳ flow của bạn
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
                    if (adapter != null) adapter.notifyDataSetChanged();
                    if (tvTotal != null) tvTotal.setText(String.format("Tổng: %,d đ", data.getTotalMoney()));
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

    @Override
    public void onIncrease(CartItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());
        api.increaseItem(body).enqueue(commonCallback("Tăng số lượng thành công"));
    }

    @Override
    public void onDecrease(CartItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());
        api.decreaseItem(body).enqueue(commonCallback("Giảm số lượng thành công"));
    }

    @Override
    public void onDelete(CartItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());
        api.deleteItem(body).enqueue(commonCallback("Xóa thành công"));
    }

    @Override
    public void onToggleSelect(CartItem item, boolean selected) {
        // nếu cần xử lý checkbox chọn hàng
    }

    private Callback<CommonResponse> commonCallback(String successMsg) {
        return new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Lỗi phản hồi", Toast.LENGTH_SHORT).show();
                }
                loadCart();
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi server", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
