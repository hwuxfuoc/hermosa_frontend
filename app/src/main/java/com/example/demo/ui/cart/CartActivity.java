package com.example.demo.ui.cart;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.demo.R;
import com.example.demo.adapters.CartAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CartItem;
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnAction {

    private RecyclerView rv;
    private TextView tvTotal;
    private CartAdapter adapter;
    private List<CartItem> items = new ArrayList<>();
    private ApiService api;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rv = findViewById(R.id.recyclerViewCart);
        tvTotal = findViewById(R.id.tvTotal);
        api = ApiClient.getClient().create(ApiService.class);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userID = prefs.getString("USER_ID", null);

        if (userID == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new CartAdapter(this, items, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
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
                } else {
                    Toast.makeText(CartActivity.this, "Không có dữ liệu giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e("API", "loadCart", t);
                Toast.makeText(CartActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
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
    public void onToggleSelect(CartItem item, boolean selected) {}

    private Callback<CommonResponse> commonCallback(String successMsg) {
        return new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CartActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
                loadCart();
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi server", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
