package com.example.giua_ki.model;


public class SizeModel {
    private String size = "";
    private double price = 0.0;

    public SizeModel() {}

    public SizeModel(String size, double price) {
        this.size = size;
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getPrice() {
        return (int) price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}