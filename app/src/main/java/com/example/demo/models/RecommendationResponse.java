package com.example.demo.models;

public class RecommendationResponse {
    private String status;
    private String message;
    private RecommendationWrapper data;  // ← Bọc thêm 1 lớp

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public RecommendationWrapper getData() {
        return data;
    }
}