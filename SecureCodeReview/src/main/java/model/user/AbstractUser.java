package main.java.model.user;

import java.sql.Timestamp;
import main.java.model.User.UserRole;

/**
 * Abstract base class for all User types
 * Implements Template Method Pattern - defines skeleton for user operations
 * Implements Open/Closed Principle - open for extension, closed for modification
 */
public abstract class AbstractUser implements IUser {
    protected int id;
    protected String name;
    protected String email;
    protected String password;
    protected UserRole userRole;
    protected Timestamp created_date;
    
    public AbstractUser(int id, String name, String email, String password, UserRole userRole, Timestamp created_date) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.created_date = created_date;
    }
    
    @Override
    public int getId() { return id; }
    
    @Override
    public String getName() { return name; }
    
    @Override
    public String getEmail() { return email; }
    
    @Override
    public String getPassword() { return password; }
    
    @Override
    public UserRole getUserRole() { return userRole; }
    
    @Override
    public Timestamp getCreatedDate() { return created_date; }
    
    @Override
    public void setId(int id) { this.id = id; }
    
    @Override
    public void setName(String name) { this.name = name; }
    
    @Override
    public void setEmail(String email) { this.email = email; }
    
    @Override
    public void setPassword(String password) { this.password = password; }
}