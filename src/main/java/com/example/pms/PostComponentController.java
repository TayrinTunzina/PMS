package com.example.pms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.*;
import java.sql.*;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PostComponentController {

    private Connection connection;
    private String loggedInUserId;

    @FXML
    private TextField productNameField;

    @FXML
    private TextField productPriceField;

    @FXML
    private Button addProductButton;

    @FXML
    private TableView<?> productTableView;

    @FXML
    private Button chooseFileButton;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField fpath;
    private File selectedImageFile;

    @FXML
    private TableView<Component> componentTableView;

    @FXML
    private TableColumn<Component, String> productNameColumn;

    @FXML
    private TableColumn<Component, String> productPriceColumn;

    @FXML
    private TableColumn<Component, Image> productImageColumn;

    private ObservableList<Component> componentList;

    public void setLoggedInUserId(String userId) {
        loggedInUserId = userId;
    }

    @FXML
    private TableColumn<Component, String> actionColumn;

    // Method to delete a component from the list and database
    private void deleteComponent(Component component) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM components WHERE name = ? AND price = ?")) {
            statement.setString(1, component.getProductName());
            statement.setString(2, component.getProductPrice());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                componentList.remove(component);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the component.");
        }
    }


    @FXML
    private void chooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            fpath.setText(selectedImageFile.getAbsolutePath());

            // Load the selected image into the ImageView
            Image image = new Image(selectedImageFile.toURI().toString());
            imageView.setImage(image);
        }
    }

    // Add action event handler for the "Add Product" button
    @FXML
    void initialize() {
        // Establish database connection when the controller is initialized
        connection = dbconnect.getConnection();

        // Initialize the TableView and its columns
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        productImageColumn.setCellValueFactory(new PropertyValueFactory<>("productImage"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));

        // Center align the content of the columns in the TableView
        productNameColumn.setStyle("-fx-alignment: CENTER;");
        productPriceColumn.setStyle("-fx-alignment: CENTER;");
        productImageColumn.setStyle("-fx-alignment: CENTER;");
        actionColumn.setStyle("-fx-alignment: CENTER;");

        // Set up the action column
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white; -fx-font-weight: bold;");
                deleteButton.setOnAction(event -> {
                    Component component = getTableView().getItems().get(getIndex());
                    deleteComponent(component);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Customize the cell factory to display images in the table
        productImageColumn.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(item);
                    imageView.setFitHeight(50); // Adjust image height as needed
                    imageView.setFitWidth(50);  // Adjust image width as needed
                    setGraphic(imageView);
                }
            }
        });

        // Initialize the list to hold components
        componentList = FXCollections.observableArrayList();

        // Load components for the logged-in user
        loadComponentsForUser();
    }

    private void loadComponentsForUser() {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM components WHERE s_id = ?")) {
            statement.setString(1, UserService.getLoggedInUserId());

            try (ResultSet resultSet = statement.executeQuery()) {
                componentList.clear();

                while (resultSet.next()) {
                    String productName = resultSet.getString("name");
                    String productPrice = resultSet.getString("price");
                    Blob blob = resultSet.getBlob("image");
                    Image image = null;

                    if (blob != null) {
                        try (InputStream inputStream = blob.getBinaryStream()) {
                            image = new Image(inputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    componentList.add(new Component(productName, productPrice, image));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load components.");
        }

        componentTableView.setItems(componentList);
    }

    @FXML
    void addProduct(ActionEvent event) {
        // Get the logged-in user's ID
        String userId = UserService.getLoggedInUserId();

        // Retrieve product details from input fields
        String productName = productNameField.getText();
        String productPrice = productPriceField.getText();

        // Validate input fields
        if (productName.isEmpty() || productPrice.isEmpty() || selectedImageFile == null) {
            // Show error message to the user
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all the fields and choose an image.");
            return;
        }

        // Store product details in the database
        storeProductDetails(userId, productName, productPrice, selectedImageFile);

        // Clear input fields
        productNameField.clear();
        productPriceField.clear();
        selectedImageFile = null; // Clear selected image file as well
        fpath.clear(); // Clear the fpath text field
        imageView.setImage(null); // Reset the ImageView to display no image
        selectedImageFile = null; // Clear selected image file as well

        // Load components for the logged-in user
        loadComponentsForUser();
    }

    private void storeProductDetails(String userId, String productName, String productPrice, File productImageFile) {
        try {
            // Prepare SQL statement
            String sql = "INSERT INTO components (s_id, name, price, image) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, productName);
            statement.setString(3, productPrice);

            // Read the image file into a FileInputStream
            FileInputStream inputStream = new FileInputStream(productImageFile);
            // Set the BLOB parameter
            statement.setBlob(4, inputStream);

            // Execute the statement
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new product was inserted successfully!");
                // Show success message to the user
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully.");
            } else {
                // Show error message to the user
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add the product.");
            }

            // Close the input stream (but not the connection)
            inputStream.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            // Handle exceptions
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the product.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private Stage stage;
    private Scene scene;

    @FXML
    void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("newsfeed_student.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}

