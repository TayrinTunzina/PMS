package com.example.pms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class EditProfileController {

    @FXML
    private TextField userNameField;

    @FXML
    private Label roleLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    private Connection connection;

    @FXML
    private ImageView profilePicImageView;

    private byte[] profilePictureBytes;

    // Constructor
    public EditProfileController() {
    }

    // Method to initialize the controller
    @FXML
    public void initialize() {
        connection = dbconnect.getConnection(); // Assuming dbconnect is your database connection class

        // Fetch and display the user's details
        fetchUserDetails();
    }

    // Method to fetch user details from the database
    private void fetchUserDetails() {
        try {
            String query = "SELECT * FROM users WHERE user_id=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, UserService.getLoggedInUserId()); // Assuming UserService provides the logged-in user's ID
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String userName = resultSet.getString("user_id");
                String role = resultSet.getString("role");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                Blob profilePicBlob = resultSet.getBlob("pic"); // Get the profile picture as a Blob

                // Set the fetched details to the text fields
                userNameField.setText(userName);
                roleLabel.setText(role);
                nameField.setText(name);
                emailField.setText(email);

                // Set the profile picture from Blob to ImageView
                if (profilePicBlob != null) {
                    byte[] profilePicBytes = profilePicBlob.getBytes(1, (int) profilePicBlob.length());
                    Image profilePic = new Image(new ByteArrayInputStream(profilePicBytes));
                    profilePicImageView.setImage(profilePic);
                }
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }

    // Method to handle the update button action
    @FXML
    private void handleUpdateButtonAction() {
        String userName = userNameField.getText();
        String role = roleLabel.getText();
        String name = nameField.getText();
        String email = emailField.getText();

        try {
            String query;
            PreparedStatement statement;
            int rowsUpdated;

            // Update user data excluding profile picture
            if (profilePictureBytes == null) {
                query = "UPDATE users SET role=?, name=?, email=? WHERE user_id=?";
                statement = connection.prepareStatement(query);
                statement.setString(1, role);
                statement.setString(2, name);
                statement.setString(3, email);
                statement.setString(4, userName);
                rowsUpdated = statement.executeUpdate();
            } else {
                // Update user data including profile picture
                query = "UPDATE users SET role=?, name=?, email=?, pic=? WHERE user_id=?";
                statement = connection.prepareStatement(query);
                statement.setString(1, role);
                statement.setString(2, name);
                statement.setString(3, email);
                statement.setBytes(4, profilePictureBytes);
                statement.setString(5, userName);
                rowsUpdated = statement.executeUpdate();
            }

            if (rowsUpdated > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User data updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user data.");
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating user data.");
        }
    }

    @FXML
    private void handleChooseFileButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            // Load the selected image into the ImageView
            Image selectedImage = new Image(selectedFile.toURI().toString());
            profilePicImageView.setImage(selectedImage);

            try {
                // Convert the selected image file to a byte array
                FileInputStream fis = new FileInputStream(selectedFile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    bos.write(buf, 0, readNum);
                }
                profilePictureBytes = bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception
            }
        }
    }



    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Stage stage;
    private Scene scene;
    @FXML
    void back(ActionEvent event) throws IOException {
        String fxmlFile;
        // Get the role of the logged-in user from UserService
        String userRole = UserService.getLoggedInUserRole();

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
