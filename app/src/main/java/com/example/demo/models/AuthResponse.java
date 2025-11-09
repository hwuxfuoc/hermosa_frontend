package com.example.demo.models;

public class AuthResponse {
    private String status;
    private String message;
    private int statuscode;     // có thể có trong 1 số API
    private String token;       // nếu backend có JWT token
    private User data;          // đối tượng user chính

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public int getStatuscode() { return statuscode; }
    public String getToken() { return token; }
    public User getData() { return data; }

    // lớp con User ánh xạ với "foundUser" hoặc "newUser" trong backend
    public static class User {
        private String _id;
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
        public String getUserID() { return userID; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public boolean isVerified() { return is_verified; }
        public String getSignupMethod() { return signupMethod; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
    }
}
