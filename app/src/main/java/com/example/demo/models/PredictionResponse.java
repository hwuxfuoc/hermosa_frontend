package com.example.demo.models;
import java.util.List;

public class PredictionResponse {
    private String status;
    private PredictionData data;

    public String getStatus() { return status; }
    public PredictionData getData() { return data; }

    public static class PredictionData {
        private String productID;
        // Sửa thành List<List<Object>> để hứng [["C03", 153], ["C02", 76]]
        private List<List<Object>> data;

        public List<List<Object>> getItems() {
            return data;
        }
    }
}
