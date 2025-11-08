package com.example.demo.utils;

import java.text.DecimalFormat;

public class MoneyHelper {
    public static String formatMoney(long amount) {
        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
        return formatter.format(amount);
    }

    public static String formatMoneyNoVND(long amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }
}