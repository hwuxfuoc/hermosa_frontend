package com.example.demo;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CommonResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity {

    // --- Layouts ---
    private LinearLayout layoutEmail, layoutOtp;
    private ScrollView layoutForm;

    // --- Views ---
    private EditText edtEmail, edtUsername, edtPassword, edtPhone;
    private Button btnSendOTP, btnRegister;
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private TextView tvOtpError, tvTimer;

    // (ProgressBar đã bị xóa)
    // private ProgressBar progressBar;

    private Button btnVerifyOtp; // Nút Xác nhận OTP

    private CountDownTimer countDownTimer;
    private ApiService apiService;
    private String verifiedEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getClient().create(ApiService.class);

        // --- Tham chiếu layout ---
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutOtp = findViewById(R.id.layoutOtp);
        layoutForm = findViewById(R.id.layoutForm);
        // (ProgressBar đã bị xóa)

        // --- Layout 1: Email ---
        edtEmail = layoutEmail.findViewById(R.id.edtEmail);
        btnSendOTP = layoutEmail.findViewById(R.id.btnSendOTP);

        // --- Layout 2: OTP ---
        otp1 = layoutOtp.findViewById(R.id.otp1);
        otp2 = layoutOtp.findViewById(R.id.otp2);
        otp3 = layoutOtp.findViewById(R.id.otp3);
        otp4 = layoutOtp.findViewById(R.id.otp4);
        otp5 = layoutOtp.findViewById(R.id.otp5);
        otp6 = layoutOtp.findViewById(R.id.otp6);
        tvOtpError = layoutOtp.findViewById(R.id.tvOtpError);
        tvTimer = layoutOtp.findViewById(R.id.tvTimer);
        btnVerifyOtp = layoutOtp.findViewById(R.id.btnVerifyOtp);

        // --- Layout 3: Register Form ---
        edtUsername = layoutForm.findViewById(R.id.edtUsername);
        edtPassword = layoutForm.findViewById(R.id.edtPassword);
        edtPhone = layoutForm.findViewById(R.id.edtPhone);
        btnRegister = layoutForm.findViewById(R.id.btnRegister);

        // --- Gán sự kiện Click ---
        btnSendOTP.setOnClickListener(v -> sendOtp());
        tvTimer.setOnClickListener(v -> resendOtp()); // (Nút Gửi lại)
        btnRegister.setOnClickListener(v -> register());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
    }

    private void sendOtp() {
        String email = edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }

        // ✅ SỬA LỖI ĐƠ APP (ANR): Vô hiệu hóa nút (Rất quan trọng)
        btnSendOTP.setEnabled(false);
        // progressBar.setVisibility(View.VISIBLE); // (Đã xóa)

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);

        apiService.signup(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                btnSendOTP.setEnabled(true);
                // progressBar.setVisibility(View.GONE); // (Đã xóa)

                if (response.isSuccessful() && response.body() != null &&
                        response.body().getStatus().trim().equalsIgnoreCase("Successful")) {

                    Toast.makeText(ActivityRegister.this, "OTP đã gửi, vui lòng kiểm tra email", Toast.LENGTH_SHORT).show();
                    verifiedEmail = email;
                    switchToOtpLayout();
                    startOtpTimer();
                } else {
                    String errorMsg = "Gửi OTP thất bại";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(ActivityRegister.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                btnSendOTP.setEnabled(true);
                // progressBar.setVisibility(View.GONE); // (Đã xóa)
                Toast.makeText(ActivityRegister.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtp() {
        String o1 = otp1.getText().toString();
        String o2 = otp2.getText().toString();
        String o3 = otp3.getText().toString();
        String o4 = otp4.getText().toString();
        String o5 = otp5.getText().toString();
        String o6 = otp6.getText().toString();

        String otp = o1 + o2 + o3 + o4 + o5 + o6;

        if (otp.length() < 6) {
            tvOtpError.setText("Vui lòng nhập đủ 6 số OTP");
            tvOtpError.setVisibility(View.VISIBLE);
            return;
        }

        // Vô hiệu hóa nút
        btnVerifyOtp.setEnabled(false);
        // progressBar.setVisibility(View.VISIBLE); // (Đã xóa)
        tvOtpError.setVisibility(View.GONE);

        // ✅ SỬA 2: XÓA KHAI BÁO 'body' BỊ TRÙNG
        Map<String, String> body = new HashMap<>();
        body.put("email", verifiedEmail);
        body.put("otp", otp);

        apiService.verifyOtp(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                btnVerifyOtp.setEnabled(true);
                // progressBar.setVisibility(View.GONE); // (Đã xóa)

                // (Backend /verify-otp trả về "Success")
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getStatus().trim().equalsIgnoreCase("Success")) {

                    Toast.makeText(ActivityRegister.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                    switchToRegisterForm(); // Chuyển sang Bước 3

                } else {
                    String errorMsg = "Xác thực thất bại";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    tvOtpError.setText(errorMsg);
                    tvOtpError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                btnVerifyOtp.setEnabled(true);
                // progressBar.setVisibility(View.GONE); // (Đã xóa)
                tvOtpError.setText("Lỗi kết nối: " + t.getMessage());
                tvOtpError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void switchToOtpLayout() {
        layoutEmail.setVisibility(View.GONE);
        layoutOtp.setVisibility(View.VISIBLE);
        layoutForm.setVisibility(View.GONE);
    }

    private void switchToRegisterForm() {
        layoutEmail.setVisibility(View.GONE);
        layoutOtp.setVisibility(View.GONE);
        layoutForm.setVisibility(View.VISIBLE);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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

        // (Vô hiệu hóa nút Gửi lại)
        tvTimer.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("email", verifiedEmail);
        apiService.resendOtp(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                tvTimer.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ActivityRegister.this, "Đã gửi lại OTP", Toast.LENGTH_SHORT).show();
                    startOtpTimer();
                }
            }
            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                tvTimer.setEnabled(true);
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

        btnRegister.setEnabled(false);
        // progressBar.setVisibility(View.VISIBLE); // (Đã xóa)

        Map<String, Object> body = new HashMap<>();
        body.put("email", verifiedEmail);
        body.put("username", username);
        body.put("password", password);
        body.put("phone", phone);

        // (Bạn cần sửa ApiService.java để setPasswordUsername nhận Map<String, Object>)
        apiService.setPasswordUsername(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                btnRegister.setEnabled(true);
                // progressBar.setVisibility(View.GONE); // (Đã xóa)

                // ✅ SỬA 3: SỬA LỖI LOGIC (Backend trả về "Success")
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getStatus().trim().equalsIgnoreCase("Success")) {

                    Toast.makeText(ActivityRegister.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Hoặc chuyển về Login
                } else {
                    Toast.makeText(ActivityRegister.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                // progressBar.setVisibility(View.GONE); // (Đã xóa)
                Toast.makeText(ActivityRegister.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}