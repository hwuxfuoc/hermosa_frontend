package com.example.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.AuthResponse;
import com.example.demo.models.User;
import com.example.demo.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword,editTextEmail;
    private Button buttonLogin, buttonGoogleLogin, buttonFacebookLogin;
    private TextView textViewForgotPassword, textViewRegister;
    private CheckBox checkBoxRemember;
    private ApiService apiService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ view
        editTextEmail = findViewById(R.id.edit_text_username);
        editTextPassword = findViewById(R.id.edit_text_password);
        buttonLogin = findViewById(R.id.button_login);
        buttonGoogleLogin = findViewById(R.id.button_google_login);
        buttonFacebookLogin = findViewById(R.id.button_facebook_login);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password);
        textViewRegister = findViewById(R.id.text_view_register);

        // Thêm checkbox Remember me (nếu bạn có trong layout, nếu chưa có thì thêm vào XML)
        checkBoxRemember = new CheckBox(this);
        checkBoxRemember.setText("Ghi nhớ đăng nhập");

        // Thêm vào layout login (nếu muốn động, hoặc thêm trong XML cho gọn)
        LinearLayout layout = (LinearLayout) ((ScrollView) findViewById(R.id.activity_login)).getChildAt(0);
        layout.addView(checkBoxRemember, layout.getChildCount() - 4);

        apiService = ApiClient.getClient().create(ApiService.class);
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Nếu đã nhớ đăng nhập
        if (prefs.getBoolean("REMEMBER_ME", false)) {
            String savedUser = prefs.getString("USERNAME", null);
            String savedPass = prefs.getString("PASSWORD", null);
            if (savedUser != null && savedPass != null) {
                editTextUsername.setText(savedUser);
                editTextPassword.setText(savedPass);
                checkBoxRemember.setChecked(true);
            }
        }

        // Nút đăng nhập
        buttonLogin.setOnClickListener(v -> login());

        // Nút Google login (placeholder)
        buttonGoogleLogin.setOnClickListener(v ->
                Toast.makeText(this, "Google login chưa được cài đặt", Toast.LENGTH_SHORT).show()
        );

        // Nút Facebook login (placeholder)
        buttonFacebookLogin.setOnClickListener(v ->
                Toast.makeText(this, "Facebook login chưa được cài đặt", Toast.LENGTH_SHORT).show()
        );

        // Quên mật khẩu
        textViewForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(ActivityLogin.this, ActivityForgotPassword.class));
        });

        // Đăng ký
        textViewRegister.setOnClickListener(v -> {
            startActivity(new Intent(ActivityLogin.this, ActivityRegister.class));
        });
    }

    private void login() {
        String email = editTextEmail.getText().toString().trim();
        /*String username = editTextUsername.getText().toString().trim();*/
        String password = editTextPassword.getText().toString().trim();

        /*if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Vui lòng nhập username");
            return;
        }*/
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Vui lòng nhập email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        // Gọi API login
        Map<String, String> body = new HashMap<>();
        // ✅ SỬA 5: Gửi "email", KHÔNG PHẢI "username"
        body.put("email", email);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                // (Ẩn ProgressBar và bật lại nút)
                // progressBar.setVisibility(View.GONE);
                // buttonLogin.setEnabled(true);

                // ✅ SỬA 3: Sửa logic check (dùng "Successful" và .trim())
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getStatus().trim().equalsIgnoreCase("Successful")) {

                    AuthResponse.User user = response.body().getData();

                    if (user != null) {

                        // ✅ SỬA 4: LƯU VÀO SESSIONMANAGER
                        // (Dùng file SessionManager đã được nâng cấp)
                        SessionManager.saveUserSession(
                                ActivityLogin.this,
                                user.getUserID(),
                                user.getName(),
                                user.getPhone(),
                                user.getAddress()
                        );

                        // (Logic "Remember Me" của bạn)
                        SharedPreferences.Editor editor = prefs.edit();
                        if (checkBoxRemember.isChecked()) {
                            editor.putString("EMAIL", email); // Lưu email
                            editor.putString("PASSWORD", password); // (Không an toàn, nhưng giữ theo code cũ)
                        } else {
                            editor.remove("EMAIL");
                            editor.remove("PASSWORD");
                        }
                        editor.putBoolean("REMEMBER_ME", checkBoxRemember.isChecked());
                        editor.apply();

                        Toast.makeText(ActivityLogin.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        // Chuyển sang MainActivity
                        Intent intent = new Intent(ActivityLogin.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // (Lấy thông báo lỗi từ server nếu có)
                    String errorMsg = "Sai email hoặc mật khẩu";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(ActivityLogin.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(ActivityLogin.this, "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
