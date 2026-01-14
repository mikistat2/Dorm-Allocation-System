package data;

import models.Building;
import models.Proctor;
import models.Room;
import models.Student;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String STUDENTS_FILE = "students.csv";
    private static final String CONFIG_FILE = "config.csv";
    private static final String PROCTORS_FILE = "proctors.csv";
    private static DataManager instance;

    private List<Student> students;
    private List<Building> buildings;
    private List<Proctor> proctors;
    private final DatabaseManager dbManager;

    private DataManager() {
        students = new ArrayList<>();
        buildings = new ArrayList<>();
        proctors = new ArrayList<>();
        dbManager = DatabaseManager.getInstance();

        loadData();

        // Migrate CSV data to DB if DB is empty
        if (students.isEmpty() && buildings.isEmpty() && proctors.isEmpty()) {
            migrateCsvToDb();
        }

        // Ensure at least one default proctor exists if both file and DB are empty
        if (proctors.isEmpty()) {
            proctors.add(new Proctor("admin", "admin123"));
            saveProctors();
        }
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public List<Student> getStudents() {
        return students;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public List<Proctor> getProctors() {
        return proctors;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
        saveConfig();
    }

    public void addStudent(Student student) {
        students.add(student);
        saveStudents();
    }

    public void addProctor(Proctor proctor) {
        proctors.add(proctor);
        saveProctors();
    }

    public void saveStudents() {
        String sql = "REPLACE INTO students (id, name, password, phone, department, year, gender, assigned_building, assigned_room) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Student s : students) {
                pstmt.setString(1, s.getId());
                pstmt.setString(2, s.getName());
                pstmt.setString(3, s.getPassword());
                pstmt.setString(4, s.getPhone());
                pstmt.setString(5, s.getDepartment());
                pstmt.setString(6, s.getYear());
                pstmt.setString(7, s.getGender());
                pstmt.setString(8, s.getAssignedBuilding());
                pstmt.setString(9, s.getAssignedRoom());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveProctors() {
        String sql = "REPLACE INTO proctors (id, password) VALUES (?, ?)";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Proctor p : proctors) {
                pstmt.setString(1, p.getId());
                pstmt.setString(2, p.getPassword());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        String sql = "REPLACE INTO buildings (name, room_count, gender) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Building b : buildings) {
                pstmt.setString(1, b.getName());
                pstmt.setInt(2, b.getRooms().size());
                pstmt.setString(3, b.getGender());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        loadConfigFromDb();
        loadStudentsFromDb();
        loadProctorsFromDb();
    }

    private void loadProctorsFromDb() {
        String sql = "SELECT id, password FROM proctors";
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                proctors.add(new Proctor(rs.getString("id"), rs.getString("password")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStudentsFromDb() {
        String sql = "SELECT * FROM students";
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Student s = new Student(
                        rs.getString("name"),
                        rs.getString("id"),
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("department"),
                        rs.getString("year"),
                        rs.getString("gender"));
                s.setAssignedBuilding(rs.getString("assigned_building"));
                s.setAssignedRoom(rs.getString("assigned_room"));
                students.add(s);

                if (!"Not Assigned".equals(s.getAssignedBuilding()) && !"--".equals(s.getAssignedRoom())) {
                    assignStudentToModel(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadConfigFromDb() {
        String sql = "SELECT name, room_count, gender FROM buildings";
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                buildings.add(new Building(
                        rs.getString("name"),
                        rs.getInt("room_count"),
                        rs.getString("gender")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void migrateCsvToDb() {
        System.out.println("Starting data migration from CSV to SQL...");
        loadConfig();
        loadStudents();
        loadProctors();

        if (!students.isEmpty() || !buildings.isEmpty() || !proctors.isEmpty()) {
            saveStudents();
            saveConfig();
            saveProctors();
            System.out.println("Migration complete!");
        }
    }

    private void loadProctors() {
        File file = new File(PROCTORS_FILE);
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    Proctor p = new Proctor(parts[0], parts[1]);
                    if (proctors.stream().noneMatch(pr -> pr.getId().equals(p.getId()))) {
                        proctors.add(p);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStudents() {
        File file = new File(STUDENTS_FILE);
        if (!file.exists())
            return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            int lineno = 1;
            while ((line = reader.readLine()) != null) {
                lineno++;
                if (line == null || line.trim().isEmpty())
                    continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 7) {
                    System.err.println("Skipping malformed students.csv line " + lineno + ": " +
                            line);
                    continue;
                }for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }
                String name = parts[0];
                String id = parts[1];
                String password = parts[2];
                String phone = parts[3];
                String department = parts[4];
                String year = parts[5];
                String gender = parts[6];
                if (id.isEmpty() || name.isEmpty()) {
                    System.err.println("Skipping student with empty id/name at line " + lineno);
                    continue;
                }
                Student s = new Student(name, id, password, phone, department, year, gender);
                s.setAssignedBuilding((parts.length >= 8 && !parts[7].isEmpty()) ? parts[7] : "Not Assigned");
                        s.setAssignedRoom((parts.length >= 9 && !parts[8].isEmpty()) ? parts[8] : "--");
                if (students.stream().noneMatch(st -> st.getId().equals(s.getId()))) {
                    students.add(s);
                }
            }} catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String name = parts[0].trim();

                    int roomCount;try {
                        roomCount = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException nfe) {
                        System.err.println("Skipping config row with invalid roomCount: " + line);
                        continue;
                    }

                    String gender = parts.length >= 3 && !parts[2].trim().isEmpty() ? parts[2].trim() : "Male";

                    if (buildings.stream().noneMatch(b -> b.getName().equals(name))) {
                        buildings.add(new Building(name, roomCount, gender));
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void assignStudentToModel(Student s) {
        for (Building b : buildings) {
            if (b.getName().equals(s.getAssignedBuilding())) {
                for (Room r : b.getRooms()) {
                    if (r.getRoomNumber().equals(s.getAssignedRoom())) {
                        r.addStudent(s.getId());
                        break;
                    }
                }
                break;
            }
        }
    }
}
