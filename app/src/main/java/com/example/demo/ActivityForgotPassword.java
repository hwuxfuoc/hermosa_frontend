package com.example.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CommonResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityForgotPassword extends AppCompatActivity {

    private EditText edtEmail;
    private Button btnSendReset;
    private TextView tvResendHint;
    private ImageButton btnBack;
    private LinearLayout layoutSuccess;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        tvResendHint = findViewById(R.id.tvResendHint);
        btnBack = findViewById(R.id.btnBack);
        layoutSuccess = findViewById(R.id.layoutSuccess);
    }

    private void setupClickListeners() {
        btnSendReset.setOnClickListener(v -> sendResetRequest());
        tvResendHint.setOnClickListener(v -> sendResetRequest());
        btnBack.setOnClickListener(v -> finish());
    }

    private void sendResetRequest() {
        String email = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }

        btnSendReset.setEnabled(false);
        btnSendReset.setText("Đang gửi...");
        tvResendHint.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        apiService.forgotPassword(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                btnSendReset.setEnabled(true);
                btnSendReset.setText("Gửi mật khẩu tạm qua email");
                tvResendHint.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getStatus();
                    if ("Sucsess".equals(status) || "Success".equals(status)) {
                        layoutSuccess.setVisibility(View.VISIBLE);
                        Toast.makeText(ActivityForgotPassword.this, "Đã gửi mật khẩu tạm!", Toast.LENGTH_SHORT).show();
                        edtEmail.setEnabled(false); // Không cho sửa email nữa
                    } else {
                        Toast.makeText(ActivityForgotPassword.this, "Email không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ActivityForgotPassword.this, "Gửi thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                btnSendReset.setEnabled(true);
                btnSendReset.setText("Gửi mật khẩu tạm qua email");
                tvResendHint.setEnabled(true);
                Toast.makeText(ActivityForgotPassword.this, "Lỗi mạng, kiểm tra kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}