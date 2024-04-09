package com.example.pms;

import javafx.event.ActionEvent;
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
    private Label priceLable;

    @FXML
    private ImageView img;

    @FXML
    private void click(MouseEvent mouseEvent) {
        myListener.onClickListener(fruit);
    }

    private Lab fruit;
    private MyListener myListener;

    public void setData(Lab fruit, MyListener myListener) {
        this.fruit = fruit;
        this.myListener = myListener;
        nameLabel.setText(fruit.getName());
        priceLable.setText(LabApplication.CURRENCY + fruit.getPrice());
        Image image = new Image(Objects.requireNonNull(ComponentController.class.getResourceAsStream(fruit.getImgSrc())));
        img.setImage(image);
    }
}

