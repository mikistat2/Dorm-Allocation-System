package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ProctorDashboardController {

    @FXML
    private Label totalStudentsLabel;

    @FXML
    private Label assignedLabel;

    @FXML
    private Label unassignedLabel;

    @FXML
    private javafx.scene.layout.FlowPane buildingsFlowPane;
    
    @FXML
    private Button allBlocksBtn;
    
    @FXML
    private Button maleBlocksBtn;
    
    @FXML
    private Button femaleBlocksBtn;
    
    private String currentFilter = "ALL"; // ALL, MALE, FEMALE

    @FXML
    public void initialize() {
        int total = 0;
        int assigned = 0;
        int unassigned = 0;

        java.util.Map<String, Integer> buildingCounts = new java.util.HashMap<>();
        java.util.List<models.Building> buildings = data.DataManager.getInstance().getBuildings();
        
        // Initialize counts for all buildings
        for (models.Building b : buildings) {
            buildingCounts.put(b.getName(), 0);
        }

        for (models.Student s : data.DataManager.getInstance().getStudents()) {
            total++;
            if ("Not Assigned".equals(s.getAssignedBuilding())) {
                unassigned++;
            } else {
                assigned++;
                String bName = s.getAssignedBuilding();
                buildingCounts.put(bName, buildingCounts.getOrDefault(bName, 0) + 1);
            }
        }

        totalStudentsLabel.setText(String.valueOf(total));
        assignedLabel.setText(String.valueOf(assigned));
        unassignedLabel.setText(String.valueOf(unassigned));
        
        loadBuildingCards(buildingCounts);
        updateFilterButtons();
    }

    private void loadBuildingCards(java.util.Map<String, Integer> counts) {
        buildingsFlowPane.getChildren().clear();
        for (String buildingName : counts.keySet()) {
            // Find building to get gender and room count
            String gender = "Male";
            int maxRooms = 0;
            for (models.Building b : data.DataManager.getInstance().getBuildings()) {
                if (b.getName().equals(buildingName)) {
                    gender = b.getGender();
                    maxRooms = b.getRooms().size();
                    break;
                }
            }
            
            // Apply filter
            if (!currentFilter.equals("ALL")) {
                if (currentFilter.equals("MALE") && !gender.equalsIgnoreCase("Male")) {
                    continue;
                }
                if (currentFilter.equals("FEMALE") && !gender.equalsIgnoreCase("Female")) {
                    continue;
                }
            }
            
            javafx.scene.layout.VBox card = new javafx.scene.layout.VBox();
            card.setAlignment(javafx.geometry.Pos.CENTER);
            card.setSpacing(8);
            card.setPadding(new javafx.geometry.Insets(15));
            card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 18; -fx-effect: dropshadow(three-pass-box, rgba(76, 201, 240, 0.15), 12, 0, 0, 6); -fx-cursor: hand;");
            card.setPrefSize(180, 180);

            // Building icon
            Label iconLabel = new Label("🏢");
            iconLabel.setStyle("-fx-font-size: 42px;");

            Label nameLabel = new Label(buildingName);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #FFFFFF;");

            Label countLabel = new Label(counts.get(buildingName) + " Students");
            countLabel.setStyle("-fx-text-fill: #4CC9F0; -fx-font-size: 13px;");
            
            Label roomsLabel = new Label("Max Rooms: " + maxRooms);
            roomsLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-opacity: 0.8;");
            
            Label genderLabel = new Label("Gender: " + gender);
            genderLabel.setStyle("-fx-text-fill: #4CC9F0; -fx-font-size: 12px;");

            card.getChildren().addAll(iconLabel, nameLabel, countLabel, roomsLabel, genderLabel);
            
            // Hover effect
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.12); -fx-background-radius: 18; -fx-effect: dropshadow(three-pass-box, rgba(76, 201, 240, 0.25), 16, 0, 0, 6); -fx-cursor: hand;"));
            card.setOnMouseExited(e -> card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 18; -fx-effect: dropshadow(three-pass-box, rgba(76, 201, 240, 0.15), 12, 0, 0, 6); -fx-cursor: hand;"));
            
            // Optional: Add click handler to filter students
            card.setOnMouseClicked(e -> {
                System.out.println("Clicked " + buildingName);
                openStudentsView("BUILDING", buildingName);
            });

            buildingsFlowPane.getChildren().add(card);
        }
        
        // Add "Add Block" Card
        javafx.scene.layout.VBox addCard = new javafx.scene.layout.VBox();
        addCard.setAlignment(javafx.geometry.Pos.CENTER);
        addCard.setSpacing(10);
        addCard.setPadding(new javafx.geometry.Insets(15));
        addCard.setStyle("-fx-background-color: #f0f8ff; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-cursor: hand; -fx-border-color: #4ecdc4; -fx-border-style: dashed; -fx-border-width: 3; -fx-border-radius: 10;");
        addCard.setPrefSize(150, 150);

        Label addIconLabel = new Label("+");
        addIconLabel.setStyle("-fx-font-size: 50px; -fx-text-fill: #4ecdc4; -fx-font-weight: bold;");

        Label addTextLabel = new Label("Add Block");
        addTextLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2b5876;");

        addCard.getChildren().addAll(addIconLabel, addTextLabel);
        addCard.setOnMouseClicked(e -> handleAddBlock());
        
        // Hover effect
        addCard.setOnMouseEntered(e -> addCard.setStyle("-fx-background-color: #e6f7ff; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 0); -fx-cursor: hand; -fx-border-color: #4ecdc4; -fx-border-style: dashed; -fx-border-width: 3; -fx-border-radius: 10;"));
        addCard.setOnMouseExited(e -> addCard.setStyle("-fx-background-color: #f0f8ff; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-cursor: hand; -fx-border-color: #4ecdc4; -fx-border-style: dashed; -fx-border-width: 3; -fx-border-radius: 10;"));

        buildingsFlowPane.getChildren().add(addCard);
    }
    
    private void handleAddBlock() {
        // Auto-generate block name
        int nextBlockNumber = data.DataManager.getInstance().getBuildings().size() + 1;
        String blockName = "Block-" + nextBlockNumber;
        
        // Create modern styled dialog for room count
        javafx.scene.control.TextInputDialog roomDialog = new javafx.scene.control.TextInputDialog("10");
        roomDialog.setTitle("Add New Building");
        roomDialog.setHeaderText("🏢  Creating: " + blockName);
        roomDialog.setContentText("Number of Rooms:");
        
        // Apply modern dark theme styling
        javafx.scene.control.DialogPane roomPane = roomDialog.getDialogPane();
        roomPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0f2744, #0a1929);" +
            "-fx-border-color: #00b4d8;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 180, 216, 0.5), 20, 0.3, 0, 0);"
        );
        
        if (roomPane.lookup(".header-panel") != null) {
            roomPane.lookup(".header-panel").setStyle("-fx-background-color: transparent; -fx-padding: 20 20 10 20;");
        }
        
        javafx.scene.control.Label roomHeaderLabel = (javafx.scene.control.Label) roomPane.lookup(".header .label");
        if (roomHeaderLabel != null) {
            roomHeaderLabel.setStyle("-fx-text-fill: #00b4d8; -fx-font-size: 18px; -fx-font-weight: bold;");
        }
        
        if (roomPane.lookup(".content") != null) {
            roomPane.lookup(".content").setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px; -fx-padding: 10 20 20 20;");
        }
        
        // Style buttons
        roomPane.lookupButton(javafx.scene.control.ButtonType.OK).setStyle(
            "-fx-background-color: linear-gradient(to bottom, #00d4ff, #00b4d8);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 20;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        
        roomPane.lookupButton(javafx.scene.control.ButtonType.CANCEL).setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4a5568, #2d3748);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 20;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        
        java.util.Optional<String> roomResult = roomDialog.showAndWait();
        
        if (!roomResult.isPresent() || roomResult.get().trim().isEmpty()) return;
        
        try {
            int rooms = Integer.parseInt(roomResult.get().trim());
            
            if (rooms <= 0) {
                showError("Number of rooms must be greater than 0.");
                return;
            }
            
            // Ask for gender with modern styling
            javafx.scene.control.ChoiceDialog<String> genderDialog = new javafx.scene.control.ChoiceDialog<>("Male", "Male", "Female");
            genderDialog.setTitle("Add New Building");
            genderDialog.setHeaderText("👥  Select Gender for " + blockName);
            genderDialog.setContentText("Building Gender:");
            
            // Apply modern dark theme styling
            javafx.scene.control.DialogPane genderPane = genderDialog.getDialogPane();
            genderPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0f2744, #0a1929);" +
                "-fx-border-color: #4ecdc4;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(78, 205, 196, 0.5), 20, 0.3, 0, 0);"
            );
            
            if (genderPane.lookup(".header-panel") != null) {
                genderPane.lookup(".header-panel").setStyle("-fx-background-color: transparent; -fx-padding: 20 20 10 20;");
            }
            
            javafx.scene.control.Label genderHeaderLabel = (javafx.scene.control.Label) genderPane.lookup(".header .label");
            if (genderHeaderLabel != null) {
                genderHeaderLabel.setStyle("-fx-text-fill: #4ecdc4; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
            
            if (genderPane.lookup(".content") != null) {
                genderPane.lookup(".content").setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px; -fx-padding: 10 20 20 20;");
            }
            
            // Style buttons
            genderPane.lookupButton(javafx.scene.control.ButtonType.OK).setStyle(
                "-fx-background-color: linear-gradient(to bottom, #5fdc73, #4cd964);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 20;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
            
            genderPane.lookupButton(javafx.scene.control.ButtonType.CANCEL).setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4a5568, #2d3748);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 20;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
            
            java.util.Optional<String> genderResult = genderDialog.showAndWait();
            
            if (!genderResult.isPresent()) return;
            
            String gender = genderResult.get();
            models.Building newBuilding = new models.Building(blockName, rooms, gender);
            
            java.util.List<models.Building> currentBuildings = data.DataManager.getInstance().getBuildings();
            currentBuildings.add(newBuilding);
            data.DataManager.getInstance().setBuildings(currentBuildings);
            
            // Show success message
            javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText("✓  Building Added");
            successAlert.setContentText(blockName + " has been successfully created with " + rooms + " rooms for " + gender + " students.");
            
            javafx.scene.control.DialogPane successPane = successAlert.getDialogPane();
            successPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0f2744, #0a1929);" +
                "-fx-border-color: #4cd964;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(76, 217, 100, 0.5), 20, 0.3, 0, 0);"
            );
            
            if (successPane.lookup(".header-panel") != null) {
                successPane.lookup(".header-panel").setStyle("-fx-background-color: transparent; -fx-padding: 20 20 10 20;");
            }
            
            javafx.scene.control.Label successHeaderLabel = (javafx.scene.control.Label) successPane.lookup(".header .label");
            if (successHeaderLabel != null) {
                successHeaderLabel.setStyle("-fx-text-fill: #4cd964; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
            
            if (successPane.lookup(".content") != null) {
                successPane.lookup(".content").setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px; -fx-padding: 10 20 20 20;");
            }
            
            successPane.lookupButton(javafx.scene.control.ButtonType.OK).setStyle(
                "-fx-background-color: linear-gradient(to bottom, #5fdc73, #4cd964);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 20;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
            
            successAlert.showAndWait();
            
            // Refresh Dashboard
            initialize();
            
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for rooms.");
        }
    }
    
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("⚠️  Invalid Input");
        alert.setContentText(message);
        
        // Apply modern dark theme styling
        javafx.scene.control.DialogPane errorPane = alert.getDialogPane();
        errorPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0f2744, #0a1929);" +
            "-fx-border-color: #ff6b6b;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(255, 107, 107, 0.5), 20, 0.3, 0, 0);"
        );
        
        if (errorPane.lookup(".header-panel") != null) {
            errorPane.lookup(".header-panel").setStyle("-fx-background-color: transparent; -fx-padding: 20 20 10 20;");
        }
        
        javafx.scene.control.Label errorHeaderLabel = (javafx.scene.control.Label) errorPane.lookup(".header .label");
        if (errorHeaderLabel != null) {
            errorHeaderLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 18px; -fx-font-weight: bold;");
        }
        
        if (errorPane.lookup(".content") != null) {
            errorPane.lookup(".content").setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px; -fx-padding: 10 20 20 20;");
        }
        
        errorPane.lookupButton(javafx.scene.control.ButtonType.OK).setStyle(
            "-fx-background-color: linear-gradient(to bottom, #ff8585, #ff6b6b);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 20;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        
        alert.showAndWait();
    }

    @FXML
    void handleTotalClick(javafx.scene.input.MouseEvent event) {
        openStudentsView("ALL", "");
    }

    @FXML
    void handleAssignedClick(javafx.scene.input.MouseEvent event) {
        openStudentsView("ASSIGNED", "");
    }

    @FXML
    void handleUnassignedClick(javafx.scene.input.MouseEvent event) {
        openStudentsView("UNASSIGNED", "");
    }

    private void openStudentsView(String type, String value) {
        ProctorStudentsController.filterType = type;
        ProctorStudentsController.filterValue = value;
        // We can't easily pass event here since it might be MouseEvent or ActionEvent
        // So we'll just use the stage from one of the labels
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/ProctorStudentsView.fxml"));
            Stage stage = (Stage) totalStudentsLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        utils.NavigationUtils.navigateTo(event, "/resources/LandingPage.fxml");
    }

    @FXML
    void handleStudentsView(ActionEvent event) {
        openStudentsView("ALL", "");
    }

    @FXML
    void handleSettingsView(ActionEvent event) {
        navigateTo(event, "/resources/ProctorSettings.fxml");
    }
    
    @FXML
    void handleFilterAll(ActionEvent event) {
        currentFilter = "ALL";
        updateFilterButtons();
        initialize();
    }
    
    @FXML
    void handleFilterMale(ActionEvent event) {
        currentFilter = "MALE";
        updateFilterButtons();
        initialize();
    }
    
    @FXML
    void handleFilterFemale(ActionEvent event) {
        currentFilter = "FEMALE";
        updateFilterButtons();
        initialize();
    }
    
    private void updateFilterButtons() {
        // Reset all buttons to inactive style
        String inactiveStyle = "-fx-background-color: #e0e0e0; -fx-text-fill: #2b5876; -fx-background-radius: 8; -fx-font-weight: bold;";
        String activeStyle = "-fx-background-color: #4ecdc4; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;";
        
        allBlocksBtn.setStyle(inactiveStyle);
        maleBlocksBtn.setStyle(inactiveStyle);
        femaleBlocksBtn.setStyle(inactiveStyle);
        
        // Set active button style
        switch (currentFilter) {
            case "ALL":
                allBlocksBtn.setStyle(activeStyle);
                break;
            case "MALE":
                maleBlocksBtn.setStyle(activeStyle);
                break;
            case "FEMALE":
                femaleBlocksBtn.setStyle(activeStyle);
                break;
        }
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + fxmlPath);
        }
    }
}
