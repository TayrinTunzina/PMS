package com.example.pms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.example.pms.dbconnect.connection;

public class PostController {

    @FXML
    private Label postText;

    @FXML
    private Hyperlink postLink;

    @FXML
    private Label date;

    @FXML
    private ImageView postImage;

    @FXML
    private ImageView deleteIcon;

    @FXML
    private ImageView likeIcon;

    @FXML
    private int postId;

    private NewsfeedController newsfeedController;

    // Other methods and fields

    public void setNewsfeedController(NewsfeedController newsfeedController) {
        this.newsfeedController = newsfeedController;
    }

    private void someMethod() {
        // Call refreshPage from NewsfeedController
        if (newsfeedController != null) {
            newsfeedController.refreshPage();
        }
    }

    @FXML
    private void initialize() {
        String userRole = UserService.getLoggedInUserRole();
        if ("Admin".equals(userRole)) {
            deleteIcon.setVisible(true);
        } else {
            deleteIcon.setVisible(false);
        }
        postLink.setOnAction(event -> {
            // Get the URL from the Hyperlink
            String url = postLink.getText();

            // Open the URL in Chrome
            openChrome(url);
        });

    }


    private void openChrome(String url) {
        try {
            // Specify the path to the Google Chrome executable
            String chromePath = "C:/Program Files/Google/Chrome/Application/chrome.exe";
            // Launch Google Chrome with the URL as an argument
            ProcessBuilder pb = new ProcessBuilder(chromePath, url);
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteIconClick() {
        String userRole = UserService.getLoggedInUserRole();
        if ("Admin".equals(userRole)) {
            deletePost(postId);
        } else {
            System.out.println("Permission denied. Only admins can delete posts.");
        }
    }

    private void deletePost(int postId) {
        try {
            String query = "DELETE FROM post WHERE post_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, postId);
            int rowsAffected = statement.executeUpdate();
            statement.close();

            if (rowsAffected > 0) {
                System.out.println("Post deleted successfully.");
                // Show alert for successful deletion
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Post Deleted");
                alert.setHeaderText(null);
                alert.setContentText("The post has been successfully deleted.");
                alert.showAndWait();

                // Remove the deleted post from the UI
                if (newsfeedController != null) {
                    newsfeedController.removeDeletedPost(postId);
                    newsfeedController.refreshPage(); // Ensure to call refreshPage after removing the post
                }

                // Call someMethod to refresh the page or update UI if needed
                someMethod();
            } else {
                System.out.println("No post was deleted. postId: " + postId);
                someMethod();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error deleting post. postId: " + postId);
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }


    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public void setLiked(boolean liked) {
        String imageUrl = liked ? "/img/red_heart.png" : "/img/heart.png";
        Image image = new Image(getClass().getResource(imageUrl).toExternalForm());
        likeIcon.setImage(image);
    }

    @FXML
    private Label likeCountLabel = new Label("0");

    @FXML
    private void handleLikeButtonClick(MouseEvent event) {
        String loggedInUserId = UserService.getLoggedInUserId();
        System.out.println("Logged-in User ID: " + loggedInUserId); // Debugging line

        // Check if the user has already liked the post
        boolean isLiked = checkIfLiked(loggedInUserId, postId);

        if (isLiked) {
            // User has already liked the post, so remove the like
            unlikePost(loggedInUserId, postId);
            // Update the heart icon to gray
            setLiked(false);
        } else {
            // User has not liked the post, so like the post
            likePost(loggedInUserId, postId);
            // Update the heart icon to red
            setLiked(true);
        }

        // Update the like count label
        updateLikeCountLabel();
    }

    // Method to update the like count label
    private void updateLikeCountLabel() {
        try {
            // Query to get the total likes count for the post
            String query = "SELECT total_likes FROM post WHERE post_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, postId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int totalLikes = resultSet.getInt("total_likes");
                likeCountLabel.setText(String.valueOf(totalLikes));
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }

    // Method to check if the user has already liked the post
    boolean checkIfLiked(String userId, int postId) {
        try {
            String query = "SELECT * FROM likes WHERE post_id = ? AND liked_by = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, postId);
            statement.setString(2, userId);
            ResultSet resultSet = statement.executeQuery();
            boolean isLiked = resultSet.next();
            resultSet.close();
            statement.close();
            return isLiked;
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions
            return false;
        }
    }

    // Method to like the post
    private void likePost(String userId, int postId) {
        try {
            // Insert a new like record into the likes table
            String query = "INSERT INTO likes (post_id, liked_by) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, postId);
            statement.setString(2, userId);
            statement.executeUpdate();
            statement.close();

            // Update the total likes count in the post table
            query = "UPDATE post SET total_likes = total_likes + 1 WHERE post_id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, postId);
            statement.executeUpdate();
            statement.close();

            System.out.println("Post liked successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }

    // Method to unlike the post
    private void unlikePost(String userId, int postId) {
        try {
            // Delete the like record from the likes table
            String query = "DELETE FROM likes WHERE post_id = ? AND liked_by = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, postId);
            statement.setString(2, userId);
            statement.executeUpdate();
            statement.close();

            // Update the total likes count in the post table
            query = "UPDATE post SET total_likes = total_likes - 1 WHERE post_id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, postId);
            statement.executeUpdate();
            statement.close();

            System.out.println("Post unliked successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }

    public void setData(Post post) {
        postText.setText(post.getPostText());
        date.setText(post.getFormattedDate());
        likeCountLabel.setText(String.valueOf(post.getLikeCount()));
        postLink.setText(post.getPostLink());

        try {
            // Convert the BLOB data to Image
            Blob blob = post.getPostImageBlob(); // Assuming you have a method to retrieve BLOB from Post
            if (blob != null) {
                byte[] blobBytes = blob.getBytes(1, (int) blob.length());
                Image image = convertBlobToImage(blobBytes);
                postImage.setImage(image);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            // Handle exceptions
        }

        // Check if the user has already liked the post
        boolean isLiked = checkIfLiked(UserService.getLoggedInUserId(), post.getPostId());
        // Update the heart icon to red if the user has liked the post
        if (isLiked) {
            likeIcon.setImage(new Image(getClass().getResource("/img/red_heart.png").toExternalForm()));
        }
    }

    // Utility method to convert Blob data to Image
    private Image convertBlobToImage(byte[] blobBytes) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(blobBytes);
        return new Image(inputStream);
    }
}
