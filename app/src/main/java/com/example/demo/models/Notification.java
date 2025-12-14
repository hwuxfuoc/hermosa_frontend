package com.example.demo.models;
import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("notiID")
    private String notiID;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("createdAt")
    private String createdAt;

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getCreatedAt() { return createdAt; }
}