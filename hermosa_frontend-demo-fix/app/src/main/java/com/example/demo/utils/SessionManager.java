package com.example.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";

    /**
     * Hàm này được gọi ở ActivityLogin, sau khi đăng nhập thành công
     */
    public static void saveUserSession(Context context, String userID, String username, String phone, String address) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_USER_ID, userID)
                .putString(KEY_USERNAME, username)
                .putString(KEY_PHONE, phone)
                .putString(KEY_ADDRESS, address)
                .apply();
    }

    // --- Các hàm Get ---

    public static String getUserID(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_ID, null);
    }

    /**
     * Lấy tên User (sửa tên hàm từ getUsername thành getUserName cho khớp)
     */
    public static String getUserName(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USERNAME, null);
    }

    /**
     * Bổ sung hàm lấy SĐT
     */
    public static String getUserPhone(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_PHONE, null);
    }

    /**
     * Bổ sung hàm lấy Địa chỉ
     */
    public static String getUserAddress(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ADDRESS, null);
    }

    /**
     * Xóa toàn bộ session khi logout
     */
    public static void clearSession(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }
}