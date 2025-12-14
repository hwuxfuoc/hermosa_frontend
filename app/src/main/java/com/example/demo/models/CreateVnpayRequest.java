package com.example.demo.models;

public class CreateVnpayRequest {

    String orderID;
    String userID;

    public CreateVnpayRequest(String orderID, String userID) {
        this.orderID = orderID;
        this.userID = userID;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getUserID() {
        return userID;
    }
}