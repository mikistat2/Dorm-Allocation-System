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
    private BarChart<String, Number> studentGenderChart;
    @FXML
    private javafx.scene.chart.PieChart studentPieChart;
    @FXML
    private TableView<Student> recentStudentsTable;
    @FXML
    private TextField searchField;

    @FXML
    private FlowPane buildingsFlowPane;
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
    private TilePane customLegend;

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
        loadGenderChart();
        loadPieChartData("GENDER");
        loadBuildingCards();
        loadRecentStudents();
        updateLastUpdated();
        updateFilterButtons();
        updatePieFilterButtons();

        if (buildingsFlowPane != null) {
            double cardWidth = 140;
            double gap = 12;
            double padding = 8; // tighter padding to fit three cards per row

            buildingsFlowPane.setHgap(gap);
            buildingsFlowPane.setVgap(gap);
            buildingsFlowPane.setPadding(new javafx.geometry.Insets(padding));

            // Target three cards per row: total = padding*2 + 3*cardWidth + 2*gap
            double wrapLength = padding * 2 + (cardWidth * 3) + (gap * 2);
            buildingsFlowPane.setPrefWrapLength(wrapLength);
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

    private void loadGenderChart() {
        if (studentGenderChart == null)
            return;

        List<Student> students = DataManager.getInstance().getStudents();

        int maleCount = 0;
        int femaleCount = 0;
        int maleAssigned = 0;
        int femaleAssigned = 0;

        for (Student student : students) {
            if ("Male".equalsIgnoreCase(student.getGender())) {
                maleCount++;
                if (!"Not Assigned".equals(student.getAssignedBuilding())) {
                    maleAssigned++;
                }
            } else if ("Female".equalsIgnoreCase(student.getGender())) {
                femaleCount++;
                if (!"Not Assigned".equals(student.getAssignedBuilding())) {
                    femaleAssigned++;
                }
            }
        }

        XYChart.Series<String, Number> totalSeries = new XYChart.Series<>();
        totalSeries.setName("Total");
        totalSeries.getData().add(new XYChart.Data<>("Male", maleCount));
        totalSeries.getData().add(new XYChart.Data<>("Female", femaleCount));

        XYChart.Series<String, Number> assignedSeries = new XYChart.Series<>();
        assignedSeries.setName("Assigned");
        assignedSeries.getData().add(new XYChart.Data<>("Male", maleAssigned));
        assignedSeries.getData().add(new XYChart.Data<>("Female", femaleAssigned));

        studentGenderChart.getData().clear();
        studentGenderChart.getData().addAll(totalSeries, assignedSeries);
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
    }

    private VBox createBuildingCard(String name, int studentCount, int maxRooms, String gender) {
        VBox card = new VBox();
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setSpacing(6);
        card.setPadding(new javafx.geometry.Insets(10));
        card.getStyleClass().add("building-card");

        if ("Male".equalsIgnoreCase(gender)) {
            card.getStyleClass().add("building-card-male");
        } else {
            card.getStyleClass().add("building-card-female");
        }

        card.setPrefSize(140, 140);

        Label iconLabel = new Label("🏢");
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #FFFFFF;");

        Label countLabel = new Label(studentCount + " Students");
        countLabel.setStyle("-fx-text-fill: #4CC9F0; -fx-font-size: 11px;");

        Label roomsLabel = new Label("Max Rooms: " + maxRooms);
        roomsLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 11px; -fx-opacity: 0.8;");

        Label genderLabel = new Label("Gender: " + gender);
        genderLabel.setStyle("-fx-text-fill: #4CC9F0; -fx-font-size: 11px;");

        card.getChildren().addAll(iconLabel, nameLabel, countLabel, roomsLabel, genderLabel);

        card.setOnMouseClicked(e -> {
            ProctorStudentsController.filterType = "BUILDING";
            ProctorStudentsController.filterValue = name;
            handleStudentsView(null);
        });

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
    void handleTotalClick(javafx.scene.input.MouseEvent event) {
        ProctorStudentsController.filterType = "ALL";
        ProctorStudentsController.filterValue = "";
        handleStudentsView(null);
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
        customLegend.setVgap(10);
        customLegend.setPrefColumns(2);
        customLegend.setAlignment(javafx.geometry.Pos.CENTER);

        // Calculate cleaner width for items
        double itemWidth = 160;

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
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + fxmlPath);
        }
    }
}
