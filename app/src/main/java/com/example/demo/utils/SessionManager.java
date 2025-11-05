package com.example.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "userID";
    private static final String KEY_USERNAME = "username";

    public static void saveUser(Context context, String userID, String username) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_USER_ID, userID)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public static String getUserID(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_ID, null);
    }

    public static String getUsername(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USERNAME, null);
    }

    public static void clearSession(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }
}
