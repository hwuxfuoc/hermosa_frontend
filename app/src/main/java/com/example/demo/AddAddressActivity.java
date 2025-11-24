package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class AddAddressActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_LOCATION = 200;

    private EditText etCustomerName, etPhone, etBuilding, etGate, etNote;
    private TextView tvPickAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_add_address);

        etCustomerName = findViewById(R.id.etCustomerName);
        etPhone = findViewById(R.id.etPhone);
        tvPickAddress = findViewById(R.id.tvPickAddress);
        etBuilding = findViewById(R.id.etBuilding);
        etGate = findViewById(R.id.etGate);
        etNote = findViewById(R.id.etNote);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Bấm vào "Chọn địa chỉ" → mở bản đồ
        tvPickAddress.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, PickLocationActivity.class), REQUEST_PICK_LOCATION);
        });

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        String name = etCustomerName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = tvPickAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.equals("Chọn địa chỉ")) {
            return; // Có thể thêm Toast
        }

        String fullAddress = address;
        if (!etBuilding.getText().toString().trim().isEmpty())
            fullAddress += ", " + etBuilding.getText().toString().trim();
        if (!etGate.getText().toString().trim().isEmpty())
            fullAddress += ", Cổng " + etGate.getText().toString().trim();
        if (!etNote.getText().toString().trim().isEmpty())
            fullAddress += " | " + etNote.getText().toString().trim();

        Intent result = new Intent();
        result.putExtra("newAddress", fullAddress);
        result.putExtra("newCustomer", name + " | " + phone);
        result.putExtra("type", getIntent().getStringExtra("type"));
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_LOCATION && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("selectedAddress");
            tvPickAddress.setText(address);
            tvPickAddress.setTextColor(getResources().getColor(android.R.color.black));
        }
    }
}