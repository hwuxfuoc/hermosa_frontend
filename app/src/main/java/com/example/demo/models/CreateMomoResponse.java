package com.example.demo.models;

public class CreateMomoResponse {
    private String message;
    private String payUrl; // Link để mở trình duyệt
    // private Object rawResponse; // Có thể bỏ qua nếu không dùng

    public String getPayUrl() {
        return payUrl;
    }
    public String getMessage() {
        return message;
    }
}