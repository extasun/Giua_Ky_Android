package com.example.giua_ki.model;

import java.util.ArrayList;

public class OrderModel {
    private String orderId;
    private String totalPrice;
    private String dateTime;
    private ArrayList<CartModel> orderDetails;

    // Constructors
    public OrderModel() {
        // Default constructor required for Firebase
    }

    public OrderModel(String orderId, String totalPrice, String dateTime, ArrayList<CartModel> orderDetails) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.dateTime = dateTime;
        this.orderDetails = orderDetails;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public ArrayList<CartModel> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(ArrayList<CartModel> orderDetails) {
        this.orderDetails = orderDetails;
    }
}

