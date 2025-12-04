package models;

import java.util.ArrayList;
import java.util.List;

public class Building {
    private String name;
    private List<Room> rooms;
    private String gender; // "Male" or "Female"

    public Building(String name, int roomCount, String gender) {
        this.name = name;
        this.gender = gender;
        this.rooms = new ArrayList<>();
        for (int i = 1; i <= roomCount; i++) {
            rooms.add(new Room("R" + i)); // Room constructor only takes room number
        }
    }

    public String getName() { return name; }
    public List<Room> getRooms() { return rooms; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
