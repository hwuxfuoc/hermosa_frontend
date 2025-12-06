package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.adapters.VoucherAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.Voucher;
import com.example.demo.models.VoucherResponse;
import com.example.demo.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherSelectionActivity extends AppCompatActivity implements VoucherAdapter.OnVoucherClickListener {

    private ImageView btnBack;
    private EditText etVoucherCode;
    private Button btnApplyInput, btnConfirm;
    private RecyclerView rvVoucherList;
    private TextView tvSavings;

    private VoucherAdapter voucherAdapter;
    private List<Voucher> voucherList = new ArrayList<>();
    private Voucher selectedVoucher = null;
    private ApiService apiService;
    private String userID;
    private static final String TAG = "VOUCHER_SELECT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_voucher_selection); // Sử dụng layout bạn cung cấp

        apiService = ApiClient.getClient().create(ApiService.class);
        userID = SessionManager.getUserID(this);

        initViews();
        setupRecyclerView();
        loadAvailableVouchers();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etVoucherCode = findViewById(R.id.etVoucherCode);
        btnApplyInput = findViewById(R.id.btnApplyInput);
        btnConfirm = findViewById(R.id.btnConfirm);
        rvVoucherList = findViewById(R.id.rvVoucherList);
        tvSavings = findViewById(R.id.tvSavings);

        tvSavings.setText("Chưa chọn voucher");
        btnConfirm.setEnabled(false);
    }

    private void setupRecyclerView() {
        voucherAdapter = new VoucherAdapter(this, voucherList, this);
        rvVoucherList.setLayoutManager(new LinearLayoutManager(this));
        rvVoucherList.setAdapter(voucherAdapter);
    }

    private void loadAvailableVouchers() {
        // Gọi API lấy danh sách voucher khả dụng (API số 2 trong hướng dẫn trước)
        apiService.getAvailableVouchers(userID).enqueue(new Callback<VoucherResponse>() {
            @Override
            public void onResponse(Call<VoucherResponse> call, Response<VoucherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    voucherList.clear();
                    if (response.body().getData() != null) {
                        voucherList.addAll(response.body().getData());
                    }
                    voucherAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(VoucherSelectionActivity.this, "Không thể tải danh sách voucher", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VoucherResponse> call, Throwable t) {
                Toast.makeText(VoucherSelectionActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Xử lý nhập mã thủ công
        btnApplyInput.setOnClickListener(view -> {
            String code = etVoucherCode.getText().toString().trim();
            if (code.isEmpty()) return;
            boolean found = false;
            for (Voucher voucherItem : voucherList) {
                if (voucherItem.getVoucherCode().equalsIgnoreCase(code)) {
                    onVoucherClick(voucherItem); // Chọn voucher này
                    found = true;
                    break;
                }
            }

            if (!found) {
                Toast.makeText(this, "Mã voucher không hợp lệ hoặc không khả dụng", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút xác nhận trả kết quả về màn hình ConfirmOrder
        btnConfirm.setOnClickListener(v -> {
            if (selectedVoucher != null) {
                Log.d(TAG, "Bấm nút XÁC NHẬN. Đang gửi voucher về ConfirmOrderActivity: " + selectedVoucher.getVoucherCode());
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedVoucher", selectedVoucher); // Voucher phải implements Serializable
                setResult(RESULT_OK, resultIntent);
                finish();
            }else {
                Log.e(TAG, "Lỗi: Nút xác nhận được bấm nhưng selectedVoucher đang là null");
            }

        });
    }

    @Override
    public void onVoucherClick(Voucher voucher) {
        Log.d(TAG, "--------------------------------------------------");
        Log.d(TAG, "Người dùng đã chọn Voucher:");
        Log.d(TAG, "Code: " + voucher.getVoucherCode());
        Log.d(TAG, "Mô tả: " + voucher.getDescription());
        Log.d(TAG, "Giảm: " + voucher.getDiscountDisplay());

        this.selectedVoucher = voucher;

        tvSavings.setText("Bạn sẽ tiết kiệm được: " + voucher.getDiscountDisplay());
        btnConfirm.setEnabled(true);
        voucherAdapter.setSelectedVoucher(voucher);
    }
}