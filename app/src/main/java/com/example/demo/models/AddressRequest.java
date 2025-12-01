package com.example.demo.models;

public class AddressRequest {
    private String userID;
    private String addressID;
    private String name;
    private String phone;
    private String type;
    private AddressDetail addressDetail;

    /*public AddressRequest(String userID,String addressID, String name, String phone, String type, AddressDetail addressDetail){
        this.userID = userID;
        this.addressID=addressID;
        this.name = name;
        this.phone = phone;
        this.type=type;
        this.addressDetail = addressDetail;
    }*/
    //Dung cho add api
    public AddressRequest(String userID, String name, String phone,String type, AddressDetail addressDetail) {
        this.userID = userID;
        this.name = name;
        this.phone = phone;
        this.type=type;
        this.addressDetail = addressDetail;
    }
    // dung cho delete api
    public AddressRequest(String userID, String addressID){
        this.userID = userID;
        this.addressID=addressID;
    }
}
