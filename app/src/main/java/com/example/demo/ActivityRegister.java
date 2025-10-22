package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhone, etUsername, etPassword, etConfirmPassword;
    private Button btnRegister, btnGoogleRegister;
    private TextView tvBackToLogin;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Tham chiếu view
        etFirstName = findViewById(R.id.edit_text_firstname);
        etLastName = findViewById(R.id.edit_text_lastname);
        etPhone = findViewById(R.id.edit_text_phone);
        etUsername = findViewById(R.id.edit_text_username);
        etPassword = findViewById(R.id.edit_text_password);
        etConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        btnRegister = findViewById(R.id.button_register);
        btnGoogleRegister = findViewById(R.id.button_google_register);
        tvBackToLogin = findViewById(R.id.text_view_back_to_login);

        requestQueue = Volley.newRequestQueue(this);

        // Nút đăng ký
        btnRegister.setOnClickListener(v -> register());

        // Nút Google register
        btnGoogleRegister.setOnClickListener(v -> {
            Toast.makeText(ActivityRegister.this, "Google register chưa cài đặt", Toast.LENGTH_SHORT).show();
        });

        // Quay lại login
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

        // Kiểm tra dữ liệu nhập
        if (TextUtils.isEmpty(firstName)) { etFirstName.setError("Vui lòng nhập họ"); return; }
        if (TextUtils.isEmpty(lastName)) { etLastName.setError("Vui lòng nhập tên"); return; }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Vui lòng nhập số điện thoại"); return; }
        if (TextUtils.isEmpty(username)) { etUsername.setError("Vui lòng nhập username"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Vui lòng nhập mật khẩu"); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Mật khẩu không khớp"); return; }

        String url = "https://your-backend.com/register"; // Thay URL backend thật
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.contains("success")) {
                        Toast.makeText(ActivityRegister.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        // Chuyển về login
                        Intent intent = new Intent(ActivityRegister.this, ActivityLogin.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ActivityRegister.this, "Đăng ký thất bại: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ActivityRegister.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("firstName", firstName);
                params.put("lastName", lastName);
                params.put("phone", phone);
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        requestQueue.add(request);
    }
}
