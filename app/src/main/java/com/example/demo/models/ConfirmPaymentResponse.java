/*
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class ConfirmPaymentResponse {
    @SerializedName("orderID")
    private String orderID;

    @SerializedName("status")
    private String status; // "done", "not_done", "pending", "failed"

    @SerializedName("method")
    private String method;

    @SerializedName("time")
    private String time;

    // Getter
    public String getOrderID() { return orderID; }
    public String getStatus() { return status; }
    public String getMethod() { return method; }
    public String getTime() { return time; }
}*/
package com.example.demo.models;

import com.example.demo.models.Order; // Import class Order hiện có của bạn

public class ConfirmPaymentResponse {
    private String message;
    private Order data; // Backend trả về object Order trong field "data"

    public Order getData() {
        return data;
    }
    public String getMessage() {
        return message;
    }
}