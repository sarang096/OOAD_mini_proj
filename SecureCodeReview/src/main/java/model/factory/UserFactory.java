package main.java.model.factory;

import main.java.model.User;
import main.java.model.Admin;
import main.java.model.Developer;
import main.java.model.Reviewer;
import main.java.model.user.*;
import main.java.model.User.UserRole;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Factory for creating different user types
 * Implements Factory Method Pattern
 * Implements Single Responsibility Principle
 */
public class UserFactory {

    /**
     * Creates a basic User object based on role string
     * Used by existing code for backward compatibility
     */
    public static User createUser(String name, String email, 
                                   String password, String role) {
        UserRole userRole;
        for (UserRole r : UserRole.values()) {
            if (r.name().equalsIgnoreCase(role)) {
                userRole = r;
                Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now());
                System.out.println("Creating user with role: " + userRole);
                return new User(0, name, email, password, userRole, currentTime);
            }
        }
        throw new IllegalArgumentException("Invalid role: " + role);
    }

    /**
     * Creates specialized user object based on role
     * Core Factory Method Pattern implementation
     * Returns Developer, Reviewer or Admin subclass
     */
    public static User createSpecializedUser(String role, int id,
                                              String name, String email,
                                              String password) {
        Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now());

        switch (role) {
            case "Developer":
                System.out.println("[Factory] Creating Developer: " + name);
                return new Developer(id, name, email, password);

            case "Reviewer":
                System.out.println("[Factory] Creating Reviewer: " + name);
                return new Reviewer(id, name, email, password);

            case "Administrator":
                System.out.println("[Factory] Creating Admin: " + name);
                return new Admin(id, name, email, password);

            case "ProjectManager":
                System.out.println("[Factory] Creating ProjectManager: " + name);
                return new Developer(id, name, email, password);

            case "Tester":
                System.out.println("[Factory] Creating Tester: " + name);
                return new Developer(id, name, email, password);
            default:
                System.out.println("[Factory] Unknown role: " + role + ", defaulting to Developer");
                return new Developer(id, name, email, password);
        }
    }
}