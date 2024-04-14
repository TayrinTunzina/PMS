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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
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

    @FXML
    private ImageView profilePic;

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


    public void removeDeletedPost(int deletedPostId) {
        Iterator<Post> iterator = posts.iterator();
        while (iterator.hasNext()) {
            Post post = iterator.next();
            if (post.getPostId() == deletedPostId) {
                iterator.remove();
                break; // Assuming postId is unique, exit loop after removing the post
            }
        }
    }

    public void refreshPage() {
        // Clear existing posts from UI
        posted.getChildren().clear();

        // Reload the list of posts
        posts.clear();
        posts.addAll(data()); // Fetch updated list of posts from the database
        postContentTextField.clear();
        taFile.clear();

        // Update the UI with the refreshed list of posts
        try {
            for (Post post : posts) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("post.fxml"));

                VBox vBox = fxmlLoader.load();
                PostController postController = fxmlLoader.getController();
                postController.setNewsfeedController(this); // Pass reference to NewsfeedController
                postController.setPostId(post.getPostId()); // Set the postId
                postController.setData(post);
                posted.getChildren().add(vBox);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void setLoggedInUserId(String userId) {
        loggedInUserId = userId;
//        System.out.println("Received user ID in NewsfeedController (setLoggedInUserId): " + loggedInUserId); // Add this line for debugging
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

                // Retrieve the profile picture as a byte array from the database
                byte[] profilePictureBytes = resultSet.getBytes("pic");

                // Check if the profile picture bytes are not null before creating ByteArrayInputStream
                if (profilePictureBytes != null) {
                    // Convert the byte array to an image
                    ByteArrayInputStream bis = new ByteArrayInputStream(profilePictureBytes);
                    Image profilePicture = new Image(bis);
                    profilePic.setImage(profilePicture);
                }

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
        // Ensure that the loggedInUserId is set before initializing
        loggedInUserId = UserService.getLoggedInUserId();
        System.out.println("Received user ID in NewsfeedController (initialize): " + loggedInUserId);

        // Display user details if the user ID is not null
        if (loggedInUserId != null) {
            displayUserDetails();
        }

        posts = new ArrayList<>(data());

        try {
            for (Post post : posts) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("post.fxml"));

                VBox vBox = fxmlLoader.load();
                PostController postController = fxmlLoader.getController();
                postController.setData(post);
                postController.setPostId(post.getPostId()); // Set the postId in the PostController
                postController.setNewsfeedController(this); // Pass reference to NewsfeedController

                posted.getChildren().add(vBox);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public List<Post> data() {
        List<Post> ls = new ArrayList<>();

        try {
            String query = "SELECT * FROM post ORDER BY created DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Post post = new Post();
                post.setPostId(resultSet.getInt("post_id")); // Fetch and set the postId
                post.setPostText(resultSet.getString("content"));
                post.setPostImageBlob(resultSet.getBlob("post_image"));

                // Parse the date string using a DateTimeFormatter
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(resultSet.getString("created"), formatter);
                post.setDates(dateTime);

                post.setLikeCount(resultSet.getInt("total_likes"));
                ls.add(post);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }

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
    void add_component(ActionEvent event) throws IOException {
        System.out.println("Logged-in User ID before navigating to PostComponentController: " + loggedInUserId); // Debug line

        FXMLLoader loader = new FXMLLoader(getClass().getResource("post_component.fxml"));
        Parent root = loader.load();

        // Pass the logged-in user's ID to the next controller
        PostComponentController controller = loader.getController();
        controller.setLoggedInUserId(loggedInUserId);

        // Set the scene
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void request(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("request.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void allocate(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("allocate.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void sendProject(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("sendProject.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void getProject(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("getProject.fxml"));
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