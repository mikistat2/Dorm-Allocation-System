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

    // Track which row is being edited
    private int editingRowIndex = -1;

    @FXML
    public void initialize() {
        // Make table editable but cells will control their own edit state
        studentsTable.setEditable(true);
        
        // All columns use standard cell value factories
        colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colDept.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("department"));
        colYear.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("year"));
        colGender.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("gender"));
        colBuilding.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("assignedBuilding"));
        colRoom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("assignedRoom"));

        // Add Edit and Delete buttons to Actions column
        colActions.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button editBtn = new javafx.scene.control.Button("✏");
            private final javafx.scene.control.Button saveBtn = new javafx.scene.control.Button("✓");
            private final javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button("🗑");
            private final javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(5);

            {
                // Use CSS classes for consistent icon buttons
                editBtn.getStyleClass().addAll("icon-btn", "edit-button");
                saveBtn.getStyleClass().addAll("icon-btn", "save-button");
                deleteBtn.getStyleClass().addAll("icon-btn", "delete-button");
                
                editBtn.setOnAction(event -> {
                    int index = getIndex();
                    editingRowIndex = index;
                    studentsTable.refresh();
                });
                
                saveBtn.setOnAction(event -> {
                    editingRowIndex = -1;
                    data.DataManager.getInstance().saveStudents();
                    studentsTable.refresh();
                });
                
                deleteBtn.setOnAction(event -> {
                    models.Student student = getTableView().getItems().get(getIndex());
                    
                    // Confirmation dialog
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete Student");
                    alert.setHeaderText("Delete " + student.getName() + "?");
                    alert.setContentText("Are you sure you want to delete this student? This action cannot be undone.");
                    
                    // Style the dialog
                    alert.getDialogPane().setStyle("-fx-background-color: #0A1A2F;");
                    alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #0A1A2F;");
                    alert.getDialogPane().lookup(".content").setStyle("-fx-text-fill: #FFFFFF;");
                    
                    java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                        // Remove from room if assigned
                        if (!"Not Assigned".equals(student.getAssignedBuilding())) {
                            for (models.Building b : data.DataManager.getInstance().getBuildings()) {
                                if (b.getName().equals(student.getAssignedBuilding())) {
                                    for (models.Room r : b.getRooms()) {
                                        if (r.getRoomNumber().equals(student.getAssignedRoom())) {
                                            r.removeStudent(student.getId());
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        
                        // Remove student from list
                        data.DataManager.getInstance().getStudents().remove(student);
                        data.DataManager.getInstance().saveStudents();
                        loadStudents();
                    }
                });
                
                buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    int index = getIndex();
                    buttonBox.getChildren().clear();
                    if (index == editingRowIndex) {
                        buttonBox.getChildren().add(saveBtn);
                    } else {
                        buttonBox.getChildren().addAll(editBtn, deleteBtn);
                    }
                    setGraphic(buttonBox);
                }
            }
        });

        // Custom cell factories for editable columns
        setupEditableColumn(colName, "name");
        setupEditableColumn(colDept, "department");
        setupEditableColumn(colYear, "year");
        setupGenderColumn();
        setupBuildingColumn();
        setupRoomColumn();

        loadStudents();
    }
    
    private void setupEditableColumn(TableColumn<models.Student, String> column, String property) {
        column.setCellFactory(col -> new javafx.scene.control.TableCell<models.Student, String>() {
            private javafx.scene.control.TextField textField;
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int index = getIndex();
                    if (index == editingRowIndex && !property.equals("id")) {
                        if (textField == null) {
                            textField = new javafx.scene.control.TextField();
                            textField.getStyleClass().add("text-field");
                            textField.setOnAction(e -> commitEdit(textField.getText()));
                            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                                if (!isNowFocused) {
                                    commitEdit(textField.getText());
                                }
                            });
                        }
                        textField.setText(item);
                        setText(null);
                        setGraphic(textField);
                        setStyle("-fx-background-color: rgba(76, 201, 240, 0.1);");
                    } else {
                        setText(item);
                        setGraphic(null);
                        setStyle("");
                    }
                }
            }
            
            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                models.Student student = getTableView().getItems().get(getIndex());
                switch(property) {
                    case "name": student.setName(newValue); break;
                    case "department": student.setDepartment(newValue); break;
                    case "year": student.setYear(newValue); break;
                }
            }
        });
    }
    
    private void setupGenderColumn() {
        colGender.setCellFactory(col -> new javafx.scene.control.TableCell<models.Student, String>() {
            private javafx.scene.control.ComboBox<String> comboBox;
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int index = getIndex();
                    if (index == editingRowIndex) {
                        if (comboBox == null) {
                            comboBox = new javafx.scene.control.ComboBox<>();
                            comboBox.getItems().addAll("Male", "Female");
                            comboBox.getStyleClass().add("combo-box");
                            comboBox.setPrefHeight(32);
                            comboBox.setOnAction(e -> {
                                models.Student student = getTableView().getItems().get(getIndex());
                                student.setGender(comboBox.getValue());
                            });
                        }
                        comboBox.setValue(item);
                        setText(null);
                        setGraphic(comboBox);
                        setStyle("-fx-background-color: rgba(76, 201, 240, 0.1);");
                    } else {
                        setText(item);
                        setGraphic(null);
                        setStyle("");
                    }
                }
            }
        });
    }
    
    private void setupBuildingColumn() {
        colBuilding.setCellFactory(col -> new javafx.scene.control.TableCell<models.Student, String>() {
            private javafx.scene.control.ComboBox<String> comboBox;
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int index = getIndex();
                    if (index == editingRowIndex) {
                        if (comboBox == null) {
                            comboBox = new javafx.scene.control.ComboBox<>();
                            comboBox.getStyleClass().add("combo-box");
                            comboBox.setPrefHeight(32);
                            comboBox.setOnAction(e -> updateStudentBuilding(getTableView().getItems().get(getIndex()), comboBox.getValue()));
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
                        setText(null);
                        setGraphic(comboBox);
                        setStyle("-fx-background-color: rgba(76, 201, 240, 0.1);");
                    } else {
                        setText(item);
                        setGraphic(null);
                        setStyle("");
                    }
                }
            }
        });
    }
    
    private void setupRoomColumn() {
        colRoom.setCellFactory(col -> new javafx.scene.control.TableCell<models.Student, String>() {
            private javafx.scene.control.ComboBox<String> comboBox;
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int index = getIndex();
                    if (index == editingRowIndex) {
                        if (comboBox == null) {
                            comboBox = new javafx.scene.control.ComboBox<>();
                            comboBox.getStyleClass().add("combo-box");
                            comboBox.setPrefHeight(32);
                            comboBox.setOnAction(e -> updateStudentRoom(getTableView().getItems().get(getIndex()), comboBox.getValue()));
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
                        setText(null);
                        setGraphic(comboBox);
                        setStyle("-fx-background-color: rgba(76, 201, 240, 0.1);");
                    } else {
                        setText(item == null || item.isEmpty() ? "--" : item);
                        setGraphic(null);
                        setStyle("");
                    }
                }
            }
        });
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
        
        // Update building and auto-assign to first available room
        if ("Not Assigned".equals(newBuilding)) {
            student.setAssignedBuilding("Not Assigned");
            student.setAssignedRoom("");
        } else {
            student.setAssignedBuilding(newBuilding);
            // Find first available room in the new building
            boolean roomAssigned = false;
            for (models.Building b : data.DataManager.getInstance().getBuildings()) {
                if (b.getName().equals(newBuilding)) {
                    for (models.Room r : b.getRooms()) {
                        if (!r.isFull()) {
                            r.addStudent(student.getId());
                            student.setAssignedRoom(r.getRoomNumber());
                            roomAssigned = true;
                            break;
                        }
                    }
                    break;
                }
            }
            if (!roomAssigned) {
                // If no room available, clear the assignment
                student.setAssignedRoom("");
            }
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
