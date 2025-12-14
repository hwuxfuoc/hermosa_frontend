package com.example.demo.models;

import java.util.List;

public class AddressResponse {
    private String message;
    private List<AddressData> data;

    public List<AddressData> getData() { return data; }
    public String getMessage() { return message; }

    public static class AddressData {
        public String addressID;
        public String name;
        public String phone;
        public String type;
        public boolean isSelected = false;

        public String addressDetail;

        public String getFullAddress() {
            return addressDetail != null ? addressDetail : "";
        }
    }
}