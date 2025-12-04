package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class StudentAuthController {

    @FXML
    private TabPane authTabPane;

    @FXML
    private TextField loginIdField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private Label loginMessageLabel;

    @FXML
    private TextField regNameField;

    @FXML
    private TextField regIdField;

    @FXML
    private PasswordField regPasswordField;

    @FXML
    private TextField regPhoneField;

    @FXML
    private TextField regDeptField;

    @FXML
    private TextField regYearField;

    @FXML
    private ComboBox<String> regGenderCombo;

    @FXML
    private Label regMessageLabel;

    @FXML
    public void initialize() {
        if (regGenderCombo != null) {
            regGenderCombo.setItems(FXCollections.observableArrayList("Male", "Female"));
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        utils.NavigationUtils.navigateTo(event, "/resources/LandingPage.fxml");
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String id = loginIdField.getText();
        String password = loginPasswordField.getText();

        if (id.isEmpty() || password.isEmpty()) {
            loginMessageLabel.setStyle("-fx-text-fill: #ff6b6b;"); // Red for error
            loginMessageLabel.setText("Please fill in all fields.");
            return;
        }

        for (models.Student s : data.DataManager.getInstance().getStudents()) {
            if (s.getId().equals(id) && s.getPassword().equals(password)) {
                // Login Success
                SessionManager.setCurrentStudent(s);
                navigateToDashboard(event);
                return;
            }
        }
        loginMessageLabel.setStyle("-fx-text-fill: #ff6b6b;"); // Red for error
        loginMessageLabel.setText("Invalid ID or Password.");
    }

    @FXML
    void handleRegister(ActionEvent event) {
        System.out.println("Register button clicked");
        try {
            regMessageLabel.setText(""); // Clear previous message
            
            String name = regNameField.getText();
            String id = regIdField.getText();
            String password = regPasswordField.getText();
            String phone = regPhoneField.getText();
            String dept = regDeptField.getText();
            String year = regYearField.getText();
            String gender = regGenderCombo.getValue();

            System.out.println("Inputs: " + name + ", " + id + ", " + gender);

            if (name == null || name.isEmpty() || 
                id == null || id.isEmpty() || 
                password == null || password.isEmpty() || 
                phone == null || phone.isEmpty() || 
                dept == null || dept.isEmpty() || 
                year == null || year.isEmpty() || 
                gender == null) {
                
                System.out.println("Validation failed: Empty fields");
                regMessageLabel.setStyle("-fx-text-fill: #ff6b6b;"); // Red for error
                regMessageLabel.setText("Please fill in all fields.");
                return;
            }

            // Check if ID exists
            for (models.Student s : data.DataManager.getInstance().getStudents()) {
                if (s.getId().equals(id)) {
                    System.out.println("Validation failed: Duplicate ID");
                    regMessageLabel.setStyle("-fx-text-fill: #ff6b6b;"); // Red for error
                    regMessageLabel.setText("Student ID already exists.");
                    return;
                }
            }

            models.Student newStudent = new models.Student(name, id, password, phone, dept, year, gender);
            data.DataManager.getInstance().addStudent(newStudent);
            
            System.out.println("Registration successful");
            regMessageLabel.setStyle("-fx-text-fill: #2ecc71;"); // Green for success
            regMessageLabel.setText("Registration Successful! Please Login.");
            
            // Switch to login tab (index 0)
            authTabPane.getSelectionModel().select(0);
            
            // Pre-fill login ID
            loginIdField.setText(id);
            loginMessageLabel.setStyle("-fx-text-fill: #2ecc71;"); // Green for success
            loginMessageLabel.setText("Registration Successful! Please Login.");
        } catch (Exception e) {
            e.printStackTrace();
            regMessageLabel.setStyle("-fx-text-fill: #ff6b6b;"); // Red for error
            regMessageLabel.setText("Error: " + e.getMessage());
        }
    }
    
    private void navigateToDashboard(ActionEvent event) {
        utils.NavigationUtils.navigateTo(event, "/resources/StudentDashboard.fxml");
    }
}
