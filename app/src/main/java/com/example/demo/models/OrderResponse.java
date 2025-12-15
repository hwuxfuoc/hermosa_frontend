package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class OrderResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Order data; // Bây giờ nó sẽ nhận diện được class Order

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Order getData() { return data; }

    // Helper method
    public String getOrderID() {
        if (data != null) {
            return data.getOrderID();
        }
        return null;
    }
}