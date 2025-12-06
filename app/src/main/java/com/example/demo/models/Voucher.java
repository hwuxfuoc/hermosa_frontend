package com.example.demo.models;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class Voucher implements Serializable{
    @SerializedName("voucherCode")
    private String voucherCode;
    @SerializedName("description")
    private String description;
    @SerializedName("discountType")
    private String discountType;
    @SerializedName("discountValue")
    private double discountValue;
    @SerializedName("minPurchaseAmount")
    private double minPurchaseAmount;
    @SerializedName("validFrom")
    private String validFrom;
    @SerializedName("validTo")
    private String validTo;
    @SerializedName("usageLimit")
    private int usageLimit;
    public String getVoucherCode(){return voucherCode;}
    public String getDescription(){return description;}
    public String getDiscountType(){return discountType;}
    public double getDiscountValue(){return discountValue;}
    public double getMinPurchaseAmount(){return minPurchaseAmount;}
    public String getValidFrom(){return validFrom;}
    public String getValidTo(){return validTo;}
    public String getDiscountDisplay(){
        if ("percentage".equals(discountType)) {
            return "Giảm " + (int)discountValue + "%";
        } else {
            return "Giảm " + String.format("%,.0f", discountValue) + "đ";
        }
    }
}