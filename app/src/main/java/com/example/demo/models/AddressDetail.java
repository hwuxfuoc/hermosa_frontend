package com.example.demo.models;

public class AddressDetail {
    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;

    public String getStreet() { return street; }
    public String getWard() { return ward; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public AddressDetail(String street, String ward, String district, String city, String country) {
        this.street = street;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.country = country;
    }

}