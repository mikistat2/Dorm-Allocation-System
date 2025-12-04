package models;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomNumber;
    private int capacity = 4;
    private List<String> studentIds;

    public Room(String roomNumber) {
        this.roomNumber = roomNumber;
        this.studentIds = new ArrayList<>();
    }

    public boolean isFull() {
        return studentIds.size() >= capacity;
    }

    public boolean addStudent(String studentId) {
        if (!isFull()) {
            studentIds.add(studentId);
            return true;
        }
        return false;
    }

    public void removeStudent(String studentId) {
        studentIds.remove(studentId);
    }

    public String getRoomNumber() { return roomNumber; }
    public List<String> getStudentIds() { return studentIds; }
}
