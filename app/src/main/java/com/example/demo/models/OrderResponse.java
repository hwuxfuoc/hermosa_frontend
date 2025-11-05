package com.example.demo.models;

import java.util.List;

public class OrderResponse {
    private String status;
    private String message;
    private List<Order> data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<Order> getData() { return data; }
}
