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
}