package com.example.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.AddUpdateResponse;
import com.example.demo.models.AddressDetail;
import com.example.demo.models.AddressRequest;
import com.example.demo.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAddressActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_LOCATION = 200;

    private EditText etCustomerName, etPhone, etBuilding, etGate, etNote;
    private TextView tvPickAddress;
    private MaterialButton btnSave;
    private android.widget.Button btnTypeHome, btnTypeWork, btnTypeOther;

    private String addressType = "Home";

     private String selectedStreet = "";
    private String selectedWard = "";
    private String selectedDistrict = "";
    private String selectedCity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_add_address);

        initViews();

        if (getIntent().hasExtra("type")) {
            updateTypeSelection(getIntent().getStringExtra("type"));
        }else {
            updateTypeSelection("home");
        }

        preFillUserData();
        //xu ly su kien chon nut
        btnTypeHome.setOnClickListener(v->updateTypeSelection("home"));
        btnTypeWork.setOnClickListener(v->updateTypeSelection("work"));
        btnTypeOther.setOnClickListener(v->updateTypeSelection("other"));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvPickAddress.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, PickLocationActivity.class), REQUEST_PICK_LOCATION);
        });

        btnSave.setOnClickListener(v -> saveAddressToApi());
    }
    private void updateTypeSelection(String type){
        this.addressType=type;
        if(btnTypeHome!=null){
            btnTypeHome.setSelected("home".equals(type));
        }
        if(btnTypeWork!=null){
            btnTypeWork.setSelected("work".equals(type));
        }
        if(btnTypeOther!=null){
            btnTypeOther.setSelected("other".equals(type));
        }
    }

    private void initViews() {
        etCustomerName = findViewById(R.id.etCustomerName);
        etPhone = findViewById(R.id.etPhone);
        tvPickAddress = findViewById(R.id.tvPickAddress);
        etBuilding = findViewById(R.id.etBuilding);
        etGate = findViewById(R.id.etGate);
        etNote = findViewById(R.id.etNote);
        btnSave = findViewById(R.id.btnSave);
        btnTypeHome=findViewById(R.id.btnTypeHome);
        btnTypeWork=findViewById(R.id.btnTypeWork);
        btnTypeOther=findViewById(R.id.btnTypeOther);
    }

    private void preFillUserData() {
        String name = SessionManager.getUserName(this);
        String phone = SessionManager.getUserPhone(this);
        if (name != null) etCustomerName.setText(name);
        if (phone != null) etPhone.setText(phone);
    }
    private void saveAddressToApi() {
        String name = etCustomerName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String building = etBuilding.getText().toString().trim();
        String gate = etGate.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || selectedStreet.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên, sđt và chọn địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Xử lý phần chi tiết hiển thị (biến này bị thay đổi nhiều lần)
        String streetDetail = selectedStreet;
        if (!building.isEmpty()) streetDetail += ", " + building;
        if (!gate.isEmpty()) streetDetail += ", Cổng " + gate;
        if (!note.isEmpty()) streetDetail += " (" + note + ")";

        // --- KHẮC PHỤC LỖI TẠI ĐÂY ---
        // Tạo một biến final để "chốt" giá trị của streetDetail
        final String finalStreetDetail = streetDetail;
        // -----------------------------

        // 2. Tạo chuỗi gửi đi Backend
        StringBuilder sb = new StringBuilder();
        sb.append(streetDetail);

        if (!selectedWard.isEmpty()) sb.append(", ").append(selectedWard);
        if (!selectedDistrict.isEmpty()) sb.append(", ").append(selectedDistrict);
        if (!selectedCity.isEmpty()) sb.append(", ").append(selectedCity);

        String addressToSend = sb.toString();

        String userID = SessionManager.getUserID(this);
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        AddressRequest request = new AddressRequest(userID, name, phone, addressType, addressToSend);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.addAddress(request).enqueue(new Callback<AddUpdateResponse>() {
            @Override
            public void onResponse(Call<AddUpdateResponse> call, Response<AddUpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddAddressActivity.this, "Thêm thành công!", Toast.LENGTH_SHORT).show();

                    Intent result = new Intent();


                    result.putExtra("newAddress", finalStreetDetail);
                    // ----------------------------------------------------------------

                    result.putExtra("newCustomer", name + " | " + phone);
                    result.putExtra("type", addressType);
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Toast.makeText(AddAddressActivity.this, "Lỗi server: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddUpdateResponse> call, Throwable t) {
                Toast.makeText(AddAddressActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_LOCATION && resultCode == RESULT_OK && data != null) {

            String fullAddress = data.getStringExtra("fullAddress");
            String oldKeyAddress = data.getStringExtra("selectedAddress");

            String displayAddress = (fullAddress != null && !fullAddress.isEmpty()) ? fullAddress : oldKeyAddress;

            String street = data.getStringExtra("street");
            String ward = data.getStringExtra("ward");
            String district = data.getStringExtra("district");
            String city = data.getStringExtra("city");

            selectedStreet = (street != null && !street.isEmpty()) ? street : displayAddress;
            selectedWard = (ward != null) ? ward : "";
            selectedDistrict = (district != null) ? district : "";
            selectedCity = (city != null) ? city : "";

            if (displayAddress != null) {
                tvPickAddress.setText(displayAddress);
                tvPickAddress.setTextColor(Color.BLACK);
            }
        }
    }
}
