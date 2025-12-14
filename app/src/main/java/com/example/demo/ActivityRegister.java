package com.example.demo;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.AuthResponse;
import com.example.demo.models.CommonResponse;
import com.example.demo.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity {

    private LinearLayout layoutEmail, layoutOtp;
    private ScrollView layoutForm;

    private EditText edtEmail;
    private Button btnSendOTP;

    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private TextView tvOtpError, tvTimer, tvResendOtp;
    private Button btnVerifyOtp;

    private EditText edtUsername, edtPassword, edtConfirmPassword;
    private ImageView imgTogglePass, imgToggleConfirmPass;
    private TextView tvPasswordError;
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
        setupPasswordToggle();
        setupClickListeners();
    }

    private void initViews() {
        View emailInclude = findViewById(R.id.layoutEmail);
        View otpInclude   = findViewById(R.id.layoutOtp);
        View formInclude  = findViewById(R.id.layoutForm);

        edtEmail   = emailInclude.findViewById(R.id.edtEmail);
        btnSendOTP = emailInclude.findViewById(R.id.btnSendOTP);

        otp1 = otpInclude.findViewById(R.id.otp1);
        otp2 = otpInclude.findViewById(R.id.otp2);
        otp3 = otpInclude.findViewById(R.id.otp3);
        otp4 = otpInclude.findViewById(R.id.otp4);
        otp5 = otpInclude.findViewById(R.id.otp5);
        otp6 = otpInclude.findViewById(R.id.otp6);
        tvOtpError  = otpInclude.findViewById(R.id.tvOtpError);
        tvTimer     = otpInclude.findViewById(R.id.tvTimer);
        tvResendOtp = otpInclude.findViewById(R.id.tvResendOtp);
        btnVerifyOtp= otpInclude.findViewById(R.id.btnVerifyOtp);

        edtUsername        = formInclude.findViewById(R.id.edtUsername);
        edtPassword        = formInclude.findViewById(R.id.edtPassword);
        edtConfirmPassword = formInclude.findViewById(R.id.edtConfirmPassword);
        imgTogglePass      = formInclude.findViewById(R.id.imgTogglePass);
        imgToggleConfirmPass = formInclude.findViewById(R.id.imgToggleConfirmPass);
        tvPasswordError    = formInclude.findViewById(R.id.tvPasswordError);
        btnRegister        = formInclude.findViewById(R.id.btnRegister);

        layoutEmail = (LinearLayout) emailInclude;
        layoutOtp   = (LinearLayout) otpInclude;
        layoutForm  = (ScrollView) formInclude;

        layoutOtp.setVisibility(View.GONE);
        layoutForm.setVisibility(View.GONE);
        tvOtpError.setVisibility(View.GONE);
        tvResendOtp.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);
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

    private void setupPasswordToggle() {
        imgTogglePass.setOnClickListener(v -> {
            if (edtPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                imgTogglePass.setImageResource(R.drawable.ic_eye_open);
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgTogglePass.setImageResource(R.drawable.ic_eye_closed);
            }
            edtPassword.setSelection(edtPassword.getText().length());
        });

        imgToggleConfirmPass.setOnClickListener(v -> {
            if (edtConfirmPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                imgToggleConfirmPass.setImageResource(R.drawable.ic_eye_open);
            } else {
                edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgToggleConfirmPass.setImageResource(R.drawable.ic_eye_closed);
            }
            edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
        });
    }

    private void sendOtp() {
        String email = edtEmail.getText().toString().trim().toLowerCase();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            return;
        }

        btnSendOTP.setEnabled(false);
        btnSendOTP.setText("Đang kiểm tra...");

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("email", email);
        loginBody.put("password", "dummy_password_for_check");

        apiService.login(loginBody).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && "Success".equals(response.body().getStatus())) {

                    btnSendOTP.setEnabled(true);
                    btnSendOTP.setText("Nhận mã OTP");
                    Toast.makeText(ActivityRegister.this,
                            "Email này đã được đăng ký! Vui lòng đăng nhập.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                sendOtpRequest(email);
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                sendOtpRequest(email);
            }
        });
    }

    private void sendOtpRequest(String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);

        apiService.signup(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                btnSendOTP.setEnabled(true);
                btnSendOTP.setText("Nhận mã OTP");

                if (response.isSuccessful() && response.body() != null
                        && response.body().getStatus() != null
                        && response.body().getStatus().toLowerCase().contains("success")) {

                    verifiedEmail = email;
                    Toast.makeText(ActivityRegister.this, "Đã gửi mã OTP!", Toast.LENGTH_SHORT).show();
                    layoutEmail.setVisibility(View.GONE);
                    layoutOtp.setVisibility(View.VISIBLE);
                    startOtpTimer();
                    clearOtpFields();
                    otp1.requestFocus();
                } else {
                    Toast.makeText(ActivityRegister.this, "Gửi OTP thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                btnSendOTP.setEnabled(true);
                btnSendOTP.setText("Nhận mã OTP");
                Toast.makeText(ActivityRegister.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startOtpTimer() {
        tvTimer.setVisibility(View.VISIBLE);
        tvResendOtp.setVisibility(View.GONE);
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(300000, 1000) {
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

                if (response.isSuccessful() && response.body() != null) {
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
                Toast.makeText(ActivityRegister.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
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
                if (response.isSuccessful() && response.body() != null) {
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
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            tvPasswordError.setText("Mật khẩu phải từ 6 ký tự");
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        if (!password.equals(confirmPass)) {
            tvPasswordError.setText("Mật khẩu không trùng khớp");
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        tvPasswordError.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("email", verifiedEmail);
        body.put("username", username);
        body.put("password", password);

        apiService.setPasswordUsername(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ActivityRegister.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                    String username = edtUsername.getText().toString().trim();

                    // LƯU VÀO SESSION
                    SessionManager.saveUserSession(
                            ActivityRegister.this,
                            verifiedEmail,
                            username,
                            "",
                            ""
                    );
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
        otp1.setText(""); otp2.setText(""); otp3.setText("");
        otp4.setText(""); otp5.setText(""); otp6.setText("");
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}