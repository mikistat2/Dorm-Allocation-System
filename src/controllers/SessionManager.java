package controllers;

import models.Student;

public class SessionManager {
    private static Student currentStudent;

    public static Student getCurrentStudent() {
        return currentStudent;
    }

    public static void setCurrentStudent(Student student) {
        currentStudent = student;
    }
}
