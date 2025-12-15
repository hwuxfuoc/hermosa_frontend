package com.example.demo.models;

import java.util.List;

public class RecommendResponse {
    private String status;
    private RecData data; // data bây giờ là Object, không phải List

    public String getStatus() {
        return status;
    }

    public RecData getData() {
        return data;
    }

    // Class con để hứng object bên trong
    public static class RecData {
        private String visitorid;
        // Dữ liệu trả về dạng: [["C09", 6.7], ["D01", 5.2]]
        // Nên ta dùng List<List<Object>> để hứng cả chuỗi và số
        private List<List<Object>> data;

        public List<List<Object>> getData() {
            return data;
        }
    }
}