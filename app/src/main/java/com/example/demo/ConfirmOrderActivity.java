package com.example.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RecyclerView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.demo.adapters.CartAdapter;
import com.example.demo.adapters.RecommendedAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CartItem;
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;
import com.example.demo.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerCart, recyclerRecommended;
    private CartAdapter cartAdapter;
    private RecommendedAdapter recommendedAdapter;
    private Button btnConfirmOrder;
    private EditText edtAddress, edtNote;
    private Spinner spinnerPayment;

    private ApiService apiService;
    private String userID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        // Ánh xạ view
        recyclerCart = findViewById(R.id.recyclerCart);
        recyclerRecommended = findViewById(R.id.recyclerRecommended);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        edtAddress = findViewById(R.id.edtAddress);
        edtNote = findViewById(R.id.edtNote);
        spinnerPayment = findViewById(R.id.spinnerPayment);

        // Recycler giỏ hàng (dọc)
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));

        // Recycler “khách hàng khác cũng đặt” (ngang)
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerRecommended.setLayoutManager(layoutManager);

        // Thiết lập Spinner phương thức thanh toán
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Tiền mặt", "Chuyển khoản"});
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPayment.setAdapter(paymentAdapter);

        // Lấy userID từ session
        userID = SessionManager.getUserID(this);
        if (userID == null) {
            Toast.makeText(this, "Chưa đăng nhập, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);

        // Gọi API lấy giỏ hàng
        fetchCartProducts();

        // Tạo dữ liệu mẫu “khách hàng khác cũng đặt”
        setupRecommended();

        // Nút xác nhận đặt hàng
        btnConfirmOrder.setOnClickListener(v -> {
            String address = edtAddress.getText().toString().trim();
            String note = edtNote.getText().toString().trim();
            String payment = spinnerPayment.getSelectedItem().toString();

            if (TextUtils.isEmpty(address)) {
                edtAddress.setError("Vui lòng nhập địa chỉ giao hàng");
                return;
            }

            createOrder(address, note, payment);
        });
    }

    // ================== LOAD CART ==================
    private void fetchCartProducts() {
        apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<CartItem> items = response.body().getData().getDetailedItems();
                    cartAdapter = new CartAdapter(ConfirmOrderActivity.this, items, null);
                    recyclerCart.setAdapter(cartAdapter);
                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================== RECOMMENDED (ngang) ==================
    private void setupRecommended() {
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add("Strawberry Smoothie");
        sampleNames.add("Oreo Smoothie");
        sampleNames.add("Chocolate Milk Tea");
        sampleNames.add("Matcha Latte");
        sampleNames.add("Green Tea");

        recommendedAdapter = new RecommendedAdapter(sampleNames);
        recyclerRecommended.setAdapter(recommendedAdapter);
    }

    // ================== CREATE ORDER ==================
    private void createOrder(String address, String note, String paymentMethod) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", paymentMethod.equals("Tiền mặt") ? "cash" : "bank");
        body.put("paymentStatus", "unpaid");
        body.put("deliver", "delivery");
        body.put("deliverAddress", address);
        body.put("note", note);

        apiService.createOrder(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CommonResponse res = response.body();
                    if ("Success".equalsIgnoreCase(res.getStatus())) {
                        Toast.makeText(ConfirmOrderActivity.this, "✅ Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        if (cartAdapter != null) cartAdapter.clearItems();
                        finish();
                    } else {
                        Toast.makeText(ConfirmOrderActivity.this, "❌ " + res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Không thể tạo đơn hàng!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
