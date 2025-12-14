package com.example.demo;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        com.facebook.FacebookSdk.fullyInitialize();
        com.facebook.appevents.AppEventsLogger.activateApp(this);
    }
}