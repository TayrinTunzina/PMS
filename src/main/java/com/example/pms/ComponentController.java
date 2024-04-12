package com.example.pms;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.util.Objects;

public class ComponentController {
    @FXML
    private Label nameLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private ImageView img;

    @FXML
    private void click(MouseEvent mouseEvent) {
        myListener.onClickListener(component);
    }

    private Lab component;
    private MyListener myListener;
    public void setData(Lab component, MyListener myListener) {
        this.component = component;
        this.myListener = myListener;
        nameLabel.setText(component.getName());
        priceLabel.setText(LabApplication.CURRENCY + component.getPrice());
        Image image = component.getImage();
        if (image != null) {
            img.setImage(image);
        } else {
            // Handle the case where the image is null
            // For example, set a default image
            // img.setImage(defaultImage);
        }
    }

}

