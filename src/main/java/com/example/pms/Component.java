package com.example.pms;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

public class Component {
    private String productName;
    private String productPrice;
    private Image productImage;
    private SimpleStringProperty action;

    public Component(String productName, String productPrice, Image productImage) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImage = productImage;
        this.action = new SimpleStringProperty("Delete");
    }


    public SimpleStringProperty actionProperty() {
        return action;
    }

    public String getAction() {
        return action.get();
    }

    public void setAction(String action) {
        this.action.set(action);
    }

    public String getProductName() {
        return productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public Image getProductImage() {
        return productImage;
    }
}
