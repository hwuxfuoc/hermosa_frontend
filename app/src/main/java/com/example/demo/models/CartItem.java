package com.example.demo.models;

import android.content.Context;
import com.example.demo.R;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CartItem /*implements Serializable*/ {

    @SerializedName("_id")
    private String id;

    @SerializedName("productID")
    private String productID;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private double price;

    @SerializedName("quantity")
    private int quantity = 1;

    @SerializedName("subtotal")
    private int subtotal;

    @SerializedName("size")
    private String size;

    @SerializedName("topping")
    private String[] topping;

    @SerializedName("note")
    private String note;

    @SerializedName("imageUrl")
    private String imageUrl;

    private int color = 0xFFFFFFFF;

    private boolean isSelected = true;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }

    public String getName() { return name != null ? name : "Unknown"; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String[] getTopping() { return topping; }
    public void setTopping(String[] topping) { this.topping = topping; }

    public String getNote() { return note != null ? note : ""; }
    public void setNote(String note) { this.note = note; }

    public String getImageUrl() {
        return imageUrl != null && !imageUrl.trim().isEmpty() ? imageUrl : "";
    }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }

    public void updateSubtotal() {
        this.subtotal = (int) (this.price * this.quantity);
    }

    public static CartItem fromProduct(Product product) {
        CartItem item = new CartItem();
        item.setProductID(product.getProductID());
        item.setName(product.getName());
        item.setImageUrl(product.getImageUrl());
        item.setColor(product.getColor());

        try {
            item.setPrice(Double.parseDouble(product.getPrice().replaceAll("[^0-9.]", "")));
        } catch (Exception e) {
            item.setPrice(0);
        }

        item.setQuantity(1);
        item.updateSubtotal();
        item.setSelected(true);
        return item;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                ", imageUrl='" + imageUrl + '\'' +
                ", color=" + color +
                ", isSelected=" + isSelected +
                '}';
    }
}