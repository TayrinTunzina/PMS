package com.example.pms;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;

import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private Label studentIdLabel;
    @FXML
    private TextArea studentDetailsTextArea;
    @FXML
    private TextField facultyTextField;
    @FXML
    private TextArea featuresTextArea;
    @FXML
    private AnchorPane sideBar;
    @FXML
    private Pane videoPane;
    @FXML
    private HBox controls;

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


    public class StudentAchievement {
        private String studentId;
        private String courseName;
        private String position;

        public StudentAchievement(String studentId, String courseName, String position) {
            this.studentId = studentId;
            this.courseName = courseName;
            this.position = position;
        }

        // Getters and setters for studentId, courseName, and position
        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    private List<StudentAchievement> retrieveStudentAchievements(String studentId) {
        List<StudentAchievement> studentAchievements = new ArrayList<>();

        // Query your database to retrieve achievements for the selected student
        try {
            String query = "SELECT project.course_name, winners.position " +
                    "FROM winners " +
                    "JOIN project ON winners.project_id = project.p_id " +
                    "WHERE project.s_id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, studentId);
            ResultSet resultSet = statement.executeQuery();

            // Populate the list of achievements with course names and positions
            while (resultSet.next()) {
                String courseName = resultSet.getString("course_name");
                String position = resultSet.getString("position");

                // Create a new StudentAchievement object
                StudentAchievement achievement = new StudentAchievement(studentId, courseName, position);

                // Add the achievement to the list
                studentAchievements.add(achievement);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the list of student achievements
        return studentAchievements;
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

            // Inside the loop in the populateCarousel method
            List<StudentAchievement> studentAchievements = retrieveStudentAchievements(studentId); // Fetch student achievements for the current student
            AnchorPane studentCard = createStudentCard(studentName, studentId, studentPicture, studentAchievements);


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
    private AnchorPane createStudentCard(String studentName, String studentId, byte[] studentPicture, List<StudentAchievement> achievements) {
        AnchorPane studentCard = new AnchorPane();
        studentCard.setPrefSize(120, 200); // Adjust the size as needed
        studentCard.setStyle("-fx-background-radius: 25; -fx-background-color: linear-gradient( to right top,#facd68, #fc76b3);");

        // Create front side content
        VBox frontVBox = new VBox(2); // Vertical spacing between nodes
        frontVBox.setAlignment(Pos.BOTTOM_CENTER); // Center nodes vertically
        frontVBox.setPadding(new Insets(15, 0, 15, 0)); // Add bottom padding of 10 pixels

        // Create student picture
        ImageView pictureView = new ImageView(new Image(new ByteArrayInputStream(studentPicture)));
        pictureView.setFitWidth(100); // Adjust the width as needed
        pictureView.setFitHeight(100); // Adjust the height as needed

        // Create student id label
        Label idLabel = new Label(studentId);
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12pt; -fx-font-family: Serif"); // Apply styles

        // Create student name label
        Label nameLabel = new Label(studentName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14pt;"); // Apply styles

        // Add nodes to front side content pane
        frontVBox.getChildren().addAll(pictureView, idLabel, nameLabel);

        // Create back side content
        VBox backVBox = new VBox(2); // Vertical spacing between nodes
        backVBox.setAlignment(Pos.CENTER); // Center nodes vertically
        backVBox.setPadding(new Insets(0, 0, 10, 0)); // Add bottom padding of 10 pixels

        // Create achievements label
        Label achievementsLabel = new Label("Achievements:");
        achievementsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12pt; -fx-font-family: Serif; -fx-underline: true;");

        // Add the label to the back side content pane
        backVBox.getChildren().add(achievementsLabel);

        // Iterate over the achievements and create labels for each achievement
        for (StudentAchievement achievement : achievements) {
            String achievementText = " ~ " + achievement.getCourseName() + " - " + achievement.getPosition();
            Label achievementLabel = new Label(achievementText);
            achievementLabel.setStyle("-fx-font-size: 10pt;");
            // Set the preferred width of the label
            achievementLabel.setPrefWidth(200); // Adjust width as needed

            // Set wrapText property to true
            achievementLabel.setWrapText(true);
            backVBox.getChildren().add(achievementLabel);
        }

        // Create a StackPane to overlay both sides
        StackPane contentPane = new StackPane();
        contentPane.setAlignment(Pos.CENTER);
        contentPane.setPrefSize(studentCard.getPrefWidth(), studentCard.getPrefHeight());

        // Add front and back sides to the StackPane
        contentPane.getChildren().addAll(frontVBox, backVBox);
        backVBox.setVisible(false); // Initially, show only the front side

        // Position the StackPane within the AnchorPane
        AnchorPane.setTopAnchor(contentPane, 0.0);
        AnchorPane.setRightAnchor(contentPane, 0.0);
        AnchorPane.setBottomAnchor(contentPane, 0.0);
        AnchorPane.setLeftAnchor(contentPane, 0.0);

        // Create and configure the button
        Button flipButton = new Button();
        flipButton.setPrefSize(20, 20); // Set preferred size for the button
        flipButton.setCursor(Cursor.HAND); // Set cursor to hand cursor
        flipButton.setLayoutX(studentCard.getPrefWidth() - flipButton.getPrefWidth()); // Adjust the position as needed
        flipButton.setLayoutY(5); // Adjust the position as needed

        // Create and set the image for the button
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/img/refresh.png")));
        imageView.setFitWidth(15); // Set the width of the image
        imageView.setFitHeight(16); // Set the height of the image
        flipButton.setGraphic(imageView); // Set the image as the graphic of the button

        // Add action handler to the button for flip transition
        flipButton.setOnAction(event -> {
            frontVBox.setVisible(!frontVBox.isVisible());
            backVBox.setVisible(!backVBox.isVisible());
        });

        studentCard.getChildren().addAll(contentPane, flipButton);

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



    public void initDta(String projectId) {
        try {
            String query = "SELECT w.team_name, w.position " +
                    "FROM winners w " +
                    "INNER JOIN project p ON w.project_id = p.p_id " +
                    "WHERE p.p_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, projectId);
            ResultSet resultSet = statement.executeQuery();

            // Check if the result set has data
            if (resultSet.next()) {
                // Retrieve project details from the result set
                String teamName = resultSet.getString("team_name");
                String position = resultSet.getString("position");
                String courseName = resultSet.getString("course_name");
//                String facultyName = resultSet.getString("f_name");
//                String studentId = resultSet.getString("s_id");
//                String studentName = resultSet.getString("s_name");
//                String features = resultSet.getString("features");
//                byte[] videoData = resultSet.getBytes("video");

                // Display the project details in the UI (e.g., update labels, text fields, etc.)
                teamNameLabel.setText(teamName);
                positionLabel.setText(position);
                courseNameLabel.setText(courseName);
            } else {
                // Handle case when no data is retrieved for the given projectId
                System.out.println("No project details found for projectId: " + projectId);
            }

            // Close the result set and statement
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions
        }
    }

    public void initData(String projectId) {
        try {
            // Query to retrieve project details from the project table
            String queryProjectDetails = "SELECT DISTINCT p.course_name, p.f_name, p.video, p.features " +
                    "FROM project p " +
                    "WHERE p.p_id = ?";
            PreparedStatement statementProjectDetails = connection.prepareStatement(queryProjectDetails);
            statementProjectDetails.setString(1, projectId);
            ResultSet resultSetProjectDetails = statementProjectDetails.executeQuery();

            // Check if the result set has data
            if (resultSetProjectDetails.next()) {
                // Retrieve project details from the result set
                String courseName = resultSetProjectDetails.getString("course_name");
                String facultyName = resultSetProjectDetails.getString("f_name");
                String features = resultSetProjectDetails.getString("features");
                byte[] videoData = resultSetProjectDetails.getBytes("video");
                // Retrieve other project details (video, features)

                courseNameLabel.setText(courseName);
                facultyTextField.setText(facultyName);
                featuresTextArea.setText(features);

                displayVideo(videoData, videoPane);

                // Now, continue with the existing code to retrieve winners' data from the winners table
                String queryWinners = "SELECT w.team_name, w.position " +
                        "FROM winners w " +
                        "WHERE w.project_id = ?";
                PreparedStatement statementWinners = connection.prepareStatement(queryWinners);
                statementWinners.setString(1, projectId);
                ResultSet resultSetWinners = statementWinners.executeQuery();

                // Check if the result set has data
                if (resultSetWinners.next()) {
                    // Retrieve winners' data from the result set
                    String teamName = resultSetWinners.getString("team_name");
                    String position = resultSetWinners.getString("position");

                    // Now, you can update your UI with the retrieved project and winners' details
                    teamNameLabel.setText(teamName);
                    positionLabel.setText(position);

                } else {
                    // Handle case when no winners' data is retrieved for the given projectId
                    System.out.println("No winners found for projectId: " + projectId);
                }

                resultSetWinners.close();
                statementWinners.close();

                String queryStudentDetails = "SELECT s_id, s_name " +
                        "FROM project p " +
                        "WHERE p.p_id = ?";
                PreparedStatement statementStudentDetails = connection.prepareStatement(queryStudentDetails);
                statementStudentDetails.setString(1, projectId);
                ResultSet resultSetStudentDetails = statementStudentDetails.executeQuery();

                    // Retrieve project details from the result set
                    StringBuilder studentDetails = new StringBuilder();

                while (resultSetStudentDetails.next()) {
                    String studentId = resultSetStudentDetails.getString("s_id");
                    String studentName = resultSetStudentDetails.getString("s_name");

                    // Append student name and ID to the StringBuilder
                    studentDetails.append(studentName).append(" - ").append(studentId).append("\n");
                }

                // Set the concatenated student details in the text area
                studentDetailsTextArea.setText(studentDetails.toString());


                // Close result sets and statements
                resultSetStudentDetails.close();
                statementStudentDetails.close();
            } else {
                // Handle case when no project details are retrieved for the given projectId
                System.out.println("No project details found for projectId: " + projectId);
            }

            // Close result sets and statements
            resultSetProjectDetails.close();
            statementProjectDetails.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions
        }
    }


    private File createTempFile(byte[] data) {
        File tempFile = null;
        try {
            // Create a temporary file
            tempFile = File.createTempFile("tempVideo", ".mp4");

            // Write the byte array data to the temporary file
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }


    public void displayVideo(byte[] videoBytes, Pane videoPane) {
        // Check if videoBytes is not null
        if (videoBytes != null && videoBytes.length > 0) {
            // Create a temporary file from the byte array
            File tempFile = createTempFile(videoBytes);
            if (tempFile != null) {
                // Create a Media object from the temporary file
                Media media = new Media(tempFile.toURI().toString());

                // Create a MediaPlayer
                MediaPlayer mediaPlayer = new MediaPlayer(media);

                // Create a MediaView
                MediaView mediaView = new MediaView(mediaPlayer);

                // Add MediaView to the provided videoPane
                videoPane.getChildren().add(mediaView);

                // Create play/pause button
                Button playPauseButton = new Button("Play");
                playPauseButton.setOnAction(e -> {
                    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        mediaPlayer.pause();
                        playPauseButton.setText("Play");
                    } else {
                        mediaPlayer.play();
                        playPauseButton.setText("Pause");
                    }
                });

                // Create seek slider
                Slider seekSlider = new Slider();
                seekSlider.setMaxWidth(Double.MAX_VALUE);
                mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                    seekSlider.setValue(newValue.toSeconds());
                });
                seekSlider.setOnMousePressed(e -> mediaPlayer.pause());
                seekSlider.setOnMouseReleased(e -> mediaPlayer.seek(Duration.seconds(seekSlider.getValue())));

                // Add play/pause button and seek slider to the existing HBox named "controls"
                HBox controls = (HBox) videoPane.lookup("#controls");
                if (controls != null) {
                    controls.getChildren().addAll(playPauseButton, seekSlider);
                } else {
                    System.out.println("Failed to find HBox with id 'controls' in the provided videoPane.");
                }

                // Play the video
                mediaPlayer.play();
            } else {
                System.out.println("Failed to create temporary file.");
            }
        } else {
            System.out.println("Video data is empty or null.");
        }
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
