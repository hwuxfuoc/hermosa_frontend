package com.example.demo.models;

import java.util.List;

public class AddUpdateResponse {
    private String message;
    private UserData data;

    public String getMessage() { return message; }
    public UserData getData() { return data; }

    public static class UserData {
        public String userID;
        public List<AddressResponse.AddressData> deliverInformation;
    }
}