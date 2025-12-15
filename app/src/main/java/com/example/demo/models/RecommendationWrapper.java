package com.example.demo.models;

import java.util.List;

public class RecommendationWrapper {
    private String visitorid;
    private int total;
    private List<Product> data;  // ← Đây mới là danh sách sản phẩm thực tế

    public String getVisitorid() {
        return visitorid;
    }

    public int getTotal() {
        return total;
    }

    public List<Product> getData() {
        return data;
    }
}