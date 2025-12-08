package com.example.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.EditText;

public class SessionManager {
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";

    public static void saveUserSession(Context context, String userID, String username, String phone, String address) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_USER_ID, userID != null ? userID : "unknown");

        // ĐẢM BẢO TÊN KHÔNG BAO GIỜ NULL HOẶC RỖNG
        String safeName = "Khách";
        if (username != null && !username.trim().isEmpty()) {
            safeName = username.trim();
        }
        editor.putString(KEY_USERNAME, safeName);

        editor.putString(KEY_PHONE, phone != null ? phone : "");
        editor.putString(KEY_ADDRESS, address != null ? address : "");
        editor.putBoolean("IS_LOGGED_IN", true); // thêm cái này cho chắc
        editor.apply();
    }

    // --- Các hàm Get ---

    public static String getUserID(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_ID, null);
    }


    public static String getUserName(Context context) {
        String name = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USERNAME, null);

        // NẾU NULL → TRẢ VỀ "Khách" ĐỂ KHÔNG BAO GIỜ BỊ TRỐNG
        return name != null && !name.trim().isEmpty() ? name.trim() : "Khách";
    }


    public static String getUserPhone(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_PHONE, null);
    }

    public static String getUserAddress(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ADDRESS, null);
    }

    public static void clearSession(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    public static void saveRememberMe(Context context, String email, String password, boolean remember) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (remember) {
            editor.putString("REMEMBER_EMAIL", email);
            editor.putString("REMEMBER_PASSWORD", password);
            editor.putBoolean("REMEMBER_ME", true);
        } else {
            editor.remove("REMEMBER_EMAIL");
            editor.remove("REMEMBER_PASSWORD");
            editor.putBoolean("REMEMBER_ME", false);
        }
        editor.apply();
    }

    public static void loadRememberMe(Context context, EditText editEmail, EditText editPassword, CheckBox checkBox) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean remember = prefs.getBoolean("REMEMBER_ME", false);
        if (remember) {
            String email = prefs.getString("REMEMBER_EMAIL", "");
            String password = prefs.getString("REMEMBER_PASSWORD", "");
            editEmail.setText(email);
            editPassword.setText(password);
            checkBox.setChecked(true);
        }
    }

    public static void clearRememberMe(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().remove("REMEMBER_ME").remove("REMEMBER_EMAIL").remove("REMEMBER_PASSWORD").apply();
    }
}