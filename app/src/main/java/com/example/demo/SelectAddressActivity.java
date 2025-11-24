package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class SelectAddressActivity extends AppCompatActivity {

    private static final String TAG = "SELECT_ADDRESS";

    private static final String DEFAULT_ADDRESS = "[Trường ĐH CNTT - ĐHQG TP.HCM] Hàn Thuyên, khu phố 6, P. Linh Chiểu, TP. Thủ Đức";
    private static final String DEFAULT_CUSTOMER = "Tên Khách Hàng | 0909123456";

    // Các view cần thao tác
    private MaterialCardView cardDeliveryOption;
    private View containerDelivery;
    private View sectionPickup; // Là LinearLayout con trong cardDeliveryOption
    private MaterialButton btnConfirm;
    private View btnBack;

    // Các nút "Thêm địa chỉ"
    private TextView btnAddHome;
    private TextView btnAddWorkAddress;
    private TextView btnAddOtherAddress;

    // Trạng thái
    private String selectedMethod = null; // "delivery" hoặc "pickup"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_delivery_address);

        Log.d(TAG, "SelectAddressActivity mở thành công!");

        // Ẩn BottomNavigation (vì đây là Activity, không dùng)
        View bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);

        // Ánh xạ các view chính
        cardDeliveryOption = findViewById(R.id.cardDeliveryOption);
        containerDelivery=findViewById(R.id.containerDelivery);
        sectionPickup = findViewById(R.id.sectionPickup);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Ánh xạ các nút "Thêm địa chỉ"
        btnAddHome = findViewById(R.id.btnAddHome);
        btnAddWorkAddress = findViewById(R.id.btnAddWorkAddress);
        btnAddOtherAddress = findViewById(R.id.btnAddOtherAddress);

        // Ban đầu nút xác nhận bị tắt
        updateConfirmButton(false);

        // Sự kiện: Chọn GIAO TẬN NƠI (toàn bộ card)
        cardDeliveryOption.setOnClickListener(null);
        cardDeliveryOption.setStrokeWidth(0);

        // Sự kiện: Chọn NHẬN TẠI QUÁN (chỉ phần dưới)
        if (sectionPickup != null) {
            sectionPickup.setOnClickListener(v -> selectMethod("pickup"));
        }
        if(containerDelivery!=null){
            containerDelivery.setOnClickListener(v->selectMethod("delivery"));
        }
        // Nút Xác nhận
        btnConfirm.setOnClickListener(v -> {
            if (selectedMethod != null) {
                Log.d(TAG, "XÁC NHẬN PHƯƠNG THỨC: " + selectedMethod);
                returnResult(
                        selectedMethod,
                        "delivery".equals(selectedMethod) ? DEFAULT_ADDRESS : null,
                        "delivery".equals(selectedMethod) ? DEFAULT_CUSTOMER : null
                );
            }
        });

        // Nút Back
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Bấm nút Back");
                finish();
            });
        }

        // === BỔ SUNG: BẤM "THÊM ĐỊA CHỈ" → MỞ MÀN HÌNH THÊM ===
        // Nút "Thêm địa chỉ Nhà"
        if (btnAddHome != null) {
            btnAddHome.setOnClickListener(v -> openAddAddress("home"));
        }

        // Nút "Thêm địa chỉ Công ty"
        if (btnAddWorkAddress != null) {
            btnAddWorkAddress.setOnClickListener(v -> openAddAddress("work"));
        }

        // Nút "Thêm địa chỉ mới"
        if (btnAddOtherAddress != null) {
            btnAddOtherAddress.setOnClickListener(v -> openAddAddress("other"));
        }
    }

    // BỔ SUNG: Hàm mở màn hình thêm địa chỉ
    private void openAddAddress(String type) {
        Log.d(TAG, "Mở màn hình thêm địa chỉ kiểu: " + type);

        Intent intent = new Intent(this, AddAddressActivity.class);
        intent.putExtra("type", type);
        startActivityForResult(intent, 100); // Yêu cầu mã 100 để nhận kết quả
    }

    // BỔ SUNG: Nhận kết quả từ AddAddressActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String newAddress = data.getStringExtra("newAddress");
            String newCustomer = data.getStringExtra("newCustomer");
            String type = data.getStringExtra("type");

            Log.d(TAG, "Nhận địa chỉ mới: " + newAddress + " cho kiểu: " + type);

            // Sau khi thêm → chọn luôn phương thức delivery + trả kết quả
            selectMethod("delivery");
            returnResult("delivery", newAddress, newCustomer);
        }
    }

    private void selectMethod(String method) {
        if (selectedMethod != null && selectedMethod.equals(method)) {
            return;
        }

        selectedMethod = method;

        if(containerDelivery!=null){
            containerDelivery.setBackgroundResource(android.R.color.white);
        }
        if(sectionPickup!=null){
            sectionPickup.setBackgroundResource(android.R.color.white);
        }

        if("delivery".equals(method)){
            Log.d(TAG,"ĐÃ CHỌN: GIAO TẬN NƠI");
            if(containerDelivery!=null){
                containerDelivery.setBackgroundResource(R.drawable.bg_btn_selected);
            }
        } else if ("pickup".equals(method)) {
            Log.d(TAG, "ĐÃ CHỌN: ĐẶT TẠI QUÁN");
            if(sectionPickup!=null){
                sectionPickup.setBackgroundResource(R.drawable.bg_btn_selected);
            }
        }
        updateConfirmButton(true);
    }

    private void updateConfirmButton(boolean enabled) {
        if (btnConfirm == null) return;

        int color = enabled ? R.color.smoothie_strawberry : R.color.light_gray;
        btnConfirm.setEnabled(enabled);
        btnConfirm.setAlpha(enabled ? 1.0f : 0.5f);
        btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(this, color));
    }

    private void returnResult(String method, String address, String customer) {
        Intent result = new Intent();
        result.putExtra("deliveryMethod", method);
        if (address != null) result.putExtra("address", address);
        if (customer != null) result.putExtra("customer", customer);
        setResult(RESULT_OK, result);
        finish();
    }
}