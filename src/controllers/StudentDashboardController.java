package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class StudentDashboardController {

    @FXML
    private Label studentNameLabel;

    @FXML
    private Label studentIdLabel;

    @FXML
    private Label studentDeptLabel;

    @FXML
    private Label buildingLabel;

    @FXML
    private Label roomLabel;

    @FXML
    private ListView<String> roommatesListView;

    @FXML
    public void initialize() {
        models.Student current = SessionManager.getCurrentStudent();
        if (current != null) {
            studentNameLabel.setText(current.getName());
            studentIdLabel.setText("ID: " + current.getId());
            studentDeptLabel.setText("Dept: " + current.getDepartment());
            buildingLabel.setText(current.getAssignedBuilding());
            roomLabel.setText(current.getAssignedRoom());

            loadRoommates(current);
        }
    }

    private void loadRoommates(models.Student current) {
        roommatesListView.getItems().clear();
        if ("Not Assigned".equals(current.getAssignedBuilding())) {
            roommatesListView.getItems().add("No roommates - Not assigned to a room yet");
            return;
        }

        int roommateCount = 0;
        for (models.Student s : data.DataManager.getInstance().getStudents()) {
            if (s.getAssignedBuilding().equals(current.getAssignedBuilding()) &&
                s.getAssignedRoom().equals(current.getAssignedRoom()) &&
                !s.getId().equals(current.getId())) {
                
                // Format: Name | ID: xxx | Dept: xxx | Year: x | Phone: xxx
                String roommateInfo = String.format("👤 %s  |  ID: %s  |  %s  |  Year %s  |  📞 %s",
                    s.getName(),
                    s.getId(),
                    s.getDepartment(),
                    s.getYear(),
                    s.getPhone()
                );
                
                roommatesListView.getItems().add(roommateInfo);
                roommateCount++;
            }
        }
        
        if (roommateCount == 0) {
            roommatesListView.getItems().add("No roommates - You have this room to yourself");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        utils.NavigationUtils.navigateTo(event, "/resources/LandingPage.fxml");
    }
}
