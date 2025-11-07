package com.example.demo.models; // (Package cho file này)

import com.example.demo.models.Order;

public class OrderResponse {
    private String status;
    private String message;
    private Order data; // "data" là một ĐỐI TƯỢNG (Order)

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Order getData() { return data; }

    public String getOrderID() {

        if (data != null) {
            return data.getOrderID();
        }
        return null;
    }
}