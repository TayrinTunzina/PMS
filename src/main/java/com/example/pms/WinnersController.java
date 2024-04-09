package com.example.pms;

import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class WinnersController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private StackPane frontPane;
    @FXML
    private StackPane backPane;

    public void foldCard() {
        RotateTransition frontRotate = new RotateTransition(Duration.seconds(1), frontPane);
        frontRotate.setAxis(javafx.geometry.Point3D.ZERO);
        frontRotate.setByAngle(-90); // Change rotation angle to -90 for folding
        frontRotate.play();

        RotateTransition backRotate = new RotateTransition(Duration.seconds(1), backPane);
        backRotate.setAxis(javafx.geometry.Point3D.ZERO);
        backRotate.setByAngle(-90); // Change rotation angle to -90 for folding
        backRotate.play();
    }

    @FXML
    void newsfeed(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Newsfeed.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void viewWinners(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("winners.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
