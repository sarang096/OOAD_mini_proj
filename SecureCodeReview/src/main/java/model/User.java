package main.java.model;

import java.sql.Timestamp;
import java.util.Objects;
import main.java.model.user.AbstractUser;

/**
 * Existing User class modified to extend AbstractUser
 * Implements Liskov Substitution Principle - can be used anywhere IUser is expected
 */
public class User extends AbstractUser {
    // Enum remains here
    public enum UserRole {
        Administrator, ProjectManager, Developer, Tester,Reviewer;
        
        /**
         * Creates a UserRole from string representation
         * Implements Factory Method Pattern - converts string to enum value
         */
        public static UserRole fromString(String roleStr) {
            try {
                return valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                // Default to Developer if invalid role string is provided
                System.out.println("[WARNING] Invalid role: " + roleStr + ", defaulting to Developer");
                return Developer;
            }
        }
    }
    
    /**
     * No-argument constructor for backward compatibility
     * Needed for code that creates User objects without parameters
     */
    public User() {
        super(0, "", "", "", UserRole.Developer, new Timestamp(System.currentTimeMillis()));
    }
    
    // Constructor calls super
    public User(int id, String name, String email, String password, UserRole userRole, Timestamp created_date) {
        super(id, name, email, password, userRole, created_date);
    }
    
    // Add an additional constructor to User to handle the case without timestamp
    public User(int id, String name, String email, String password, UserRole userRole) {
        this(id, name, email, password, userRole, new Timestamp(System.currentTimeMillis()));
    }
    
    // Legacy getter methods for backward compatibility
    public int getter_id() {
        return getId();
    }
    
    public String getter_name() {
        return getName();
    }
    
    public String getter_email() {
        return getEmail();
    }
    
    public String getter_password() {
        return getPassword();
    }
    
    public UserRole getter_userrole() {
        return getUserRole();
    }
    
    public Timestamp getter_createdDate() {
        return getCreatedDate();
    }
    
    // Legacy setter methods for backward compatibility
    public void setter_id(int id) {
        setId(id);
    }
    
    public void setter_name(String name) {
        setName(name);
    }
    
    public void setter_email(String email) {
        setEmail(email);
    }
    
    public void setter_password(String password) {
        setPassword(password);
    }
    
    public void setter_userrole(UserRole userRole) {
        this.userRole = userRole;
    }
    
    public void setter_created_date(Timestamp created_date) {
        this.created_date = created_date;
    }
    
    // Implement equals and hashCode for proper collection behavior
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getId() == user.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}