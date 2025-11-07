package com.example.demo;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.demo.fragment.FragmentCart;
import com.example.demo.fragment.FragmentHome;
import com.example.demo.fragment.FragmentNotification;
import com.example.demo.fragment.FragmentProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView; // Bottom Navigation Bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Mặc định load HomeFragment khi app mở
        replaceFragment(new FragmentHome());

        // Lắng nghe sự kiện khi chọn item trong bottom nav
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                selectedFragment = new FragmentHome();
            } else if (itemId == R.id.menu_cart) {
                selectedFragment = new FragmentCart();
            } else if (itemId == R.id.menu_notification) {
                selectedFragment = new FragmentNotification();
            } else if (itemId == R.id.menu_profile) {
                selectedFragment = new FragmentProfile();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }

            return false;
        });

        // Xử lý EdgeToEdge / bottom margin
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (view, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            view.setPadding(0, 0, 0, bottomInset); // tự động chừa khoảng nếu có thanh điều hướng
            return insets;
        });
    }

    // Hàm thay fragment
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
