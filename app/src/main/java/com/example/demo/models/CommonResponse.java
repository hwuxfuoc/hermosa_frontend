package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class CommonResponse {
    @SerializedName("data")
    private Object data;
    @SerializedName("status")
    private String status;
    @SerializedName("statuscode")
    private int statuscode;
    @SerializedName("message")
    private String message;
    public Object getData() {
        return data;
    }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}