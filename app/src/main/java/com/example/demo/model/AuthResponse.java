package com.example.demo.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AuthResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    private int statuscode;     // có thể có trong 1 số API
    private String token;       // nếu backend có JWT token
    @SerializedName("data")
    private List<User> data;        // đối tượng user chính

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public int getStatuscode() { return statuscode; }
    public String getToken() { return token; }
    public List<User> getData() { return data; }

    // lớp con User ánh xạ với "foundUser" hoặc "newUser" trong backend
    public static class User {
        @SerializedName("_id")
        private String _id;
        private String userID;
        @SerializedName("name")
        private String name;
        @SerializedName("email")
        private String email;
        private String password;
        private boolean is_verified;
        private String signupMethod;

        // getter
        public String getId() { return _id; }
        public String getUserID() { return userID; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public boolean isVerified() { return is_verified; }
        public String getSignupMethod() { return signupMethod; }
    }
}
