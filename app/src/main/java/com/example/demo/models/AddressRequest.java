package com.example.demo.models;

public class AddressRequest {
    private String userID;
    private String addressID;
    private String name;
    private String phone;
    private String type;

    private String address;
    public AddressRequest(String userID, String name, String phone, String type, String address) {
        this.userID = userID;
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.address = address;
    }

    public AddressRequest(String userID, String addressID){
        this.userID = userID;
        this.addressID = addressID;
    }
}