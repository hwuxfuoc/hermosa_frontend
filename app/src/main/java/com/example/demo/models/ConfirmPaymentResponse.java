package com.example.demo.models;

import com.example.demo.models.Order;

public class ConfirmPaymentResponse {
    private String message;
    private Order data;

    public Order getData() {
        return data;
    }
    public String getMessage() {
        return message;
    }
}