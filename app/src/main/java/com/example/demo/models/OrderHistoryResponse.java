/*
package com.example.demo.models;

import java.util.List;

public class OrderHistoryResponse {
    private String status;
    private String message;
    private List<Order> data; // Backend trả về mảng danh sách đơn hàng

    public String getStatus() { return status; }
    public List<Order> getData() { return data; }
}*/
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderHistoryResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<OrderItem> data;

    // Getter & Setter
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<OrderItem> getData() { return data; }

    public static class OrderItem {
        @SerializedName("orderID")
        private String orderID;

        @SerializedName("createAt")
        private long createAt; // timestamp

        @SerializedName("status")
        private String status;

        @SerializedName("products")
        private List<ProductItem> products;

        @SerializedName("finalTotal")
        private double finalTotal;

        @SerializedName("paymentMethod") // nếu có
        private String paymentMethod;

        // Getter
        public String getOrderID() { return orderID; }
        public long getCreateAt() { return createAt; }
        public String getStatus() { return status; }
        public List<ProductItem> getProducts() { return products; }
        public double getFinalTotal() { return finalTotal; }
        public String getPaymentMethod() { return paymentMethod != null ? paymentMethod : "Tiền mặt"; }

        public static class ProductItem {
            @SerializedName("productID")
            private String productID;

            @SerializedName("name")
            private String name;

            @SerializedName("image")
            private String image; // URL hoặc tên ảnh

            @SerializedName("quantity")
            private int quantity;

            // Getter
            public String getProductID() { return productID; }
            public String getName() { return name; }
            public String getImage() { return image; }
            public int getQuantity() { return quantity; }
        }
    }
}