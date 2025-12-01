/*
package com.example.demo.models;

import java.util.ArrayList;
import java.util.List;

public class AddressResponse {
    private String message;
    private List<AddressData> data;

    public List<AddressData> getData() { return data; }
    public String getMessage() { return message; }

    // Class con mô tả 1 địa chỉ trả về từ Server
    public static class AddressData {
        public String addressID;
        public String name;
        public String phone;
        public String type;
        public AddressDetail addressDetail;

        public String getFullAddress() {
            if (addressDetail == null) return "";
            StringBuilder sb = new StringBuilder();
            if (addressDetail.getStreet() != null && !addressDetail.getStreet().isEmpty()) {
                sb.append(addressDetail.getStreet());
            }
            if (addressDetail.getWard() != null && !addressDetail.getWard().isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(addressDetail.getWard());
            }
            if (addressDetail.getDistrict() != null && !addressDetail.getDistrict().isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(addressDetail.getDistrict());
            }
            if (addressDetail.getCity() != null && !addressDetail.getCity().isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(addressDetail.getCity());
            }

            return sb.toString();
        }
    }
}*/
package com.example.demo.models;

import java.util.List;

public class AddressResponse {
    private String message;
    private List<AddressData> data;

    public List<AddressData> getData() { return data; }
    public String getMessage() { return message; }

    // Class con mô tả 1 địa chỉ trả về từ Server
    public static class AddressData {
        public String addressID;
        public String name;
        public String phone;
        public String type;

        // --- SỬA QUAN TRỌNG: Đổi thành List để hứng mảng [] từ Server ---
        public List<AddressDetail> addressDetail;
        // ----------------------------------------------------------------

        public String getFullAddress() {
            // Kiểm tra null và rỗng vì bây giờ nó là List
            if (addressDetail == null || addressDetail.isEmpty()) {
                return "";
            }

            // Lấy phần tử đầu tiên trong mảng ra để xử lý
            AddressDetail detail = addressDetail.get(0);

            StringBuilder sb = new StringBuilder();
            if (detail.getStreet() != null && !detail.getStreet().isEmpty()) {
                sb.append(detail.getStreet());
            }
            if (detail.getWard() != null && !detail.getWard().isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(detail.getWard());
            }
            if (detail.getDistrict() != null && !detail.getDistrict().isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(detail.getDistrict());
            }
            if (detail.getCity() != null && !detail.getCity().isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(detail.getCity());
            }

            return sb.toString();
        }
    }
}