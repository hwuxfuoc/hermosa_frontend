package com.example.demo.models;

import com.google.gson.annotations.SerializedName;

public class AutoApplyVoucherResponse {
    @SerializedName("bestVoucher")
    private Voucher bestVoucher;

    @SerializedName("discountAmount")
    private long discountAmount;

    // getters
    public Voucher getBestVoucher() { return bestVoucher; }
    public long getDiscountAmount() { return discountAmount; }
}