// File: app/src/main/java/com/example/demo/model/AuthResponse.java
package com.example.demo.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("statuscode")
    private Integer statuscode;

    @SerializedName("token")
    private String token;

    // data là OBJECT, không phải List!
    @SerializedName("data")
    private User data;

    // Getter
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Integer getStatuscode() { return statuscode; }
    public String getToken() { return token; }
    public User getData() { return data; }  // ĐÚNG: User, không phải List<User>

    // Lớp User bên trong
    public static class User {
        @SerializedName("_id")
        private String id;

        @SerializedName("userID")
        private String userID;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("password")
        private String password;

        @SerializedName("is_verified")
        private boolean is_verified;

        @SerializedName("signupMethod")
        private String signupMethod;

        // Getter
        public String getId() { return id; }
        public String getUserID() {
            return userID != null ? userID : id;
        }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public boolean isVerified() { return is_verified; }
        public String getSignupMethod() { return signupMethod; }
    }
}