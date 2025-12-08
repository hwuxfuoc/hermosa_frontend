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
