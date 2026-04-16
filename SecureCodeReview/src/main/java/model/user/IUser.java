package main.java.model.user;

import java.sql.Timestamp;
import main.java.model.User.UserRole;

/**
 * Interface for User models
 * Implements Interface Segregation Principle - clients depend only on methods they use
 * Implements Dependency Inversion Principle - high level modules depend on abstractions
 */
public interface IUser {
    int getId();
    String getName();
    String getEmail();
    String getPassword();
    UserRole getUserRole();
    Timestamp getCreatedDate();
    
    void setId(int id);
    void setName(String name);
    void setEmail(String email);
    void setPassword(String password);
}