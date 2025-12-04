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

            List<models.Building> buildings = new ArrayList<>();
            for (int i = 1; i <= buildingsCount; i++) {
                // Default to Male for buildings created through settings
                // Recommend using Add Block feature from dashboard for gender-specific buildings
                buildings.add(new models.Building("Block " + i, roomsPerBuilding, "Male"));
            }

            data.DataManager.getInstance().setBuildings(buildings);
            
            messageLabel.setText("Configuration Saved! " + buildingsCount + " Blocks created.");
        } catch (NumberFormatException e) {
            messageLabel.setText("Please enter valid numbers.");
        }
    }
}
