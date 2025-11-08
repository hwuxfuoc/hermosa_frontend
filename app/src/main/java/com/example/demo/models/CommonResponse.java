package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class CommonResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("statuscode")
    private int statuscode;
    @SerializedName("message")
    private String message;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
}