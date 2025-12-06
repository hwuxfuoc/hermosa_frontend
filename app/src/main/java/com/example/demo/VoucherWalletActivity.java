package com.example.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherWalletActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvVoucherList;
    private View layoutEmpty;

    private VoucherAdapter voucherAdapter;
    private List<Voucher> voucherList = new ArrayList<>();
    private ApiService apiService;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_wallet); // ← Tạo layout này nhé

        apiService = ApiClient.getClient().create(ApiService.class);
        userID = SessionManager.getUserID(this);

        initViews();
        setupRecyclerView();
        loadMyVouchers();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvVoucherList = findViewById(R.id.rvVoucherList);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        voucherAdapter = new VoucherAdapter(this, voucherList, null); // không cần listener vì chỉ xem
        rvVoucherList.setLayoutManager(new LinearLayoutManager(this));
        rvVoucherList.setAdapter(voucherAdapter);
    }

    private void loadMyVouchers() {
        apiService.getMyVouchers(userID).enqueue(new Callback<VoucherResponse>() {
            @Override
            public void onResponse(Call<VoucherResponse> call, Response<VoucherResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    voucherList.clear();
                    voucherList.addAll(response.body().getData());
                    voucherAdapter.notifyDataSetChanged();

                    if (voucherList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvVoucherList.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvVoucherList.setVisibility(View.VISIBLE);
                    }
                } else {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvVoucherList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<VoucherResponse> call, Throwable t) {
                Toast.makeText(VoucherWalletActivity.this, "Lỗi tải ví voucher", Toast.LENGTH_SHORT).show();
                layoutEmpty.setVisibility(View.VISIBLE);
                rvVoucherList.setVisibility(View.GONE);
            }
        });
    }
}