// File: CartResponseWrapper.java
package com.example.demo.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CartResponseWrapper {
    @SerializedName("status")
    private String status;

    @SerializedName("cartData")
    private CartData data;

    public String getStatus() { return status; }
    public CartData getData() { return data; }

    public static class CartData {
        @SerializedName("items")
        private List<CartItem> items;

        @SerializedName("totalMoney")
        private long totalMoney;

        public List<CartItem> getItems() { return items; }
        public long getTotalMoney() { return totalMoney; }
    }
}