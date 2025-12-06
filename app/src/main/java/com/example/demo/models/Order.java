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