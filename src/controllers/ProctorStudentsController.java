package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;

public class ProctorStudentsController {

    @FXML
    private TableView<models.Student> studentsTable;

    @FXML
    private TableColumn<models.Student, String> colName;

    @FXML
    private TableColumn<models.Student, String> colId;

    @FXML
    private TableColumn<models.Student, String> colDept;

    @FXML
    private TableColumn<models.Student, String> colYear;

    @FXML
    private TableColumn<models.Student, String> colGender;

    @FXML
    private TableColumn<models.Student, String> colBuilding;

    @FXML
    private TableColumn<models.Student, String> colRoom;

    // Filter State
    public static String filterType = "ALL"; // ALL, ASSIGNED, UNASSIGNED, BUILDING
    public static String filterValue = "";

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colDept.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("department"));
        colYear.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("year"));
        colGender.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("gender"));
        colBuilding.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("assignedBuilding"));
        colRoom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("assignedRoom"));

        loadStudents();
    }

    private void loadStudents() {
        java.util.List<models.Student> allStudents = data.DataManager.getInstance().getStudents();
        java.util.List<models.Student> filteredList = new java.util.ArrayList<>();

        for (models.Student s : allStudents) {
            boolean matches = false;
            switch (filterType) {
                case "ASSIGNED":
                    matches = !"Not Assigned".equals(s.getAssignedBuilding());
                    break;
                case "UNASSIGNED":
                    matches = "Not Assigned".equals(s.getAssignedBuilding());
                    break;
                case "BUILDING":
                    matches = s.getAssignedBuilding().equals(filterValue);
                    break;
                case "ALL":
                default:
                    matches = true;
                    break;
            }
            if (matches) {
                filteredList.add(s);
            }
        }

        studentsTable.setItems(javafx.collections.FXCollections.observableArrayList(filteredList));
    }

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
    void handleAutoAssign(ActionEvent event) {
        java.util.List<models.Student> students = data.DataManager.getInstance().getStudents();
        java.util.List<models.Building> buildings = data.DataManager.getInstance().getBuildings();

        if (buildings.isEmpty()) {
            System.out.println("No buildings configured!");
            return;
        }

        for (models.Student s : students) {
            if ("Not Assigned".equals(s.getAssignedBuilding())) {
                boolean assigned = false;
                // Filter buildings by student gender
                for (models.Building b : buildings) {
                    // Only consider buildings matching student's gender
                    if (!b.getGender().equals(s.getGender())) {
                        continue;
                    }
                    
                    for (models.Room r : b.getRooms()) {
                        if (!r.isFull()) {
                            r.addStudent(s.getId());
                            s.setAssignedBuilding(b.getName());
                            s.setAssignedRoom(r.getRoomNumber());
                            assigned = true;
                            break;
                        }
                    }
                    if (assigned) break;
                }
                
                if (!assigned) {
                    System.out.println("Could not assign student " + s.getName() + " (" + s.getGender() + ") - no available " + s.getGender() + " buildings");
                }
            }
        }
        data.DataManager.getInstance().saveStudents();
        loadStudents(); // Refresh table
        System.out.println("Auto Assign Completed");
    }
}
