package com.example.pms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class newsfeed_fApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NewsfeedController.class.getResource("newsfeed_faculty.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1300, 750);
        stage.setTitle("Newsfeed");
        stage.setScene(scene);
        stage.show();
    }
}
