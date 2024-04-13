package com.example.pms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectController {

    @FXML
    private VBox card1;

    @FXML
    private ComboBox<String> projectIdComboBox;

    @FXML
    private ComboBox<String> projectIdComboBox2;

    @FXML
    private VBox projectDetailsCard;

    @FXML
    private TextArea projectFeaturesTextArea;

    @FXML
    private TextField reportFileTextField;

    @FXML
    private TextField videoFileTextField;

    @FXML
    private Button checkRequestButton;

    @FXML
    private TextField feedbackTextField;

    @FXML
    private VBox card2;

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
    private TextArea studentTextarea;

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

        // Check if secondCard exists in the loaded FXML file
        if (secondCard != null) {
            secondCard.setVisible(false); // Hide the second card initially
        }

        // Check if projectIdComboBox exists in the loaded FXML file
        if (projectIdComboBox != null) {
            // Populate ComboBox with unique project IDs
            populateComboBox();

            // Set action for the button
            checkRequestButton.setOnAction(event -> handleCheckRequest());
        }

        if (projectIdComboBox2 != null) {
            // Populate ComboBox with unique project IDs
            populateComboBox2();
        }

        if (projectDetailsCard != null) {
            projectDetailsCard.setVisible(false); // Hide the second card initially
        }
    }


    private void populateComboBox2() {
        String loggedInUserId = UserService.getLoggedInUserId();

        try {

            String sql = "SELECT p_id FROM project WHERE s_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, loggedInUserId);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            // Populate the ComboBox with project IDs
            while (resultSet.next()) {
                String projectId = resultSet.getString("p_id");
                projectIdComboBox2.getItems().add(projectId);
            }

            // Close the result set and statement
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    @FXML
    private void onSearchButtonClicked(ActionEvent event) {
        String selectedProjectId = projectIdComboBox2.getValue();

        // Fetch project features based on the selected project ID
        String projectFeatures = fetchProjectFeatures(selectedProjectId);

        // Fetch remarks based on the selected project ID
        String remarks = fetchRemarks(selectedProjectId);

        // Display the project ID
        projectIdTextField.setText(selectedProjectId);

        // Display project features in the projectFeaturesTextArea
        if (projectFeatures != null) {
            projectFeaturesTextArea.setText(projectFeatures);
        } else {
            projectFeaturesTextArea.clear();
        }

        // Display remarks in the feedbackTextField
        if (remarks != null) {
            feedbackTextField.setText(remarks);
        } else {
            feedbackTextField.clear();
        }

        // Show the search results card
        projectDetailsCard.setVisible(true);
    }


    @FXML
    private void onGetFeedbackButtonClicked(ActionEvent event) {
        String projectId = projectIdTextField.getText();
        String projectFeatures = projectFeaturesTextArea.getText();

        // Fetch the file paths or other necessary information from the UI elements
        String projectVideoPath = videoFileTextField.getText(); // Get the path of the project video file
        String projectReportPath = reportFileTextField.getText(); // Get the path of the project report file

        // Read the bytes from the files
        byte[] videoBytes = readBytesFromFile(new File(projectVideoPath));
        byte[] reportBytes = readBytesFromFile(new File(projectReportPath));

        // Update the project table with the new information
        try {
            String sql = "UPDATE project SET video = ?, report = ?, features = ? WHERE p_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setBytes(1, videoBytes);
            statement.setBytes(2, reportBytes);
            statement.setString(3, projectFeatures);
            statement.setString(4, projectId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Feedback Updated", "Feedback updated successfully.");
                clearFieldsAndHideCard();
            } else {
                showAlert(Alert.AlertType.ERROR, "Feedback Update Failed", "Failed to update feedback.");
                clearFieldsAndHideCard();
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }


    @FXML
    private void onChooseReportButtonClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Project Report");

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            reportFileTextField.setText(selectedFile.getAbsolutePath());
            byte[] reportBytes = readBytesFromFile(selectedFile);
        }
    }

    @FXML
    private void onChooseVideoButtonClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Project Video");

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            videoFileTextField.setText(selectedFile.getAbsolutePath());
            byte[] videoBytes = readBytesFromFile(selectedFile);
        }
    }











    private byte[] readBytesFromFile(File file) {
        try {
            Path filePath = file.toPath();
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return null;
        }
    }

    private String fetchRemarks(String projectId) {
        String remarks = "";

        try {
            // Your SQL query to fetch remarks based on the project ID
            String sql = "SELECT remark FROM project WHERE p_id = ?";

            // Create a prepared statement
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, projectId);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            // Check if the result set has data
            if (resultSet.next()) {
                // Retrieve the remarks from the result set
                remarks = resultSet.getString("remark");
            }

            // Close the result set and statement
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

        return remarks;
    }


    // Method to fetch project details based on the project ID
    private String fetchProjectFeatures(String projectId) {
        String features = "";

        try {
            // Your SQL query to fetch project features based on the project ID
            String sql = "SELECT DISTINCT features FROM project WHERE p_id = ?";

            // Create a prepared statement
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, projectId);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            // Check if the result set has data
            if (resultSet.next()) {
                // Retrieve the project features from the result set
                features = resultSet.getString("features");
            }

            // Close the result set and statement
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

        return features;
    }

    private void populateComboBox() {
        Set<String> projectIds = new HashSet<>();
        String loggedInUserId = UserService.getLoggedInUserId(); // Assuming this method gets the logged-in user's ID

        try (PreparedStatement statement = connection.prepareStatement("SELECT DISTINCT p_id FROM request WHERE f_id = ?")) {
            statement.setString(1, loggedInUserId); // Set the value for the parameter
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    projectIds.add(resultSet.getString("p_id"));
                }
            }
            projectIdComboBox.getItems().addAll(projectIds);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleCheckRequest() {
        String selectedProjectId = projectIdComboBox.getValue();

        try {
            // Prepare the SQL statement to fetch details based on the selected project ID
            String sql = "SELECT r.f_id, r.s_id, u.name AS student_name, r.course_name FROM request r " +
                    "JOIN users u ON r.s_id = u.user_id " +
                    "WHERE r.p_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, selectedProjectId);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            // Check if the result set has data
            if (resultSet.next()) {
                // Retrieve the details from the result set for the first row
                String facultyId = resultSet.getString("f_id");
                String courseName = resultSet.getString("course_name");

                // Fetch faculty name
                String facultyName = fetchUserName(facultyId);

                // Initialize a StringBuilder to store all student names and IDs
                StringBuilder studentsInfoBuilder = new StringBuilder();

                // Process the first row
                String studentId = resultSet.getString("s_id");
                String studentName = resultSet.getString("student_name");
                studentsInfoBuilder.append(studentId).append(" - ").append(studentName);

                // Iterate through the remaining rows to fetch student names and IDs
                while (resultSet.next()) {
                    studentId = resultSet.getString("s_id");
                    studentName = resultSet.getString("student_name");
                    studentsInfoBuilder.append("\n").append(studentId).append(" - ").append(studentName);
                }

                // Populate the text fields in card2 with the fetched details
                facultyIdTextField.setText(facultyName);
                courseTextField.setText(courseName);
                projectIdTextField.setText(selectedProjectId);
                studentTextarea.setText(studentsInfoBuilder.toString());

                // Show card2
                card2.setVisible(true);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Data Found", "No details found for the selected project ID.");
            }

            // Close the result set and statement
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }



    // Method to fetch user names based on user IDs
    private String fetchUserName(String userId) throws SQLException {
        String userName = "";
        String sql = "SELECT name FROM users WHERE user_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, userId);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            userName = resultSet.getString("name");
        }
        resultSet.close();
        statement.close();
        return userName;
    }

    @FXML
    private void allocateStudents(ActionEvent event) {
        String projectId = projectIdTextField.getText();
        String courseName = courseTextField.getText();
        String facultyId = UserService.getLoggedInUserId();
        String facultyName = facultyIdTextField.getText();
        String studentsInfo = studentTextarea.getText();

        try {
            // Check if the project ID is already allocated
            if (isProjectAllocated(projectId)) {
                showAlert(Alert.AlertType.WARNING, "Project Already Allocated", "Project ID " + projectId + " is already allocated.");
                // Clear the fields and update the view
                resetFields();
                return; // Exit the method
            }

            // Split the students' info by newline character
            String[] studentsInfoArray = studentsInfo.split("\\n");

            // Prepare the SQL statement to insert data into the project table
            String sql = "INSERT INTO project (p_id, course_name, f_id, f_name, s_id, s_name) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            // Insert each student's info into the project table
            for (String studentInfo : studentsInfoArray) {
                // Split student info into ID and name using "-"
                String[] studentData = studentInfo.split(" - ");
                if (studentData.length == 2) {
                    String studentId = studentData[0].trim(); // Trim to remove leading/trailing whitespace
                    String studentName = studentData[1].trim(); // Trim to remove leading/trailing whitespace

                    statement.setString(1, projectId);
                    statement.setString(2, courseName);
                    statement.setString(3, facultyId);
                    statement.setString(4, facultyName);
                    statement.setString(5, studentId);
                    statement.setString(6, studentName);

                    // Execute the insert statement
                    statement.executeUpdate();
                } else {
                    // Handle the case where student data is not in the expected format
                    // For example, show an error message or skip this student
                    System.err.println("Invalid student data format: " + studentInfo);
                }
            }

            // Update the status column of request table to "allocated" for all related entries
            updateRequestStatus(projectId);

            // Show a confirmation message
            showAlert(Alert.AlertType.CONFIRMATION, "Allocation Success", "Students allocated successfully.");

            // Clear the fields and update the view
            resetFields();

            // Close the statement
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
            showAlert(Alert.AlertType.ERROR, "Allocation Error", "Failed to allocate students.");
        }
    }

    // Method to update the status column of the request table to "allocated"
    private void updateRequestStatus(String projectId) throws SQLException {
        String sqlUpdate = "UPDATE request SET status = 'allocated' WHERE p_id = ?";
        PreparedStatement updateStatement = connection.prepareStatement(sqlUpdate);
        updateStatement.setString(1, projectId);
        updateStatement.executeUpdate();
        updateStatement.close();
    }

    // Method to check if the project ID is already allocated
    private boolean isProjectAllocated(String projectId) throws SQLException {
        String sqlCheck = "SELECT * FROM request WHERE p_id = ? AND status = 'allocated'";
        PreparedStatement checkStatement = connection.prepareStatement(sqlCheck);
        checkStatement.setString(1, projectId);
        ResultSet resultSet = checkStatement.executeQuery();
        boolean allocated = resultSet.next(); // If next() returns true, project is allocated
        resultSet.close();
        checkStatement.close();
        return allocated;
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
        String courseName = courseTextField.getText();

        try {
            // Check if the request already exists
            String checkSql = "SELECT status FROM request WHERE p_id = ? AND s_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);
            checkStatement.setString(1, projectId);
            checkStatement.setString(2, studentId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                String status = resultSet.getString("status");
                if (status.equals("pending")) {
                    // Request already exists and is pending, show alert
                    showAlert(Alert.AlertType.INFORMATION, "Request Already Exists", "A request for this project already exists.");
                    clearFields();
                } else if (status.equals("allocated")) {
                    // Project already allocated, show alert
                    showAlert(Alert.AlertType.ERROR, "Project Already Allocated", "The project is already allocated.");
                    clearFields();
                }
            } else {
                // Insert the data into the request table
                String insertSql = "INSERT INTO request (p_id, course_name, s_id, f_id, status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                insertStatement.setString(1, projectId);
                insertStatement.setString(2, courseName);
                insertStatement.setString(3, studentId);
                insertStatement.setString(4, facultyId);
                insertStatement.setString(5, "pending");
                int rowsAffected = insertStatement.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.CONFIRMATION, "Request Submitted", "Request submitted successfully.");
                    clearFields(); // Clear the text fields after success
                } else {
                    showAlert(Alert.AlertType.ERROR, "Request Failed", "Failed to submit request.");
                }

                insertStatement.close();
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

    // Method to reset fields and update the view
    private void resetFields() {
        projectIdComboBox.getSelectionModel().clearSelection(); // Clear selection in the ComboBox
        projectIdTextField.clear();
        courseTextField.clear();
        facultyIdTextField.clear();
        studentTextarea.clear();
        card2.setVisible(false); // Hide card2
    }

    private void clearFieldsAndHideCard() {
        projectIdComboBox2.getSelectionModel().clearSelection();
        projectIdTextField.clear();
        reportFileTextField.clear();
        videoFileTextField.clear();
        projectFeaturesTextArea.clear();
        projectDetailsCard.setVisible(false);
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
