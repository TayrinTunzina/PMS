package com.example.pms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectController {

    @FXML
    private ComboBox<String> courseComboBox;

    @FXML
    private ComboBox<String> fidComboBox;

    private Connection connection;

    @FXML
    private VBox secondCard;

    @FXML
    private TextField facultyIdTextField;

    @FXML
    private TextField facultyNameTextField;

    @FXML
    private TextField courseTextField;

    @FXML
    private TextField projectIdTextField;

    @FXML
    private Button requestButton;

    @FXML
    private Button seeDetailsButton;

    // Initialize method to establish database connection and populate courseComboBox
    @FXML
    public void initialize() {
        connection = dbconnect.getConnection();
        secondCard.setVisible(false); // Hide the second card initially
    }

    @FXML
    private void showDetails(ActionEvent event) {
        // Show the second card
        secondCard.setVisible(true);

        // Get the selected faculty name and course name from the combo boxes
        String selectedFacultyName = fidComboBox.getValue();
        String selectedCourseName = courseComboBox.getValue();
        System.out.println("Selected Faculty Name: " + selectedFacultyName);
        System.out.println("Selected Course Name: " + selectedCourseName);

        // Populate details in the text fields
        populateDetails(selectedFacultyName, selectedCourseName);
    }



    private void populateDetails(String selectedFacultyName, String selectedCourseName) {
        try {
            // Prepare the SQL statement
            String sql = "SELECT c.f_id, u.name, c.course_name FROM course c JOIN users u ON c.f_id = u.user_id WHERE u.name = ? AND c.course_name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, selectedFacultyName);
            statement.setString(2, selectedCourseName);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            // Populate text fields with the fetched data
            if (resultSet.next()) {
                String facultyId = resultSet.getString("f_id");
                String facultyName = resultSet.getString("name");
                String courseName = resultSet.getString("course_name");

                System.out.println("Faculty ID: " + facultyId);
                System.out.println("Faculty Name: " + facultyName);
                System.out.println("Course Name: " + courseName);

                // Set the values to the text fields
                facultyIdTextField.setText(facultyId);
                facultyNameTextField.setText(facultyName);
                courseTextField.setText(courseName);
            } else {
                System.out.println("No data found for faculty name: " + selectedFacultyName + " and course name: " + selectedCourseName);
            }

            // Close the connections
            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }


    // Event handler for courseComboBox selection change
    @FXML
    private void onCourseSelected() {
        String selectedCourse = courseComboBox.getValue();
        if (selectedCourse != null) {
            populateFidComboBox(selectedCourse);
        }
    }

    // Method to populate fidComboBox with F_ID values based on selected course name
    private void populateFidComboBox(String courseName) {
        List<String> nameList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT u.name FROM course c JOIN users u ON c.f_id = u.user_id WHERE c.course_name = ?")) {
            statement.setString(1, courseName); // Set the value for the parameter
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    nameList.add(resultSet.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ObservableList<String> items = FXCollections.observableArrayList(nameList);
        fidComboBox.setItems(items);
    }

    @FXML
    private void onRequestButtonClicked(ActionEvent event) {
        String projectId = projectIdTextField.getText();
        String studentId = UserService.getLoggedInUserId(); // Assuming this method gets the logged-in user's ID
        String facultyId = facultyIdTextField.getText(); // Fetching faculty ID from the textField

        try {
            // Check if the request already exists
            String checkSql = "SELECT * FROM request WHERE p_id = ? AND s_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);
            checkStatement.setString(1, projectId);
            checkStatement.setString(2, studentId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                // Request already exists, show alert
                showAlert(Alert.AlertType.INFORMATION, "Request Already Exists", "A request for this project already exists.");
            } else {
                // Insert the data into the request table
                String sql = "INSERT INTO request (p_id, s_id, f_id) VALUES (?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, projectId);
                statement.setString(2, studentId);
                statement.setString(3, facultyId);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.CONFIRMATION, "Request Submitted", "Request submitted successfully.");
                    clearFields(); // Clear the text fields after success
                } else {
                    showAlert(Alert.AlertType.ERROR, "Request Failed", "Failed to submit request.");
                }

                statement.close();
            }
            checkStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    // Method to show an alert
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to clear the text fields, combo box, and hide the second card
    private void clearFields() {
        projectIdTextField.clear();
        fidComboBox.getSelectionModel().clearSelection(); // Clear combo box selection
        courseComboBox.getSelectionModel().clearSelection(); // Clear combo box selection
        secondCard.setVisible(false); // Hide the second card
        // Clear other text fields as needed
    }




    private Stage stage;
    private Scene scene;
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
