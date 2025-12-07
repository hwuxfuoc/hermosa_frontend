/*
package com.example.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.fragment.FragmentCart;
import com.example.demo.fragment.FragmentHome;
import com.example.demo.fragment.FragmentNotification;
import com.example.demo.fragment.FragmentProfile;
import com.example.demo.models.CommonResponse; // Đảm bảo bạn có model này hoặc dùng Object/ResponseBody
import com.example.demo.utils.SessionManager; // Class quản lý UserID của bạn
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private static final String TAG = "MainActivity_FCM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Mặc định load Home
        if (savedInstanceState == null) {
            replaceFragment(new FragmentHome(), "FragmentHome");
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String tag = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new FragmentHome();
                tag = "FragmentHome";
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new FragmentCart();
                tag = "FragmentCart";
            } else if (itemId == R.id.nav_noti) {
                selectedFragment = new FragmentNotification();
                tag = "FragmentNotification";
            } else if (itemId == R.id.menu_profile) {
                selectedFragment = new FragmentProfile();
                tag = "FragmentProfile";
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment, tag);
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (view, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            view.setPadding(0, 0, 0, bottomInset);
            return insets;
        });

        // 1. Xin quyền thông báo (Android 13+)
        checkNotificationPermission();

        // 2. Lấy FCM Token và gửi lên Server
        getAndSendFCMToken();

        // 3. Kiểm tra xem App có được mở từ Thông báo không
        handleNotificationIntent(getIntent());
    }

    // --- BỔ SUNG 1: Xử lý Intent khi App đang chạy ngầm mà người dùng bấm thông báo ---
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("OPEN_FROM_NOTI")) {
            boolean openFromNoti = intent.getBooleanExtra("OPEN_FROM_NOTI", false);
            if (openFromNoti) {
                Log.d(TAG, "App mở từ thông báo -> Chuyển sang Tab Notification");
                // Chuyển BottomNavigation sang tab Thông báo
                bottomNavigationView.setSelectedItemId(R.id.nav_noti);

                // Nếu muốn truyền ID thông báo vào Fragment để highlight:
                // String notiID = intent.getStringExtra("NOTIFICATION_ID");
                // FragmentNotification frag = new FragmentNotification();
                // Bundle args = new Bundle(); args.putString("ID", notiID); ...
            }
        }
    }

    // --- BỔ SUNG 2: Lấy Token từ Firebase và gửi về Backend ---
    private void getAndSendFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Lấy FCM token thất bại", task.getException());
                            return;
                        }

                        // Lấy token thành công
                        String token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);

                        // Gọi API lưu token
                        updateFcmTokenToServer(token);
                    }
                });
    }

    private void updateFcmTokenToServer(String token) {
        String userID = SessionManager.getUserID(this);
        if (userID == null) {
            Log.e(TAG, "Chưa đăng nhập, không gửi token");
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        HashMap<String, String> string = new HashMap<>();
        string.put("userID", userID);
        string.put("fcmToken", token);

        apiService.saveFcmToken(string).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Cập nhật Token lên Server thành công");
                } else {
                    Log.e(TAG, "Lỗi Server khi lưu token: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng khi lưu token", t);
            }
        });
    }

    // --- BỔ SUNG 3: Tách hàm xin quyền cho gọn ---
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }

    public void reloadCartFragment() {
        FragmentCart fragment = (FragmentCart) getSupportFragmentManager()
                .findFragmentByTag("FragmentCart");
        if (fragment != null && fragment.isVisible()) {
            fragment.onCartUpdated();
        }
    }
}*/
package com.example.demo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.fragment.FragmentCart;
import com.example.demo.fragment.FragmentHome;
import com.example.demo.fragment.FragmentNotification;
import com.example.demo.fragment.FragmentProfile;
import com.example.demo.models.CommonResponse;
import com.example.demo.service.MyFirebaseMessagingService;
import com.example.demo.utils.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private static final String TAG = "MainActivity_FCM";

    // 1. KHAI BÁO BỘ THU SÓNG (RECEIVER) ĐỂ NHẬN THÔNG BÁO KHI APP ĐANG MỞ
    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String title = intent.getStringExtra("title");
                String body = intent.getStringExtra("body");
                String notiID = intent.getStringExtra("notiID");

                // Hiện Popup ngay lập tức
                showInAppNotificationDialog(title, body);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            replaceFragment(new FragmentHome(), "FragmentHome");
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String tag = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new FragmentHome();
                tag = "FragmentHome";
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new FragmentCart();
                tag = "FragmentCart";
            } else if (itemId == R.id.nav_noti) {
                selectedFragment = new FragmentNotification();
                tag = "FragmentNotification";
            } else if (itemId == R.id.menu_profile) {
                selectedFragment = new FragmentProfile();
                tag = "FragmentProfile";
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment, tag);
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (view, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            view.setPadding(0, 0, 0, bottomInset);
            return insets;
        });

        // Xin quyền & Lấy Token & Xử lý Intent
        checkNotificationPermission();
        getAndSendFCMToken();
        handleNotificationIntent(getIntent());
    }

    // 2. ĐĂNG KÝ LẮNG NGHE KHI MÀN HÌNH HIỆN LÊN (onResume)
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MyFirebaseMessagingService.EVENT_NOTIFICATION_RECEIVED);

        // Android 13 (API 33) yêu cầu cờ bảo mật khi đăng ký Receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(notificationReceiver, filter);
        }
    }

    // 3. HỦY ĐĂNG KÝ KHI MÀN HÌNH TẮT (onPause) ĐỂ TRÁNH LỖI
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(notificationReceiver);
    }

    // 4. HÀM HIỂN THỊ POPUP (DIALOG)
    private void showInAppNotificationDialog(String title, String body) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(body)
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(true)
                .setPositiveButton("Xem ngay", (dialog, which) -> {
                    // Chuyển sang Tab Thông báo
                    if (bottomNavigationView != null) {
                        bottomNavigationView.setSelectedItemId(R.id.nav_noti);
                    }
                })
                .setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("OPEN_FROM_NOTI")) {
            Log.d(TAG, "App mở từ thông báo -> Chuyển sang Tab Notification");
            bottomNavigationView.setSelectedItemId(R.id.nav_noti);
        }
    }

    private void getAndSendFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Lấy token thất bại", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);
                        updateFcmTokenToServer(token);
                    }
                });
    }

    private void updateFcmTokenToServer(String token) {
        String userID = SessionManager.getUserID(this);
        if (userID == null) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        HashMap<String, String> string = new HashMap<>();
        string.put("userID", userID);
        string.put("fcmToken", token);

        apiService.saveFcmToken(string).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if(response.isSuccessful()) Log.d(TAG, "Token đã lưu lên Server");
            }
            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {}
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }

    public void reloadCartFragment() {
        FragmentCart fragment = (FragmentCart) getSupportFragmentManager()
                .findFragmentByTag("FragmentCart");
        if (fragment != null && fragment.isVisible()) {
            fragment.onCartUpdated();
        }
    }
}