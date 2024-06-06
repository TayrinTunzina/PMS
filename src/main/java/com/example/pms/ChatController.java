package com.example.pms;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatController {

    @FXML
    private VBox userContainer;
    @FXML
    private VBox messageContainer;
    private Connection connection;

    @FXML
    private TextField searchField;

    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;

    private String loggedInUserId;

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
    }

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String clientId;

    @FXML
    public void initialize() {
        fetchAndDisplayUsers();
        connectToServer();
    }

    private void connectToServer() {
        try {
            // Connect to the server
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected to server"); // Debugging line
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    private Label currentlySelectedUserLabel; // To keep track of the currently selected user label

    private void fetchAndDisplayUsers() {
        try {
            // Fetch the logged-in user's ID as a String
            loggedInUserId = UserService.getLoggedInUserId();
            connection = dbconnect.getConnection();

            String query = "SELECT " +
                    "CASE WHEN ? = f_id THEN s_id ELSE f_id END AS user_id, " +
                    "CASE WHEN ? = f_id THEN s_name ELSE f_name END AS user_name " +
                    "FROM project WHERE ? IN (f_id, s_id)";

            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                query += " AND (s_id = ? OR s_name LIKE ? OR f_id = ? OR f_name LIKE ?)";
            }

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, loggedInUserId);
            preparedStatement.setString(2, loggedInUserId);
            preparedStatement.setString(3, loggedInUserId);

            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchQuery = searchField.getText();
                preparedStatement.setString(4, searchQuery);
                preparedStatement.setString(5, "%" + searchQuery + "%");
                preparedStatement.setString(6, searchQuery);
                preparedStatement.setString(7, "%" + searchQuery + "%");
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            userContainer.getChildren().clear(); // Clear existing users before adding new ones

            while (resultSet.next()) {
                String userId = resultSet.getString("user_id");
                String userName = resultSet.getString("user_name");

                // Create a label to display user ID and name
                Label userLabel = new Label(userId + " - " + userName);
                userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                userLabel.setTextAlignment(TextAlignment.JUSTIFY);
                userLabel.setStyle("-fx-background-color: #E9EBEE; -fx-padding: 8px; -fx-border-radius: 15px; -fx-background-radius: 15px;");
                userLabel.setMinWidth(156); // Ensure full text is visible

                userLabel.setOnMouseClicked(event -> {
                    // Handle click event to start a chat with this user
                    startChatWithUser(userId);
                    // Update the styles to highlight the selected user
                    if (currentlySelectedUserLabel != null) {
                        currentlySelectedUserLabel.setStyle("-fx-background-color: #E9EBEE; -fx-padding: 8px; -fx-border-radius: 15px; -fx-background-radius: 15px;");
                    }
                    userLabel.setStyle("-fx-background-color: #4b7de7; -fx-text-fill: #FFFFFF; -fx-padding: 8px; -fx-border-radius: 15px; -fx-background-radius: 15px;");
                    currentlySelectedUserLabel = userLabel;
                });

                // Add the label to the VBox
                userContainer.getChildren().add(userLabel);

                // Add a separator after each user
                Separator separator = new Separator(Orientation.HORIZONTAL);
                userContainer.getChildren().add(separator);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void startChatWithUser(String userId) {
        try {
            // Fetch the details of the selected user
            String loggedInUserId = UserService.getLoggedInUserId();
            String[] loggedInUserName = new String[1]; // Single-element array to hold the logged-in user's name

            // Fetch the receiver's name from the database
            String receiverName = ""; // Variable to hold the receiver's name

            Connection connection = dbconnect.getConnection();

            // Retrieve the logged-in user's name from the database
            String nameQuery = "SELECT name FROM users WHERE user_id = ?";
            PreparedStatement nameStatement = connection.prepareStatement(nameQuery);
            nameStatement.setString(1, loggedInUserId);
            ResultSet nameResult = nameStatement.executeQuery();

            if (nameResult.next()) {
                loggedInUserName[0] = nameResult.getString("name"); // Assign logged-in user's name to array
            }

            // Fetch the receiver's name from the database
            String receiverNameQuery = "SELECT name FROM users WHERE user_id = ?";
            PreparedStatement receiverNameStatement = connection.prepareStatement(receiverNameQuery);
            receiverNameStatement.setString(1, userId);
            ResultSet receiverNameResult = receiverNameStatement.executeQuery();

            if (receiverNameResult.next()) {
                receiverName = receiverNameResult.getString("name"); // Assign receiver's name
            }

            // Store names in a map
            Map<String, String> namesMap = new HashMap<>();
            namesMap.put("loggedInUserName", loggedInUserName[0]);
            namesMap.put("receiverName", receiverName);

            // List to store messages
            List<String> messagesList = new ArrayList<>();

            // Start a new thread to continuously fetch and display messages
            new Thread(() -> {
                try {
                    while (true) {
                        // Fetch the messages exchanged between the logged-in user and the selected user
                        String query = "SELECT m.*, " +
                                "CASE " +
                                "WHEN m.sender_id = ? THEN sender.name " +
                                "ELSE receiver.name " +
                                "END AS sender_name, " +
                                "CASE " +
                                "WHEN m.receiver_id = ? THEN receiver.name " +
                                "ELSE sender.name " +
                                "END AS receiver_name " +
                                "FROM messages m " +
                                "INNER JOIN users sender ON m.sender_id = sender.user_id " +
                                "INNER JOIN users receiver ON m.receiver_id = receiver.user_id " +
                                "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?)";

                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, loggedInUserId);
                        preparedStatement.setString(2, userId);
                        preparedStatement.setString(3, userId);
                        preparedStatement.setString(4, loggedInUserId);
                        preparedStatement.setString(5, loggedInUserId);
                        preparedStatement.setString(6, userId);

                        ResultSet resultSet = preparedStatement.executeQuery();

                        // Clear existing messages list before adding new ones
                        messagesList.clear();

                        // Add fetched messages to the list
                        while (resultSet.next()) {
                            String senderId = resultSet.getString("sender_id");
                            String senderName = resultSet.getString("sender_name");
                            String receiverId = resultSet.getString("receiver_id");
                            String receiverName2 = resultSet.getString("receiver_name"); // Fetch receiver name here
                            String message = resultSet.getString("message");

                            // Format the message
                            String formattedMessage = message;

                            // Add the formatted message to the list
                            messagesList.add(senderId + ":" + formattedMessage); // Prepend senderId to the message
                        }

                        resultSet.close();
                        preparedStatement.close();

                        // Update the message container on the JavaFX Application Thread
                        Platform.runLater(() -> {
                            // Clear existing messages before adding new ones
                            messageContainer.getChildren().clear();

                            // Add messages from the list to the message container
                            for (String message : messagesList) {
                                HBox messageBox = new HBox();
                                Label messageLabel = new Label(message.substring(message.indexOf(':') + 1));
                                messageLabel.setStyle("-fx-background-color: #4b7de7; -fx-background-radius: 10px; -fx-padding: 8px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
                                messageLabel.setMaxWidth(300); // Limiting width to avoid stretching
                                messageLabel.setWrapText(true); // Allowing text wrapping

                                if (message.startsWith(loggedInUserId)) { // Check if the message was sent by the logged-in user
                                    // Message sent by logged-in user
                                    messageBox.setAlignment(Pos.CENTER_RIGHT);
                                    messageBox.getChildren().add(messageLabel);
                                } else {
                                    // Message received from other user
                                    messageBox.setAlignment(Pos.CENTER_LEFT);
                                    messageBox.getChildren().add(messageLabel);
                                }

                                // Add marginal gap (top and bottom) to the message box
                                VBox.setMargin(messageBox, new Insets(5, 0, 5, 0));

                                messageContainer.getChildren().add(messageBox);
                            }
                        });

                        // Sleep for a few seconds before fetching messages again
                        Thread.sleep(5000); // Adjust the interval as needed
                    }
                } catch (SQLException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            // Modify the code to send userId instead of clientId from the client side
            sendButton.setOnAction(event -> {
                String message = messageField.getText();
                if (!message.isEmpty()) {
                    try {
                        // Store the message in the database
                        String insertQuery = "INSERT INTO messages (sender_id, sender_name, receiver_id, receiver_name, message) VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                        insertStatement.setString(1, loggedInUserId); // Send the loggedInUserId as the sender's ID
                        insertStatement.setString(2, namesMap.get("loggedInUserName")); // Use the logged-in user's name
                        insertStatement.setString(3, userId); // Send userId as the recipient's ID
                        insertStatement.setString(4, namesMap.get("receiverName")); // Use the receiver's name
                        insertStatement.setString(5, message);
                        insertStatement.executeUpdate();

                        // Send the message to the server
                        outputStream.writeObject(loggedInUserId); // Send loggedInUserId instead of clientId
                        outputStream.flush();
                        outputStream.writeObject(userId); // Send userId as the recipient's ID
                        outputStream.flush();
                        outputStream.writeObject(message);
                        outputStream.flush();

                        // Clear the messageField
                        messageField.clear();

                        // Display the sent message immediately
                        Platform.runLater(() -> {
                            HBox messageBox = new HBox();
                            Label messageLabel = new Label(message);
                            messageLabel.setStyle("-fx-background-color: #4b7de7; -fx-background-radius: 10px; -fx-padding: 8px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
                            messageLabel.setMaxWidth(300);
                            messageLabel.setWrapText(true);
                            messageBox.setAlignment(Pos.CENTER_RIGHT);
                            messageBox.getChildren().add(messageLabel);

                            // Add marginal gap (top and bottom) to the message box
                            VBox.setMargin(messageBox, new Insets(5, 0, 5, 0));

                            messageContainer.getChildren().add(messageBox);
                        });

                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    @FXML
    void searchUser(ActionEvent event) {
        // Fetch users based on the search query
        fetchAndDisplayUsers();
    }

}
