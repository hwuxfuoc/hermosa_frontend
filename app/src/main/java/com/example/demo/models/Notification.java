package com.example.demo.models;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    @SerializedName("notiID")
    private String notiID;
    @SerializedName("title")
    private String title;
    @SerializedName("message")
    private String message;
    @SerializedName("createdAt")
    private Date createdAt;
    @SerializedName("sent")
    private boolean sent;
    public String getNotiID() {
        return notiID;
    }
    public String getTitle() {
        return title;
    }
    public String getMessage() {
        return message;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public boolean isSent() {
        return sent;
    }
}