package com.example.demo.model;

import android.content.Context;

import com.example.demo.ProductData;
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
    private String imageName;   // tên file drawable (vd: "cake_strawberry_cheese")
    private String imageUrl;    // optional: url nếu backend trả ảnh
    private int color;          // optional: màu (int ARGB) nếu backend trả

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
    public String getImageUrl() { return imageUrl; }
    public int getColor() { return color; } // trực tiếp nếu đã set

    // ===== Setters =====
    public void setQuantity(int q) { this.quantity = q; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
    public void setImageName(String name) { this.imageName = name; }
    public void setImageUrl(String url) { this.imageUrl = url; }
    public void setColor(int color) { this.color = color; }

    // ===== Logic phụ trợ cho ảnh =====
    public int getDrawableResId(Context context) {
        if (imageName == null) {
            return R.drawable.logo_app;
        }
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        if (resId == 0) {
            resId = R.drawable.logo_app; // ảnh mặc định nếu không tìm thấy
        }
        return resId;
    }

    /**
     * Trả về màu dùng cho nền item:
     * 1) nếu backend đã set color (không bằng 0) -> dùng luôn
     * 2) nếu không, cố map theo tên sản phẩm với ProductData (nếu ProductData có)
     * 3) fallback trắng
     */
    public int resolveColor(Context context) {
        if (this.color != 0) return this.color;

        // nếu bạn dùng ProductData để khởi tạo danh sách sản phẩm (với màu), ta thử dò
        try {
            for (com.example.demo.model.Product p : com.example.demo.ProductData.getAllProducts()) {
                if (p.getName() != null && p.getName().equalsIgnoreCase(this.name)) {
                    // giả sử Product có getColor() trả int
                    return p.getColor();
                }
            }
        } catch (Exception ignored) { }

        // fallback trắng
        return 0xFFFFFFFF;
    }
}
