package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TopSellingResponse {
    private String status;
    private String message;

    @SerializedName("data")
    private List<Product> data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Product> getData() {
        return data;
    }
}