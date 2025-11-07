// ActivityRegister.java - ĐÃ SỬA HOÀN CHỈNH - KHỚP 100% VỚI XML CỦA BẠN
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

    // --- Step 2: OTP (6 ô) ---
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private TextView tvOtpError, tvTimer;
    private TextView tvResendOtp; // Đây là cái bạn quên đặt ID trong XML
    private Button btnVerifyOtp;  // Bạn chưa có nút này → mình sẽ thêm vào XML

    // --- Step 3: Form ---
    private EditText edtUsername, edtPassword, edtPhone;
    private Button btnRegister;

    private ApiService apiService;
    private String verifiedEmail = "";
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupOtpInput();
        setupClickListeners();
    }

    private void initViews() {
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutOtp = findViewById(R.id.layoutOtp);
        layoutForm = findViewById(R.id.layoutForm);

        // Step 1
        edtEmail = findViewById(R.id.edtEmail);
        btnSendOTP = findViewById(R.id.btnSendOTP);

        // Step 2 - OTP
        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        tvOtpError = findViewById(R.id.tvOtpError);
        tvTimer = findViewById(R.id.tvTimer);
        tvResendOtp = findViewById(R.id.tvResendOtp); // ĐÃ THÊM TRONG XML DƯỚI
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp); // ĐÃ THÊM TRONG XML

        // Step 3
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtPhone = findViewById(R.id.edtPhone);
        btnRegister = findViewById(R.id.btnRegister);

        // Ẩn ban đầu
        layoutOtp.setVisibility(View.GONE);
        layoutForm.setVisibility(View.GONE);
        tvOtpError.setVisibility(View.GONE);
        if (tvResendOtp != null) tvResendOtp.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnSendOTP.setOnClickListener(v -> sendOtp());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        tvResendOtp.setOnClickListener(v -> resendOtp());
        btnRegister.setOnClickListener(v -> completeRegistration());
    }

    private void setupOtpInput() {
        EditText[] otpFields = {otp1, otp2, otp3, otp4, otp5, otp6};
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) {
                        otpFields[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }
            });
        }
    }

    private void sendOtp() {
        String email = edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            return;
        }

        btnSendOTP.setEnabled(false);
        btnSendOTP.setText("Đang gửi...");

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);

        apiService.signup(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                btnSendOTP.setEnabled(true);
                btnSendOTP.setText("Nhận mã OTP");

                if (response.isSuccessful() && response.body() != null &&
                        "success".equalsIgnoreCase(response.body().getStatus())) {
                    Toast.makeText(ActivityRegister.this, "OTP đã gửi đến email!", Toast.LENGTH_LONG).show();
                    verifiedEmail = email;
                    switchToOtpLayout();
                    startOtpTimer();
                } else {
                    Toast.makeText(ActivityRegister.this, "Lỗi: " + (response.body() != null ? response.body().getMessage() : "Unknown"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                btnSendOTP.setEnabled(true);
                btnSendOTP.setText("Nhận mã OTP");
                Toast.makeText(ActivityRegister.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void switchToOtpLayout() {
        layoutEmail.setVisibility(View.GONE);
        layoutOtp.setVisibility(View.VISIBLE);
        layoutForm.setVisibility(View.GONE);
        clearOtpFields();
        tvOtpError.setVisibility(View.GONE);
        tvResendOtp.setVisibility(View.GONE);
        otp1.requestFocus();
    }

    private void startOtpTimer() {
        tvTimer.setVisibility(View.VISIBLE);
        tvResendOtp.setVisibility(View.GONE);
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(300000, 1000) { // 5 phút
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvTimer.setText(String.format("Còn lại: %d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("OTP đã hết hạn");
                tvResendOtp.setVisibility(View.VISIBLE);
                tvResendOtp.setText("Gửi lại OTP");
            }
        }.start();
    }

    private void verifyOtp() {
        String otp = otp1.getText().toString() + otp2.getText().toString() +
                otp3.getText().toString() + otp4.getText().toString() +
                otp5.getText().toString() + otp6.getText().toString();

        if (otp.length() != 6) {
            tvOtpError.setText("Vui lòng nhập đủ 6 số");
            tvOtpError.setVisibility(View.VISIBLE);
            return;
        }

        btnVerifyOtp.setEnabled(false);
        btnVerifyOtp.setText("Đang xác thực...");

        Map<String, String> body = new HashMap<>();
        body.put("email", verifiedEmail);
        body.put("otp", otp);

        apiService.verifyOtp(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                btnVerifyOtp.setEnabled(true);
                btnVerifyOtp.setText("Xác nhận OTP");

                if (response.isSuccessful() && response.body() != null &&
                        "success".equalsIgnoreCase(response.body().getStatus())) {
                    Toast.makeText(ActivityRegister.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                    layoutOtp.setVisibility(View.GONE);
                    layoutForm.setVisibility(View.VISIBLE);
                    if (countDownTimer != null) countDownTimer.cancel();
                } else {
                    tvOtpError.setText("OTP sai hoặc đã hết hạn");
                    tvOtpError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                btnVerifyOtp.setEnabled(true);
                btnVerifyOtp.setText("Xác nhận OTP");
                Toast.makeText(ActivityRegister.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendOtp() {
        tvResendOtp.setText("Đang gửi lại...");
        Map<String, String> body = new HashMap<>();
        body.put("email", verifiedEmail);

        apiService.resendOtp(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActivityRegister.this, "Đã gửi lại OTP!", Toast.LENGTH_SHORT).show();
                    startOtpTimer();
                    clearOtpFields();
                    otp1.requestFocus();
                } else {
                    tvResendOtp.setText("Gửi lại thất bại");
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                tvResendOtp.setText("Lỗi mạng");
            }
        });
    }

    private void completeRegistration() {
        // Code đăng ký như cũ...
        // (giữ nguyên phần bạn đã có)
        // Mình để ngắn cho dễ đọc
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(username) || password.length() < 6 || !phone.matches("\\d{10,11}")) {
            Toast.makeText(this, "Vui lòng điền đầy đủ và đúng định dạng", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("email", verifiedEmail);
        body.put("username", username);
        body.put("password", password);
        body.put("phone", phone);

        apiService.setPasswordUsername(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        "success".equalsIgnoreCase(response.body().getStatus())) {
                    Toast.makeText(ActivityRegister.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ActivityRegister.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(ActivityRegister.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearOtpFields() {
        otp1.setText(""); otp2.setText(""); otp3.setText(""); otp4.setText(""); otp5.setText(""); otp6.setText("");
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}