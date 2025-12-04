package data;

import models.Building;
import models.Proctor;
import models.Room;
import models.Student;

import java.io.*;
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

    private DataManager() {
        students = new ArrayList<>();
        buildings = new ArrayList<>();
        proctors = new ArrayList<>();
        loadData();
        
        // Ensure at least one default proctor exists if file is empty
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

    public List<Student> getStudents() { return students; }
    public List<Building> getBuildings() { return buildings; }
    public List<Proctor> getProctors() { return proctors; }

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
        try (PrintWriter writer = new PrintWriter(new FileWriter(STUDENTS_FILE))) {
            // Header
            writer.println("Name,ID,Password,Phone,Department,Year,Gender,Building,Room");
            for (Student s : students) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        s.getName(), s.getId(), s.getPassword(), s.getPhone(),
                        s.getDepartment(), s.getYear(), s.getGender(),
                        s.getAssignedBuilding(), s.getAssignedRoom());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveProctors() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PROCTORS_FILE))) {
            writer.println("ID,Password");
            for (Proctor p : proctors) {
                writer.printf("%s,%s%n", p.getId(), p.getPassword());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            // Save each building: Name,RoomCount,Gender
            for (Building b : buildings) {
                writer.printf("%s,%d,%s%n", b.getName(), b.getRooms().size(), b.getGender());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        loadConfig();
        loadStudents();
        loadProctors();
    }
    
    private void loadProctors() {
        File file = new File(PROCTORS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    proctors.add(new Proctor(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStudents() {
        File file = new File(STUDENTS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines
                
                String[] parts = line.split(",");
                
                // Need at least 7 fields for basic student info
                if (parts.length >= 7) {
                    Student s = new Student(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
                    
                    // Handle optional Building and Room fields
                    if (parts.length >= 8 && !parts[7].trim().isEmpty()) {
                        s.setAssignedBuilding(parts[7].trim());
                    } else {
                        s.setAssignedBuilding("Not Assigned");
                    }
                    
                    if (parts.length >= 9 && !parts[8].trim().isEmpty()) {
                        s.setAssignedRoom(parts[8].trim());
                    } else {
                        s.setAssignedRoom("--");
                    }
                    
                    students.add(s);
                    
                    // If assigned, update the room in the building model
                    if (!"Not Assigned".equals(s.getAssignedBuilding()) && !"--".equals(s.getAssignedRoom())) {
                        assignStudentToModel(s);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0];
                    int roomCount = Integer.parseInt(parts[1]);
                    String gender = parts[2];
                    buildings.add(new Building(name, roomCount, gender));
                } else if (parts.length >= 2) {
                    // Backward compatibility: if no gender specified, default to "Male"
                    String name = parts[0];
                    int roomCount = Integer.parseInt(parts[1]);
                    buildings.add(new Building(name, roomCount, "Male"));
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
