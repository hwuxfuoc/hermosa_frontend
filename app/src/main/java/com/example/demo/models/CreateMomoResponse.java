/*
package com.example.demo.models;

public class CreateMomoResponse {
    String message;
    String payUrl;

    public String getMessage() {
        return message;
    }

    public String getPayUrl() {
        return payUrl;
    }
}
*//*

package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class CreateMomoResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("payUrl")
    private String payUrl;

    // --- GETTER (Null-safe) ---
    public String getMessage() {
        return message != null ? message : "";
    }

    public String getPayUrl() {
        return payUrl;
    }
}*/
package com.example.demo.models;

public class CreateMomoResponse {
    private String message;
    private String payUrl; // Link để mở trình duyệt
    // private Object rawResponse; // Có thể bỏ qua nếu không dùng

    public String getPayUrl() {
        return payUrl;
    }
    public String getMessage() {
        return message;
    }
}