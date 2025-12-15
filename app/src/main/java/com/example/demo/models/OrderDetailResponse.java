package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderDetailResponse {

    @SerializedName("status") private String status;
    @SerializedName("message") private String message;
    @SerializedName("data") private OrderInfo data;

    public String getStatus() { return status; }
    public OrderInfo getData() { return data; }

    public static class OrderInfo {
        @SerializedName("orderID") private String orderID;
        @SerializedName("finalTotal") private int finalTotal; // Tổng thanh toán
        @SerializedName("status") private String status;
        @SerializedName("createAt") private String date;

        // Map đúng tên trường trong JSON bạn gửi:
        @SerializedName("deliveryFee") private int deliveryFee;
        @SerializedName("deliverAddress") private String deliverAddress;

        @SerializedName("products") private List<ProductItem> products;

        // Getters
        public String getOrderID() { return orderID; }
        public int getFinalTotal() { return finalTotal; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
        public int getDeliveryFee() { return deliveryFee; }
        public String getDeliverAddress() { return deliverAddress; }
        public List<ProductItem> getProducts() { return products; }
    }

    public static class ProductItem {
        @SerializedName("productID") private String productID;
        @SerializedName("quantity") private int quantity;

        // JSON có trường "name", map vào đây để hiển thị tên
        @SerializedName("name") private String name;

        // JSON dùng "subtotal" (tổng tiền món), ta map tạm vào biến price để hiện thị
        @SerializedName("subtotal") private int price;

        // JSON KHÔNG CÓ "picture", nên biến này sẽ luôn null (chờ Backend sửa)
        @SerializedName("picture") private String picture;

        public String getProductID() { return productID; }
        public int getQuantity() { return quantity; }
        public String getName() { return name; }
        public int getPrice() { return price; } // Thực tế là subtotal
        public String getPicture() { return picture; }
        public void setPicture(String picture) {
            this.picture = picture;
        }
    }
}