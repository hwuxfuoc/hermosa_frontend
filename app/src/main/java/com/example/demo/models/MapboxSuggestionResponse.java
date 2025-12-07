/*
package com.example.demo.models;

import java.util.List;

public class MapboxSuggestionResponse {
    private String message;
    private List<SuggestionItem> data;

    public String getMessage() { return message; }
    public List<SuggestionItem> getData() { return data; }

    public static class SuggestionItem {
        public String name;
        public String street;
        public String ward;
        public String district;
        public String city;
        public String country;
        public double lat;
        public double lon;

        // Getter nếu cần
    }
}*/
package com.example.demo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MapboxSuggestionResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<SuggestionItem> data;

    public String getMessage() {
        return message;
    }

    // Khi gọi getData, ta sẽ kích hoạt hàm parse để điền dữ liệu vào các biến phẳng (lat, lon, street...)
    public List<SuggestionItem> getData() {
        if (data != null) {
            for (SuggestionItem item : data) {
                item.extractMapboxData();
            }
        }
        return data;
    }

    public static class SuggestionItem {
        // --- CÁC TRƯỜNG MAP VỚI JSON (BACKEND TRẢ VỀ) ---
        @SerializedName("id")
        public String id;

        @SerializedName("text")
        public String text; // Tên địa điểm (VD: Đường số 7)

        @SerializedName("place_name")
        public String place_name; // Địa chỉ full tiếng Anh/Gốc

        @SerializedName("place_name_vi") // <--- KHẮC PHỤC LỖI CỦA BẠN TẠI ĐÂY
        public String place_name_vi;

        @SerializedName("center")
        public List<Double> center; // [kinh_độ, vĩ_độ]

        @SerializedName("context")
        public List<ContextItem> context; // Chứa Quận, Phường, Tỉnh...

        // --- CÁC TRƯỜNG PHẲNG ĐỂ ACTIVITY DÙNG (Sẽ được tự động điền) ---
        // (Không có @SerializedName vì JSON không có trực tiếp, ta tự tính toán)
        public String name;
        public String street;
        public String ward;
        public String district;
        public String city;
        public double lat;
        public double lon;

        // --- HÀM TỰ ĐỘNG TÁCH DỮ LIỆU TỪ MAPBOX ---
        public void extractMapboxData() {
            // 1. Lấy tên hiển thị ưu tiên tiếng Việt
            this.name = (place_name_vi != null && !place_name_vi.isEmpty()) ? place_name_vi : place_name;

            // 2. Tách tọa độ từ mảng center [lng, lat]
            if (center != null && center.size() >= 2) {
                this.lon = center.get(0);
                this.lat = center.get(1);
            }

            // 3. Tên đường (thường là text)
            this.street = text;

            // 4. Duyệt mảng context để tìm Phường, Quận, Tỉnh
            // Mapbox trả về context từ nhỏ đến lớn: Neighborhood -> Locality -> Place -> Country
            if (context != null) {
                for (ContextItem ctx : context) {
                    if (ctx.id.startsWith("neighborhood")) {
                        this.ward = ctx.text_vi != null ? ctx.text_vi : ctx.text;
                    } else if (ctx.id.startsWith("locality") || ctx.id.startsWith("district")) {
                        this.district = ctx.text_vi != null ? ctx.text_vi : ctx.text;
                    } else if (ctx.id.startsWith("place")) {
                        this.city = ctx.text_vi != null ? ctx.text_vi : ctx.text;
                    }
                }
            }
        }
    }

    // Class phụ để hứng dữ liệu trong mảng "context"
    public static class ContextItem {
        @SerializedName("id")
        public String id;
        @SerializedName("text")
        public String text;
        @SerializedName("text_vi")
        public String text_vi;
    }
}