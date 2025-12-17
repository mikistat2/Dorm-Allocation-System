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
    private javafx.scene.control.ComboBox<String> genderComboBox;

    @FXML
    private TextField numRoomsField;

    @FXML
    private Label messageLabel;

    @FXML
    void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/ProctorDashboardEnhanced.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        if (genderComboBox != null) {
            genderComboBox.getItems().addAll("Male", "Female");
            genderComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    void handleSaveSettings(ActionEvent event) {
        try {
            int roomsPerBuilding = Integer.parseInt(numRoomsField.getText());
            String gender = genderComboBox.getValue();

            if (roomsPerBuilding <= 0) {
                messageLabel.setStyle("-fx-text-fill: #ff6b6b;");
                messageLabel.setText("Please enter a valid number of rooms.");
                return;
            }

            if (gender == null || gender.isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: #ff6b6b;");
                messageLabel.setText("Please select a gender.");
                return;
            }

            // Load existing buildings to append to them
            List<models.Building> existingBuildings = new ArrayList<>(data.DataManager.getInstance().getBuildings());

            // Determine the next block number
            int nextBlockNum = 1;
            for (models.Building b : existingBuildings) {
                try {
                    String name = b.getName(); // e.g., "Block 1"
                    if (name.startsWith("Block ")) {
                        int num = Integer.parseInt(name.substring(6).trim());
                        if (num >= nextBlockNum) {
                            nextBlockNum = num + 1;
                        }
                    }
                } catch (Exception ignored) {
                    // Ignore parsing errors for non-standard names
                }
            }

            String newBuildingName = "Block " + nextBlockNum;
            models.Building newBuilding = new models.Building(newBuildingName, roomsPerBuilding, gender);

            existingBuildings.add(newBuilding);
            data.DataManager.getInstance().setBuildings(existingBuildings);

            messageLabel.setStyle("-fx-text-fill: #4cd964;");
            messageLabel.setText("✓ " + newBuildingName + " (" + gender + ") added!");

            // Clear fields for next entry
            // numRoomsField.clear();
            // Keep room number as user might want to add multiple similar blocks

        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            messageLabel.setText("Please enter valid numbers.");
        }
    }
}
