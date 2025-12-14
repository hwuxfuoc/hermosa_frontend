package com.example.demo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CartResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private Data data;
    @SerializedName("price")
    private Long price;

    public Long getPrice() {
        return price != null ? price : 0L;
    }

    public String getStatus() {
        return status;
    }

    public Data getData() {
        return data;
    }

    public static class Data {

        @SerializedName("items")
        private List<CartItem> items = new ArrayList<>();

        @SerializedName("totalMoney")
        private long totalMoney = 0;

        public List<CartItem> getItems() {
            return items != null ? items : new ArrayList<>();
        }

        public long getTotalMoney() {
            return totalMoney;
        }
    }

    public static class CartItem implements Parcelable {

        @SerializedName("_id")
        private String id;

        @SerializedName("productID")
        private String productID;

        @SerializedName("name")
        private String name;

        @SerializedName("price")
        private long price;

        @SerializedName("picture")
        private String picture;

        @SerializedName("category")
        private String category;

        @SerializedName("quantity")
        private int quantity = 1;

        @SerializedName("subtotal")
        private int subtotal = 0;

        @SerializedName("size")
        private String size;

        @SerializedName("topping")
        private List<String> topping;

        @SerializedName("note")
        private String note;

        private boolean selected = true;

        public String getId() {
            return id;
        }

        public String getProductID() {
            return productID;
        }

        public String getName() {
            return name != null ? name : "Không rõ";
        }

        public long getPrice() {
            return price;
        }

        public String getPicture() {
            return picture;
        }

        public String getCategory() {
            return category;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getSubtotal() {
            return subtotal;
        }

        public String getSize() {
            return size != null ? size : "medium";
        }

        public List<String> getTopping() {
            return topping != null ? topping : new ArrayList<>();
        }

        public String getNote() {
            return note != null ? note : "";
        }

        public boolean isSelected() {
            return selected;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public void setSubtotal(int subtotal) {
            this.subtotal = subtotal;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public void setTopping(List<String> topping) {
            this.topping = topping;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        protected CartItem(Parcel in) {
            id = in.readString();
            productID = in.readString();
            name = in.readString();
            price = in.readLong();
            picture = in.readString();
            category = in.readString();
            quantity = in.readInt();
            subtotal = in.readInt();
            size = in.readString();
            topping = in.createStringArrayList();
            note = in.readString();
            selected = in.readByte() != 0;
        }

        public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
            @Override
            public CartItem createFromParcel(Parcel in) {
                return new CartItem(in);
            }

            @Override
            public CartItem[] newArray(int size) {
                return new CartItem[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(productID);
            dest.writeString(name);
            dest.writeLong(price);
            dest.writeString(picture);
            dest.writeString(category);
            dest.writeInt(quantity);
            dest.writeInt(subtotal);
            dest.writeString(size);
            dest.writeStringList(topping);
            dest.writeString(note);
            dest.writeByte((byte) (selected ? 1 : 0));
        }
    }
}