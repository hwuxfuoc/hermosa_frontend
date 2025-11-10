package com.example.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private ImageView imgTogglePassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private TextView textViewForgotPassword; // ← THÊM DÒNG NÀY
    private CheckBox checkBoxRemember;
    private ApiService apiService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.edit_text_username);
        editTextPassword = findViewById(R.id.edit_text_password);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        buttonLogin = findViewById(R.id.button_login);
        textViewRegister = findViewById(R.id.text_view_register);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password); // ← THÊM DÒNG NÀY
        checkBoxRemember = findViewById(R.id.checkBoxRemember);

        apiService = ApiClient.getClient().create(ApiService.class);
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Load nhớ đăng nhập
        if (prefs.getBoolean("REMEMBER_ME", false)) {
            editTextEmail.setText(prefs.getString("EMAIL", ""));
            editTextPassword.setText(prefs.getString("PASSWORD", ""));
            checkBoxRemember.setChecked(true);
        }

        // Hiện/ẩn mật khẩu
        imgTogglePassword.setOnClickListener(v -> {
            if (editTextPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                imgTogglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            editTextPassword.setSelection(editTextPassword.getText().length());
        });

        buttonLogin.setOnClickListener(v -> login());
        textViewRegister.setOnClickListener(v -> startActivity(new Intent(this, ActivityRegister.class)));
        textViewForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ActivityForgotPassword.class)));
    }

    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Nhập mật khẩu");
            return;
        }

        buttonLogin.setEnabled(false);
        buttonLogin.setText("Đang đăng nhập...");

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Log.d("LOGIN", "Gửi: email=" + email);

        // DÙNG .login() – endpoint mới
        apiService.login(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Đăng nhập");

                Log.d("LOGIN", "Code: " + response.code());
                if (response.body() != null) {
                    Log.d("LOGIN", "Status: " + response.body().getStatus());
                    Log.d("LOGIN", "UserID: " + (response.body().getData() != null ? response.body().getData().getUserID() : "null"));
                }

                if (response.isSuccessful() && response.body() != null) {
                    if ("Success".equals(response.body().getStatus())) {
                        AuthResponse.User user = response.body().getData();
                        String userId = user != null ? user.getUserID() : "unknown";

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("EMAIL", email);
                        editor.putString("USER_ID", userId);
                        editor.putBoolean("IS_LOGGED_IN", true);
                        if (checkBoxRemember.isChecked()) {
                            editor.putString("PASSWORD", password);
                            editor.putBoolean("REMEMBER_ME", true);
                        }
                        editor.apply();

                        Toast.makeText(ActivityLogin.this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ActivityLogin.this, MainActivity.class));
                        finish();
                        return;
                    }
                }

                Toast.makeText(ActivityLogin.this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Đăng nhập");
                Log.e("LOGIN", "Lỗi: " + t.getMessage());
                Toast.makeText(ActivityLogin.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}