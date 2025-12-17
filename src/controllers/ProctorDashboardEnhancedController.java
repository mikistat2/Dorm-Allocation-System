package controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Building;
import models.Student;
import data.DataManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProctorDashboardEnhancedController {

    @FXML
    private Label totalStudentsLabel;
    @FXML
    private Label assignedLabel;
    @FXML
    private Label unassignedLabel;
    @FXML
    private Label buildingsCountLabel;
    @FXML
    private Label occupancyLabel;
    @FXML
    private Label lastUpdatedLabel;

    @FXML
    private ProgressBar occupancyProgress;

    @FXML
    private javafx.scene.chart.PieChart studentPieChart;
    @FXML
    private TableView<Student> recentStudentsTable;
    @FXML
    private TextField searchField;

    @FXML
    private TilePane buildingsFlowPane;
    @FXML
    private Button allBlocksBtn;
    @FXML
    private Button maleBlocksBtn;
    @FXML
    private Button femaleBlocksBtn;

    @FXML
    private Button genderPieBtn;
    @FXML
    private Button yearPieBtn;
    @FXML
    private Button deptPieBtn;
    @FXML
    private FlowPane customLegend;

    private static final String[] PIE_COLORS = {
            "#00D9FF", // Bright Cyan
            "#FF006E", // Hot Pink
            "#8338EC", // Vivid Purple
            "#3A86FF", // Electric Blue
            "#FFBE0B", // Golden Yellow
            "#FB5607", // Bright Orange
            "#06FFA5", // Neon Green
            "#FF4D6D" // Coral Red
    };

    private static final String[] PIE_COLOR_CLASSES = {
            "pie-chart-blue",
            "pie-chart-pink",
            "pie-chart-purple",
            "pie-chart-blue-2",
            "pie-chart-yellow",
            "pie-chart-orange",
            "pie-chart-green",
            "pie-chart-red"
    };

    private String currentFilter = "ALL";
    private String currentPieFilter = "GENDER";
    private ObservableList<Student> allStudents;

    @FXML
    public void initialize() {
        loadStatistics();

        loadPieChartData("GENDER");
        loadBuildingCards();
        loadRecentStudents();
        updateLastUpdated();
        updateFilterButtons();
        updatePieFilterButtons();

        if (buildingsFlowPane != null) {
            // Bind tile width to container width to ensure exactly 3 columns with gaps
            buildingsFlowPane.prefTileWidthProperty().bind(
                    buildingsFlowPane.widthProperty().divide(3).subtract(15) // Subtracting gap/padding approximation
            );
            buildingsFlowPane.setPrefColumns(3);
        }

        if (studentPieChart != null) {
            // Re-apply colors when the chart is attached to a scene
            studentPieChart.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    refreshPieChartColors(studentPieChart.getData());
                    forcePieColorsHard();
                }
            });

            // Re-apply colors on data list mutations
            studentPieChart.getData()
                    .addListener((ListChangeListener<javafx.scene.chart.PieChart.Data>) change -> forcePieColorsHard());
        }

        // Add search functionality
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterStudents(newValue);
            });
        }
    }

    private void loadStatistics() {
        List<Student> students = DataManager.getInstance().getStudents();
        List<Building> buildings = DataManager.getInstance().getBuildings();

        int total = students.size();
        int assigned = 0;
        int unassigned = 0;
        int totalCapacity = 0;

        // Calculate total capacity
        for (Building building : buildings) {
            totalCapacity += building.getRooms().size();
        }

        // Count assigned/unassigned
        for (Student student : students) {
            if ("Not Assigned".equals(student.getAssignedBuilding())) {
                unassigned++;
            } else {
                assigned++;
            }
        }

        // Update labels
        totalStudentsLabel.setText(String.valueOf(total));
        assignedLabel.setText(String.valueOf(assigned));
        unassignedLabel.setText(String.valueOf(unassigned));
        buildingsCountLabel.setText(String.valueOf(buildings.size()));

        // Calculate occupancy
        double occupancy = totalCapacity > 0 ? (double) assigned / totalCapacity : 0;
        occupancyProgress.setProgress(occupancy);
        occupancyLabel.setText(String.format("%.0f%% Occupancy", occupancy * 100));
    }

    private void loadRecentStudents() {
        if (recentStudentsTable == null)
            return;

        List<Student> students = DataManager.getInstance().getStudents();
        allStudents = FXCollections.observableArrayList(students);

        // Show only first 10 students
        ObservableList<Student> recentStudents = FXCollections.observableArrayList(
                students.subList(0, Math.min(10, students.size())));

        recentStudentsTable.setItems(recentStudents);

        // Configure columns if not already configured
        if (recentStudentsTable.getColumns().size() > 0) {
            recentStudentsTable.getColumns().get(0).setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("name"));
            recentStudentsTable.getColumns().get(1).setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("id"));
            recentStudentsTable.getColumns().get(2).setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("gender"));
            recentStudentsTable.getColumns().get(3).setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("year"));
            recentStudentsTable.getColumns().get(4).setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("assignedRoom"));
            recentStudentsTable.getColumns().get(5).setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("assignedBuilding"));
            recentStudentsTable.getColumns().get(6).setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("assignedBuilding"));
        }
    }

    private void filterStudents(String searchText) {
        if (recentStudentsTable == null || allStudents == null)
            return;

        if (searchText == null || searchText.trim().isEmpty()) {
            recentStudentsTable.setItems(FXCollections.observableArrayList(
                    allStudents.subList(0, Math.min(10, allStudents.size()))));
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        ObservableList<Student> filteredList = FXCollections.observableArrayList();

        for (Student student : allStudents) {
            String fullName = student.getName().toLowerCase();
            String studentId = student.getId().toLowerCase();

            if (fullName.contains(lowerCaseFilter) || studentId.contains(lowerCaseFilter)) {
                filteredList.add(student);
                if (filteredList.size() >= 10)
                    break;
            }
        }

        recentStudentsTable.setItems(filteredList);
    }

    private void loadBuildingCards() {
        if (buildingsFlowPane == null)
            return;

        buildingsFlowPane.getChildren().clear();
        List<Building> buildings = DataManager.getInstance().getBuildings();
        List<Student> students = DataManager.getInstance().getStudents();

        Map<String, Integer> buildingCounts = new HashMap<>();
        for (Building building : buildings) {
            buildingCounts.put(building.getName(), 0);
        }

        for (Student student : students) {
            String building = student.getAssignedBuilding();
            if (!"Not Assigned".equals(building)) {
                buildingCounts.put(building, buildingCounts.getOrDefault(building, 0) + 1);
            }
        }

        for (Building building : buildings) {
            String gender = building.getGender();

            // Apply filter
            if (!currentFilter.equals("ALL")) {
                if (currentFilter.equals("MALE") && !gender.equalsIgnoreCase("Male"))
                    continue;
                if (currentFilter.equals("FEMALE") && !gender.equalsIgnoreCase("Female"))
                    continue;
            }

            VBox card = createBuildingCard(
                    building.getName(),
                    buildingCounts.get(building.getName()),
                    building.getRooms().size(),
                    gender);

            buildingsFlowPane.getChildren().add(card);
        }

        // Add the special "Add Building" card at the end
        buildingsFlowPane.getChildren().add(createAddBuildingCard());
    }

    private VBox createBuildingCard(String name, int studentCount, int maxRooms, String gender) {
        VBox card = new VBox();
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setSpacing(8);
        card.setPadding(new javafx.geometry.Insets(15));
        card.getStyleClass().add("building-card");

        // Icon
        Label iconLabel = new Label("🏢");
        iconLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: linear-gradient(to bottom, #E0F7FA, #B2EBF2);");
        // Add a subtle glow/shadow to the icon
        iconLabel.setEffect(
                new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.web("rgba(76, 201, 240, 0.3)")));

        // Building Name
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("card-title-large");

        // Student Count
        Label countLabel = new Label(studentCount + " Students");
        countLabel.getStyleClass().add("card-text-highlight");

        // Max Rooms (Muted with larger font than before)
        Label roomsLabel = new Label("Max Rooms: " + maxRooms);
        roomsLabel.getStyleClass().add("card-text-muted");

        // Gender (Using Highlight color)
        Label genderLabel = new Label("Gender: " + gender);
        genderLabel.getStyleClass().add("card-text-highlight");

        card.getChildren().addAll(iconLabel, nameLabel, countLabel, roomsLabel, genderLabel);

        card.setOnMouseClicked(e -> {
            ProctorStudentsController.filterType = "BUILDING";
            ProctorStudentsController.filterValue = name;
            handleStudentsView(null);
        });

        return card;
    }

    private VBox createAddBuildingCard() {
        VBox card = new VBox();
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setSpacing(10);
        card.setPadding(new javafx.geometry.Insets(15));
        card.getStyleClass().add("add-building-card");

        // Icon
        Label iconLabel = new Label("➕");
        iconLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: #4CC9F0;");

        // Label
        Label textLabel = new Label("Add Building");
        textLabel.setStyle("-fx-text-fill: #4CC9F0; -fx-font-size: 14px; -fx-font-weight: bold;");

        card.getChildren().addAll(iconLabel, textLabel);

        card.setOnMouseClicked(e -> handleSettingsView(null));

        return card;
    }

    private void updateLastUpdated() {
        if (lastUpdatedLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            lastUpdatedLabel.setText("Last Updated: " + now.format(formatter));
        }
    }

    private void updateFilterButtons() {
        if (allBlocksBtn == null)
            return;

        allBlocksBtn.getStyleClass().removeAll("action-button", "filter-button");
        maleBlocksBtn.getStyleClass().removeAll("action-button", "filter-button");
        femaleBlocksBtn.getStyleClass().removeAll("action-button", "filter-button");

        switch (currentFilter) {
            case "ALL":
                allBlocksBtn.getStyleClass().add("action-button");
                maleBlocksBtn.getStyleClass().add("filter-button");
                femaleBlocksBtn.getStyleClass().add("filter-button");
                break;
            case "MALE":
                allBlocksBtn.getStyleClass().add("filter-button");
                maleBlocksBtn.getStyleClass().add("action-button");
                femaleBlocksBtn.getStyleClass().add("filter-button");
                break;
            case "FEMALE":
                allBlocksBtn.getStyleClass().add("filter-button");
                maleBlocksBtn.getStyleClass().add("filter-button");
                femaleBlocksBtn.getStyleClass().add("action-button");
                break;
        }
    }

    @FXML
    void handleFilterAll(ActionEvent event) {
        currentFilter = "ALL";
        updateFilterButtons();
        loadBuildingCards();
    }

    @FXML
    void handleFilterMale(ActionEvent event) {
        currentFilter = "MALE";
        updateFilterButtons();
        loadBuildingCards();
    }

    @FXML
    void handleFilterFemale(ActionEvent event) {
        currentFilter = "FEMALE";
        updateFilterButtons();
        loadBuildingCards();
    }

    @FXML
    void handleTotalClick(ActionEvent event) {
        ProctorStudentsController.filterType = "ALL";
        ProctorStudentsController.filterValue = "";
        handleStudentsView(event);
    }

    @FXML
    void handleAssignedClick(ActionEvent event) {
        ProctorStudentsController.filterType = "ASSIGNED";
        ProctorStudentsController.filterValue = "";
        handleStudentsView(event);
    }

    @FXML
    void handleUnassignedClick(ActionEvent event) {
        ProctorStudentsController.filterType = "UNASSIGNED";
        ProctorStudentsController.filterValue = "";
        handleStudentsView(event);
    }

    @FXML
    void handleStudentsView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/ProctorStudentsView.fxml"));
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) totalStudentsLabel.getScene().getWindow();
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSettingsView(ActionEvent event) {
        navigateTo(event, "/resources/ProctorSettings.fxml");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        navigateTo(event, "/resources/LandingPage.fxml");
    }

    @FXML
    void handleExportPDF(ActionEvent event) {
        java.util.List<models.Student> students = data.DataManager.getInstance().getStudents();

        if (students.isEmpty()) {
            showAlert("No Data", "There are no students to export.", javafx.scene.control.Alert.AlertType.WARNING);
            return;
        }

        // Use JavaFX PrinterJob to print/save as PDF
        javafx.print.PrinterJob printerJob = javafx.print.PrinterJob.createPrinterJob();

        if (printerJob != null) {
            // Show print dialog (user can select "Print to PDF")
            boolean proceed = printerJob.showPrintDialog(((Node) event.getSource()).getScene().getWindow());

            if (proceed) {
                // Create a snapshot of the table for printing
                javafx.scene.layout.VBox printContent = new javafx.scene.layout.VBox(15);
                printContent.setPadding(new javafx.geometry.Insets(20));

                // Title
                javafx.scene.text.Text title = new javafx.scene.text.Text("Student Allocation Report");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                // Timestamp
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                javafx.scene.text.Text timestamp = new javafx.scene.text.Text(
                        "Generated: " + dateFormat.format(new java.util.Date()));
                timestamp.setStyle("-fx-font-size: 10px;");

                // Student count
                javafx.scene.text.Text count = new javafx.scene.text.Text(
                        "Total Students: " + students.size());
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
                for (models.Student student : students) {
                    String row = String.format("%-20s %-12s %-15s %-6s %-8s %-20s %-20s",
                            truncate(student.getName(), 20),
                            student.getId(),
                            truncate(student.getDepartment(), 15),
                            student.getYear(),
                            student.getGender(),
                            truncate(student.getAssignedBuilding(), 20),
                            truncate(student.getAssignedRoom(), 20));
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
        if (text == null)
            return "";
        if (text.length() <= maxLength)
            return text;
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

    // Pie Chart Event Handlers
    @FXML
    void handlePieGender(ActionEvent event) {
        currentPieFilter = "GENDER";
        updatePieFilterButtons();
        loadPieChartData("GENDER");
    }

    @FXML
    void handlePieYear(ActionEvent event) {
        currentPieFilter = "YEAR";
        updatePieFilterButtons();
        loadPieChartData("YEAR");
    }

    @FXML
    void handlePieDepartment(ActionEvent event) {
        currentPieFilter = "DEPARTMENT";
        updatePieFilterButtons();
        loadPieChartData("DEPARTMENT");
    }

    private void loadPieChartData(String dimension) {
        if (studentPieChart == null)
            return;

        List<Student> students = DataManager.getInstance().getStudents();
        Map<String, Integer> dataMap = new HashMap<>();

        // Group data by dimension
        for (Student student : students) {
            String key = "";
            switch (dimension) {
                case "GENDER":
                    key = student.getGender();
                    break;
                case "YEAR":
                    key = student.getYear();
                    break;
                case "DEPARTMENT":
                    key = student.getDepartment();
                    break;
            }
            dataMap.put(key, dataMap.getOrDefault(key, 0) + 1);
        }

        // Create pie chart data with cleaner labels
        ObservableList<javafx.scene.chart.PieChart.Data> pieChartData = FXCollections.observableArrayList();
        int total = students.size();

        for (Map.Entry<String, Integer> entry : dataMap.entrySet()) {
            // Show just the category name in the label
            javafx.scene.chart.PieChart.Data slice = new javafx.scene.chart.PieChart.Data(
                    entry.getKey(),
                    entry.getValue());
            pieChartData.add(slice);
        }

        studentPieChart.setData(pieChartData);
        studentPieChart.setLegendVisible(false);
        studentPieChart.setLabelsVisible(true);

        // Apply colors to pie slices and sync legend with the rendered order
        refreshPieChartColors(pieChartData);
        forcePieColorsHard();

        // Create custom legend that mirrors the slice order/colors
        createCustomLegend(pieChartData);

        // Add tooltips with detailed information and percentages
        for (javafx.scene.chart.PieChart.Data data : studentPieChart.getData()) {
            final int count = (int) data.getPieValue();
            final double percentage = (count * 100.0) / total;

            final Tooltip tooltip = new Tooltip(
                    String.format("%s\n━━━━━━━━━━━━\nCount: %d students\nPercentage: %.1f%%\nTotal: %d students",
                            data.getName(), count, percentage, total));
            tooltip.setStyle(
                    "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-color: linear-gradient(to bottom, rgba(15, 31, 56, 0.98), rgba(10, 20, 40, 0.98)); "
                            +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 10px; " +
                            "-fx-padding: 12px 16px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 15, 0.4, 0, 3);");

            Platform.runLater(() -> {
                if (data.getNode() == null)
                    return;

                Tooltip.install(data.getNode(), tooltip);

                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setScaleX(1.08);
                    data.getNode().setScaleY(1.08);
                    data.getNode().setEffect(new javafx.scene.effect.DropShadow(
                            12, javafx.scene.paint.Color.web("rgba(76, 201, 240, 0.4)")));
                });
                data.getNode().setOnMouseExited(e -> {
                    data.getNode().setScaleX(1.0);
                    data.getNode().setScaleY(1.0);
                    data.getNode().setEffect(null);
                });
            });
        }
    }

    private void refreshPieChartColors(ObservableList<javafx.scene.chart.PieChart.Data> pieData) {
        if (pieData == null || pieData.isEmpty())
            return;

        Platform.runLater(() -> {
            studentPieChart.applyCss();
            studentPieChart.layout();

            int index = 0;
            for (javafx.scene.chart.PieChart.Data data : pieData) {
                final String color = PIE_COLORS[index % PIE_COLORS.length];
                final String cssClass = PIE_COLOR_CLASSES[index % PIE_COLOR_CLASSES.length];

                applySliceColor(data, color, cssClass);

                data.nodeProperty().addListener((obs, oldNode, newNode) -> applySliceColor(data, color, cssClass));
                index++;
            }
        });
    }

    // Force additional passes after layout to ensure nodes are realized and colored
    private void forcePieColorsHard() {
        if (studentPieChart == null)
            return;

        Runnable apply = () -> refreshPieChartColors(studentPieChart.getData());

        PauseTransition p1 = new PauseTransition(Duration.millis(80));
        p1.setOnFinished(e -> apply.run());

        PauseTransition p2 = new PauseTransition(Duration.millis(200));
        p2.setOnFinished(e -> apply.run());

        p1.play();
        p2.play();
    }

    private void applySliceColor(javafx.scene.chart.PieChart.Data data, String color, String cssClass) {
        if (data.getNode() == null)
            return;

        data.getNode().setStyle(
                "-fx-pie-color: " + color + "; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.2); " +
                        "-fx-border-width: 1px;");

        if (!data.getNode().getStyleClass().contains(cssClass)) {
            data.getNode().getStyleClass().add(cssClass);
        }
    }

    private void createCustomLegend(ObservableList<javafx.scene.chart.PieChart.Data> pieData) {
        if (customLegend == null)
            return;

        customLegend.getChildren().clear();
        customLegend.setHgap(12);
        customLegend.setVgap(8);
        customLegend.setAlignment(javafx.geometry.Pos.CENTER);

        // Wrap to two columns based on target item width
        double itemWidth = 160;
        double hgap = customLegend.getHgap();
        customLegend.setPrefWrapLength(itemWidth * 2 + hgap);

        int colorIndex = 0;
        for (javafx.scene.chart.PieChart.Data data : pieData) {
            HBox legendItem = new HBox(8);
            legendItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            legendItem.setPrefWidth(itemWidth);
            // Add a subtle background to each legend item for better readability
            legendItem.setStyle(
                    "-fx-padding: 5; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 4;");

            javafx.scene.shape.Rectangle colorBox = new javafx.scene.shape.Rectangle(12, 12);
            colorBox.setFill(javafx.scene.paint.Color.web(PIE_COLORS[colorIndex % PIE_COLORS.length]));
            colorBox.setArcWidth(4);
            colorBox.setArcHeight(4);

            VBox textBox = new VBox(2);
            textBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label categoryLabel = new Label(data.getName());
            categoryLabel.setStyle("-fx-text-fill: #E0F7FA; -fx-font-size: 11px; -fx-font-weight: bold;");

            Label countLabel = new Label((int) data.getPieValue() + " Students");
            countLabel.setStyle("-fx-text-fill: #90E0EF; -fx-font-size: 10px;");

            textBox.getChildren().addAll(categoryLabel, countLabel);
            legendItem.getChildren().addAll(colorBox, textBox);

            customLegend.getChildren().add(legendItem);

            colorIndex++;
        }
    }

    private void updatePieFilterButtons() {
        if (genderPieBtn == null)
            return;

        genderPieBtn.getStyleClass().removeAll("action-button", "filter-button");
        yearPieBtn.getStyleClass().removeAll("action-button", "filter-button");
        deptPieBtn.getStyleClass().removeAll("action-button", "filter-button");

        switch (currentPieFilter) {
            case "GENDER":
                genderPieBtn.getStyleClass().add("action-button");
                yearPieBtn.getStyleClass().add("filter-button");
                deptPieBtn.getStyleClass().add("filter-button");
                break;
            case "YEAR":
                genderPieBtn.getStyleClass().add("filter-button");
                yearPieBtn.getStyleClass().add("action-button");
                deptPieBtn.getStyleClass().add("filter-button");
                break;
            case "DEPARTMENT":
                genderPieBtn.getStyleClass().add("filter-button");
                yearPieBtn.getStyleClass().add("filter-button");
                deptPieBtn.getStyleClass().add("action-button");
                break;
        }
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage;

            if (event != null && event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                // Fallback for calls without event (e.g. from mouse click handlers)
                // Use a node safely known to be in the scene, like buildingsFlowPane
                if (buildingsFlowPane != null && buildingsFlowPane.getScene() != null) {
                    stage = (Stage) buildingsFlowPane.getScene().getWindow();
                } else {
                    System.err.println("Navigation Error: Could not resolve Stage from event or fallback node.");
                    return;
                }
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + fxmlPath);
        }
    }
}
