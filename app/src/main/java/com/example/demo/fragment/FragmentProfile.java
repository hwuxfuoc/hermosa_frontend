package com.example.demo.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.example.demo.ActivityLogin;
import com.example.demo.R;
import com.example.demo.SelectAddressActivity;
import com.example.demo.VoucherWalletActivity;
import com.example.demo.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class FragmentProfile extends Fragment {

    private LinearLayout layoutVoucher, layoutAddress, layoutFavorite, layoutHistory;
    private LinearLayout layoutLogout, layoutDeleteAccount, layoutForgotPassword;
    private TextView tvProfileName, tvUserName, tvUserPoints;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserPoints = view.findViewById(R.id.tvUserPoints);

        layoutVoucher = view.findViewById(R.id.layout_voucher);
        layoutAddress = view.findViewById(R.id.layout_address);
        layoutFavorite = view.findViewById(R.id.layout_favorite);
        layoutHistory = view.findViewById(R.id.layout_history);
        layoutLogout = view.findViewById(R.id.layout_logout);
        layoutDeleteAccount = view.findViewById(R.id.layout_delete_account);
        layoutForgotPassword = view.findViewById(R.id.layout_forgot_password);

        updateUserInfo();

        setClickable(layoutVoucher);
        setClickable(layoutAddress);
        setClickable(layoutFavorite);
        setClickable(layoutHistory);
        setClickable(layoutLogout);
        setClickable(layoutDeleteAccount);
        setClickable(layoutForgotPassword);

        layoutFavorite.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FragmentFavorite())
                    .addToBackStack(null)
                    .commit();
        });

        layoutLogout.setOnClickListener(v -> showLogoutDialog());

        layoutDeleteAccount.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tính năng xóa tài khoản đang phát triển!", Toast.LENGTH_LONG).show();
        });

        layoutForgotPassword.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FragmentForgotPassword())
                    .addToBackStack(null)  // Để bấm back về được
                    .commit();
        });

        layoutVoucher.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), VoucherWalletActivity.class);
            startActivity(intent);
        });

        layoutAddress.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SelectAddressActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void showLogoutDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        MaterialButton btnConfirmLogout = dialogView.findViewById(R.id.btnConfirmLogout);
        MaterialButton btnCancelLogout = dialogView.findViewById(R.id.btnCancelLogout);

        btnConfirmLogout.setOnClickListener(v -> {
            SessionManager.clearSession(requireContext());
            Toast.makeText(requireContext(), "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

            dialog.dismiss();

            Intent intent = new Intent(requireContext(), ActivityLogin.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        btnCancelLogout.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateUserInfo() {
        String name = SessionManager.getUserName(requireContext());

        tvProfileName.setText(name != null && !name.isEmpty() ? name : "User");
        tvUserName.setText(name != null && !name.isEmpty() ? name : "User");
        tvUserPoints.setText("2345 points • Thành viên Kim Cương");
    }

    private void setClickable(LinearLayout layout) {
        layout.setClickable(true);
        layout.setFocusable(true);
        // FIX: ripple_rounded_corners → dùng drawable có sẵn
        layout.setBackground(ContextCompat.getDrawable(requireContext(), android.R.drawable.list_selector_background));
    }
}