package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class CreateVnpayResponse {
    @SerializedName("url")
    private String url;

    @SerializedName("message")
    private String message;

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }
}