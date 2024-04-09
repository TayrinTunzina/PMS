package com.example.pms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NewsfeedApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NewsfeedController.class.getResource("Newsfeed.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 750);
        stage.setTitle("Newsfeed");
        stage.setScene(scene);
        stage.show();
    }
}
