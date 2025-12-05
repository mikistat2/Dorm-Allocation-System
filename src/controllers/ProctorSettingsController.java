package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProctorSettingsController {

    @FXML
    private TextField numBuildingsField;

    @FXML
    private TextField numRoomsField;

    @FXML
    private Label messageLabel;

    @FXML
    void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/ProctorDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSaveSettings(ActionEvent event) {
        try {
            int buildingsCount = Integer.parseInt(numBuildingsField.getText());
            int roomsPerBuilding = Integer.parseInt(numRoomsField.getText());

            if (buildingsCount <= 0 || roomsPerBuilding <= 0) {
                messageLabel.setStyle("-fx-text-fill: #ff6b6b;");
                messageLabel.setText("Please enter positive numbers.");
                return;
            }

            List<models.Building> buildings = new ArrayList<>();
            
            // Create dialog for each building to select gender
            for (int i = 1; i <= buildingsCount; i++) {
                String buildingName = "Block " + i;
                
                // Create choice dialog for gender selection
                javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>("Male", "Male", "Female");
                dialog.setTitle("Building Gender Selection");
                dialog.setHeaderText("Configure " + buildingName);
                dialog.setContentText("Select gender for " + buildingName + ":");
                
                // Style the dialog
                dialog.getDialogPane().setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #0f2744, #0a1929);" +
                    "-fx-border-color: #00b4d8;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;"
                );
                
                if (dialog.getDialogPane().lookup(".header-panel") != null) {
                    dialog.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: transparent; -fx-padding: 20;");
                }
                
                javafx.scene.control.Label headerLabel = (javafx.scene.control.Label) dialog.getDialogPane().lookup(".header .label");
                if (headerLabel != null) {
                    headerLabel.setStyle("-fx-text-fill: #00b4d8; -fx-font-size: 16px; -fx-font-weight: bold;");
                }
                
                if (dialog.getDialogPane().lookup(".content") != null) {
                    dialog.getDialogPane().lookup(".content").setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px;");
                }
                
                // Show dialog and get result
                java.util.Optional<String> result = dialog.showAndWait();
                
                if (result.isPresent()) {
                    String gender = result.get();
                    buildings.add(new models.Building(buildingName, roomsPerBuilding, gender));
                } else {
                    // User cancelled, stop creating buildings
                    messageLabel.setStyle("-fx-text-fill: #ff9500;");
                    messageLabel.setText("Configuration cancelled. Created " + buildings.size() + " blocks.");
                    if (!buildings.isEmpty()) {
                        data.DataManager.getInstance().setBuildings(buildings);
                    }
                    return;
                }
            }

            data.DataManager.getInstance().setBuildings(buildings);
            
            messageLabel.setStyle("-fx-text-fill: #4cd964;");
            messageLabel.setText("✓ Configuration Saved! " + buildingsCount + " Blocks created.");
        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            messageLabel.setText("Please enter valid numbers.");
        }
    }
}
