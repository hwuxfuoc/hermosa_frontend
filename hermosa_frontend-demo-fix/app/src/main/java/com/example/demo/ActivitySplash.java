package com.example.demo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.utils.NetworkUtil;

public class ActivitySplash extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500; // 2.5 giây

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (NetworkUtil.isNetworkConnected(this)) {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(ActivitySplash.this, ActivityLogin.class);
                startActivity(intent);
                finish();
            }, SPLASH_DELAY);
        } else {
            showNoInternetDialog();
        }
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Không có kết nối Internet")
                .setMessage("Vui lòng kiểm tra lại kết nối WiFi hoặc dữ liệu di động rồi thử lại.")
                .setCancelable(false)
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    if (NetworkUtil.isNetworkConnected(this)) {
                        Intent intent = new Intent(ActivitySplash.this, ActivityLogin.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Vẫn chưa có mạng!", Toast.LENGTH_SHORT).show();
                        showNoInternetDialog();
                    }
                })
                .setNegativeButton("Thoát", (dialog, which) -> finishAffinity())
                .show();
    }
}
