package com.example.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ActivityForgotPassword extends AppCompatActivity {

    private LinearLayout layoutPhone, layoutOtp, layoutResetPassword;
    private EditText editTextPhone, editTextOtp, editTextNewPassword, editTextConfirmPassword;
    private Button buttonSendOtp, buttonVerifyOtp, buttonResetPassword;
    private TextView textViewBackToLogin;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        requestQueue = Volley.newRequestQueue(this);

        buttonSendOtp.setOnClickListener(v -> sendOtp());
        buttonVerifyOtp.setOnClickListener(v -> verifyOtp());
        buttonResetPassword.setOnClickListener(v -> resetPassword());

        textViewBackToLogin.setOnClickListener(v -> finish()); // Quay về login
    }

    private void initViews() {
        layoutPhone = findViewById(R.id.layout_phone_input);
        layoutOtp = findViewById(R.id.layout_otp_verification);
        layoutResetPassword = findViewById(R.id.layout_reset_password);

        editTextPhone = findViewById(R.id.edit_text_phone);
        editTextOtp = findViewById(R.id.edit_text_otp);
        editTextNewPassword = findViewById(R.id.edit_text_new_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);

        buttonSendOtp = findViewById(R.id.button_send_otp);
        buttonVerifyOtp = findViewById(R.id.button_verify_otp);
        buttonResetPassword = findViewById(R.id.button_reset_password);

        textViewBackToLogin = findViewById(R.id.text_view_back_to_login);
    }

    // Gửi OTP tới số điện thoại
    private void sendOtp() {
        String phone = editTextPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Nhập số điện thoại");
            return;
        }

        String url = "https://your-backend.com/send-otp"; // đổi thành URL backend thật
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(ActivityForgotPassword.this, "OTP đã gửi", Toast.LENGTH_SHORT).show();
                    layoutPhone.setVisibility(View.GONE);
                    layoutOtp.setVisibility(View.VISIBLE);
                },
                error -> Toast.makeText(ActivityForgotPassword.this, "Gửi OTP thất bại", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("phone", phone);
                return params;
            }
        };
        requestQueue.add(request);
    }

    // Xác thực OTP
    private void verifyOtp() {
        String otp = editTextOtp.getText().toString().trim();
        if (TextUtils.isEmpty(otp)) {
            editTextOtp.setError("Nhập OTP");
            return;
        }

        String url = "https://your-backend.com/verify-otp"; // đổi thành URL backend thật
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(ActivityForgotPassword.this, "OTP hợp lệ", Toast.LENGTH_SHORT).show();
                    layoutOtp.setVisibility(View.GONE);
                    layoutResetPassword.setVisibility(View.VISIBLE);
                },
                error -> Toast.makeText(ActivityForgotPassword.this, "OTP không đúng", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("phone", editTextPhone.getText().toString().trim());
                params.put("otp", otp);
                return params;
            }
        };
        requestQueue.add(request);
    }

    // Reset mật khẩu
    private void resetPassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            editTextNewPassword.setError("Nhập mật khẩu mới");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        String url = "https://your-backend.com/reset-password"; // đổi thành URL backend thật
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(ActivityForgotPassword.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    finish(); // quay về login
                },
                error -> Toast.makeText(ActivityForgotPassword.this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("phone", editTextPhone.getText().toString().trim());
                params.put("newPassword", newPassword);
                return params;
            }
        };
        requestQueue.add(request);
    }
}
