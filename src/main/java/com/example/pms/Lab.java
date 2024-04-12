package com.example.pms;

import javafx.scene.image.Image;

public class Lab {
    private String name;
    private String imgSrc;
    private String price;
    private String color;
    private Image image;

    private String sellerId;

    private String email;

    // Getters and setters for the email field
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImage(Image image) {
        this.image = image;
    }
    public Image getImage() {
        return image;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgSrc() {
        return this.imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getPrice() {
        return this.price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}

