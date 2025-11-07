package com.example.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ActivityForgotPassword extends AppCompatActivity {

    private EditText edtEmail, edtTempPass, edtNewPass, edtConfirmPass;
    private Button btnSendReset, btnResetPassword;
    private TextView tvResendHint, tvError;
    private ImageButton btnBack;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        requestQueue = Volley.newRequestQueue(this);

        // Nút gửi yêu cầu quên mật khẩu
        btnSendReset.setOnClickListener(v -> sendResetRequest());

        // Nút đặt lại mật khẩu
        btnResetPassword.setOnClickListener(v -> resetPassword());

        // Nút gửi lại yêu cầu
        tvResendHint.setOnClickListener(v -> sendResetRequest());

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtTempPass = findViewById(R.id.edtTempPass);
        edtNewPass = findViewById(R.id.edtNewPass);
        edtConfirmPass = findViewById(R.id.edtConfirmPass);
        btnSendReset = findViewById(R.id.btnSendReset);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvResendHint = findViewById(R.id.tvResendHint);
        tvError = findViewById(R.id.tvError);
        btnBack = findViewById(R.id.btnBack);
    }

    // Gửi yêu cầu đặt lại mật khẩu (bước 1)
    private void sendResetRequest() {
        String email = edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }

        String url = "https://your-backend.com/api/send-reset-password"; // TODO: đổi thành URL thật của bạn
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Đã gửi mật khẩu tạm qua email", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Gửi yêu cầu thất bại", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        requestQueue.add(request);
    }

    // Đặt lại mật khẩu mới (bước 2)
    private void resetPassword() {
        String email = edtEmail.getText().toString().trim();
        String tempPass = edtTempPass.getText().toString().trim();
        String newPass = edtNewPass.getText().toString().trim();
        String confirmPass = edtConfirmPass.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Nhập email");
            return;
        }
        if (TextUtils.isEmpty(tempPass)) {
            edtTempPass.setError("Nhập mật khẩu tạm thời");
            return;
        }
        if (TextUtils.isEmpty(newPass)) {
            edtNewPass.setError("Nhập mật khẩu mới");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            edtConfirmPass.setError("Mật khẩu không khớp");
            return;
        }

        String url = "https://your-backend.com/api/reset-password"; // TODO: đổi thành URL thật
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    finish(); // quay lại trang đăng nhập
                },
                error -> {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("Mật khẩu tạm thời không đúng hoặc đã hết hạn");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("tempPassword", tempPass);
                params.put("newPassword", newPass);
                return params;
            }
        };

        requestQueue.add(request);
    }
}
