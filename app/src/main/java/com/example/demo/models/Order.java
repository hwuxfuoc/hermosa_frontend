package com.example.demo.models;
import java.util.List;
public class Order {
    private String orderID;
    private String userID;
    private String status;
    private int totalInvoice;
    private List <CartItem> products;
    private String paymentStatus;
    private String paymentMethod;
    private String deliver;
    private String deliverAddress;
    private String note;
    /*private long totalInvoice;*/
 // Nên dùng long cho tiền tệ

    // --- Bổ sung các trường thiếu ---
    private long finalTotal;
    private long deliveryFee;
    private long tipsforDriver;
    private long discountAmount;
    private String voucherCodeApply;
    private String storeName;
    private String storeAddress;
    private String orderDate;
    private String deliverIn;
    private String createAt; // Đổi từ long sang String
    private String doneIn;
    public String getCreateAt() { return createAt; }
    public void setCreateAt(String createAt) { this.createAt = createAt; }
    public String getDoneIn() { return doneIn; }
    public void setDoneIn(String doneIn) { this.doneIn = doneIn; }


    public String getVoucherCodeApply() {
        return voucherCodeApply;
    }

    public void setVoucherCodeApply(String voucherCodeApply) {
        this.voucherCodeApply = voucherCodeApply;
    }

    public String getOrderID(){return orderID;}
    public String getUserID(){return userID;}
    public String getStatus(){return status;}
    public String getStoreName() { return storeName; }
    public String getStoreAddress() { return storeAddress; }
    public long getFinalTotal() { return finalTotal; }
    public long getDeliveryFee() { return deliveryFee; }
    public long getTipsforDriver() { return tipsforDriver; }
    public long getDiscountAmount() { return discountAmount; }
    public int getTotalInvoice(){return totalInvoice;}
    public List<CartItem> getProducts(){return products;}
    public String getPaymentMethod(){return paymentMethod;}
    public String getPaymentStatus(){return paymentStatus;}
    public String getDeliver(){return deliver;}
    public String getDeliverAddress(){return deliverAddress;}
    public String getNote(){return note;}
    public String getOrderDate() { return orderDate; }
    public String getDeliverIn() { return deliverIn; }
}
/*
package com.example.demo.models;

import com.google.gson.annotations.SerializedName; // Cần thư viện GSON
import java.io.Serializable;
import java.util.List;

// 1. Thêm implements Serializable để truyền qua Intent
public class Order implements Serializable {

    @SerializedName("_id") // Map với _id của MongoDB nếu cần
    private String id;

    @SerializedName("orderID")
    private String orderID;

    @SerializedName("userID")
    private String userID;

    @SerializedName("status")
    private String status;

    // 2. Đổi sang long cho đồng bộ tiền tệ
    @SerializedName("totalInvoice")
    private long totalInvoice;

    @SerializedName("finalTotal")
    private long finalTotal;

    @SerializedName("deliveryFee")
    private long deliveryFee;

    @SerializedName("tipsforDriver")
    private long tipsforDriver;

    @SerializedName("discountAmount")
    private long discountAmount;

    @SerializedName("voucherCodeApply")
    private String voucherCodeApply;

    // Lưu ý: Class CartItem cũng phải implements Serializable
    @SerializedName("products")
    private List<CartResponse.CartItem> products;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("deliver")
    private String deliver; // Có thể là boolean ở BE, nhưng String ở đây cũng được nếu BE trả về chuỗi

    @SerializedName("deliverAddress")
    private String deliverAddress;

    @SerializedName("note")
    private String note;

    // Backend trả về 'createAt' (số timestamp), map vào đây
    @SerializedName("createAt")
    private long createAt;

    // --- Getter Methods ---
    public String getOrderID() { return orderID; }
    public String getUserID() { return userID; }
    public String getStatus() { return status; }

    public long getTotalInvoice() { return totalInvoice; }
    public long getFinalTotal() { return finalTotal; }
    public long getDeliveryFee() { return deliveryFee; }
    public long getTipsforDriver() { return tipsforDriver; }
    public long getDiscountAmount() { return discountAmount; }

    public String getVoucherCodeApply() { return voucherCodeApply; }
    public List<CartResponse.CartItem> getProducts() { return products; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getDeliverAddress() { return deliverAddress; }
    public String getNote() { return note; }
    public long getCreateAt() { return createAt; }

    // --- Setter Methods (Nếu cần thiết) ---
    public void setVoucherCodeApply(String voucherCodeApply) {
        this.voucherCodeApply = voucherCodeApply;
    }
}*/
