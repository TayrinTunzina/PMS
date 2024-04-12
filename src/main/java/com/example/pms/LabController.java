package com.example.pms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class LabController implements Initializable {
    @FXML
    private VBox chosenCompCard;

    @FXML
    private Label compNameLabel;

    @FXML
    private Label compPriceLabel;

    @FXML
    private Label sellerId;

    @FXML
    private ImageView compImg;

    @FXML
    private ScrollPane scroll;

    @FXML
    private GridPane grid;

    private Connection connection;

    private List<Lab> components = new ArrayList<>();
    private Image image;
    private MyListener myListener;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Establish database connection
        connection = dbconnect.getConnection();

        // Fetch components from the database
        components = fetchComponentsFromDatabase();

        // Set up the chosen component and listener
        if (!components.isEmpty()) {
            setChosenComponent(components.get(0));
            myListener = component -> setChosenComponent(component);
        }

        int column = 0;
        int row = 1;
        try {
            for (Lab component : components) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("component.fxml"));
                AnchorPane anchorPane = fxmlLoader.load();

                ComponentController componentController = fxmlLoader.getController();
                componentController.setData(component, myListener);

                if (column == 3) {
                    column = 0;
                    row++;
                }

                grid.add(anchorPane, column++, row); //(child,column,row)
                //set grid width
                grid.setMinWidth(Region.USE_COMPUTED_SIZE);
                grid.setPrefWidth(Region.USE_COMPUTED_SIZE);
                grid.setMaxWidth(Region.USE_PREF_SIZE);

                //set grid height
                grid.setMinHeight(Region.USE_COMPUTED_SIZE);
                grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
                grid.setMaxHeight(Region.USE_PREF_SIZE);

                GridPane.setMargin(anchorPane, new Insets(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Lab> fetchComponentsFromDatabase() {
        List<Lab> components = new ArrayList<>();

        // Example: Fetch components from a database
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM components");
             ResultSet resultSet = statement.executeQuery()) {

            int colorIndex = 0;
            String[] colorScheme = {"6A7324", "A7745B", "F16C31", "291D36", "22371D", "FB5D03", "80080C", "FFB605", "5F060E", "E7C00F"}; // Provided color scheme

            while (resultSet.next()) {
                Lab component = new Lab();
                component.setSellerId(resultSet.getString("s_id"));
                component.setName(resultSet.getString("name"));
                component.setPrice(resultSet.getString("price"));
                // Retrieve the image data as a Blob
                Blob blob = resultSet.getBlob("image");
                if (blob != null) {
                    try (InputStream inputStream = blob.getBinaryStream()) {
                        Image image = new Image(inputStream);
                        component.setImage(image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Assign a color to the component
                component.setColor(colorScheme[colorIndex % colorScheme.length]);
                colorIndex++;
                components.add(component);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return components;
    }



    private void setChosenComponent(Lab component) {
        compNameLabel.setText(component.getName());
        compPriceLabel.setText(LabApplication.CURRENCY + component.getPrice());
        sellerId.setText(component.getSellerId());
        Image image = component.getImage();
        if (image != null) {
            compImg.setImage(image);
        } else {
            // Handle case when image is not available
            // For example, set a default image
            // compImg.setImage(defaultImage);
        }

        // Assign colors to components
        String color = component.getColor();
        if (color != null && !color.isEmpty()) {
            chosenCompCard.setStyle("-fx-background-color: #" + color + ";\n" +
                    "    -fx-background-radius: 30;");
        } else {
            // If color is not available, set a default background color
            chosenCompCard.setStyle("-fx-background-color: #FFFFFF;\n" +
                    "    -fx-background-radius: 30;");
        }
    }




    private Stage stage;
    private Scene scene;
    private Parent root;

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

