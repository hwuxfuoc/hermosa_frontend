package com.example.demo.utils;

import android.content.Context;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ImageUrlHelper {

    private static Map<String, String> imageUrlMap = null;

    public static void init(Context context) {
        if (imageUrlMap != null) return; // Đã khởi tạo rồi

        imageUrlMap = new HashMap<>();
        try {
            InputStream is = context.getAssets().open("product_images.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);
            Iterator<String> keys = obj.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                String url = obj.getString(key);
                imageUrlMap.put(key, url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getImageUrl(String productID) {
        if (imageUrlMap == null || productID == null) return "";
        return imageUrlMap.getOrDefault(productID, "");
    }
}