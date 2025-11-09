package com.example.demo.models;

public class CreateMomoRequest {
    String orderID;
    String userID;
    public CreateMomoRequest(String orderID, String userID){
        this.orderID = orderID;
        this.userID = userID;
    }
}
