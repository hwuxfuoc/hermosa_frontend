package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderHistoryResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<HistoryItem> data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<HistoryItem> getData() { return data; }

    public static class HistoryItem {
        @SerializedName("orderInfo")
        private OrderInfo orderInfo;

        @SerializedName("products")
        private List<ProductQuantity> products;

        @SerializedName("pictures")
        private List<ProductDetail> pictures;

        public OrderInfo getOrderInfo() { return orderInfo; }
        public List<ProductQuantity> getProducts() { return products; }
        public List<ProductDetail> getPictures() { return pictures; }
    }

    public static class OrderInfo {
        @SerializedName("_id")
        private String orderID;

        @SerializedName("finalTotal")
        private int totalPrice;

        @SerializedName("status")
        private String status;

        @SerializedName("date")
        private String date;

        public String getOrderID() { return orderID; }
        public int getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
    }

    public static class ProductQuantity {
        @SerializedName("productID")
        private String productID;

        @SerializedName("quantity")
        private int quantity;

        public String getProductID() { return productID; }
        public int getQuantity() { return quantity; }
    }

    public static class ProductDetail {
        @SerializedName("productID")
        private String productID;

        @SerializedName("name")
        private String name;

        @SerializedName("image")
        private String image;

        public String getProductID() { return productID; }
        public String getName() { return name; }
        public String getImage() { return image; }
    }
}