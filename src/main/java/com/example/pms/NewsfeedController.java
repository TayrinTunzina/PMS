package com.example.pms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class NewsfeedController implements Initializable {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    private Connection connection;
    private String loggedInUserId;

    @FXML
    private VBox posted;
    List<Post> posts;

    @FXML
    private Button choosefilebtn;

    @FXML
    private TextField taFile;

    @FXML
    private Button postButton;

    @FXML
    private TextField postContentTextField;

    @FXML
    private void handlechoosefilebtn(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            taFile.setText(selectedFile.getAbsolutePath());
//            taFile.appendText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handlePostButton(ActionEvent event) {
        String postContent = postContentTextField.getText();
        String filePath = taFile.getText();

        // Validate that both content and file path are provided
        if (postContent.isEmpty() || filePath.isEmpty()) {
            // Show error message to the user
            // For example, you can use ErrorMassageLabel.setText("Please enter post content and choose a file.");
            return;
        }

        // Insert the post data into the database
        // Insert the post data into the database
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            String query = "INSERT INTO post (post_image, content) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setBinaryStream(1, fis, new File(filePath).length()); // Set the image data as a BLOB
            statement.setString(2, postContent);
            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                // Post inserted successfully
                // Show a success message to the user
                showAlert(Alert.AlertType.INFORMATION, "Success", "Post inserted successfully.");
                // Refresh the page
                refreshPage();
            } else {
                // Failed to insert the post
                // Show an error message to the user
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to insert the post.");
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., display an error message)
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // Handle file not found exception
        } catch (IOException e) {
            e.printStackTrace();
            // Handle IO exception
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void refreshPage() {
        // You need to reload the data or reset the UI components here
        // For example, you can clear the text fields and reset the file chooser
        postContentTextField.clear();
        taFile.clear();
        // You may also reload the list of posts or update the UI in any other way
    }


    public void setLoggedInUserId(String userId) {
        loggedInUserId = userId;
        System.out.println("Received user ID in NewsfeedController: " + loggedInUserId); // Add this line for debugging
    }

    public void displayUserDetails() {
        try {
            String query = "SELECT * FROM users WHERE user_id=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, loggedInUserId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String userName = resultSet.getString("user_id");
                String role = resultSet.getString("role");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");

                // Set the retrieved user details to the labels
                userNameLabel.setText(userName);
                roleLabel.setText(role);
                nameLabel.setText(name);
                emailLabel.setText(email);

                // Log the retrieved user details
                System.out.println("User details retrieved successfully:");
                System.out.println("User ID: " + userName);
                System.out.println("Role: " + role);
                System.out.println("Name: " + name);
                System.out.println("Email: " + email);
            } else {
                System.out.println("No user found with user ID: " + loggedInUserId);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error retrieving user details: " + e.getMessage()); // Add error message logging
            // Handle the exception
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        connection = dbconnect.getConnection();
        System.out.println("Received user ID in NewsfeedController: " + loggedInUserId);
        displayUserDetails();

        posts = new ArrayList<>(data());

        try {
            for (Post post : posts) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("post.fxml"));

                VBox vBox = fxmlLoader.load();
                PostController postController = fxmlLoader.getController();
                postController.setData(post);
                posted.getChildren().add(vBox);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Post> data(){
        List<Post> ls = new ArrayList<>();
        Post post;

        post = new Post();
        post.setPostText("CSE project show 23");
        post.setPostImageSrc("/img/post2.jpg");
        post.setDates("4 DAYS AGO");
        post.setNbLikes("20");
//        post.setNbComments("5");
        ls.add(post);

        post = new Post();
        post.setPostText("CSE project show 22");
        post.setPostImageSrc("/img/post1.jpg");
        post.setDates("1 WEEK AGO");
        post.setNbLikes("55");
//        post.setNbComments("10");
        ls.add(post);

        return ls;
    }

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    void lab_component(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("lab.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void users(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("users.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void courses(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("courses.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void add_winners(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("addWinners.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
