package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast; // Bổ sung Toast

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager; // Bổ sung
import androidx.recyclerview.widget.RecyclerView; // Bổ sung

import com.example.demo.adapters.AddressAdapter; // Bổ sung Adapter
import com.example.demo.api.ApiClient; // Bổ sung API
import com.example.demo.api.ApiService; // Bổ sung API
import com.example.demo.models.AddressResponse; // Bổ sung Model
import com.example.demo.utils.SessionManager; // Bổ sung Session để lấy UserID
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList; // Bổ sung List
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectAddressActivity extends AppCompatActivity {

    private static final String TAG = "SELECT_ADDRESS";

    // --- GIỮ NGUYÊN CODE CŨ ---
    private static final String DEFAULT_ADDRESS = "[Trường ĐH CNTT - ĐHQG TP.HCM] Hàn Thuyên, khu phố 6, P. Linh Chiểu, TP. Thủ Đức";
    private static final String DEFAULT_CUSTOMER = "Tên Khách Hàng | 0909123456";

    // Các view cần thao tác
    private MaterialCardView cardDeliveryOption;
    private View containerDelivery;
    private View sectionPickup;
    private MaterialButton btnConfirm;
    private View btnBack;

    // Các nút "Thêm địa chỉ"
    private TextView btnAddHome;
    private TextView btnAddWorkAddress;
    private TextView btnAddOtherAddress;

    // --- BỔ SUNG: RecyclerView để hiển thị danh sách ---
    private RecyclerView rvHomeAddress, rvWorkAddress, rvOtherAddress;

    // Trạng thái
    private String selectedMethod = null; // "delivery" hoặc "pickup"
    private String selectedAddressStr = "";
    private String selectedCustomerStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_delivery_address);

        Log.d(TAG, "SelectAddressActivity mở thành công!");

        View bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        initViews();
        setupEventHandlers();
        fetchAddressList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAddressList();
    }

    private void initViews() {
        // --- CODE CŨ: Ánh xạ view có sẵn ---
        cardDeliveryOption = findViewById(R.id.cardDeliveryOption);
        containerDelivery = findViewById(R.id.containerDelivery);
        sectionPickup = findViewById(R.id.sectionPickup);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        btnAddHome = findViewById(R.id.btnAddHome);
        btnAddWorkAddress = findViewById(R.id.btnAddWorkAddress);
        btnAddOtherAddress = findViewById(R.id.btnAddOtherAddress);

        // --- BỔ SUNG: Ánh xạ RecyclerView và set LayoutManager ---
        rvHomeAddress = findViewById(R.id.rvHomeAddress);
        rvWorkAddress = findViewById(R.id.rvWorkAddress);
        rvOtherAddress = findViewById(R.id.rvOtherAddress);

        // Bắt buộc phải có LayoutManager thì RecyclerView mới hiện
        rvHomeAddress.setLayoutManager(new LinearLayoutManager(this));
        rvWorkAddress.setLayoutManager(new LinearLayoutManager(this));
        rvOtherAddress.setLayoutManager(new LinearLayoutManager(this));

        // Ban đầu nút xác nhận bị tắt
        updateConfirmButton(false);
    }

    private void setupEventHandlers() {
        // --- GIỮ NGUYÊN LOGIC CŨ ---
        cardDeliveryOption.setOnClickListener(null);
        cardDeliveryOption.setStrokeWidth(0);

        if (sectionPickup != null) {
            sectionPickup.setOnClickListener(v -> selectMethod("pickup"));
        }
        if(containerDelivery!=null){
            // Sửa nhẹ: Khi bấm vào khung Delivery, nếu chưa chọn địa chỉ cụ thể thì vẫn selectMethod
            containerDelivery.setOnClickListener(v-> selectMethod("delivery"));
        }

        // --- SỬA LOGIC NÚT XÁC NHẬN ---
        btnConfirm.setOnClickListener(v -> {
            if (selectedMethod != null) {
                Log.d(TAG, "XÁC NHẬN PHƯƠNG THỨC: " + selectedMethod);

                // Nếu chọn Delivery: Ưu tiên lấy địa chỉ User vừa bấm chọn trong List.
                // Nếu User không chọn gì cả, lấy DEFAULT (hoặc bắt buộc chọn tùy bạn)
                String finalAddress = "delivery".equals(selectedMethod)
                        ? (selectedAddressStr.isEmpty() ? DEFAULT_ADDRESS : selectedAddressStr)
                        : null;

                String finalCustomer = "delivery".equals(selectedMethod)
                        ? (selectedCustomerStr.isEmpty() ? DEFAULT_CUSTOMER : selectedCustomerStr)
                        : null;

                returnResult(selectedMethod, finalAddress, finalCustomer);
            }
        });

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddHome != null) btnAddHome.setOnClickListener(v -> openAddAddress("Home")); // Type khớp Backend
        if (btnAddWorkAddress != null) btnAddWorkAddress.setOnClickListener(v -> openAddAddress("Work"));
        if (btnAddOtherAddress != null) btnAddOtherAddress.setOnClickListener(v -> openAddAddress("Other"));
    }

    // --- BỔ SUNG: HÀM GỌI API ---
    private void fetchAddressList() {
        String userID = SessionManager.getUserID(this);
        if (userID == null || userID.isEmpty()) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Gọi API lấy danh sách (type = null để lấy tất cả)
        apiService.getListAddress(userID).enqueue(new Callback<AddressResponse>() {
            @Override
            public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Gọi hàm lọc và hiển thị
                    filterAndDisplayAddresses(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<AddressResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi API Address: " + t.getMessage());
            }
        });
    }

    // --- BỔ SUNG: HÀM LỌC VÀ HIỂN THỊ DỮ LIỆU ---
    private void filterAndDisplayAddresses(List<AddressResponse.AddressData> list) {
        if (list == null) return;

        List<AddressResponse.AddressData> homeList = new ArrayList<>();
        List<AddressResponse.AddressData> workList = new ArrayList<>();
        List<AddressResponse.AddressData> otherList = new ArrayList<>();

        // Logic chia list theo Type
        for (AddressResponse.AddressData item : list) {
            if (item.type == null) {
                otherList.add(item);
            } else if (item.type.equalsIgnoreCase("Home")) {
                homeList.add(item);
            } else if (item.type.equalsIgnoreCase("Work")) {
                workList.add(item);
            } else {
                otherList.add(item);
            }
        }

        // Gán Adapter cho RecyclerView
        // Khi Click vào 1 item -> Gọi hàm onAddressItemClicked
        rvHomeAddress.setAdapter(new AddressAdapter(homeList, this::onAddressItemClicked));
        rvWorkAddress.setAdapter(new AddressAdapter(workList, this::onAddressItemClicked));
        rvOtherAddress.setAdapter(new AddressAdapter(otherList, this::onAddressItemClicked));
    }

    // --- BỔ SUNG: XỬ LÝ KHI CLICK VÀO 1 ĐỊA CHỈ CỤ THỂ ---
    private void onAddressItemClicked(AddressResponse.AddressData data) {
        // 1. Lưu thông tin địa chỉ vừa chọn
        selectedAddressStr = data.getFullAddress();
        selectedCustomerStr = data.name + " | " + data.phone;

        // 2. Tự động chuyển UI sang chế độ "Giao hàng tận nơi"
        selectMethod("delivery");

        // 3. Thông báo nhẹ cho người dùng biết
        Toast.makeText(this, "Đã chọn: " + data.name, Toast.LENGTH_SHORT).show();
    }

    // --- CÁC HÀM CŨ GIỮ NGUYÊN (Logic UI) ---
    private void openAddAddress(String type) {
        Intent intent = new Intent(this, AddAddressActivity.class);
        intent.putExtra("type", type);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String newAddress = data.getStringExtra("newAddress");
            String newCustomer = data.getStringExtra("newCustomer");
            // String type = data.getStringExtra("type"); // Không cần dùng ở đây nữa vì onResume sẽ tự load lại list

            // Nếu người dùng vừa thêm mới -> Tự chọn cái đó luôn
            if(newAddress != null) selectedAddressStr = newAddress;
            if(newCustomer != null) selectedCustomerStr = newCustomer;

            selectMethod("delivery");
        }
    }

    private void selectMethod(String method) {
        if (selectedMethod != null && selectedMethod.equals(method)) {
            return;
        }
        selectedMethod = method;

        // Reset màu nền
        if(containerDelivery!=null) containerDelivery.setBackgroundResource(android.R.color.white);
        if(sectionPickup!=null) sectionPickup.setBackgroundResource(android.R.color.white);

        // Highlight mục được chọn
        if("delivery".equals(method)){
            if(containerDelivery!=null) containerDelivery.setBackgroundResource(R.drawable.bg_btn_selected); // Đảm bảo drawable này tồn tại
        } else if ("pickup".equals(method)) {
            if(sectionPickup!=null) sectionPickup.setBackgroundResource(R.drawable.bg_btn_selected);

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