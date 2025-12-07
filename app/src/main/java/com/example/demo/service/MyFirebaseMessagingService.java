package com.example.demo.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.demo.MainActivity;
import com.example.demo.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_SERVICE";
    public static final String EVENT_NOTIFICATION_RECEIVED = "com.example.demo.NEW_NOTIFICATION";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String title = "Thông báo mới";
        String body = "Bạn có tin nhắn mới";
        String notiID = null;

        // 1. Ưu tiên lấy dữ liệu từ 'data' payload (Backend gửi cái này)
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();

            if (data.containsKey("notificationID")) notiID = data.get("notificationID");
            if (data.containsKey("title")) title = data.get("title");
            if (data.containsKey("body")) body = data.get("body");
        }

        // 2. Nếu có 'notification' payload thì lấy đè lên (Firebase Console gửi cái này)
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        // 3. Tiến hành hiển thị
        showNotification(title, body, notiID);
    }

    private void showNotification(String title, String body, String notiID) {
        // --- PHẦN 1: Hiện thông báo trên thanh trạng thái (System Tray) ---
        String channelId = "my_channel_id";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Thông báo ứng dụng",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (notiID != null) {
            intent.putExtra("OPEN_FROM_NOTI", true);
            intent.putExtra("NOTIFICATION_ID", notiID);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher) // Thay bằng R.drawable.ic_noti nếu có
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        manager.notify((int) System.currentTimeMillis(), builder.build());

        // --- PHẦN 2: Gửi tín hiệu sang MainActivity để hiện Popup (In-App) ---
        Intent broadcastIntent = new Intent(EVENT_NOTIFICATION_RECEIVED);
        broadcastIntent.putExtra("title", title);
        broadcastIntent.putExtra("body", body);
        broadcastIntent.putExtra("notiID", notiID);
        broadcastIntent.setPackage(getPackageName()); // Bảo mật: Chỉ gửi trong app mình
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Token mới: " + token);
        // Lưu token vào SharedPreferences để MainActivity gửi lên server sau
        getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .edit()
                .putString("FCM_TOKEN", token)
                .apply();
    }
}