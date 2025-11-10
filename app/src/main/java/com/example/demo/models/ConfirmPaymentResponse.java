/*
package com.example.demo.models;

public class ConfirmPaymentResponse {
    String orderID;
    String status; // Sẽ là "done" hoặc "not_done"
    String method;

    // Getter
    public String getStatus() {
        return status;
    }
}
*/
// ConfirmPaymentResponse.java
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class ConfirmPaymentResponse {
    @SerializedName("orderID")
    private String orderID;

    @SerializedName("status")
    private String status; // "done", "not_done", "pending", "failed"

    @SerializedName("method")
    private String method;

    @SerializedName("time")
    private String time;

    // Getter
    public String getOrderID() { return orderID; }
    public String getStatus() { return status; }
    public String getMethod() { return method; }
    public String getTime() { return time; }
}