package com.example.demo.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.adapters.NotificationAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.Notification;
import com.example.demo.models.NotificationListResponse;
import com.example.demo.models.NotificationListResponse;
import com.example.demo.service.MyFirebaseMessagingService;
import com.example.demo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentNotification extends Fragment {

    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private NotificationAdapter adapter;
    private ApiService apiService;

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadNotifications();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(new ArrayList<>());
        rvNotifications.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);

        loadNotifications();
    }

    private void loadNotifications() {
        String userID = SessionManager.getUserID(getContext());
        if (userID == null) return;

        apiService.getNotifications(userID).enqueue(new Callback<NotificationListResponse>() {
            @Override
            public void onResponse(Call<NotificationListResponse> call, Response<NotificationListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> list = response.body().getData();

                    if (list != null && !list.isEmpty()) {
                        adapter.updateList(list);
                        rvNotifications.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    } else {
                        rvNotifications.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<NotificationListResponse> call, Throwable t) {
                // Xử lý lỗi nếu cần (VD: Toast báo lỗi mạng)
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            IntentFilter filter = new IntentFilter(MyFirebaseMessagingService.EVENT_NOTIFICATION_RECEIVED);

            // SỬA LỖI Ở ĐÂY: Thêm cờ RECEIVER_NOT_EXPORTED cho Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getContext().registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                ContextCompat.registerReceiver(getContext(), updateReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
            }
        }

        loadNotifications();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            try {
                getContext().unregisterReceiver(updateReceiver);
            } catch (IllegalArgumentException e) {
                // Bỏ qua lỗi nếu Receiver chưa được đăng ký
            }
        }
    }
}