package com.example.demo.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificationListResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Notification>data;

    public List<Notification> getData() {
        return data;
    }
}
