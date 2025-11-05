package com.example.demo.models;

import android.content.Context;

import com.example.demo.R;
import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("_id")
    private String id;

    private String productID;
    private String name;
    private int price;
    private int quantity;
    private int subtotal;
    private String size;
    private String[] topping;
    private String imageName;  // thêm trường này để map ảnh trong drawable

    // ===== Getters =====
    public String getId() { return id; }
    public String getProductID() { return productID; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getSubtotal() { return subtotal; }
    public String getSize() { return size; }
    public String[] getTopping() { return topping; }
    public String getImageName() { return imageName; }

    // ===== Setters =====
    public void setQuantity(int q) { this.quantity = q; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
    public void setImageName(String name) { this.imageName = name; }

    // ===== Logic phụ trợ cho ảnh =====
    public int getDrawableResId(Context context) {
        // tìm ảnh trong drawable dựa vào imageName
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        if (resId == 0) {
            resId = R.drawable.logo_app; // ảnh mặc định nếu không tìm thấy
        }
        return resId;
    }
}
