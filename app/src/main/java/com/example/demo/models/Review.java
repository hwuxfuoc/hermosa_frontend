package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Review implements Serializable {
    @SerializedName("productID")
    private String productID;

    @SerializedName("rating")
    private float rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("userName")
    private String userName;

    @SerializedName("date")
    private String date;

    public Review(String productID, float rating, String comment) {
        this.productID = productID;
        this.rating = rating;
        this.comment = comment;
    }

    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}