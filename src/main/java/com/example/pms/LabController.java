package com.example.pms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class LabController implements Initializable {
    @FXML
    private VBox chosenFruitCard;

    @FXML
    private Label fruitNameLable;

    @FXML
    private Label fruitPriceLabel;

    @FXML
    private ImageView fruitImg;

    @FXML
    private ScrollPane scroll;

    @FXML
    private GridPane grid;

    private List<Lab> fruits = new ArrayList<>();
    private Image image;
    private MyListener myListener;

    private List<Lab> getData() {
        List<Lab> fruits = new ArrayList<>();
        Lab fruit;

        fruit = new Lab();
        fruit.setName("Arduino Uno");
        fruit.setPrice(200);
        fruit.setImgSrc("/img/arduino_uno.png");
        fruit.setColor("6A7324");
        fruits.add(fruit);

        fruit = new Lab();
        fruit.setName("RFID Module");
        fruit.setPrice(80);
        fruit.setImgSrc("/img/RFID.png");
        fruit.setColor("A7745B");
        fruits.add(fruit);

        fruit = new Lab();
        fruit.setName("Breadboard");
        fruit.setPrice(90);
        fruit.setImgSrc("/img/breadboard.png");
        fruit.setColor("F16C31");
        fruits.add(fruit);

        fruit = new Lab();
        fruit.setName("Servo Motor");
        fruit.setPrice(20);
        fruit.setImgSrc("/img/servo_motor.png");
        fruit.setColor("291D36");
        fruits.add(fruit);

        fruit = new Lab();
        fruit.setName("Jumper Wires");
        fruit.setPrice(40);
        fruit.setImgSrc("/img/jumper_wires.png");
        fruit.setColor("22371D");
        fruits.add(fruit);

        fruit = new Lab();
        fruit.setName("Resistor");
        fruit.setPrice(20);
        fruit.setImgSrc("/img/resistor.png");
        fruit.setColor("FB5D03");
        fruits.add(fruit);

        fruit = new Lab();
        fruit.setName("Capacitor");
        fruit.setPrice(60);
        fruit.setImgSrc("/img/capacitors.png");
        fruit.setColor("80080C");
        fruits.add(fruit);

        fruit = new Lab();
        fruit.setName("LCD Display");
        fruit.setPrice(100);
        fruit.setImgSrc("/img/I2C-LCD.png");
        fruit.setColor("FFB605");
        fruits.add(fruit);

        fruit = new Lab();
        fruit.setName("I2C Module");
        fruit.setPrice(200);
        fruit.setImgSrc("/img/I2c-Display-Module.png");
        fruit.setColor("5F060E");
        fruits.add(fruit);

        fruit = new Lab();
        fruit.setName("LED Lights");
        fruit.setPrice(20);
        fruit.setImgSrc("/img/LEDs.png");
        fruit.setColor("E7C00F");
        fruits.add(fruit);

        return fruits;
    }

    private void setChosenFruit(Lab fruit) {
        fruitNameLable.setText(fruit.getName());
        fruitPriceLabel.setText(LabApplication.CURRENCY + fruit.getPrice());
        image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(fruit.getImgSrc())));
        fruitImg.setImage(image);
        chosenFruitCard.setStyle("-fx-background-color: #" + fruit.getColor() + ";\n" +
                "    -fx-background-radius: 30;");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fruits.addAll(getData());
        if (!fruits.isEmpty()) {
            setChosenFruit(fruits.getFirst());
            myListener = new MyListener() {
                @Override
                public void onClickListener(Lab fruit) {
                    setChosenFruit(fruit);
                }
            };
        }
        int column = 0;
        int row = 1;
        try {
            for (Lab fruit : fruits) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("component.fxml"));
                AnchorPane anchorPane = fxmlLoader.load();

                ComponentController componentController = fxmlLoader.getController();
                componentController.setData(fruit, myListener);

                if (column == 3) {
                    column = 0;
                    row++;
                }

                grid.add(anchorPane, column++, row); //(child,column,row)
                //set grid width
                grid.setMinWidth(Region.USE_COMPUTED_SIZE);
                grid.setPrefWidth(Region.USE_COMPUTED_SIZE);
                grid.setMaxWidth(Region.USE_PREF_SIZE);

                //set grid height
                grid.setMinHeight(Region.USE_COMPUTED_SIZE);
                grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
                grid.setMaxHeight(Region.USE_PREF_SIZE);

                GridPane.setMargin(anchorPane, new Insets(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    void newsfeed(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Newsfeed.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}

