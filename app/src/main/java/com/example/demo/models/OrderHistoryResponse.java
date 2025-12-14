/*
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderHistoryResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<HistoryItem> data; // Danh sách các item lịch sử

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<HistoryItem> getData() { return data; }

    // --- Class đại diện cho 1 phần tử trong mảng "data" ---
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

    // --- Thông tin đơn hàng (orderInfo) ---
    public static class OrderInfo {
        @SerializedName("_id")
        private String orderID;

        @SerializedName("finalTotal") // Hoặc "totalPrice" tùy DB của bạn
        private int totalPrice;

        @SerializedName("status")
        private String status;

        @SerializedName("date") // Hoặc "createdAt" tùy DB
        private String date;

        public String getOrderID() { return orderID; }
        public int getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
    }

    // --- Số lượng sản phẩm (trong mảng products) ---
    public static class ProductQuantity {
        @SerializedName("productID")
        private String productID;

        @SerializedName("quantity")
        private int quantity;

        public String getProductID() { return productID; }
        public int getQuantity() { return quantity; }
    }

    // --- Chi tiết hình ảnh/tên (trong mảng pictures) ---
    public static class ProductDetail {
        @SerializedName("productID")
        private String productID;

        @SerializedName("name") // Tên món ăn trong bảng Menu
        private String name;

        @SerializedName("image") // Link ảnh
        private String image;

        public String getProductID() { return productID; }
        public String getName() { return name; }
        public String getImage() { return image; }
    }
}*//*

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


    // ... (Giữ nguyên HistoryItem, OrderInfo, ProductQuantity) ...

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

    // ... (Giữ nguyên OrderInfo và ProductQuantity) ...
    public static class OrderInfo {
        @SerializedName("_id")
        private String orderID;

        @SerializedName("finalTotal")
        private int totalPrice;

        @SerializedName("status")
        private String status;

        @SerializedName("createAt") // Log của bạn trả về là "createAt" chứ ko phải "date"
        private String date;
        @SerializedName("paymentMethod")
        private String paymentMethod;

        public String getOrderID() { return orderID; }
        public int getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
        public String getPaymentMethod() { return paymentMethod; }
    }

    public static class ProductQuantity {
        @SerializedName("productID")
        private String productID;

        @SerializedName("quantity")
        private int quantity;

        public String getProductID() { return productID; }
        public int getQuantity() { return quantity; }
    }

    // === PHẦN QUAN TRỌNG CẦN SỬA ===
    public static class ProductDetail {
        @SerializedName("productID")
        private String productID;

        @SerializedName("name")
        private String name;

        // SỬA DÒNG NÀY: JSON trả về "picture", không phải "image"
        @SerializedName("picture")
        private String image;

        public String getProductID() { return productID; }
        public String getName() { return name; }
        public String getImage() { return image; }
    }
}*/
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
