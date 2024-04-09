package com.example.pms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LabApplication extends Application {
    public static final String CURRENCY = "Tk. ";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LabApplication.class.getResource("lab.fxml"));
        Scene scene1 = new Scene(fxmlLoader.load(), 1278, 750);
        stage.setTitle("Lab Components");
        stage.setScene(scene1);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
