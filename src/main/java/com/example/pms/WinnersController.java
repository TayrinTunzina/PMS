package com.example.pms;

import javafx.animation.RotateTransition;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WinnersController {

    private Stage stage;
    private Scene scene;
    private Parent root;


    @FXML
    private ComboBox<String> projectIdComboBox;

    private Connection connection;


    @FXML
    private TextField searchTextField;

    // Initialize method
    public void initialize() {
        connection = dbconnect.getConnection();

        // Check if projectIdComboBox exists in the loaded FXML file
        if (projectIdComboBox != null) {
            // Populate ComboBox with unique project IDs
            populateComboBox();

            // Add search functionality
            addSearchFunctionality();
        }
    }

    private void populateComboBox() {
        try {
            String sql = "SELECT DISTINCT p_id FROM project";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            // Populate the ComboBox with project IDs
            while (resultSet.next()) {
                String projectId = resultSet.getString("p_id");
                projectIdComboBox.getItems().add(projectId);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    private void addSearchFunctionality() {
        // Wrap the existing items in a FilteredList
        FilteredList<String> filteredList = new FilteredList<>(FXCollections.observableArrayList(projectIdComboBox.getItems()));

        // Bind the predicate of the filtered list to the text property of the search text field
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(item -> {
                // If the search text is empty, show all items
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Convert both the item and search text to lowercase for case-insensitive search
                String lowerCaseFilter = newValue.toLowerCase();

                if (item.toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Show item if it contains the search text
                }

                return false; // Hide item otherwise
            });
        });

        // Set the items of the ComboBox to the filtered list
        projectIdComboBox.setItems(filteredList);
    }













    @FXML
    void viewWinners(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("winners.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void back(ActionEvent event) throws IOException {
        String fxmlFile;
        // Get the role of the logged-in user from UserService
        String userRole = UserService.getLoggedInUserRole();

        // Determine the appropriate FXML file based on the user role
        switch (userRole) {
            case "Admin":
                fxmlFile = "Newsfeed.fxml";
                break;
            case "Student":
                fxmlFile = "newsfeed_student.fxml";
                break;
            case "Faculty":
                fxmlFile = "newsfeed_faculty.fxml";
                break;
            default:
                // Handle unknown role
                return;
        }

        // Load the FXML file and navigate to it
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
