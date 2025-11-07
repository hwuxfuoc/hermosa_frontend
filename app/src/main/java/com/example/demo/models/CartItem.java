package com.example.demo.models;

import android.content.Context;

import com.example.demo.Product;
import com.example.demo.R;
import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("_id")
    private String id;

    private String productID;
    private String name;

    // SỬA 1: Đổi price sang double để khớp với Adapter (%,.0f)
    private double price;

    private int quantity;
    private int subtotal;
    private String size;
    private String[] topping;
    private String imageName;

    // --- BỔ SUNG 1: Trường để lưu màu (giống Product.java) ---
    private int color;

    // --- BỔ SUNG 2: Trường cho CheckBox ---
    private boolean isSelected = false; // Mặc định là chưa chọn

    // ===== Getters =====
    public String getId() { return id; }
    public String getProductID() { return productID; }
    public String getName() { return name; }

    // Sửa kiểu trả về của getPrice()
    public double getPrice() { return price; }

    public int getQuantity() { return quantity; }
    public int getSubtotal() { return subtotal; }
    public String getSize() { return size; }
    public String[] getTopping() { return topping; }
    public String getImageName() { return imageName; }

    // --- BỔ SUNG: Getters/Setters cho color (ĐÂY LÀ HÀM BỊ THIẾU) ---
    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    // --- BỔ SUNG: Getters/Setters cho isSelected ---
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }

    // ===== Setters =====
    public void setQuantity(int q) { this.quantity = q; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
    public void setImageName(String name) { this.imageName = name; }
    public void setPrice(double price) { this.price = price; }
    public void setName(String name) {this.name = name;}


    // ===== Logic phụ trợ cho ảnh =====
    public int getDrawableResId(Context context) {
        if (imageName == null || imageName.isEmpty()) {
            return R.drawable.logo_app;
        }
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        if (resId == 0) {
            resId = R.drawable.logo_app;
        }
        return resId;
    }
    // ================== MAPPER từ Product sang CartItem ==================
    public static CartItem fromProduct(Product p) {
        CartItem item = new CartItem();
        item.setName(p.getName());
        try {
            // Chuyển giá từ String → double an toàn
            item.setPrice(Double.parseDouble(p.getPrice().replaceAll("[^0-9.]", "")));
        } catch (Exception e) {
            item.setPrice(0);
        }
        item.setQuantity(p.getQuantity());
        item.setImageName(p.getImageName());
        item.setColor(p.getColor());
        item.setSubtotal((int)(item.getQuantity() * item.getPrice()));
        return item;
    }

}