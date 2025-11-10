package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class CreateVnpayResponse {
    @SerializedName("url")
    private String url;

    public String getUrl() {
        return url;
    }
}