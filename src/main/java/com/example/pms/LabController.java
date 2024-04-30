package com.example.pms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import java.awt.Desktop;


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

    @FXML
    private TextField searchField;
    private Lab chosenComponent;

    private String loggedInUserId;

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loggedInUserId = UserService.getLoggedInUserId();
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

    @FXML
    void searchComponents(ActionEvent event) {
        // Fetch components based on the search query
        components = fetchComponentsFromDatabase();

        // Clear the existing components from the grid
        grid.getChildren().clear();

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

        try {
            connection = dbconnect.getConnection();

            String query = "SELECT components.*, users.email " +
                    "FROM components " +
                    "LEFT JOIN users ON components.s_id = users.user_id " +
                    "WHERE users.user_id IS NULL OR users.user_id != ?";


            // Check if there is a search query
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                query += " AND components.name LIKE ?";
            }

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, loggedInUserId);

            // If search query is present, set the parameter
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                statement.setString(2, "%" + searchField.getText() + "%");
            }

            ResultSet resultSet = statement.executeQuery();

            int colorIndex = 0;
            String[] colorScheme = {"6A7324", "A7745B", "F16C31", "291D36", "22371D", "FB5D03", "80080C", "FFB605", "5F060E", "E7C00F"}; // Provided color scheme

            while (resultSet.next()) {
                Lab component = new Lab();
                component.setSellerId(resultSet.getString("s_id"));
                component.setName(resultSet.getString("name"));
                component.setPrice(resultSet.getString("price"));
                component.setSellerEmail(resultSet.getString("email"));
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

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return components;
    }


    private void setChosenComponent(Lab component) {
        chosenComponent = component;
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

    @FXML
    private void mailSeller(ActionEvent event) {
        // Get the Lab component associated with the button that triggered the event
        Lab component = chosenComponent;
        // Check if a component is selected
        if (component != null) {
            // Get the seller's email from the component
            String sellerEmail = component.getSellerEmail();
            // Encode component name and price
            String encodedComponentName = encodeValue(component.getName());
            String encodedPrice = encodeValue(component.getPrice());
            // Construct Gmail URL with encoded subject and body
            String subject = "Regarding Component Sale: " + component.getName();
            String body = "Hello,\n\nI am interested in purchasing the following component:\n\n"
                    + "Component Name: " + component.getName() + "\n"
                    + "Price: " + component.getPrice() + "\n\n"
                    + "Please let me know the availability and any further details.\n\n"
                    + "Thank you.\n";
            String encodedSubject = encodeValue(subject);
            String encodedBody = encodeValue(body);
            String gmailUrl = "https://mail.google.com/mail/?view=cm&fs=1&to=" + sellerEmail
                    + "&su=" + encodedSubject + "&body=" + encodedBody;
            // Debugging line to print the seller's email
            System.out.println("Seller's email: " + sellerEmail);
            // Open default web browser with the constructed Gmail URL
            openChrome(gmailUrl);
        } else {
            // Handle the case when no component is selected
            System.out.println("No component selected.");
        }
    }

    // Method to encode special characters in a string
    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    // Method to open default web browser with a specified URL
    // Method to open Google Chrome with a specified URL
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

    // Method to open default email client with a specified mailto URI
    private void openDefaultEmailClient(String mailtoUri) {
        try {
            Desktop.getDesktop().mail(new URI(mailtoUri));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
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

