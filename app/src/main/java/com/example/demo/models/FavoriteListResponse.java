package com.example.demo.models;

import java.util.List;

public class FavoriteListResponse {
    private String status;
    private String message;
    private int count;
    private List<MenuResponse.MenuItem> data;  // Đây là danh sách sản phẩm yêu thích

    // Getter và Setter (bắt buộc cho Retrofit/Gson)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<MenuResponse.MenuItem> getData() {
        return data;
    }

    public void setData(List<MenuResponse.MenuItem> data) {
        this.data = data;
    }
}