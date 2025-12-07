package com.example.demo.models;

import java.util.ArrayList;
import java.util.List;

public class ProductData {
    private static final List<Product> allProducts = new ArrayList<>();
    public static final List<Product> cartList = new ArrayList<>();

    public static List<Product> getCartList() {
        return cartList;
    }

    public static List<Product> getAllProducts() {
        return allProducts;
    }

    public static List<Product> getProductsByCategory(String category) {
        List<Product> filteredList = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getCategory() != null &&
                    p.getCategory().equalsIgnoreCase(category != null ? category : "")) {
                filteredList.add(p);
            }
        }
        return filteredList;
    }
}