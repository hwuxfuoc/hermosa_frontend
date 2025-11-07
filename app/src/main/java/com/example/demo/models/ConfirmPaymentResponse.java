package com.example.demo.models;

public class ConfirmPaymentResponse {
    String orderID;
    String status; // Sẽ là "done" hoặc "not_done"
    String method;

    // Getter
    public String getStatus() {
        return status;
    }
}
