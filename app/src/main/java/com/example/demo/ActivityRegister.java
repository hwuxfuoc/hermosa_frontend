package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

public class ActivityRegister extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhone, etUsername, etPassword, etConfirmPassword;
    private Button btnRegister, btnGoogleRegister;
    private TextView tvBackToLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFirstName = findViewById(R.id.edit_text_firstname);
        etLastName = findViewById(R.id.edit_text_lastname);
        etPhone = findViewById(R.id.edit_text_phone);
        etUsername = findViewById(R.id.edit_text_username);
        etPassword = findViewById(R.id.edit_text_password);
        etConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        btnRegister = findViewById(R.id.button_register);
        btnGoogleRegister = findViewById(R.id.button_google_register);
        tvBackToLogin = findViewById(R.id.text_view_back_to_login);

        // Khởi tạo API
        apiService = ApiClient.getClient().create(ApiService.class);

        btnRegister.setOnClickListener(v -> register());
        btnGoogleRegister.setOnClickListener(v ->
                Toast.makeText(ActivityRegister.this, "Google register chưa cài đặt", Toast.LENGTH_SHORT).show()
        );

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityRegister.this, ActivityLogin.class);
            startActivity(intent);
            finish();
        });
    }

    private void register() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) { etFirstName.setError("Vui lòng nhập họ"); return; }
        if (TextUtils.isEmpty(lastName)) { etLastName.setError("Vui lòng nhập tên"); return; }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Vui lòng nhập số điện thoại"); return; }
        if (TextUtils.isEmpty(username)) { etUsername.setError("Vui lòng nhập email hoặc username"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Vui lòng nhập mật khẩu"); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Mật khẩu không khớp"); return; }

        // Gộp họ tên
        String fullName = firstName + " " + lastName;

        Map<String, Object> body = new HashMap<>();
        body.put("name", fullName);
        body.put("mail", username);
        body.put("phone", phone);
        body.put("password", password);

        Call<CommonResponse> call = apiService.signup(body);
        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CommonResponse res = response.body();
                    if ("success".equalsIgnoreCase(res.getStatus())) {
                        Toast.makeText(ActivityRegister.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ActivityRegister.this, ActivityLogin.class));
                        finish();
                    } else {
                        Toast.makeText(ActivityRegister.this, "Lỗi: " + res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ActivityRegister.this, "Đăng ký thất bại (API response lỗi)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(ActivityRegister.this, "Không kết nối được server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

