package com.example.demo.models; // (Hoặc package của bạn)

// Import List (quan trọng)
import java.util.List;

import com.example.demo.models.Order;

public class OrderListResponse {

    private String status;
    private String message;
    private List<Order> data;//lich su mua hang

    // Getters
    public String getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
    public List<Order> getData() {
        return data;
    }
}
/*
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderListResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Order> data; // Trả về 1 danh sách Order

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<Order> getData() { return data; }
}*/
