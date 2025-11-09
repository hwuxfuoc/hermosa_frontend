package com.example.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SelectAddressActivity extends AppCompatActivity {

    private RelativeLayout optionDelivery; // Nút "Thông tin giao hàng"
    private LinearLayout optionPickup;   // Nút "Nhận tại quán"
    private ImageButton btnBack;

    // Các thông tin địa chỉ (bạn sẽ lấy từ API hoặc SharedPreferences)
    private final String currentAddress = "[Trường Đại học Công nghệ Thông tin - ĐHQG TP.HCM] Hàn Thuyên, khu phố 6 P, Thủ Đức";
    private final String currentCustomer = "Tên Khách Hàng | 090912345";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address); // Dùng layout XML bạn vừa gửi

        // Ánh xạ View
        optionDelivery = findViewById(R.id.optionDelivery);
        optionPickup = findViewById(R.id.optionPickup);
        btnBack = findViewById(R.id.btnBack);

        // --- Xử lý khi chọn "Giao hàng" (chọn địa chỉ hiện tại) ---
        optionDelivery.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newMethod", "delivery"); // Phương thức
            resultIntent.putExtra("newAddress", currentAddress); // Địa chỉ
            resultIntent.putExtra("newCustomerInfo", currentCustomer); // Thông tin

            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        // --- Xử lý khi chọn "Tại quán" ---
        optionPickup.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newMethod", "pickup"); // Phương thức

            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        // --- Xử lý nút Back ---
        btnBack.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }
}