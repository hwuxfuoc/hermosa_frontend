/*
package com.example.demo;

import android.content.Context;

public class Product {
    private String name;         // tên
    private String price;        // giá
    private int imageResId;      // ID ảnh nội bộ (drawable)
    private int color;           // màu nền
    private String description;  // mô tả
    private String category;     // danh mục
    private boolean isFavorite;  // trạng thái yêu thích
    private int quantity = 0;    // số lượng
    private String imageName;    // tên ảnh trong resource (VD: "strawberry_cheese")
    private String imageUrl;     // 🔹 URL ảnh từ API (VD: https://res.cloudinary.com/...)

    // ====== Constructor gốc ======
    public Product(String name, String price, int imageResId, int color) {
        this(name, price, imageResId, color, "", "");
    }

    public Product(String name, String price, int imageResId, int color, String description, String category) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.color = color;
        this.description = description;
        this.category = category;
    }

    // ====== Getter / Setter ======
    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getImageResId() { return imageResId; }
    public int getColor() { return color; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    // 🔹 Getter/Setter mới cho ảnh URL
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    */
/**
     * Hàm tự động tìm ID ảnh trong resource (drawable)
     * Nếu không có, trả về 0 để dùng ảnh online thay thế.
     *//*

    public int resolveImageResource(Context context) {
        if (imageResId != 0) return imageResId;
        if (imageName == null || imageName.isEmpty()) return 0;

        int resId = context.getResources().getIdentifier(
                imageName, "drawable", context.getPackageName()
        );
        return resId;
    }

    */
/**
     * 🔹 Hàm tiện ích xác định nên hiển thị ảnh online hay ảnh nội bộ.
     * Nếu imageUrl khác null → load online
     * Nếu không → dùng ảnh drawable
     *//*

    public boolean hasOnlineImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }
}
*/
