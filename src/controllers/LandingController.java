package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import utils.NavigationUtils;

public class LandingController {

    @FXML
    private Button studentButton;

    @FXML
    private Button proctorButton;

    @FXML
    void handleStudentButton(ActionEvent event) {
        NavigationUtils.navigateTo(event, "/resources/StudentAuth.fxml");
    }

    @FXML
    void handleProctorButton(ActionEvent event) {
        NavigationUtils.navigateTo(event, "/resources/ProctorLogin.fxml");
    }
}
