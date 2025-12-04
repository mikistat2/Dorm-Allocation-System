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

    @FXML
    private TableColumn<models.Student, Void> colActions;

    // Filter State
    public static String filterType = "ALL"; // ALL, ASSIGNED, UNASSIGNED, BUILDING
    public static String filterValue = "";

    @FXML
    public void initialize() {
        // Make table editable
        studentsTable.setEditable(true);
        
        // Name column - editable
        colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colName.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(event -> {
            models.Student student = event.getRowValue();
            student.setName(event.getNewValue());
            data.DataManager.getInstance().saveStudents();
        });
        
        // ID column - read only (shouldn't change student ID)
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        
        // Department column - editable
        colDept.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("department"));
        colDept.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        colDept.setOnEditCommit(event -> {
            models.Student student = event.getRowValue();
            student.setDepartment(event.getNewValue());
            data.DataManager.getInstance().saveStudents();
        });
        
        // Year column - editable
        colYear.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("year"));
        colYear.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        colYear.setOnEditCommit(event -> {
            models.Student student = event.getRowValue();
            student.setYear(event.getNewValue());
            data.DataManager.getInstance().saveStudents();
        });
        
        // Gender column - editable with dropdown
        colGender.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("gender"));
        colGender.setCellFactory(javafx.scene.control.cell.ComboBoxTableCell.forTableColumn("Male", "Female"));
        colGender.setOnEditCommit(event -> {
            models.Student student = event.getRowValue();
            student.setGender(event.getNewValue());
            data.DataManager.getInstance().saveStudents();
        });
        
        // Building column - editable with dropdown
        colBuilding.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("assignedBuilding"));
        colBuilding.setCellFactory(column -> new javafx.scene.control.TableCell<models.Student, String>() {
            private javafx.scene.control.ComboBox<String> comboBox;
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (comboBox == null) {
                        comboBox = new javafx.scene.control.ComboBox<>();
                        comboBox.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFFFFF;");
                        comboBox.setOnAction(e -> {
                            models.Student student = getTableView().getItems().get(getIndex());
                            String newBuilding = comboBox.getValue();
                            updateStudentBuilding(student, newBuilding);
                        });
                    }
                    
                    comboBox.getItems().clear();
                    comboBox.getItems().add("Not Assigned");
                    
                    models.Student student = getTableView().getItems().get(getIndex());
                    for (models.Building b : data.DataManager.getInstance().getBuildings()) {
                        if (b.getGender().equals(student.getGender())) {
                            comboBox.getItems().add(b.getName());
                        }
                    }
                    
                    comboBox.setValue(item);
                    setGraphic(comboBox);
                }
            }
        });
        
        // Room column - editable with dropdown
        colRoom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("assignedRoom"));
        colRoom.setCellFactory(column -> new javafx.scene.control.TableCell<models.Student, String>() {
            private javafx.scene.control.ComboBox<String> comboBox;
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (comboBox == null) {
                        comboBox = new javafx.scene.control.ComboBox<>();
                        comboBox.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFFFFF;");
                        comboBox.setOnAction(e -> {
                            models.Student student = getTableView().getItems().get(getIndex());
                            String newRoom = comboBox.getValue();
                            updateStudentRoom(student, newRoom);
                        });
                    }
                    
                    comboBox.getItems().clear();
                    models.Student student = getTableView().getItems().get(getIndex());
                    
                    if (!"Not Assigned".equals(student.getAssignedBuilding())) {
                        for (models.Building b : data.DataManager.getInstance().getBuildings()) {
                            if (b.getName().equals(student.getAssignedBuilding())) {
                                for (models.Room r : b.getRooms()) {
                                    comboBox.getItems().add(r.getRoomNumber());
                                }
                                break;
                            }
                        }
                    }
                    
                    comboBox.setValue(item);
                    setGraphic(comboBox);
                }
            }
        });

        // Add Edit icon button to Actions column
        colActions.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button editBtn = new javafx.scene.control.Button("✏");

            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CC9F0; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2;");
                editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: rgba(76, 201, 240, 0.2); -fx-text-fill: #4CC9F0; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2; -fx-background-radius: 4;"));
                editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CC9F0; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2;"));
                editBtn.setOnAction(event -> {
                    // Enable editing mode for the row
                    int index = getIndex();
                    studentsTable.edit(index, colName);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editBtn);
                }
            }
        });

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
    
    private void updateStudentBuilding(models.Student student, String newBuilding) {
        String oldBuilding = student.getAssignedBuilding();
        String oldRoom = student.getAssignedRoom();
        
        // Remove from old room
        if (!"Not Assigned".equals(oldBuilding)) {
            for (models.Building b : data.DataManager.getInstance().getBuildings()) {
                if (b.getName().equals(oldBuilding)) {
                    for (models.Room r : b.getRooms()) {
                        if (r.getRoomNumber().equals(oldRoom)) {
                            r.removeStudent(student.getId());
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        // Update building
        if ("Not Assigned".equals(newBuilding)) {
            student.setAssignedBuilding("Not Assigned");
            student.setAssignedRoom("");
        } else {
            student.setAssignedBuilding(newBuilding);
            student.setAssignedRoom("");
        }
        
        data.DataManager.getInstance().saveStudents();
        loadStudents();
    }
    
    private void updateStudentRoom(models.Student student, String newRoom) {
        String building = student.getAssignedBuilding();
        String oldRoom = student.getAssignedRoom();
        
        if (!"Not Assigned".equals(building)) {
            for (models.Building b : data.DataManager.getInstance().getBuildings()) {
                if (b.getName().equals(building)) {
                    // Remove from old room
                    if (oldRoom != null && !oldRoom.isEmpty()) {
                        for (models.Room r : b.getRooms()) {
                            if (r.getRoomNumber().equals(oldRoom)) {
                                r.removeStudent(student.getId());
                                break;
                            }
                        }
                    }
                    
                    // Add to new room
                    for (models.Room r : b.getRooms()) {
                        if (r.getRoomNumber().equals(newRoom)) {
                            r.addStudent(student.getId());
                            student.setAssignedRoom(newRoom);
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        data.DataManager.getInstance().saveStudents();
        loadStudents();
    }

    @FXML
    void handleBack(ActionEvent event) {
        // Use NavigationUtils to preserve current scene and window size
        utils.NavigationUtils.navigateTo(event, "/resources/ProctorDashboard.fxml");
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
