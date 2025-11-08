package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MenuResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private List<MenuItem> data;

    public String getStatus() { return status; }
    public List<MenuItem> getData() { return data; }

    public static class MenuItem {
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

        @SerializedName("description")
        private String description;

        @SerializedName("category")
        private String category;

        @SerializedName("sumofFavorites")
        private int sumofFavorites;

        @SerializedName("sumofRatings")
        private int sumofRatings;

        // TẤT CẢ GETTER BẮT BUỘC PHẢI CÓ!
        public String getId() { return id; }
        public String getProductID() { return productID; }
        public String getName() { return name; }
        public long getPrice() { return price; }
        public String getPicture() { return picture; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public int getSumofFavorites() { return sumofFavorites; }
        public int getSumofRatings() { return sumofRatings; }
    }

    public static class SingleProductResponse {
        @SerializedName("status") private String status;
        @SerializedName("data") private MenuItem data;

        public String getStatus() { return status; }
        public MenuItem getData() { return data; }
    }
}