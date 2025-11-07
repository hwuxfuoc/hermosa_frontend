package com.example.demo.models;

/**
 * Dùng để gửi (Request) thông tin lên server
 * khi gọi API /payment/create-payment-vnpay
 */
public class CreateVnpayRequest {

    // Tên biến này phải khớp với code backend (req.body.orderID)
    String orderID;
    String userID;

    // Constructor để tạo đối tượng
    public CreateVnpayRequest(String orderID, String userID) {
        this.orderID = orderID;
        this.userID = userID;
    }

    // Getters (Retrofit/Gson có thể cần)
    public String getOrderID() {
        return orderID;
    }

    public String getUserID() {
        return userID;
    }
}