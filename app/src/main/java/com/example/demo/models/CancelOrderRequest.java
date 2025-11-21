package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class CancelOrderRequest {
    // Tên biến này phải trùng khớp với backend: let {orderID} = req.body
    @SerializedName("orderID")
    private String orderID;

    // Constructor để tạo nhanh: new CancelOrderRequest("ORD-123")
    public CancelOrderRequest(String orderID) {
        this.orderID = orderID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
}