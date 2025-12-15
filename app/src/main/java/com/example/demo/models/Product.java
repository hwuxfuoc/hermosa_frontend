package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import android.graphics.Color;

public class Product implements Serializable {

    // ==================================================================
    // 1. CÁC FIELD TỪ API (Backend trả về - KHÔNG ĐƯỢC ĐỔI TÊN)
    // ==================================================================

    @SerializedName("_id")
    private String id;

    @SerializedName("productID")
    private String productID;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private String price;

    @SerializedName("picture")
    private String imageUrl;

    @SerializedName("backgroundHexacode")
    private String backgroundHexacode;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("sumofFavorites")
    private int sumofFavorites;

    @SerializedName("sumofRatings")
    private int sumofRatings;

    // ==================================================================
    // 2. CÁC FIELD DÙNG TRONG APP (UI + Logic)
    // ==================================================================

    private int imageResId;
    private int color;
    private boolean isFavorite = false;
    private int quantity = 0;
    private int subtotal = 0;
    private String size = "medium";
    private String[] topping = new String[0];
    private boolean selected = false;

    // ==================================================================
    // 3. SERIAL VERSION UID (BẮT BUỘC KHI implements Serializable)
    // ==================================================================
    private static final long serialVersionUID = 1L;

    // ==================================================================
    // 4. CÁC CONSTRUCTOR – RẤT THÔNG MINH!
    // ==================================================================

    /**
     * Constructor 1: Tạo từ API đầy đủ (dùng khi load từ backend)
     */
    public Product(String id, String name, String productID, String price, String imageUrl,
                   String description, String category, int sumofFavorites, int sumofRatings) {
        this.id = id;
        this.name = name;
        this.productID = productID;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.category = category;
        this.sumofFavorites = sumofFavorites;
        this.sumofRatings = sumofRatings;

        this.color = (category != null && category.toLowerCase().contains("cake"))
                ? 0xFFF1BCBC : 0xFFA71317;
    }

    /**
     * Constructor 2: Dùng cho dữ liệu Local (ProductData.java) – CÓ productID
     */
    public Product(String productID, String name, String price, int imageResId, int color,
                   String description, String category) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.color = color;
        this.description = description;
        this.category = category != null ? category.toLowerCase() : "cake";
        this.productID = productID;
    }

    /**
     * Constructor 3: Dùng trong fromCartItem() – backend trả imageUrl (String)
     */
    public Product(String name, String price, String imageUrl, int color, String productID) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.color = color;
        this.productID = productID;
    }

    /**
     * Constructor 4: Tạo nhanh (Recommended) – không có description
     */
    public Product(String name, String price, int imageResId, int color) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.color = color;
    }
    // === THÊM CONSTRUCTOR NÀY VÀO Product.java ===

    /**
     * Constructor 6 tham số – DÙNG TRONG BaseDescriptionActivity
     * Không cần productID vì không thêm giỏ ở đây
     */
    public Product(String name, String price, int imageResId, int color, String description, String category) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.color = color;
        this.description = description;
        this.category = category != null ? category.toLowerCase() : "cake";
    }
    // ==================================================================
    // 5. GETTER & SETTER – ĐẦY ĐỦ, CHUẨN
    // ==================================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductID() {
        return productID != null ? productID : "UNKNOWN"; // ← TRÁNH NULL
    }
    public void setProductID(String productID) { this.productID = productID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public long getPriceLong() {
        try {
            return Long.parseLong(price.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getSumofFavorites() { return sumofFavorites; }
    public void setSumofFavorites(int sumofFavorites) { this.sumofFavorites = sumofFavorites; }

    public int getSumofRatings() { return sumofRatings; }
    public void setSumofRatings(int sumofRatings) { this.sumofRatings = sumofRatings; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String[] getTopping() { return topping; }
    public void setTopping(String[] topping) {
        this.topping = topping != null ? topping : new String[0];
    }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // ==================================================================
    // 6. HÀM CHUYỂN ĐỔI – SIÊU THÔNG MINH!
    // ==================================================================

    /**
     * Chuyển từ MenuResponse.MenuItem (API) → Product
     */

    public static Product fromMenuItem(MenuResponse.MenuItem item) {
        String normalizedCat = normalizeCategory(item.getCategory());

        int color = 0xFFEB4341; // default

        String hexCode = item.getBackgroundHexacode();
        if (hexCode != null && !hexCode.trim().isEmpty()) {
            try {
                String hex = hexCode.trim();
                if (!hex.startsWith("#")) hex = "#" + hex;
                if (hex.length() == 6) hex = "#FF" + hex;
                if (hex.length() == 7) hex = "#FF" + hex.substring(1);
                color = Color.parseColor(hex);
            } catch (Exception e) {
                switch (normalizedCat) {
                    case "drink": color = 0xFFA71317; break;
                    case "food":  color = 0xFF388E3C; break;
                    default:      color = 0xFFEB4341; break;
                }
            }
        } else {
            switch (normalizedCat) {
                case "drink": color = 0xFFA71317; break;
                case "food":  color = 0xFF388E3C; break;
                default:      color = 0xFFEB4341; break;
            }
        }

        Product p = new Product(
                item.getId(),
                item.getName(),
                item.getProductID(),
                String.valueOf(item.getPrice()),
                item.getPicture(),
                item.getDescription(),
                normalizedCat,
                item.getSumofFavorites(),
                item.getSumofRatings()
        );

        p.setImageUrl(item.getPicture());
        p.setColor(color);
        p.setCategory(normalizedCat);

        return p;
    }

    public static String normalizeCategory(String category) {
        if (category == null) return "";
        String lower = category.toLowerCase().trim();
        if (lower.contains("launch") || lower.contains("lunch") || lower.contains("food")) {
            return "food";
        } else if (lower.contains("drink") || lower.contains("coffee") || lower.contains("tea")) {
            return "drink";
        } else if (lower.contains("cake") || lower.contains("dessert") || lower.contains("donut")) {
            return "cake";
        }
        return lower;
    }

    /**
     * Chuyển từ CartResponse.CartItem → Product
     * DÙNG TRONG GIỎ HÀNG → hiển thị màu + ảnh đúng
     */
    public static Product fromCartItem(CartResponse.CartItem cartItem) {
        Product p = new Product(
                cartItem.getName(),
                String.valueOf(cartItem.getPrice()),
                cartItem.getPicture(),
                cartItem.getCategory().contains("cake") ? 0xFFF1BCBC : 0xFFA71317,
                cartItem.getProductID()
        );
        p.setQuantity(cartItem.getQuantity());
        p.setSubtotal(cartItem.getSubtotal());
        p.setSize(cartItem.getSize());
        p.setTopping(cartItem.getTopping() != null ? cartItem.getTopping().toArray(new String[0]) : new String[0]);
        return p;
    }

    private List<Review> reviews = new ArrayList<>();

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}