package com.example.pms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import static com.example.pms.dbconnect.connection;

public class PostController {

    @FXML
    private Label postText;

    @FXML
    private Label date;

    @FXML
    private ImageView postImage;

    @FXML
    private ImageView deleteIcon;

    @FXML
    private ImageView likeIcon;

    @FXML
    private Label likeCountLabel = new Label("0");

    @FXML
    private void handleLikeButtonClick(MouseEvent event) {
        String loggedInUserId = UserService.getLoggedInUserId();
        System.out.println("Logged-in User ID: " + loggedInUserId); // Debugging line

        int currentLikes = Integer.parseInt(likeCountLabel.getText());
        currentLikes++; // Increment the like count
        likeCountLabel.setText(String.valueOf(currentLikes)); // Update the label

        // Update the like count in the associated Post object
        Post post = getAssociatedPost(); // Implement this method to get the associated Post object
        post.setLikeCount(currentLikes);

        String imageUrl = "/img/red_heart.png";
        likeIcon.setImage(new Image(getClass().getResource(imageUrl).toExternalForm()));

        // Debugging line
        System.out.println("Updating post with ID: " + post.getPostId() + ", Likes: " + currentLikes + ", Liked by: " + loggedInUserId);

        // Store the like information in the database
        try {
            String query = "UPDATE post SET like_count = ?, liked_by = ? WHERE post_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, currentLikes);
            statement.setString(2, loggedInUserId); // Use the logged-in user ID
            statement.setInt(3, post.getPostId()); // Assuming you have a method to get the post ID
            statement.executeUpdate();
            statement.close();
            System.out.println("Post updated successfully."); // Debugging line
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }



    public Post getAssociatedPost() {
        Post post = new Post();
        return post;
    }

    public void setData(Post post) {
        postText.setText(post.getPostText());
        date.setText(post.getFormattedDate());
        likeCountLabel.setText(String.valueOf(post.getLikeCount()));

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
    }

    // Utility method to convert Blob data to Image
    private Image convertBlobToImage(byte[] blobBytes) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(blobBytes);
        return new Image(inputStream);
    }
}
