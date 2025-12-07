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