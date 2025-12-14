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

    public List<SuggestionItem> getData() {
        if (data != null) {
            for (SuggestionItem item : data) {
                item.extractMapboxData();
            }
        }
        return data;
    }

    public static class SuggestionItem {
        @SerializedName("id")
        public String id;

        @SerializedName("text")
        public String text;

        @SerializedName("place_name")
        public String place_name;

        @SerializedName("place_name_vi")
        public String place_name_vi;

        @SerializedName("center")
        public List<Double> center;

        @SerializedName("context")
        public List<ContextItem> context;

        public String name;
        public String street;
        public String ward;
        public String district;
        public String city;
        public double lat;
        public double lon;

        public void extractMapboxData() {
            this.name = (place_name_vi != null && !place_name_vi.isEmpty()) ? place_name_vi : place_name;

            if (center != null && center.size() >= 2) {
                this.lon = center.get(0);
                this.lat = center.get(1);
            }

            this.street = text;

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

    public static class ContextItem {
        @SerializedName("id")
        public String id;
        @SerializedName("text")
        public String text;
        @SerializedName("text_vi")
        public String text_vi;
    }
}