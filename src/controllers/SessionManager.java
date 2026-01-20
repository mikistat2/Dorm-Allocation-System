package controllers;

import models.Proctor;
import models.Student;

public class SessionManager {
    private static Student currentStudent;
    private static Proctor currentProctor;

    public static Student getCurrentStudent() {
        return currentStudent;
    }

    public static void setCurrentStudent(Student student) {
        currentStudent = student;
    }

    public static Proctor getCurrentProctor() {
        return currentProctor;
    }

    public static void setCurrentProctor(Proctor proctor) {
        currentProctor = proctor;
    }

    public static void logout() {
        currentStudent = null;
        currentProctor = null;
    }
}
