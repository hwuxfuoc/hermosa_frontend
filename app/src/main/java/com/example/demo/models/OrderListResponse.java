package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderListResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Order> data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<Order> getData() { return data; }
}