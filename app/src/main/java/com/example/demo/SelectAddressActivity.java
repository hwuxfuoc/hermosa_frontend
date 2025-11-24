/*
package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SelectAddressActivity extends AppCompatActivity {

    private static final String TAG = "SELECT_ADDRESS_DEBUG";
    private static final String DEFAULT_ADDRESS = "[Trường ĐH CNTT - ĐHQG TP.HCM] Hàn Thuyên, khu phố 6, P. Linh Chiểu, TP. Thủ Đức";
    private static final String DEFAULT_CUSTOMER = "Tên Khách Hàng | 0909123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_delivery_address);

        Log.d(TAG, "SelectAddressActivity mở thành công! Giữ nguyên giao diện Fragment");

        // Ẩn BottomNavigation (vì đây là Activity)
        View bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);

        View cardDeliveryOption = findViewById(R.id.cardDeliveryOption);
        View sectionPickup = findViewById(R.id.sectionPickup);

        // ← SỬA TỪ ImageButton → ImageView (hoặc View cũng được)
        View btnBack = findViewById(R.id.btnBack);  // Dùng View là an toàn nhất
        // Hoặc nếu muốn giữ kiểu: ImageView btnBack = findViewById(R.id.btnBack);

        View btnConfirm = findViewById(R.id.btnConfirm);

        // Gắn sự kiện bình thường
        if (cardDeliveryOption != null) {
            cardDeliveryOption.setOnClickListener(v -> {
                Log.d(TAG, "USER CHỌN: GIAO TẬN NƠI");
                returnResult("delivery", DEFAULT_ADDRESS, DEFAULT_CUSTOMER);
            });
        }

        if (sectionPickup != null) {
            sectionPickup.setOnClickListener(v -> {
                Log.d(TAG, "USER CHỌN: NHẬN TẠI QUÁN");
                returnResult("pickup", null, null);
            });
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> returnResult("pickup", null, null));
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "USER BẤM NÚT BACK");
                finish();
            });
        }
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
*/
/*
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
    private View sectionPickup; // Là LinearLayout con trong cardDeliveryOption
    private MaterialButton btnConfirm;
    private View btnBack;

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
        sectionPickup = findViewById(R.id.sectionPickup);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Ban đầu nút xác nhận bị tắt
        updateConfirmButton(false);

        // Sự kiện: Chọn GIAO TẬN NƠI (toàn bộ card)
        cardDeliveryOption.setOnClickListener(v -> selectMethod("delivery"));

        // Sự kiện: Chọn NHẬN TẠI QUÁN (chỉ phần dưới)
        if (sectionPickup != null) {
            sectionPickup.setOnClickListener(v -> selectMethod("pickup"));
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
    }

    private void selectMethod(String method) {
        if (selectedMethod != null && selectedMethod.equals(method)) {
            return; // Đã chọn rồi → không làm gì
        }

        selectedMethod = method;

        // Reset border về mặc định
        cardDeliveryOption.setStrokeWidth(0);
        cardDeliveryOption.setStrokeColor(ContextCompat.getColor(this, android.R.color.transparent));

        // Tô đỏ phần được chọn
        int redColor = ContextCompat.getColor(this, R.color.smoothie_strawberry); // bạn tạo màu này trong colors.xml

        if ("delivery".equals(method)) {
            cardDeliveryOption.setStrokeWidth(2);
            cardDeliveryOption.setStrokeColor(redColor);
            Log.d(TAG, "ĐÃ CHỌN: GIAO TẬN NƠI → khung đỏ");
        } else if ("pickup".equals(method) && sectionPickup != null) {
            // Tô đỏ phần Nhận tại quán bằng cách đổi background tạm thời
            sectionPickup.setBackgroundResource(R.drawable.bg_btn_selected);
            Log.d(TAG, "ĐÃ CHỌN: NHẬN TẠI QUÁN → phần dưới đỏ");
        }

        // Bật nút xác nhận
        updateConfirmButton(true);
    }

    private void updateConfirmButton(boolean enabled) {
        if (btnConfirm == null) return;

        if (enabled) {
            btnConfirm.setEnabled(true);
            btnConfirm.setAlpha(1.0f);
            btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.smoothie_strawberry));
        } else {
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.5f);
            btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.smoothie_strawberry));
        }
    }

    private void returnResult(String method, String address, String customer) {
        Intent result = new Intent();
        result.putExtra("deliveryMethod", method);
        if (address != null) result.putExtra("address", address);
        if (customer != null) result.putExtra("customer", customer);
        setResult(RESULT_OK, result);
        finish();
    }
}*/
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
        cardDeliveryOption.setOnClickListener(v -> selectMethod("delivery"));

        // Sự kiện: Chọn NHẬN TẠI QUÁN (chỉ phần dưới)
        if (sectionPickup != null) {
            sectionPickup.setOnClickListener(v -> selectMethod("pickup"));
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

        // Reset border
        cardDeliveryOption.setStrokeWidth(0);
        cardDeliveryOption.setStrokeColor(ContextCompat.getColor(this, android.R.color.transparent));

        // Tô đỏ toàn card
        int redColor = ContextCompat.getColor(this, R.color.smoothie_strawberry); // #B71C1C hoặc màu đỏ bạn có
        cardDeliveryOption.setStrokeWidth(2);
        cardDeliveryOption.setStrokeColor(redColor);

        Log.d(TAG, "ĐÃ CHỌN: " + method + " → bo đỏ toàn card");

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