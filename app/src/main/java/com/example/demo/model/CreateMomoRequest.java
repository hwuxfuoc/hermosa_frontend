package com.example.demo.model;

public class CreateMomoRequest {
    String orderID;
    String userID;
    public CreateMomoRequest(String orderID, String userID){
        this.orderID = orderID;
        this.userID = userID;
    }
}