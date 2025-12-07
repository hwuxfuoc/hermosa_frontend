/*
package com.example.demo.models;

public class CreateMomoRequest {
    String orderID;
    String userID;
    public CreateMomoRequest(String orderID, String userID){
        this.orderID = orderID;
        this.userID = userID;
    }
}
*//*

package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class CreateMomoRequest {

    @SerializedName("orderID")
    private String orderID;

    @SerializedName("userID")
    private String userID;

    public CreateMomoRequest(String orderID, String userID) {
        this.orderID = orderID;
        this.userID = userID;
    }

    // Getter (bắt buộc cho Gson)
    public String getOrderID() { return orderID; }
    public String getUserID() { return userID; }
}*/
package com.example.demo.models;

public class CreateMomoRequest {
    private String orderID;

    public CreateMomoRequest(String orderID) {
        this.orderID = orderID;
    }
}