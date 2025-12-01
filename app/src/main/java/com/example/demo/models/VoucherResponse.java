package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VoucherResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Voucher> data; // Danh sách voucher

    // Dành cho trường hợp API trả về thông tin giảm giá cụ thể (như api auto-apply)
    @SerializedName("discountAmount")
    private double discountAmount;

    @SerializedName("bestVoucher")
    private Voucher bestVoucher;

    public String getMessage() { return message; }
    public List<Voucher> getData() { return data; }
    public double getDiscountAmount() { return discountAmount; }
    public Voucher getBestVoucher() { return bestVoucher; }
}