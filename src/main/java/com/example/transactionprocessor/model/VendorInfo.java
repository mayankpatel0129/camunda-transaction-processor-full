package com.example.transactionprocessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VendorInfo {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("merchantId")
    private String merchantId;
    
    @JsonProperty("category")
    private String category;
    
    public VendorInfo() {}
    
    public VendorInfo(String name, String location, String merchantId, String category) {
        this.name = name;
        this.location = location;
        this.merchantId = merchantId;
        this.category = category;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    @Override
    public String toString() {
        return "VendorInfo{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}