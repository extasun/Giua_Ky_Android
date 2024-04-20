package com.example.giua_ki.model;

import java.io.Serializable;

public class ProductModel implements Serializable {

    private String name,key;
    private int price;
    private String imageUrl;
    private int discount;

    public ProductModel() {
    }

    public ProductModel(String name, int price, String imageUrl, int discount) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.discount = discount;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getDiscount() {
        return discount;
    }
    public double getFinalPrice() {
        return price * (1 - discount / 100.0);
    }
    public void setDiscount(int discount) {
        this.discount = discount;
    }
}
