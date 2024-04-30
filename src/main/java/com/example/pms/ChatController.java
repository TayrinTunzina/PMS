package com.example.pms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatController {

    @FXML
    private VBox userContainer;

    private Connection connection;

    @FXML
    private TextField searchField;

    private String loggedInUserId;

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
    }

    public void initialize() {
        // Fetch users and display them in the VBox
        fetchAndDisplayUsers();
    }

    @FXML
    void searchUser(ActionEvent event) {
        // Fetch users based on the search query
        fetchAndDisplayUsers();
    }

    private void fetchAndDisplayUsers() {
        try {
            // Fetch the logged-in user's ID as a String
            loggedInUserId = UserService.getLoggedInUserId();
            connection = dbconnect.getConnection();

            String query = "SELECT " +
                    "CASE WHEN ? = f_id THEN s_id ELSE f_id END AS user_id, " +
                    "CASE WHEN ? = f_id THEN s_name ELSE f_name END AS user_name " +
                    "FROM project WHERE ? IN (f_id, s_id)";

            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                query += " AND (s_id = ? OR s_name LIKE ? OR f_id = ? OR f_name LIKE ?)";
            }

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, loggedInUserId);
            preparedStatement.setString(2, loggedInUserId);
            preparedStatement.setString(3, loggedInUserId);

            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchQuery = searchField.getText();
                preparedStatement.setString(4, searchQuery);
                preparedStatement.setString(5, "%" + searchQuery + "%");
                preparedStatement.setString(6, searchQuery);
                preparedStatement.setString(7, "%" + searchQuery + "%");
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            userContainer.getChildren().clear(); // Clear existing users before adding new ones

            while (resultSet.next()) {
                String userId = resultSet.getString("user_id");
                String userName = resultSet.getString("user_name");

                // Create a label to display user ID and name
                Label userLabel = new Label(userId + " - " + userName);
                userLabel.setFont(Font.font("Arial", 12));
                userLabel.setOnMouseClicked(event -> {
                    // Handle click event to start a chat with this user
                    startChatWithUser(userId);
                });

                // Add the label to the VBox
                userContainer.getChildren().add(userLabel);

                // Add a separator after each user
                Separator separator = new Separator(Orientation.HORIZONTAL);
                userContainer.getChildren().add(separator);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void startChatWithUser(String userId) {
        // Implement logic to start a chat with the selected user
    }

    @FXML
    void closeChat(ActionEvent event) {
        // Get the stage associated with the close button's event
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Close the stage
        stage.close();
    }
}
