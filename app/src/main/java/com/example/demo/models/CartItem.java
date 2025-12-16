package com.example.demo.models;

import android.content.Context;
import com.example.demo.R;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CartItem /*implements Serializable*/ {

    @SerializedName("_id")
    private String id;                    // ObjectId từ MongoDB

    @SerializedName("productID")
    private String productID;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private double price;                 // Giá sau khi tính size + topping

    @SerializedName("quantity")
    private int quantity = 1;

    @SerializedName("subtotal")
    private int subtotal;                 // = price × quantity

    @SerializedName("size")
    private String size;                  // null / "medium" / "large"

    @SerializedName("topping")
    private String[] topping;             // mảng tên topping

    @SerializedName("note")
    private String note;                  // Ghi chú (có thể null)

    // THÊM: URL ảnh từ backend (menu.imageUrl)
    @SerializedName("imageUrl")
    private String imageUrl;

    // Màu nền card (từ Product.getColor())
    private int color = 0xFFFFFFFF;       // Mặc định trắng

    // Checkbox chọn để tính tiền
    private boolean isSelected = true;

    // ================== GETTERS & SETTERS ==================
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

    // IMAGE URL
    public String getImageUrl() {
        return imageUrl != null && !imageUrl.trim().isEmpty() ? imageUrl : "";
    }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // COLOR
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    // SELECTED
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }

    // CẬP NHẬT SUBTOTAL KHI THAY ĐỔI QUANTITY
    public void updateSubtotal() {
        this.subtotal = (int) (this.price * this.quantity);
    }

    // ================== FROM PRODUCT (khi thêm từ menu) ==================
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

    // ================== TO STRING (dùng để debug) ==================
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