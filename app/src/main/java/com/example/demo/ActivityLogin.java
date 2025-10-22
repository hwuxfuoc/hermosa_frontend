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

public class ActivityLogin extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonGoogleLogin;
    private TextView textViewForgotPassword, textViewRegister;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Tham chiếu view
        editTextUsername = findViewById(R.id.edit_text_username);
        editTextPassword = findViewById(R.id.edit_text_password);
        buttonLogin = findViewById(R.id.button_login);
        buttonGoogleLogin = findViewById(R.id.button_google_login);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password);
        textViewRegister = findViewById(R.id.text_view_register);

        requestQueue = Volley.newRequestQueue(this);

        // Sự kiện nút đăng nhập
        buttonLogin.setOnClickListener(v -> login());

        // Sự kiện Google login
        buttonGoogleLogin.setOnClickListener(v -> {
            // TODO: Thêm logic đăng nhập bằng Google
            Toast.makeText(ActivityLogin.this, "Google login chưa cài đặt", Toast.LENGTH_SHORT).show();
        });

        // Sự kiện quên mật khẩu
        textViewForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLogin.this, ActivityForgotPassword.class);
            startActivity(intent);
        });

        // Sự kiện đăng ký
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLogin.this, ActivityRegister.class);
            startActivity(intent);
        });
    }

    private void login() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Vui lòng nhập username hoặc số điện thoại");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        String url = "http://localhost:8000/user/google/login"; // Thay URL backend thật
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Xử lý phản hồi từ server
                    if (response.contains("success")) { // ví dụ server trả về "success"
                        Toast.makeText(ActivityLogin.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        // Ví dụ mở HomeActivity
                        // Intent intent = new Intent(ActivityLogin.this, ActivityHome.class);
                        // startActivity(intent);
                        // finish();
                    } else {
                        Toast.makeText(ActivityLogin.this, "Tên đăng nhập hoặc mật khẩu sai", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ActivityLogin.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        requestQueue.add(request);
    }
}
