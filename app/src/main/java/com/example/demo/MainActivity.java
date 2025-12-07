package com.example.demo;

import static androidx.media.session.MediaButtonReceiver.handleIntent;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import com.example.demo.fragment.FragmentCart;
import com.example.demo.fragment.FragmentHome;
import com.example.demo.fragment.FragmentNotification;
import com.example.demo.fragment.FragmentOrderTracking;
import com.example.demo.fragment.FragmentProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load Home mặc định
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
                selectedFragment = new FragmentOrderTracking();
                tag = "FragmentOrderTracking";
            } else if (itemId == R.id.menu_notification) {
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        handleIntent(getIntent());
    }

    private void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }

    // BỔ SUNG: Hàm reload giỏ hàng từ BottomSheet
    public void reloadCartFragment() {
        FragmentCart fragment = (FragmentCart) getSupportFragmentManager()
                .findFragmentByTag("FragmentCart");
        if (fragment != null && fragment.isVisible()) {
            fragment.onCartUpdated(); // Gọi reload
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
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

            // Xóa extra để tránh mở lại khi xoay màn hình
            intent.removeExtra("OPEN_ORDER_TRACKING");
            intent.removeExtra("ORDER_ID");
        }
    }
}