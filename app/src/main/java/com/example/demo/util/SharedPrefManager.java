package com.example.demo.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private static final String PREF_NAME = "hermosa_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // Lưu user sau khi đăng nhập
    public void saveUser(String userId, String userName) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    // Lấy user ID (dùng cho API cart)
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    // Lấy tên người dùng (hiển thị ở Home)
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "Guest");
    }

    // Đăng xuất
    public void clear() {
        editor.clear();
        editor.apply();
    }

    // Kiểm tra đã login chưa
    public boolean isLoggedIn() {
        return getUserId() != null;
    }
}