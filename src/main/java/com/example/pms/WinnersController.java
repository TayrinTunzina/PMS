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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;

public class WinnersController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField projectIdTextField;

    @FXML
    private TextField trimesterTextField;
    @FXML
    private TextField nameTextField;

    @FXML
    private ComboBox<String> projectIdComboBox;
    @FXML
    private ComboBox<String> positionComboBox;
    private Connection connection;

    @FXML
    private TextField searchTextField;

    @FXML
    private ComboBox<String> projectIdComboBox1;

    @FXML
    private TextField searchTextField1;

    @FXML
    private TableView<WinnerDetails> tableView;

    @FXML
    private TableColumn<WinnerDetails, String> projectIdColumn;

    @FXML
    private TableColumn<WinnerDetails, String> trimesterColumn;

    @FXML
    private TableColumn<WinnerDetails, String> positionColumn;

    @FXML
    private TableColumn<WinnerDetails, String> teamNameColumn;

    @FXML
    private TableColumn<WinnerDetails, String> courseNameColumn;

    @FXML
    private TableColumn<WinnerDetails, String> facultyNameColumn;

    @FXML
    private TableColumn<WinnerDetails, String> studentNameColumn;



    // Initialize method
    public void initialize() {
        connection = dbconnect.getConnection();

        // Check if projectIdComboBox exists in the loaded FXML file
        if (projectIdComboBox != null && searchTextField != null) {
            // Populate ComboBox with unique project IDs
            populateComboBox();

            // Add search functionality
            addSearchFunctionality();
        }

        if (projectIdComboBox1 != null && searchTextField1 != null) {
            // Populate ComboBox with unique project IDs
            populateComboBox2();

            // Add search functionality
            addSearchFunctionality2();
        }

        // Set cell value factories for table columns
        projectIdColumn.setCellValueFactory(new PropertyValueFactory<>("projectID"));
        trimesterColumn.setCellValueFactory(new PropertyValueFactory<>("trimester"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        teamNameColumn.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        facultyNameColumn.setCellValueFactory(new PropertyValueFactory<>("facultyName"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
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
    private void onSearchButtonClicked(ActionEvent event) {
        String selectedProjectId = projectIdComboBox.getValue();

        // Display the project ID
        projectIdTextField.setText(selectedProjectId);
    }


    @FXML
    private void onAddWinnersButtonClicked(ActionEvent event) {
        String projectId = projectIdTextField.getText();
        String trimester = trimesterTextField.getText();
        String teamName = nameTextField.getText();
        String position = positionComboBox.getValue();

        if (projectId.isEmpty() || trimester.isEmpty() || teamName.isEmpty() || position == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        try {
            // Create the SQL query
            String sql = "INSERT INTO winners (project_id, trimester, team_name, position) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, projectId);
            statement.setString(2, trimester);
            statement.setString(3, teamName);
            statement.setString(4, position);

            // Execute the query
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Winners added successfully.");
                // Refresh the UI
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add winners.");
            }

            // Close the statement
            statement.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showAlert(Alert.AlertType.WARNING, "Duplicate Entry", "A winner with the same details already exists.");
            } else {
                e.printStackTrace(); // Handle the exception appropriately
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding winners.");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding winners.");
        }
    }


    // Method to clear the input fields after successful addition
    private void clearFields() {
        projectIdTextField.clear();
        trimesterTextField.clear();
        nameTextField.clear();
        positionComboBox.getSelectionModel().clearSelection();
    }









    private void populateComboBox2() {
        try {
            String sql = "SELECT project_id FROM winners";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            // Populate the ComboBox with project IDs
            while (resultSet.next()) {
                String projectId = resultSet.getString("project_id");
                projectIdComboBox1.getItems().add(projectId);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    private void addSearchFunctionality2() {
        // Wrap the existing items in a FilteredList
        FilteredList<String> filteredList = new FilteredList<>(FXCollections.observableArrayList(projectIdComboBox1.getItems()));

        // Bind the predicate of the filtered list to the text property of the search text field
        searchTextField1.textProperty().addListener((observable, oldValue, newValue) -> {
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
        projectIdComboBox1.setItems(filteredList);
    }






    @FXML
    void onSearchButtonClicked2(ActionEvent event) {
        String selectedProjectId = projectIdComboBox1.getValue();
        populateTableView(selectedProjectId);
    }


    public class WinnerDetails {
        private String projectID;
        private String trimester;
        private String position;
        private String teamName;
        private String courseName;
        private String facultyName;
        private String studentName;

        public WinnerDetails(String projectID, String trimester, String position, String teamName, String courseName, String facultyName, String studentName) {
            this.projectID = projectID;
            this.trimester = trimester;
            this.position = position;
            this.teamName = teamName;
            this.courseName = courseName;
            this.facultyName = facultyName;
            this.studentName = studentName;
        }

        // Getters and setters
        public String getProjectID() {
            return projectID;
        }

        public void setProjectID(String projectID) {
            this.projectID = projectID;
        }

        public String getTrimester() {
            return trimester;
        }

        public void setTrimester(String trimester) {
            this.trimester = trimester;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getFacultyName() {
            return facultyName;
        }

        public void setFacultyName(String facultyName) {
            this.facultyName = facultyName;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }
    }




    private void populateTableView(String projectId) {
        // Clear the existing items in the table view
        tableView.getItems().clear();

        try {
            // Create the SQL query to fetch the details
            String sql = "SELECT w.project_id, w.trimester, w.position, w.team_name, p.course_name, p.f_name, p.s_name " +
                    "FROM winners w " +
                    "INNER JOIN project p ON w.project_id = p.p_id " +
                    "WHERE w.project_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, projectId);
            ResultSet resultSet = statement.executeQuery();

            // Populate the table view with the fetched details
            while (resultSet.next()) {
                String projectID = resultSet.getString("project_id");
                String trimester = resultSet.getString("trimester");
                String position = resultSet.getString("position");
                String teamName = resultSet.getString("team_name");
                String courseName = resultSet.getString("course_name");
                String facultyName = resultSet.getString("f_name");
                String studentName = resultSet.getString("s_name");

                // Create WinnerDetails object
                WinnerDetails winnerDetails = new WinnerDetails(projectID, trimester, position, teamName, courseName, facultyName, studentName);

                // Add the winnerDetails to the table view
                tableView.getItems().add(winnerDetails);
            }

            // Close the result set and statement
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }


























    @SuppressWarnings("static-access")
    public void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
