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
    void handleExportPDF(ActionEvent event) {
        if (studentsTable.getItems().isEmpty()) {
            showAlert("No Data", "There are no students to export.", javafx.scene.control.Alert.AlertType.WARNING);
            return;
        }
        
        // Use JavaFX PrinterJob to print/save as PDF
        javafx.print.PrinterJob printerJob = javafx.print.PrinterJob.createPrinterJob();
        
        if (printerJob != null) {
            // Show print dialog (user can select "Print to PDF")
            boolean proceed = printerJob.showPrintDialog(studentsTable.getScene().getWindow());
            
            if (proceed) {
                // Create a snapshot of the table for printing
                javafx.scene.layout.VBox printContent = new javafx.scene.layout.VBox(15);
                printContent.setPadding(new javafx.geometry.Insets(20));
                
                // Title
                javafx.scene.text.Text title = new javafx.scene.text.Text("Student Allocation Report");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                
                // Timestamp
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                javafx.scene.text.Text timestamp = new javafx.scene.text.Text("Generated: " + dateFormat.format(new java.util.Date()));
                timestamp.setStyle("-fx-font-size: 10px;");
                
                // Student count
                javafx.scene.text.Text count = new javafx.scene.text.Text("Total Students: " + studentsTable.getItems().size());
                count.setStyle("-fx-font-size: 10px;");
                
                // Create text representation of table
                javafx.scene.layout.VBox tableText = new javafx.scene.layout.VBox(5);
                
                // Header
                String header = String.format("%-20s %-12s %-15s %-6s %-8s %-20s %-20s", 
                    "Name", "ID", "Department", "Year", "Gender", "Building", "Room");
                javafx.scene.text.Text headerText = new javafx.scene.text.Text(header);
                headerText.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 9px; -fx-font-weight: bold;");
                tableText.getChildren().add(headerText);
                
                // Separator
                javafx.scene.text.Text separator = new javafx.scene.text.Text("─".repeat(110));
                separator.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 9px;");
                tableText.getChildren().add(separator);
                
                // Rows
                for (models.Student student : studentsTable.getItems()) {
                    String row = String.format("%-20s %-12s %-15s %-6s %-8s %-20s %-20s",
                        truncate(student.getName(), 20),
                        student.getId(),
                        truncate(student.getDepartment(), 15),
                        student.getYear(),
                        student.getGender(),
                        truncate(student.getAssignedBuilding(), 20),
                        truncate(student.getAssignedRoom(), 20)
                    );
                    javafx.scene.text.Text rowText = new javafx.scene.text.Text(row);
                    rowText.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 8px;");
                    tableText.getChildren().add(rowText);
                }
                
                printContent.getChildren().addAll(title, timestamp, count, new javafx.scene.text.Text(""), tableText);
                
                // Print
                boolean printed = printerJob.printPage(printContent);
                
                if (printed) {
                    printerJob.endJob();
                    showAlert("Success", "Document sent to printer/PDF successfully!", 
                             javafx.scene.control.Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to print document.", 
                             javafx.scene.control.Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Error", "No printer available. Please install a PDF printer (e.g., Microsoft Print to PDF).", 
                     javafx.scene.control.Alert.AlertType.ERROR);
        }
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    private void showAlert(String title, String content, javafx.scene.control.Alert.AlertType type) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Style the dialog
        alert.getDialogPane().setStyle("-fx-background-color: #0A1A2F;");
        if (alert.getDialogPane().lookup(".header-panel") != null) {
            alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #0A1A2F;");
        }
        if (alert.getDialogPane().lookup(".content") != null) {
            alert.getDialogPane().lookup(".content").setStyle("-fx-text-fill: #FFFFFF;");
        }
        
        alert.showAndWait();
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
