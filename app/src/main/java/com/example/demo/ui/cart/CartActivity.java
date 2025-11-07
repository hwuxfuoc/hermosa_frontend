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
    private TextView tvTotal; // TextView này sẽ hiển thị tổng tiền của item ĐÃ CHỌN
    private CartAdapter adapter;
    private List<CartItem> items = new ArrayList<>();
    private ApiService api;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cart);

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

                    // SỬA: Không set tổng tiền ở đây
                    // tvTotal.setText(String.format("Tổng: %,d đ", data.getTotalMoney()));

                    // BỔ SUNG: Gọi onDataChanged để tính tổng tiền (ban đầu sẽ là 0)
                    onDataChanged();
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

    /**
     * SỬA: Bổ sung logic cho hàm này.
     * Khi người dùng tick checkbox, hàm này cập nhật trạng thái trong model.
     */
    @Override
    public void onToggleSelect(CartItem item, boolean selected) {
        item.setSelected(selected); // Cập nhật trạng thái (true/false)
    }

    /**
     * BỔ SUNG: Đây là hàm bị thiếu gây ra lỗi.
     * Hàm này được Adapter gọi mỗi khi có thay đổi (tăng, giảm, xóa, tick)
     * để tính toán lại tổng tiền của các item ĐÃ ĐƯỢC CHỌN.
     */
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


    private Callback<CommonResponse> commonCallback(String successMsg) {
        return new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CartActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
                // SỬA: Không gọi loadCart() ngay lập tức nếu không cần thiết
                // (Trừ khi bạn muốn làm mới toàn bộ)
                // loadCart();

                // Thay vào đó, nếu thành công, chỉ cần gọi onDataChanged()
                onDataChanged();

                // Nếu bạn vẫn muốn loadCart(), hãy đảm bảo onDataChanged() được gọi SAU KHI load xong
                loadCart(); // Giữ lại nếu bạn muốn cập nhật toàn bộ giỏ hàng từ BE
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi server", Toast.LENGTH_SHORT).show();
            }
        };
    }
}