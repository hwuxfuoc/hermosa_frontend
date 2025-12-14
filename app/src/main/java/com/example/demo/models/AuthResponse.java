package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    private String status;
    private String message;
    private int statuscode;
    private String token;
    @SerializedName("data")
    private User data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public int getStatuscode() { return statuscode; }
    public String getToken() { return token; }
    public User getData() { return data; }

    public static class User {
        @SerializedName("_id")
        private String _id;
        @SerializedName("userID")
        private String userID;
        private String name;
        private String email;
        private String password;
        private boolean is_verified;
        private String signupMethod;
        private String phone;
        private String address;
        // getter
        public String getId() { return _id; }
        public String getUserID() { return userID != null ? userID : _id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public boolean isVerified() { return is_verified; }
        public String getSignupMethod() { return signupMethod; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
    }
}
