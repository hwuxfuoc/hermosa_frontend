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
import com.example.demo.fragment.FragmentOrderHistory;
import com.example.demo.fragment.FragmentOrderTracking;
import com.example.demo.models.CommonResponse;
import com.example.demo.service.MyFirebaseMessagingService;
import com.example.demo.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
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

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String title = intent.getStringExtra("title");
                String body = intent.getStringExtra("body");
                String notiID = intent.getStringExtra("notiID");

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

        replaceFragment(new FragmentHome(), "FragmentHome");

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String tag = null;

            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                selectedFragment = new FragmentHome();
                tag = "FragmentHome";
            } else if (itemId == R.id.menu_cart) {
                selectedFragment = new FragmentCart();
                tag = "FragmentCart";
            } else if (itemId == R.id.menu_order) {
                selectedFragment = new FragmentOrderHistory();
                tag = "FragmentOrderHistory";
            }  else if (itemId == R.id.menu_profile) {
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        handleIntent(getIntent());

        checkNotificationPermission();
        getAndSendFCMToken();
        handleNotificationIntent(getIntent());
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MyFirebaseMessagingService.EVENT_NOTIFICATION_RECEIVED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(notificationReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(notificationReceiver);
    }

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
        handleIntent(intent);
        handleNotificationIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("OPEN_ORDER_TRACKING")) {
            String orderID = intent.getStringExtra("ORDER_ID");

            FragmentOrderTracking fragment = new FragmentOrderTracking();
            Bundle bundle = new Bundle();
            bundle.putString("ORDER_ID", orderID);
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

            intent.removeExtra("OPEN_ORDER_TRACKING");
            intent.removeExtra("ORDER_ID");
        }
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
            fragment.onCartUpdated(); // Gọi reload
        }
    }
}