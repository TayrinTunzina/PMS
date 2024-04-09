package com.example.pms;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.Objects;

public class PostController {

    @FXML
    private Label postText;

    @FXML
    private Label date;

    @FXML
    private Label likes;

    @FXML
    private ImageView postImage;

    @FXML
    private ImageView deleteIcon;

    public void setData(Post post){
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(post.getPostImageSrc())));

        postText.setText(post.getPostText());
        postImage.setImage(image);

        date.setText(post.getDates());
        likes.setText(post.getNbLikes());
    }
}
