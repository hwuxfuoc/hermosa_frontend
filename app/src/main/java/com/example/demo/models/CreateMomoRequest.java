package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class CreateMomoRequest {

    @SerializedName("orderID")
    private String orderID;

    @SerializedName("userID")
    private String userID;

    public CreateMomoRequest(String orderID, String userID) {
        this.orderID = orderID;
        this.userID = userID;
    }

    // Getter (bắt buộc cho Gson)
    public String getOrderID() { return orderID; }
    public String getUserID() { return userID; }
}