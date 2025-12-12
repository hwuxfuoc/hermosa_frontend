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
}