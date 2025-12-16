package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
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

    // --- SỬA LẠI CLASS NÀY ---
    public static class OrderInfo {
        // 1. Map trường _id riêng (nếu cần dùng sau này)
        @SerializedName("_id")
        private String mongoID;

        // 2. Map trường orderID đúng với JSON (ORD-...)
        @SerializedName("orderID")
        private String orderID;

        @SerializedName("finalTotal")
        private int totalPrice;

        @SerializedName("status")
        private String status;

        @SerializedName("createAt")
        private String date;

        @SerializedName("paymentMethod")
        private String paymentMethod;

        // Getter trả về orderID (ORD-...)
        public String getOrderID() { return orderID; }

        // Getter cho các trường khác
        public int getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getMongoID() { return mongoID; }
    }

    public static class ProductQuantity {
        @SerializedName("productID")
        private String productID;
        @SerializedName("quantity")
        private int quantity;

        public String getProductID() { return productID; }
        public int getQuantity() { return quantity; }
    }

    /*public static class ProductDetail {
        @SerializedName("productID")
        private String productID;
        @SerializedName("name")
        private String name;
        @SerializedName("picture")
        private String image;

        public String getProductID() { return productID; }
        public String getName() { return name; }
        public String getImage() { return image; }
    }*/
    public static class ProductDetail implements Serializable {
        @SerializedName("productID")
        private String productID;

        @SerializedName("name")
        private String name;

        @SerializedName("picture")
        private String image;

        public String getProductID() { return productID; }
        public String getName() { return name; }
        public String getImage() { return image; }
    }
}
