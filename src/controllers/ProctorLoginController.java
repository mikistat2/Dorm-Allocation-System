package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ProctorLoginController {

    @FXML
    private TextField proctorIdField;

    @FXML
    private PasswordField proctorPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/LandingPage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String id = proctorIdField.getText();
        String password = proctorPasswordField.getText();

        boolean loginSuccess = false;
        for (models.Proctor p : data.DataManager.getInstance().getProctors()) {
            if (p.getId().equals(id) && p.getPassword().equals(password)) {
                loginSuccess = true;
                break;
            }
        }

        if (loginSuccess) {
            navigateToDashboard(event);
        } else {
            messageLabel.setText("Invalid Proctor Credentials.");
        }
    }
    
    
    private void navigateToDashboard(ActionEvent event) {
        utils.NavigationUtils.navigateTo(event, "/resources/ProctorDashboard.fxml");
    }
}
