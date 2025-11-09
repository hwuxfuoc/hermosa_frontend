package com.example.demo.model;

import com.google.gson.annotations.SerializedName;

public class MenuItem {

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

    // Getters
    public String getId() {
        return id;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPrice() {
        return price;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getSumofFavorites() {
        return sumofFavorites;
    }

    public int getSumofRatings() {
        return sumofRatings;
    }
}