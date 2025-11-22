package com.example.demo.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.R;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CommonResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentForgotPassword extends Fragment {

    private EditText edtTempPass, edtNewPass, edtConfirmPass;
    private Button btnResetPassword;
    private TextView tvError;

    private ApiService apiService;

    public FragmentForgotPassword() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
        // Dùng chung XML với ActivityForgotPassword (chỉ khác phần đầu)
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient().create(ApiService.class);

        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        edtTempPass = view.findViewById(R.id.edtTempPass);
        edtNewPass = view.findViewById(R.id.edtNewPass);
        edtConfirmPass = view.findViewById(R.id.edtConfirmPass);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        tvError = view.findViewById(R.id.tvError);

        // Ẩn các phần không cần thiết trong Fragment (phần gửi email)
        View layoutTop = view.findViewById(R.id.btnBack); // Nếu có btnBack thì ẩn cả tiêu đề
        if (layoutTop != null && layoutTop.getParent() instanceof ViewGroup) {
            ((ViewGroup) layoutTop.getParent()).setVisibility(View.GONE);
        }

        // Ẩn phần nhập email + nút gửi cũ
        View oldSection = view.findViewById(R.id.edtEmail);
        if (oldSection != null) {
            ((ViewGroup) oldSection.getParent()).setVisibility(View.GONE);
        }
        View btnSend = view.findViewById(R.id.btnSendReset);
        if (btnSend != null) btnSend.setVisibility(View.GONE);
        View resendHint = view.findViewById(R.id.tvResendHint);
        if (resendHint != null) resendHint.setVisibility(View.GONE);
        View successLayout = view.findViewById(R.id.layoutSuccess);
        if (successLayout != null) successLayout.setVisibility(View.GONE);

        // Hiện tiêu đề "Đặt lại mật khẩu mới"
        TextView tvTitle = view.findViewById(R.id.tvNewPassTitle);
        if (tvTitle != null) tvTitle.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String tempPass = edtTempPass.getText().toString().trim();
        String newPass = edtNewPass.getText().toString().trim();
        String confirmPass = edtConfirmPass.getText().toString().trim();

        if (TextUtils.isEmpty(tempPass)) {
            tvError.setText("Vui lòng nhập mật khẩu tạm thời");
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        if (newPass.length() < 6) {
            tvError.setText("Mật khẩu mới phải ít nhất 6 ký tự");
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            tvError.setText("Xác nhận mật khẩu không khớp");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        tvError.setVisibility(View.GONE);
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Đang xử lý...");

        Map<String, String> body = new HashMap<>();
        body.put("tempPassword", tempPass);     // Backend dùng tempPassword
        body.put("newPassword", newPass);

        apiService.changePassword(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Đặt lại mật khẩu");

                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getStatus();
                    if ("success".equalsIgnoreCase(status) || "Sucsess".equalsIgnoreCase(status)) {
                        Toast.makeText(getContext(), "Đổi mật khẩu thành công! Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
                        // Có thể logout tự động hoặc chuyển về Login
                        requireActivity().finishAffinity();
                        // Hoặc: startActivity(new Intent(requireContext(), ActivityLogin.class));
                    } else {
                        tvError.setText("Mật khẩu tạm không đúng hoặc đã hết hạn");
                        tvError.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvError.setText("Lỗi server, thử lại sau");
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Đặt lại mật khẩu");
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}