package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.adapters.AddressAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.AddressResponse;
import com.example.demo.utils.SessionManager;
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


    private MaterialCardView cardDeliveryOption;
    private View containerDelivery;
    private View sectionPickup;
    private MaterialButton btnConfirm;
    private View btnBack;


    private TextView btnAddHome;
    private TextView btnAddWorkAddress;
    private TextView btnAddOtherAddress;

    private RecyclerView rvHomeAddress, rvWorkAddress, rvOtherAddress;

    private String selectedMethod = null;
    private String selectedAddressStr = "";
    private String selectedCustomerStr = "";
    private String selectedAddressID = null;

    //them
    private List<AddressResponse.AddressData> homeList = new ArrayList<>();
    private List<AddressResponse.AddressData> workList = new ArrayList<>();
    private List<AddressResponse.AddressData> otherList = new ArrayList<>();

    // Giữ adapter để gọi notifyDataSetChanged
    private AddressAdapter adapterHome, adapterWork, adapterOther;

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
        cardDeliveryOption = findViewById(R.id.cardDeliveryOption);
        containerDelivery = findViewById(R.id.containerDelivery);
        sectionPickup = findViewById(R.id.sectionPickup);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        btnAddHome = findViewById(R.id.btnAddHome);
        btnAddWorkAddress = findViewById(R.id.btnAddWorkAddress);
        btnAddOtherAddress = findViewById(R.id.btnAddOtherAddress);

        rvHomeAddress = findViewById(R.id.rvHomeAddress);
        rvWorkAddress = findViewById(R.id.rvWorkAddress);
        rvOtherAddress = findViewById(R.id.rvOtherAddress);

        rvHomeAddress.setLayoutManager(new LinearLayoutManager(this));
        rvWorkAddress.setLayoutManager(new LinearLayoutManager(this));
        rvOtherAddress.setLayoutManager(new LinearLayoutManager(this));

        updateConfirmButton(false);
    }

    private void setupEventHandlers() {
        cardDeliveryOption.setOnClickListener(null);
        cardDeliveryOption.setStrokeWidth(0);

        if (sectionPickup != null) {
            sectionPickup.setOnClickListener(v -> selectMethod("pickup"));
        }
        if(containerDelivery!=null){
            containerDelivery.setOnClickListener(v-> selectMethod("delivery"));
        }
        btnConfirm.setOnClickListener(v -> {
            if (selectedMethod != null) {
                Log.d(TAG, "XÁC NHẬN PHƯƠNG THỨC: " + selectedMethod);

                String finalAddress = "delivery".equals(selectedMethod)
                        ? (selectedAddressStr.isEmpty() ? DEFAULT_ADDRESS : selectedAddressStr)
                        : null;

                String finalCustomer = "delivery".equals(selectedMethod)
                        ? (selectedCustomerStr.isEmpty() ? DEFAULT_CUSTOMER : selectedCustomerStr)
                        : null;
                String finalAddressID = "delivery".equals(selectedMethod) ? selectedAddressID : null;
                returnResult(selectedMethod, finalAddress, finalCustomer,finalAddressID);
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
    private void filterAndDisplayAddresses(List<AddressResponse.AddressData> list) {
        if (list == null) return;

        homeList.clear();
        workList.clear();
        otherList.clear();

        // 2. Chia dữ liệu vào 3 list
        for (AddressResponse.AddressData item : list) {
            item.isSelected = false;
            if (!selectedAddressStr.isEmpty() && item.getFullAddress().equals(selectedAddressStr)) {
                item.isSelected = true;
            }

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

        // 3. Khởi tạo Adapter và lưu vào biến toàn cục
        adapterHome = new AddressAdapter(homeList, this::onAddressItemClicked);
        rvHomeAddress.setAdapter(adapterHome);

        adapterWork = new AddressAdapter(workList, this::onAddressItemClicked);
        rvWorkAddress.setAdapter(adapterWork);

        adapterOther = new AddressAdapter(otherList, this::onAddressItemClicked);
        rvOtherAddress.setAdapter(adapterOther);
    }
    private void onAddressItemClicked(AddressResponse.AddressData clickedItem) {
        deselectAll(homeList);
        deselectAll(workList);
        deselectAll(otherList);

        clickedItem.isSelected = true;
        selectedAddressStr = clickedItem.getFullAddress();
        selectedCustomerStr = clickedItem.name + " | " + clickedItem.phone;
        selectedAddressID=clickedItem.addressID;
        selectMethod("delivery");
        if (adapterHome != null) adapterHome.notifyDataSetChanged();
        if (adapterWork != null) adapterWork.notifyDataSetChanged();
        if (adapterOther != null) adapterOther.notifyDataSetChanged();
    }
    private void deselectAll(List<AddressResponse.AddressData> list) {
        if (list == null) return;
        for (AddressResponse.AddressData data : list) {
            data.isSelected = false;
        }
    }

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

    private void returnResult(String method, String address, String customer,String addressID) {
        Intent result = new Intent();
        result.putExtra("deliveryMethod", method);
        if (address != null) result.putExtra("address", address);
        if (customer != null) result.putExtra("customer", customer);
        if (addressID != null) result.putExtra("addressID", addressID);
        setResult(RESULT_OK, result);
        finish();
    }
}