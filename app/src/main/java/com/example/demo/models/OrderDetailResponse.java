/*
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderDetailResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private OrderInfo data; // Chú ý: Đây là Object, KHÔNG phải List

    public String getStatus() { return status; }
    public OrderInfo getData() { return data; }

    // Dùng lại class OrderInfo giống bên History, hoặc tạo mới nếu cần thêm trường
    */
/*public static class OrderInfo {
        @SerializedName("orderID")
        private String orderID;

        @SerializedName("finalTotal")
        private int totalPrice;

        @SerializedName("status")
        private String status;

        @SerializedName("createAt")
        private String date;

        @SerializedName("products")
        private List<ProductItem> products;

        // Getters...
        public String getOrderID() { return orderID; }
        public int getTotalPrice() { return totalPrice; }
        public List<ProductItem> getProducts() { return products; }
    }*//*


    public static class ProductItem {
        @SerializedName("productID")
        private String productID;

        @SerializedName("quantity")
        private int quantity;

        // --- THÊM CÁC TRƯỜNG NÀY ---
        @SerializedName("name")
        private String name;       // Tên món

        @SerializedName("picture")
        private String picture;    // Link ảnh

        @SerializedName("price")
        private int price;         // Giá tiền (nếu có)

        // Getter
        public String getProductID() { return productID; }
        public int getQuantity() { return quantity; }
        public String getName() { return name; }
        public String getPicture() { return picture; }
        public int getPrice() { return price; }
    }
    public static class OrderInfo {
        @SerializedName("orderID")
        private String orderID;
        @SerializedName("finalTotal")
        private int totalPrice;
        @SerializedName("status")
        private String status;
        @SerializedName("createAt")
        private String date;
        @SerializedName("products")
        private List<ProductItem> products;
        @SerializedName("receiverName") private String receiverName;
        @SerializedName("phoneNumber") private String phoneNumber;
        @SerializedName("deliverAddress") private String address;
        public String getAddress() { return address; }
        @SerializedName("deliveryFee")
        private int deliveryFee; // API trả về số, không phải String

        // --- BỔ SUNG GETTER ---
        public int getDeliveryFee() { return deliveryFee; }

        // --- BỔ SUNG GETTER NÀY ---
        public String getOrderID() { return orderID; }
        public int getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; } // <--- Thêm cái này
        public String getDate() { return date; }     // <--- Thêm cái này
        public List<ProductItem> getProducts() { return products; }
    }
}*/
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
