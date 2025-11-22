/*
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

*/
/**
 * CLASS CHÍNH: CartResponse
 * Đại diện cho toàn bộ phản hồi JSON từ API giỏ hàng
 * Endpoint: GET /api/cart/view-and-caculate-total-money?userID=...
 *
 * Ví dụ JSON thực tế từ backend:
 * {
 *   "status": "Success",
 *   "data": {
 *     "items": [ ...danh sách món... ],
 *     "totalMoney": 185000
 *   }
 * }
 *//*

public class CartResponse {

    // ------------------------------------------------------------------
    // 1. TRẠNG THÁI PHẢN HỒI
    // ------------------------------------------------------------------
    private String status; // "Success" hoặc "Failed"

    // ------------------------------------------------------------------
    // 2. DỮ LIỆU THỰC TẾ CỦA GIỎ HÀNG
    // ------------------------------------------------------------------
    // Backend trả về key "data" → phải đặt đúng tên biến hoặc dùng @SerializedName
    private Data data;

    // ------------------- GETTER (BẮT BUỘC) -------------------
    public String getStatus() { return status; }

    */
/**
     * Lấy dữ liệu giỏ hàng (items + totalMoney)
     * Dùng trong FragmentCart: response.body().getData().getItems()
     *//*

    public Data getData() { return data; }
    // ------------------------------------------------------------------


    */
/**
     * CLASS CON: Data
     * Đại diện cho object "data" trong JSON
     * Chứa danh sách món + tổng tiền
     *//*

    public static class Data {

        // Danh sách các món trong giỏ hàng
        // Có thể null nếu giỏ trống
        private List<CartItem> items;

        // Tổng tiền của tất cả món (đã tính size + topping + quantity)
        private long totalMoney;

        // Getter cho RecyclerView
        public List<CartItem> getItems() { return items; }

        // Getter cho TextView tổng tiền
        public long getTotalMoney() { return totalMoney; }
    }


    */
/**
     * CLASS CON: CartItem
     * Đại diện cho MỘT món trong giỏ hàng
     * Mỗi phần tử trong mảng "items" sẽ được Gson tự động map thành 1 CartItem
     *
     * ĐÃ ĐƯỢC FIX HOÀN CHỈNH – ĐỒNG BỘ 100% VỚI:
     * - Backend Node.js (có size, topping, note)
     * - Product.java (có getSize(), getTopping())
     * - Giỏ hàng UI (checkbox, màu nền, ảnh)
     *//*

    public static class CartItem {

        // ------------------------------------------------------------------
        // 1. FIELD TỪ BACKEND (PHẢI DÙNG @SerializedName)
        // ------------------------------------------------------------------
        @SerializedName("_id")              // MongoDB ObjectId của mục trong giỏ
        private String id;

        @SerializedName("productID")         // Mã sản phẩm: C01, D05
        private String productID;

        @SerializedName("name")              // Tên món: "Strawberry Cheese"
        private String name;

        @SerializedName("price")             // Giá gốc 1 món (chưa tính size/topping)
        private long price;

        @SerializedName("picture")           // URL ảnh từ Cloudinary
        private String picture;

        @SerializedName("category")          // "cake", "drink", "lunch" → dùng để đổi màu nền
        private String category;

        // ------------------------------------------------------------------
        // 2. FIELD TỪ BACKEND (KHÔNG CÓ @SerializedName → tên giống hệt JSON)
        // ------------------------------------------------------------------
        private int quantity;                // Số lượng: 1, 2, 3...
        private int subtotal;                // Thành tiền = (price + size + topping) × quantity
        private String size;                 // "small", "medium", "large"
        private List<String> topping;        // ["Trân châu", "Pudding"] – backend trả mảng
        private String note;                 // Ghi chú khách hàng

        // ------------------------------------------------------------------
        // 3. FIELD DÙNG TRONG APP (UI + Logic)
        // ------------------------------------------------------------------
        private boolean selected = true;     // Checkbox mặc định được tick

        // ------------------------------------------------------------------
        // 4. GETTER – BẮT BUỘC PHẢI CÓ ĐỦ ĐỂ PRODUCT.JAVA DÙNG ĐƯỢC
        // ------------------------------------------------------------------
        public String getId() { return id; }
        public String getProductID() { return productID; }
        public String getName() { return name; }
        public long getPrice() { return price; }
        public String getPicture() { return picture; }
        public String getCategory() { return category; }

        public int getQuantity() { return quantity; }
        public int getSubtotal() { return subtotal; }

        // PHẢI CÓ 2 DÒNG NÀY → để Product.fromCartItem() gọi được
        public String getSize() { return size != null ? size : "medium"; }
        public List<String> getTopping() { return topping; }
        public String getNote() { return note; }

        public boolean isSelected() { return selected; }

        // ------------------------------------------------------------------
        // 5. SETTER – CẦN KHI TĂNG/GIẢM SỐ LƯỢNG HOẶC CẬP NHẬT CHECKBOX
        // ------------------------------------------------------------------
        */
/**
         * Tự động tính lại subtotal khi thay đổi số lượng
         *//*

        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.subtotal = (int) (this.price * quantity);
        }

        public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
        public void setSize(String size) { this.size = size; }
        public void setTopping(List<String> topping) { this.topping = topping; }
        public void setNote(String note) { this.note = note; }

        */
/**
         * Dùng khi tick/untick checkbox trong giỏ hàng
         *//*

        public void setSelected(boolean selected) { this.selected = selected; }

        */
/**
         * Dùng khi load ảnh từ menu (nếu backend cart chưa có picture)
         *//*

        public void setPicture(String picture) { this.picture = picture; }
    }
}*/
/*
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;


*/
/**
 * CLASS CHÍNH: CartResponse
 * Đại diện cho toàn bộ phản hồi JSON từ API giỏ hàng
 * Endpoint: GET /cart/view-and-caculate-total-money?userID=...
 *
 * Ví dụ JSON:
 * {"status":"Success","message":"View cart successfull","data":null}
 * hoặc
 * {"status":"Success","message":"View cart successfull","data":{"items":[...],"totalMoney":185000}}
 *//*

public class CartResponse {

    private String status;
    private String message;
    private Data data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Data getData() { return data; }

    */
/**
     * CLASS CON: Data
     * Đảm bảo items luôn là ArrayList (không null)
     *//*

    public static class Data {

        @SerializedName("items")
        private List<CartItem> items = new ArrayList<>();  // Khởi tạo mặc định

        @SerializedName("totalMoney")
        private long totalMoney = 0;

        public List<CartItem> getItems() {
            return items != null ? items : new ArrayList<>();
        }

        public long getTotalMoney() { return totalMoney; }
    }

    */
/**
     * CLASS CON: CartItem
     * Đồng bộ 100% với backend
     *//*

    public static class CartItem {

        @SerializedName("_id")
        private String id;

        @SerializedName("productID")
        private String productID;

        @SerializedName("name")
        private String name;

        @SerializedName("price")
        private long price;

        @SerializedName("picture")
        private String picture;

        @SerializedName("category")
        private String category;

        private int quantity;
        private int subtotal;
        private String size;
        private List<String> topping;
        private String note;

        // UI: checkbox
        private boolean selected = true;

        // --- GETTER ---
        public String getId() { return id; }
        public String getProductID() { return productID; }
        public String getName() { return name; }
        public long getPrice() { return price; }
        public String getPicture() { return picture; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
        public int getSubtotal() { return subtotal; }
        public String getSize() { return size != null ? size : "medium"; }
        public List<String> getTopping() { return topping != null ? topping : new ArrayList<>(); }
        public String getNote() { return note; }
        public boolean isSelected() { return selected; }

        // --- SETTER ---
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
        public void setSize(String size) { this.size = size; }
        public void setTopping(List<String> topping) { this.topping = topping; }
        public void setNote(String note) { this.note = note; }
        public void setSelected(boolean selected) { this.selected = selected; }
        public void setPicture(String picture) { this.picture = picture; }
    }
}*/
package com.example.demo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * MODEL: CartResponse
 * Đại diện cho toàn bộ phản hồi từ API: GET /cart/view-and-caculate-total-money
 *
 * JSON mẫu:
 * {
 *   "status": "Success",
 *   "data": {
 *     "items": [ ... ],
 *     "totalMoney": 100000
 *   }
 * }
 */
public class CartResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private Data data;

    // --- GETTER ---
    public String getStatus() { return status; }
    public Data getData() { return data; }

    // --- INNER CLASS: Data ---
    public static class Data {

        @SerializedName("items")
        private List<CartItem> items = new ArrayList<>();

        @SerializedName("totalMoney")
        private long totalMoney = 0;

        // --- GETTER (Null-safe) ---
        public List<CartItem> getItems() {
            return items != null ? items : new ArrayList<>();
        }

        public long getTotalMoney() { return totalMoney; }
    }

    // --- INNER CLASS: CartItem (implements Parcelable) ---
    public static class CartItem implements Parcelable {

        @SerializedName("_id")
        private String id;

        @SerializedName("productID")
        private String productID;

        @SerializedName("name")
        private String name;

        @SerializedName("price")
        private long price;

        @SerializedName("picture")
        private String picture;

        @SerializedName("category")
        private String category;

        @SerializedName("quantity")
        private int quantity = 1;

        @SerializedName("subtotal")
        private int subtotal = 0;

        @SerializedName("size")
        private String size;

        @SerializedName("topping")
        private List<String> topping;

        @SerializedName("note")
        private String note;

        // UI: checkbox chọn món
        private boolean selected = true;

        // --- GETTER (Null-safe) ---
        public String getId() { return id; }
        public String getProductID() { return productID; }
        public String getName() { return name != null ? name : "Không rõ"; }
        public long getPrice() { return price; }
        public String getPicture() { return picture; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
        public int getSubtotal() { return subtotal; }
        public String getSize() { return size != null ? size : "medium"; }
        public List<String> getTopping() { return topping != null ? topping : new ArrayList<>(); }
        public String getNote() { return note != null ? note : ""; }
        public boolean isSelected() { return selected; }

        // --- SETTER ---
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
        public void setSize(String size) { this.size = size; }
        public void setTopping(List<String> topping) { this.topping = topping; }
        public void setNote(String note) { this.note = note; }
        public void setSelected(boolean selected) { this.selected = selected; }
        public void setPicture(String picture) { this.picture = picture; }

        // --- PARCELABLE IMPLEMENTATION ---
        protected CartItem(Parcel in) {
            id = in.readString();
            productID = in.readString();
            name = in.readString();
            price = in.readLong();
            picture = in.readString();
            category = in.readString();
            quantity = in.readInt();
            subtotal = in.readInt();
            size = in.readString();
            topping = in.createStringArrayList();
            note = in.readString();
            selected = in.readByte() != 0;
        }

        public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
            @Override
            public CartItem createFromParcel(Parcel in) {
                return new CartItem(in);
            }

            @Override
            public CartItem[] newArray(int size) {
                return new CartItem[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(productID);
            dest.writeString(name);
            dest.writeLong(price);
            dest.writeString(picture);
            dest.writeString(category);
            dest.writeInt(quantity);
            dest.writeInt(subtotal);
            dest.writeString(size);
            dest.writeStringList(topping);
            dest.writeString(note);
            dest.writeByte((byte) (selected ? 1 : 0));
        }
    }
}