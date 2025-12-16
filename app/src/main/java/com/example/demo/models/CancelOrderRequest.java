package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class CancelOrderRequest {

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