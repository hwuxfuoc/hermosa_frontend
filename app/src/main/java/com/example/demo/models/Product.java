/*
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

*/
/**
 * CLASS CHÍNH: Product
 * Đại diện cho 1 sản phẩm (bánh, đồ uống, lunch)
 * ĐƯỢC DÙNG Ở NHIỀU NƠI:
 *   - Hiển thị danh sách sản phẩm (Home, Menu)
 *   - Thêm vào giỏ hàng
 *   - Hiển thị trong giỏ hàng (với màu nền riêng)
 *   - Chi tiết sản phẩm
 *   - Yêu thích, đánh giá
 *
 * LÝ DO implements Serializable:
 *   → Để truyền object Product qua Intent/Bundle giữa các Fragment/Activity
 *   → Rất quan trọng khi mở ProductDetailFragment.from(product)
 *//*

public class Product implements Serializable {

    // ==================================================================
    // 1. CÁC FIELD TỪ API (Backend trả về - KHÔNG ĐƯỢC ĐỔI TÊN)
    // ==================================================================

    @SerializedName("_id")
    private String id; // MongoDB ObjectId → ví dụ: "672f1a2b9e1c2d3e4f567890"

    @SerializedName("productID")
    private String productID; // Mã sản phẩm: C01, D05, L12

    @SerializedName("name")
    private String name; // Tên hiển thị: "Strawberry Cheese"

    @SerializedName("price")
    private String price; // Backend trả String: "85000" (không có đơn vị)
    // TẠI SAO LẠI DÙNG String?
    // → Vì backend menu trả price là String (tránh lỗi parse)
    // → Dễ format: 85.000 đ, 100.000 đ

    @SerializedName("picture")
    private String imageUrl; // URL Cloudinary: "https://res.cloudinary.com/.../cake01.jpg"

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category; // "cake", "drink", "lunch"

    @SerializedName("sumofFavorites")
    private int sumofFavorites;

    @SerializedName("sumofRatings")
    private int sumofRatings;

    // ==================================================================
    // 2. CÁC FIELD DÙNG TRONG APP (UI + Logic)
    // ==================================================================

    private int imageResId;  // Dùng khi không có internet → hiện ảnh local (drawable)
    private int color;       // Màu nền card trong giỏ hàng: hồng cho cake, đỏ cho drink
    private boolean isFavorite = false; // Người dùng có yêu thích không?
    private int quantity = 0;    // Số lượng trong giỏ hàng
    private int subtotal = 0;    // Thành tiền = giá × số lượng + topping + size
    private String size = "medium"; // small, medium, large
    private String[] topping = new String[0]; // ["Trân châu", "Pudding"]
    private boolean selected = false; // Checkbox trong giỏ hàng

    // ==================================================================
    // 3. SERIAL VERSION UID (BẮT BUỘC KHI implements Serializable)
    // ==================================================================
    private static final long serialVersionUID = 1L;
    // → Nếu không có → Android báo warning
    // → Nếu thay đổi cấu trúc class → tăng lên 2L, 3L...

    // ==================================================================
    // 4. CÁC CONSTRUCTOR – RẤT THÔNG MINH!
    // ==================================================================

    // Constructor 1: Dùng cho Recommended (tạo nhanh từ API)
    public Product(String name, String price, String imageUrl, int color, String productID) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.color = color;
        this.productID = productID;
    }

    // Constructor 2: Tạo thủ công khi không có mạng (hiển thị gợi ý)
    public Product(String name, String price, int imageResId, int color) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.color = color;
    }

    // Constructor 3: Tạo từ API đầy đủ (dùng khi load chi tiết)
    public Product(String id, String productID, String name, String price, String imageUrl,
                   String description, String category, int sumofFavorites, int sumofRatings) {
        this.id = id;
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.category = category;
        this.sumofFavorites = sumofFavorites;
        this.sumofRatings = sumofRatings;
        // TỰ ĐỘNG GÁN MÀU THEO CATEGORY → SIÊU TIỆN!
        this.color = category != null && category.contains("cake") ? 0xFFF1BCBC : 0xFFA71317;
    }
    // Constructor 4: Dùng cho dữ liệu Local (File ProductData.java)
    */
/*public Product(String name, String price, int imageResId, int color, String description, String category) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.color = color;
    }*//*

    public Product(String name, String price, int imageResId, int color, String description, String category) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.color = color;
        this.description = description;
        this.category = category != null ? category.toLowerCase() : "cake";
    }

    // ==================================================================
    // 5. GETTER & SETTER (ĐẦY ĐỦ, CHUẨN)
    // ==================================================================
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    // HÀM SIÊU HAY: Chuyển "85000" → 85000 (long)
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

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public int getSumofFavorites() { return sumofFavorites; }
    public void setSumofFavorites(int sumofFavorites) { this.sumofFavorites = sumofFavorites; }

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

    */
/**
     * Chuyển từ MenuResponse.MenuItem (API) → Product (dùng trong app)
     * Dùng khi load danh sách sản phẩm từ /api/menu/all-product
     *//*

    public static Product fromMenuItem(MenuResponse.MenuItem item) {
        return new Product(
                item.getId(),
                item.getProductID(),
                item.getName(),
                String.valueOf(item.getPrice()), // API trả long → chuyển thành String
                item.getPicture(),
                item.getDescription(),
                item.getCategory(),
                item.getSumofFavorites(),
                item.getSumofRatings()
        );
    }

    */
/**
     * Chuyển từ CartResponse.CartItem → Product
     * DÙNG TRONG GIỎ HÀNG → để hiển thị màu + ảnh đúng
     * LƯU Ý: Backend cart KHÔNG trả picture → phải lấy từ menu
     *//*

    public static Product fromCartItem(CartResponse.CartItem cartItem) {
        Product p = new Product(
                cartItem.getName(),
                String.valueOf(cartItem.getPrice()),
                cartItem.getPicture(), // nếu backend cart đã có picture → dùng
                cartItem.getCategory().contains("cake") ? 0xFFF1BCBC : 0xFFA71317,
                cartItem.getProductID()
        );
        p.setQuantity(cartItem.getQuantity());
        p.setSubtotal(cartItem.getSubtotal());
        p.setSize(cartItem.getSize());
        p.setTopping(cartItem.getTopping() != null ? cartItem.getTopping().toArray(new String[0]) : new String[0]);
        return p;
    }
}*/
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * CLASS CHÍNH: Product
 * Đại diện cho 1 sản phẩm (bánh, đồ uống, lunch)
 * ĐƯỢC DÙNG Ở NHIỀU NƠI:
 *   - Hiển thị danh sách sản phẩm (Home, Menu)
 *   - Thêm vào giỏ hàng
 *   - Hiển thị trong giỏ hàng (với màu nền riêng)
 *   - Chi tiết sản phẩm
 *   - Yêu thích, đánh giá
 *
 * LÝ DO implements Serializable:
 *   → Để truyền object Product qua Intent/Bundle giữa các Fragment/Activity
 *   → Rất quan trọng khi mở ProductDetailFragment.from(product)
 */
public class Product implements Serializable {

    // ==================================================================
    // 1. CÁC FIELD TỪ API (Backend trả về - KHÔNG ĐƯỢC ĐỔI TÊN)
    // ==================================================================

    @SerializedName("_id")
    private String id; // MongoDB ObjectId → ví dụ: "672f1a2b9e1c2d3e4f567890"

    @SerializedName("productID")
    private String productID; // Mã sản phẩm: C01, D05, L12 – BẮT BUỘC

    @SerializedName("name")
    private String name; // Tên hiển thị: "Strawberry Cheese"

    @SerializedName("price")
    private String price; // Backend trả String: "85000" (không có đơn vị)
    // TẠI SAO LẠI DÙNG String?
    // → Vì backend menu trả price là String (tránh lỗi parse)
    // → Dễ format: 85.000 đ, 100.000 đ

    @SerializedName("picture")
    private String imageUrl; // URL Cloudinary: "https://res.cloudinary.com/.../cake01.jpg"

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category; // "cake", "drink", "lunch"

    @SerializedName("sumofFavorites")
    private int sumofFavorites;

    @SerializedName("sumofRatings")
    private int sumofRatings;

    // ==================================================================
    // 2. CÁC FIELD DÙNG TRONG APP (UI + Logic)
    // ==================================================================

    private int imageResId;  // Dùng khi không có internet → hiện ảnh local (drawable)
    private int color;       // Màu nền card trong giỏ hàng: hồng cho cake, đỏ cho drink
    private boolean isFavorite = false; // Người dùng có yêu thích không?
    private int quantity = 0;    // Số lượng trong giỏ hàng
    private int subtotal = 0;    // Thành tiền = giá × số lượng + topping + size
    private String size = "medium"; // small, medium, large
    private String[] topping = new String[0]; // ["Trân châu", "Pudding"]
    private boolean selected = false; // Checkbox trong giỏ hàng

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
    public Product(String id, String productID, String name, String price, String imageUrl,
                   String description, String category, int sumofFavorites, int sumofRatings) {
        this.id = id;
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.category = category;
        this.sumofFavorites = sumofFavorites;
        this.sumofRatings = sumofRatings;
        // Tự động gán màu theo category
        this.color = (category != null && category.toLowerCase().contains("cake")) ? 0xFFF1BCBC : 0xFFA71317;
    }

    /**
     * Constructor 2: Dùng cho dữ liệu Local (ProductData.java) – CÓ productID
     */
    public Product(String name, String price, int imageResId, int color,
                   String description, String category, String productID) {
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

    // HÀM SIÊU HAY: Chuyển "85000" → 85000 (long)
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
        return new Product(
                item.getId(),
                item.getProductID(),
                item.getName(),
                String.valueOf(item.getPrice()),
                item.getPicture(),
                item.getDescription(),
                item.getCategory(),
                item.getSumofFavorites(),
                item.getSumofRatings()
        );
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
}