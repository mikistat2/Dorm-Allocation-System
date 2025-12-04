package models;

public class Student {
    private String name;
    private String id;
    private String password;
    private String phone;
    private String department;
    private String year;
    private String gender;
    private String assignedBuilding;
    private String assignedRoom;

    public Student(String name, String id, String password, String phone, String department, String year, String gender) {
        this.name = name;
        this.id = id;
        this.password = password;
        this.phone = phone;
        this.department = department;
        this.year = year;
        this.gender = gender;
        this.assignedBuilding = "Not Assigned";
        this.assignedRoom = "--";
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAssignedBuilding() { return assignedBuilding; }
    public void setAssignedBuilding(String assignedBuilding) { this.assignedBuilding = assignedBuilding; }

    public String getAssignedRoom() { return assignedRoom; }
    public void setAssignedRoom(String assignedRoom) { this.assignedRoom = assignedRoom; }
}
