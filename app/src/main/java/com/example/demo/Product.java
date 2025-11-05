package com.example.demo;

import android.content.Context;

public class Product {
    String name; // tên
    String price; // giá
    int imageResId; // id ảnh
    int color; // màu nền
    String description; // mô tả
    String category; // danh mục
    private boolean isFavorite; // trạng thái yêu thích
    private int quantity = 0;
    private String imageName; // 🔹 tên ảnh trong resource (VD: "strawberry_cheese")



    public Product(String name, String price, int imageResId, int color) {
        this(name, price, imageResId, color, "", ""); // mặc định không có mô tả
    }

    public Product(String name, String price, int imageResId, int color, String description, String category) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.color = color;
        this.description = description;
        this.category = category;
    }

    // ================== Getter/Setter gốc ==================
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



    // Getter/Setter cho imageName
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    /**
     * Hàm tự động tìm ID ảnh dựa vào tên ảnh trong resource
     * - Nếu có imageResId sẵn, dùng luôn
     * - Nếu có imageName (VD: "matcha_latte") → tìm ID trong drawable
     */
    public int resolveImageResource(Context context) {
        if (imageResId != 0) return imageResId; // nếu có ID sẵn
        if (imageName == null || imageName.isEmpty()) return 0;

        int resId = context.getResources().getIdentifier(
                imageName, "drawable", context.getPackageName()
        );
        return resId;
    }
}
