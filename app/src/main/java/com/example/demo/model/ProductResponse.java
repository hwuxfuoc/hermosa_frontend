package com.example.demo.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Product> data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<Product> getData() { return data; }
}