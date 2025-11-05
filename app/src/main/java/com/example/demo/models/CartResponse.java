package com.example.demo.models;

import java.util.List;

public class CartResponse {
    // Nếu backend trả về { status: "...", message: "...", data: { items: [...], totalMoney: 123 } }
    // bạn cần map đúng cấu trúc. Mình ở đây map theo backend bạn đã dùng ở ví dụ trước:
    private String status;
    private String message;

    // Nếu backend trả data là object chứa items và totalMoney
    private CartData data;

    // Nếu backend trả total ở root, bạn có thể thêm field total (nếu cần)
    private int total; // optional


    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public CartData getData() { return data; }
    public int getTotal() {
        // nếu backend trả total ở data.totalMoney
        if (data != null) return data.getTotalMoney();
        return total;
    }

    public static class CartData {
        private String userID;
        private List<CartItem> items;
        private int totalMoney;

        public String getUserID() { return userID; }
        public List<CartItem> getItems() { return items; }
        public int getTotalMoney() { return totalMoney; }
    }


}
