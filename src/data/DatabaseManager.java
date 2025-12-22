package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/dorm_system";
    private static final String DB_USER = "root"; // Default MySQL user
    private static final String DB_PASS = "147253"; // Usually empty or "root" for local dev
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static DatabaseManager instance;

    private DatabaseManager() {
        try {
            Class.forName(DB_DRIVER); // Load MySQL Driver
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found! Please add it to the lib folder.");
        }
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // Create Students table
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "password VARCHAR(100), " +
                    "phone VARCHAR(20), " +
                    "department VARCHAR(100), " +
                    "year VARCHAR(20), " +
                    "gender VARCHAR(20), " +
                    "assigned_building VARCHAR(100), " +
                    "assigned_room VARCHAR(50)" +
                    ")");

            // Create Proctors table
            stmt.execute("CREATE TABLE IF NOT EXISTS proctors (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "password VARCHAR(100)" +
                    ")");

            // Create Buildings table
            stmt.execute("CREATE TABLE IF NOT EXISTS buildings (" +
                    "name VARCHAR(100) PRIMARY KEY, " +
                    "room_count INTEGER, " +
                    "gender VARCHAR(20)" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
