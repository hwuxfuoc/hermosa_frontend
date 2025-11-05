package com.example.demo;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.model.CommonResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity {

    // --- Layouts ---
    private LinearLayout layoutEmail, layoutOtp;
    private ScrollView layoutForm;

    // --- Step 1: Email ---
    private EditText edtEmail;
    private Button btnSendOTP;

    // --- Step 2: OTP ---
    private EditText otp1, otp2, otp3, otp4;
    private TextView tvOtpError, tvTimer;
    private CountDownTimer countDownTimer;

    // --- Step 3: Form ---
    private EditText edtUsername, edtPassword, edtPhone;
    private Button btnRegister;

    private ApiService apiService;
    private String verifiedEmail = ""; // lưu lại email đã xác thực thành công

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // XML tổng của bạn

        apiService = ApiClient.getClient().create(ApiService.class);

        // --- Tham chiếu layout ---
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutOtp = findViewById(R.id.layoutOtp);
        layoutForm = findViewById(R.id.layoutForm);

        // --- Layout 1: Email ---
        edtEmail = layoutEmail.findViewById(R.id.edtEmail);
        btnSendOTP = layoutEmail.findViewById(R.id.btnSendOTP);

        // --- Layout 2: OTP ---
        otp1 = layoutOtp.findViewById(R.id.otp1);
        otp2 = layoutOtp.findViewById(R.id.otp2);
        otp3 = layoutOtp.findViewById(R.id.otp3);
        otp4 = layoutOtp.findViewById(R.id.otp4);
        tvOtpError = layoutOtp.findViewById(R.id.tvOtpError);
        tvTimer = layoutOtp.findViewById(R.id.tvTimer);

        // --- Layout 3: Register Form ---
        edtUsername = layoutForm.findViewById(R.id.edtUsername);
        edtPassword = layoutForm.findViewById(R.id.edtPassword);
        edtPhone = layoutForm.findViewById(R.id.edtPhone);
        btnRegister = layoutForm.findViewById(R.id.btnRegister);

        // --- Step 1: Gửi OTP ---
        btnSendOTP.setOnClickListener(v -> sendOtp());

        // --- Step 2: Xác thực OTP ---
        layoutOtp.findViewById(R.id.tvTimer).setOnClickListener(v -> resendOtp());
        layoutOtp.findViewById(R.id.tvOtpError).setVisibility(View.GONE);

        // --- Step 3: Đăng ký ---
        btnRegister.setOnClickListener(v -> register());
    }

    private void sendOtp() {
        String email = edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        apiService.signup(new HashMap<String, Object>() {{
            put("email", email);
        }}).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getStatus().equalsIgnoreCase("success")) {

                    Toast.makeText(ActivityRegister.this, "OTP đã gửi, vui lòng kiểm tra email", Toast.LENGTH_SHORT).show();
                    verifiedEmail = email;
                    switchToOtpLayout();
                    startOtpTimer();
                } else {
                    Toast.makeText(ActivityRegister.this, "Gửi OTP thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(ActivityRegister.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchToOtpLayout() {
        layoutEmail.setVisibility(View.GONE);
        layoutOtp.setVisibility(View.VISIBLE);
        layoutForm.setVisibility(View.GONE);
    }

    private void startOtpTimer() {
        tvTimer.setVisibility(View.VISIBLE);
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(5 * 60 * 1000, 1000) { // 5 phút
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvTimer.setText(String.format("%d:%02d", minutes, seconds));
            }

            public void onFinish() {
                tvTimer.setText("Hết hạn, bấm để gửi lại OTP");
            }
        }.start();
    }

    private void resendOtp() {
        if (verifiedEmail.isEmpty()) return;

        Map<String, String> body = new HashMap<>();
        body.put("email", verifiedEmail);

        apiService.resendOtp(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActivityRegister.this, "Đã gửi lại OTP", Toast.LENGTH_SHORT).show();
                    startOtpTimer();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(ActivityRegister.this, "Lỗi gửi lại OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void register() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(username)) { edtUsername.setError("Vui lòng nhập tên đăng nhập"); return; }
        if (TextUtils.isEmpty(password)) { edtPassword.setError("Vui lòng nhập mật khẩu"); return; }
        if (TextUtils.isEmpty(phone)) { edtPhone.setError("Vui lòng nhập số điện thoại"); return; }

        Map<String, Object> body = new HashMap<>();
        body.put("email", verifiedEmail);
        body.put("username", username);
        body.put("password", password);
        body.put("phone", phone);

        apiService.setPasswordUsername(new HashMap<String, String>() {{
            put("email", verifiedEmail);
            put("username", username);
            put("password", password);
        }}).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getStatus().equalsIgnoreCase("success")) {

                    Toast.makeText(ActivityRegister.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ActivityRegister.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(ActivityRegister.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
