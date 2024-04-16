package com.example.pms;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UsersController {
    @FXML
    private TextField id;

    @FXML
    private TextField name;

    @FXML
    private ComboBox<String> cbRole;

    @FXML
    private TextField email;

    @FXML
    private TextField password;

    private Connection connection;

    @FXML
    private TableView<User> userTableView;

    @FXML
    private TableColumn<User, Integer> incrementColumn;

    @FXML
    private TableColumn<User, String> userIdColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    private int rowCount = 0;

    public void initialize() {
        connection = dbconnect.getConnection();
        initializeColumns();

        cbRole.setItems(FXCollections.observableArrayList("Faculty", "Student"));

        userTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Populate fields with selected user's information
                id.setText(newSelection.getUserId());
                name.setText(newSelection.getName());
                email.setText(newSelection.getEmail());

                // Fetch password from the database based on user_id
                String userId = newSelection.getUserId();
                String passwordFromDB = fetchPasswordFromDB(userId);
                password.setText(passwordFromDB);

                // Set the role from the selected button
                String selectedRole = cbRole.getValue();
                if (selectedRole != null) {
                    cbRole.getSelectionModel().select(selectedRole);
                }
            }
        });
    }

    // Method to fetch password from the database based on user_id
    private String fetchPasswordFromDB(String userId) {
        String password = ""; // Default value
        try {
            String sql = "SELECT password FROM users WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                password = resultSet.getString("password");
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while fetching the password.");
        }
        return password;
    }

    // Initialize table columns
    private void initializeColumns() {
        incrementColumn.setCellValueFactory(cellData -> cellData.getValue().incrementProperty().asObject());
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        // Set a row factory to update increment values
        userTableView.setRowFactory(tableView -> {
            final TableRow<User> row = new TableRow<>();
            row.itemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue != oldValue) {
                    newValue.setIncrement(++rowCount);
                }
            });
            return row;
        });
    }

    @FXML
    void onFacultyButtonClicked(ActionEvent event) {
        String selectedRole = "Faculty";
        cbRole.setValue(selectedRole);
        fetchUsersByRole(selectedRole);
    }

    // Event handler for "Students" button
    @FXML
    void onStudentsButtonClicked(ActionEvent event) {
        String selectedRole = "Student";
        cbRole.setValue(selectedRole);
        fetchUsersByRole(selectedRole);
    }

    // Fetch users based on their role and populate the table view
    private void fetchUsersByRole(String role) {
        ObservableList<User> userList = FXCollections.observableArrayList();
        rowCount = 0; // Reset row count before fetching users

        try {
            String sql = "SELECT * FROM users WHERE role = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, role);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String userId = resultSet.getString("user_id");
                String userName = resultSet.getString("name");
                String userEmail = resultSet.getString("email");

                // Set the increment to 0 for each user fetched
                userList.add(new User(0, userId, userName, userEmail));
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while fetching users by role.");
        }

        userTableView.setItems(userList);
    }


    // User class to represent user data
    public class User {
        private final SimpleIntegerProperty increment;
        private final SimpleStringProperty userId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty email;

        private final SimpleStringProperty role;
        private final SimpleStringProperty password;

        public User(Integer increment, String userId, String name, String email) {
            this.increment = new SimpleIntegerProperty(increment);
            this.userId = new SimpleStringProperty(userId);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.role = new SimpleStringProperty("");
            this.password = new SimpleStringProperty("");
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public void setEmail(String email) {
            this.email.set(email);
        }

        public Integer getIncrement() {
            return increment.get();
        }

        public void setIncrement(Integer increment) {
            this.increment.set(increment);
        }

        public SimpleIntegerProperty incrementProperty() {
            return increment;
        }

        public String getUserId() {
            return userId.get();
        }

        public SimpleStringProperty userIdProperty() {
            return userId;
        }

        public String getName() {
            return name.get();
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public String getEmail() {
            return email.get();
        }

        public SimpleStringProperty emailProperty() {
            return email;
        }
        public String getRole() {
            return role.get();
        }

        public void setRole(String role) {
            this.role.set(role);
        }

        public SimpleStringProperty roleProperty() {
            return role;
        }

        public String getPassword() {
            return password.get();
        }

        public void setPassword(String password) {
            this.password.set(password);
        }

        public SimpleStringProperty passwordProperty() {
            return password;
        }
    }

    // Save user data to the database
    @FXML
    void onSaveButton(ActionEvent event) {
        String userId = id.getText();
        String userRole = cbRole.getValue();
        String userName = name.getText();
        String userEmail = email.getText();
        String userPassword = password.getText();

        if (userId.isEmpty() || userRole == null || userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        // Insert user data into the users table
        try {
            String sql = "INSERT INTO users (user_id, role, name, email, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, userRole);
            statement.setString(3, userName);
            statement.setString(4, userEmail);
            statement.setString(5, userPassword);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User data saved successfully.");
                clearFields();

                // Create a new User instance and add it to the TableView's items list
                User newUser = new User(0, userId, userName, userEmail);
                userTableView.getItems().add(newUser);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save user data.");
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving user data.");
        }
    }

    // Update user data in the database
    @FXML
    void onUpdateButton(ActionEvent event) {
        // Get the selected user from the table view
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a user to update.");
            return;
        }

        // Retrieve updated data from text fields and combo box
        String userId = id.getText();
        String userName = name.getText();
        String userEmail = email.getText();
        String userRole = cbRole.getValue();
        String userPassword = password.getText();

        if (userId.isEmpty() || userName.isEmpty() || userEmail.isEmpty() || userRole == null || userPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        // Update user data in the database
        try {
            String sql = "UPDATE users SET name = ?, email = ?, role = ?, password = ? WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userName);
            statement.setString(2, userEmail);
            statement.setString(3, userRole);
            statement.setString(4, userPassword);
            statement.setString(5, userId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User data updated successfully.");
                // Update the table view with the updated data
                selectedUser.setName(userName);
                selectedUser.setEmail(userEmail);
                selectedUser.setRole(userRole);
                selectedUser.setPassword(userPassword);
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
    void onDeleteButton(ActionEvent event) {
        // Get the selected user from the table view
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a user to delete.");
            return;
        }

        // Confirm deletion with a dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete this user?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete user data from the database
            try {
                String sql = "DELETE FROM users WHERE user_id = ?";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, selectedUser.getUserId());

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User data deleted successfully.");
                    // Remove the deleted user from the table view
                    userTableView.getItems().remove(selectedUser);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user data.");
                }
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting user data.");
            }
        }
    }

    @FXML
    void onClearButton(ActionEvent event){
        clearFields();
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

    // Utility method to show alerts
    @SuppressWarnings("static-access")
    public void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Utility method to clear input fields
    private void clearFields() {
        id.clear();
        name.clear();
        cbRole.getSelectionModel().clearSelection();
        email.clear();
        password.clear();
    }

}