package com.example.pms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class WinnersController {

    private Connection connection;

    @FXML
    private ComboBox<String> aoopComboBox;

    @FXML
    private ComboBox<String> dbmsComboBox;

    @FXML
    private ComboBox<String> sadComboBox;

    @FXML
    private ComboBox<String> electroComboBox;

    @FXML
    private ComboBox<String> microComboBox;

    @FXML
    private ComboBox<String> seComboBox;


    @FXML
    private BorderPane carouselPane; // Inject the BorderPane from your FXML

    private String selectedTrimester;

    @FXML
    private Label teamNameLabel;

    @FXML
    private Label positionLabel;

    @FXML
    private Label courseNameLabel;

    @FXML
    private AnchorPane sideBar;

    @FXML
    private GridPane gridPane; // Declare a reference to your GridPane


    private Stage stage;
    private Scene scene;


    public void setStage(Stage stage){
        this.stage = stage;
    }


    @FXML
    public void initialize() {
        connection = dbconnect.getConnection();
        // Populate ComboBoxes with project IDs and add event listeners
        populateComboBox(aoopComboBox, "Advanced Object Oriented Programming Lab");
        populateComboBox(dbmsComboBox, "Database Management Systems Lab");
        populateComboBox(sadComboBox, "System Analysis and Design Lab");
        populateComboBox(electroComboBox, "Electronics Lab");
        populateComboBox(microComboBox, "Microprocessors and Microcontrollers Lab");
        populateComboBox(seComboBox, "Software Engineering Lab");
        // Populate ComboBoxes for other courses and add event listeners

    }

    private void populateComboBox(ComboBox<String> comboBox, String courseName) {
        if (comboBox == null) {
            // If the comboBox is null, simply return without performing any operation
            return;
        }

        try {
            // Query to get distinct trimester values from winners table for a specific course
            String query = "SELECT DISTINCT trimester FROM winners " +
                    "JOIN project ON winners.project_id = project.p_id " +
                    "WHERE project.course_name=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, courseName);
            ResultSet resultSet = statement.executeQuery();

            // Populate the ComboBox with trimester values
            while (resultSet.next()) {
                String trimester = resultSet.getString("trimester");
                comboBox.getItems().add(trimester);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public class Winner {

        private String projectId;
        private String teamName;
        private String position;
        private String courseName;
        private List<String> studentNames;
        private List<String> studentIds;
        private List<byte[]> studentPictures; // Add this list for storing student pictures


        // Constructor with parameters
        public Winner(String projectId, String teamName, String position, String courseName) {
            this.projectId = projectId;
            this.teamName = teamName;
            this.position = position;
            this.courseName = courseName;
            studentNames = new ArrayList<>();
            studentIds = new ArrayList<>();
            studentPictures = new ArrayList<>();
        }

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public List<String> getStudentNames() {
            return studentNames;
        }

        public List<String> getStudentIds() {
            return studentIds;
        }

        public List<byte[]> getStudentPictures() {
            return studentPictures;
        }

        // Method to add student name, ID, and picture
        public void addStudent(String sName, String sId, byte[] picture) {
            studentNames.add(sName);
            studentIds.add(sId);
            if (picture != null) {
                studentPictures.add(picture);
            } else {
                // Use a default image when the picture is null
                try {
                    InputStream defaultImageStream = getClass().getResourceAsStream("/img/default-avatar.png");
                    byte[] defaultImageBytes = defaultImageStream.readAllBytes();
                    studentPictures.add(defaultImageBytes);
                } catch (IOException e) {
                    // Handle the IOException
                    e.printStackTrace();
                }
            }
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }
    }



    private int currentIndex;
    private List<Winner> winnersList; // Define winnersList as a class-level field

    // Method to retrieve winners for the selected trimester from the database
    private List<Winner> retrieveWinners(String trimester) {
        Map<String, Winner> winnersMap = new HashMap<>();

        // Query your database to retrieve winners for the selected trimester
        try {
            String query = "SELECT winners.team_name, winners.position, project.p_id, project.course_name, project.s_name, project.s_id, users.pic " +
                    "FROM winners " +
                    "JOIN project ON winners.project_id = project.p_id " +
                    "JOIN users ON project.s_id = users.user_id " +
                    "WHERE winners.trimester = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, trimester);
            ResultSet resultSet = statement.executeQuery();

            // Populate the list of winners with team names, positions, and course names
            while (resultSet.next()) {
                String projectId = resultSet.getString("p_id");
                String teamName = resultSet.getString("team_name");
                String position = resultSet.getString("position");
                String courseName = resultSet.getString("course_name");
                String sName = resultSet.getString("s_name");
                String sId = resultSet.getString("s_id");
                byte[] picture = resultSet.getBytes("pic");


                // If the winner doesn't exist in the map, create a new Winner object
                // Otherwise, retrieve the existing Winner object from the map
                Winner winner = winnersMap.computeIfAbsent(teamName + position + courseName,
                        k -> new Winner(projectId, teamName, position, courseName));

                // Add student name and ID to the winner
                winner.addStudent(sName, sId, picture);
                // Print fetched values
                System.out.println("Project Id: " + projectId + ", Team Name: " + teamName + ", Position: " + position + ", Course Name: " + courseName +
                        ", Student Name: " + sName + ", Student ID: " + sId);

            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the values of the map as a list
        return new ArrayList<>(winnersMap.values());
    }


    private void populateCarousel(List<Winner> winnersList) {
        if (winnersList == null || winnersList.isEmpty()) {
            // If there are no winners, display a message or handle it accordingly
            return;
        }

        // Get the current winner
        Winner currentWinner = winnersList.get(currentIndex);

        // Print out the current winner's data
        System.out.println("Current Winner: " + currentWinner.getTeamName() + ", " +
                currentWinner.getPosition() + ", " +
                currentWinner.getCourseName());

        // Set the winner details in the labels
        String teamName = currentWinner.getTeamName();
        String position = currentWinner.getPosition();
        String courseName = currentWinner.getCourseName();
        String projectId = currentWinner.getProjectId();


        System.out.println("Setting labels: ProjectId - " + projectId + ", Team Name - " + teamName + ", Position - " + position + ", Course Name - " + courseName);

        // Ensure labels are not null before updating their text
        if (teamNameLabel != null && positionLabel != null && courseNameLabel != null) {
            teamNameLabel.setText(teamName);
            positionLabel.setText(position);
            courseNameLabel.setText(courseName);
        } else {
            System.err.println("Labels are null. Make sure they are properly initialized.");
        }
        System.out.println("GridPane object: " + gridPane);

        // Clear existing content in the grid pane
        gridPane.getChildren().clear();

        // Populate student cards
        int maxRows = 2; // Maximum number of rows
        int maxCols = 3; // Maximum number of columns
        int rowIndex = 0; // Initialize the row index
        int colIndex = 0; // Initialize the column index
        for (int i = 0; i < currentWinner.getStudentNames().size(); i++) {
            String studentName = currentWinner.getStudentNames().get(i);
            String studentId = currentWinner.getStudentIds().get(i);
            byte[] studentPicture = currentWinner.getStudentPictures().get(i);

            // Create a new AnchorPane for each student card
            AnchorPane studentCard = createStudentCard(studentName, studentId, studentPicture);

            // Add the student card to the grid pane at the current row and column
            gridPane.add(studentCard, colIndex, rowIndex);

            // Increment the column index
            colIndex++;

            // Check if the column index exceeds the maximum number of columns
            if (colIndex >= maxCols) {
                // Reset the column index to 0 and increment the row index
                colIndex = 0;
                rowIndex++;

                // Check if the row index exceeds the maximum number of rows
                if (rowIndex >= maxRows) {
                    // Exit the loop if the maximum number of rows is reached
                    break;
                }
            }
        }

// Create project details button
        Button projectDetailsButton = new Button("Project Details");
        projectDetailsButton.setAlignment(Pos.CENTER);
        projectDetailsButton.setContentDisplay(ContentDisplay.CENTER);
        projectDetailsButton.setMnemonicParsing(false);
        projectDetailsButton.setPrefHeight(34.0);
        projectDetailsButton.setPrefWidth(146.0);
        projectDetailsButton.setTextFill(Color.WHITE);

        // Set the font to bold
        Font boldFont = Font.font("System", FontWeight.BOLD, 15.0);
        projectDetailsButton.setFont(boldFont);

        // Set the p_id property
        projectDetailsButton.getProperties().put("p_id", projectId);

        // Add an action event handler to the button
        projectDetailsButton.setOnAction(event -> {
            // Call the method to handle project details button action
            handleProjectDetailsButtonAction(event);
        });

        // Add the button to the bottom section of the BorderPane
        BorderPane.setAlignment(projectDetailsButton, Pos.TOP_CENTER);
        BorderPane.setMargin(projectDetailsButton, new Insets(25.0));
        carouselPane.setBottom(projectDetailsButton);
    }


    // Method to create a student card

    private AnchorPane createStudentCard(String studentName, String studentId, byte[] studentPicture) {
        AnchorPane studentCard = new AnchorPane();
        studentCard.setPrefSize(120, 200); // Adjust the size as needed
        studentCard.setStyle("-fx-background-radius: 25; -fx-background-color: linear-gradient( to right top,#facd68, #fc76b3);");

        // Create a StackPane to center the content
        StackPane contentPane = new StackPane();
        contentPane.setAlignment(Pos.CENTER);
        contentPane.setPrefSize(studentCard.getPrefWidth(), studentCard.getPrefHeight());

        // Create student picture
        ImageView pictureView = new ImageView(new Image(new ByteArrayInputStream(studentPicture)));
        pictureView.setFitWidth(100); // Adjust the width as needed
        pictureView.setFitHeight(100); // Adjust the height as needed

        // Create student id label
        Label idLabel = new Label(studentId);
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12pt;"); // Apply styles

        // Create student name label
        Label nameLabel = new Label(studentName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14pt;"); // Apply styles

        // Add nodes to content pane
        VBox vbox = new VBox(10); // Vertical spacing between nodes
        vbox.getChildren().addAll(pictureView, idLabel, nameLabel);
        vbox.setAlignment(Pos.CENTER); // Center nodes vertically

        contentPane.getChildren().add(vbox);

        // Center the content pane within the student card
        AnchorPane.setTopAnchor(contentPane, 0.0);
        AnchorPane.setRightAnchor(contentPane, 0.0);
        AnchorPane.setBottomAnchor(contentPane, 0.0);
        AnchorPane.setLeftAnchor(contentPane, 0.0);

        // Add content pane to student card
        studentCard.getChildren().add(contentPane);

        return studentCard;
    }


    // Event handler for the left arrow
    @FXML
    private void handleLeftArrowClick(MouseEvent mouseEvent) {
        System.out.println("Left arrow clicked");
        if (winnersList != null && currentIndex > 0) {
            currentIndex--; // Move to the previous winner
            System.out.println("Before left arrow click - currentIndex: " + currentIndex);
            Winner currentWinner = winnersList.get(currentIndex);
            System.out.println("Current Winner: " + currentWinner.getTeamName() + ", " +
                    currentWinner.getPosition() + ", " +
                    currentWinner.getCourseName());
            populateCarousel(winnersList); // Pass winnersList to update the carousel
        } else {
            // Handle case when currentIndex is already at the beginning
            System.out.println("Left arrow clicked but currentIndex is already at the beginning.");
        }
    }

    @FXML
    private void handleRightArrowClick(MouseEvent mouseEvent) {
        System.out.println("Right arrow clicked");
        if (winnersList != null && currentIndex < winnersList.size() - 1) {
            currentIndex++; // Move to the next winner
            System.out.println("Before right arrow click - currentIndex: " + currentIndex);
            Winner currentWinner = winnersList.get(currentIndex);
            System.out.println("Current Winner: " + currentWinner.getTeamName() + ", " +
                    currentWinner.getPosition() + ", " +
                    currentWinner.getCourseName());
            populateCarousel(winnersList); // Pass winnersList to update the carousel
        } else {
            // Handle case when currentIndex is already at the end
            System.out.println("Right arrow clicked but currentIndex is already at the end.");
        }
    }


    @FXML
    private void handleViewButtonAction(ActionEvent event) {
        try {
            // Debugging: Print selected trimester
            System.out.println("Selected Trimester: " + selectedTrimester);

            // Retrieve winners for the selected trimester
            winnersList = retrieveWinners(selectedTrimester); // Update class-level winnersList

            if (winnersList != null && !winnersList.isEmpty()) {
                // Initialize currentIndex to 0 only if winnersList is not null and not empty
                currentIndex = 0;
            } else {
                // Handle case when winnersList is null or empty
                System.out.println("No winners found for the selected trimester.");
                return;
            }

            // Debugging: Print number of winners retrieved
            System.out.println("Number of winners retrieved: " + winnersList.size());

            // Print out the details of each winner in the winnersList
            for (Winner winner : winnersList) {
                System.out.println("Winner: " + winner.getTeamName() + ", " + winner.getPosition() + ", " + winner.getCourseName());
            }

            // Load the carousel FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CarouselView.fxml"));
            Parent root = loader.load();

            // Get the controller associated with the loaded FXML file
            WinnersController carouselController = loader.getController();

            // Set the selected trimester in the carousel controller
            carouselController.setSelectedTrimester(selectedTrimester);

            // Populate carousel with data for the selected trimester
            carouselController.populateCarousel(winnersList);

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Replace the content of the current stage with the carousel layout
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
// Add a method to set the selected trimester
    private void setSelectedTrimester(String trimester) {
        selectedTrimester = trimester;
        winnersList = retrieveWinners(trimester); // Assign the retrieved winners to winnersList
    }


    @FXML
    private void updateSelectedTrimester(ActionEvent event) {
        ComboBox<String> comboBox = (ComboBox<String>) event.getSource();
        selectedTrimester = comboBox.getValue();
        System.out.println("Selected Trimester: " + selectedTrimester);
    }


    @FXML
    private void handleProjectDetailsButtonAction(ActionEvent event) {
        // Get the button that triggered the event
        Button button = (Button) event.getSource();

        // Get the p_id from the button's properties if it exists
        Object property = button.getProperties().get("p_id");
        if (property != null) {
            String projectId = property.toString();

            // Print the projectId to check if the correct p_id is passed
            System.out.println("Project ID passed: " + projectId);

            try {
                // Call the method to handle project details with the projectId
                handleProjectDetails(projectId, event);
            } catch (IOException e) {
                e.printStackTrace(); // Handle the IOException
            }
        } else {
            System.err.println("No projectId found in button properties.");
        }
    }


    private void handleProjectDetails(String projectId, ActionEvent event) throws IOException {
        // Open the project details view and pass the projectId
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectDetails.fxml"));
        Parent root = loader.load();

        // Get the controller associated with the loaded FXML file
        WinnersController projectController = loader.getController();

        // Call a method in the project controller to initialize with the projectId
        projectController.initData(projectId);

        // Create a new stage for the project details
        Stage projectDetailsStage = new Stage();
        projectDetailsStage.setScene(new Scene(root));

        // Set the stage's properties (e.g., title)
        projectDetailsStage.setTitle("Project Details");

        // Show the project details stage
        projectDetailsStage.show();
    }



    public void initData(String projectId) {
        // Retrieve project details based on the projectId
        // Display the project details in the UI
    }









    @FXML
    void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("courses.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void backn(ActionEvent event) throws IOException {
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
