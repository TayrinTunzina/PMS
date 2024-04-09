package com.example.pms;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    private String loggedInUserId;

    @FXML
    private Label ErrorMassageLabel;

    @FXML
    private ComboBox<String> role;

    @FXML
    private TextField txt_id;

    @FXML
    private PasswordField txt_password;

    private final String DB_URL = "jdbc:mysql://localhost:3306/pms";
    private final String USER = "root";
    private final String PASSWORD = "";

    private Connection connection;

    public LoginController() {
        connection = dbconnect.getConnection();
    }

    @FXML
    void login() {
        try {
            String query = "SELECT * FROM users WHERE user_id=? AND password=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, txt_id.getText());
            statement.setString(2, txt_password.getText());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String userRole = resultSet.getString("role");
                if (userRole.equals(role.getValue())) {
                    openDashboard(userRole);
                } else {
                    ErrorMassageLabel.setText("Invalid role for this user.");
                }
            } else {
                ErrorMassageLabel.setText("Invalid credentials.");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ErrorMassageLabel.setText("Error connecting to database.");
        }
    }

    private void openDashboard(String userRole) {
        try {
            String query = "SELECT * FROM users WHERE user_id=? AND password=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, txt_id.getText());
            statement.setString(2, txt_password.getText());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                loggedInUserId = resultSet.getString("user_id");
                // Redirect to the dashboard
                redirectToDashboard(userRole);
            } else {
                ErrorMassageLabel.setText("Invalid credentials.");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            ErrorMassageLabel.setText("Error connecting to database.");
        }
    }


    private void redirectToDashboard(String userRole) throws IOException {
        System.out.println("Redirecting to dashboard with user ID: " + loggedInUserId); // Add this line for debugging
        String fxmlFile;
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

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        NewsfeedController controller = loader.getController();

        // Set the logged-in user ID using UserService
        UserService.setLoggedInUserId(loggedInUserId);

        // Pass logged-in user ID to NewsfeedController using UserService
        controller.setLoggedInUserId(loggedInUserId);

        // Call displayUserDetails directly
        controller.displayUserDetails();

        Stage stage = (Stage) txt_id.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }


}
