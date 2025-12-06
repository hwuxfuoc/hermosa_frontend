package com.example.demo;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // BẮT BUỘC PHẢI GỌI DÒNG NÀY TRƯỚC KHI DÙNG FACEBOOK SDK
        com.facebook.FacebookSdk.fullyInitialize();
        com.facebook.appevents.AppEventsLogger.activateApp(this);
    }
}