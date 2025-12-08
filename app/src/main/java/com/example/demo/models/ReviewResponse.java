package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Review> reviews;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<Review> getReviews() { return reviews != null ? reviews : new java.util.ArrayList<>(); }
}