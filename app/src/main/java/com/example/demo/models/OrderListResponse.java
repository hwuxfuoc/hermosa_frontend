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

