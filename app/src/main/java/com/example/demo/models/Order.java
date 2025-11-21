/*
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

    public String getOrderID(){return orderID;}
    public String getUserID(){return userID;}
    public String getStatus(){return status;}
    public int getTotalInvoice(){return totalInvoice;}
    public List<CartItem> getProducts(){return products;}
    public String getPaymentMethod(){return paymentMethod;}
    public String getPaymentStatus(){return paymentStatus;}
    public String getDeliver(){return deliver;}
    public String getDeliverAddress(){return deliverAddress;}
    public String getNote(){return note;}
}
*/
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order {
    @SerializedName("orderID")
    private String orderID;

    @SerializedName("userID")
    private String userID;

    @SerializedName("status")
    private String status; // "pending", "done"...

    @SerializedName("totalInvoice")
    private Double totalInvoice; // Nên dùng Double thay vì int để an toàn tiền tệ

    @SerializedName("products")
    private List<CartItem> products; // Đảm bảo bạn đã có class CartItem

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    // --- QUAN TRỌNG: ĐỔI SANG BOOLEAN ---
    @SerializedName("deliver")
    private boolean deliver; // true = Ship, false = Tại quán

    @SerializedName("deliverAddress")
    private String deliverAddress;

    @SerializedName("note")
    private String note;

    // --- GETTERS ---
    public String getOrderID() { return orderID; }
    public String getUserID() { return userID; }
    public String getStatus() { return status; }
    public Double getTotalInvoice() { return totalInvoice; }
    public List<CartItem> getProducts() { return products; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }

    // Getter cho boolean thường đặt là is...
    public boolean isDeliver() { return deliver; }

    public String getDeliverAddress() { return deliverAddress; }
    public String getNote() { return note; }
}
/*
package com.example.demo.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order {
    @SerializedName("orderID") private String orderID;
    @SerializedName("status") private String status; // "pending", "preparing", ...
    @SerializedName("totalInvoice") private Double totalInvoice;
    @SerializedName("deliver") private boolean deliver; // true=Ship, false=Pickup
    @SerializedName("deliverAddress") private String deliverAddress;
    @SerializedName("paymentMethod") private String paymentMethod;
    @SerializedName("products") private List<CartItem> products;

    public String getStatus() { return status; }
    public Double getTotalInvoice() { return totalInvoice; }
    public boolean isDeliver() { return deliver; } // Getter quan trọng
    public String getDeliverAddress() { return deliverAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public List<CartItem> getProducts() { return products; }
}*/
