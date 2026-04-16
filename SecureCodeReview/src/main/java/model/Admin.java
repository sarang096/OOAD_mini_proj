package main.java.model;

import java.sql.Timestamp;

public class Admin extends User {

    public Admin() {
        super(0, "", "", "", UserRole.Administrator, new Timestamp(System.currentTimeMillis()));
    }

    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password, UserRole.Administrator, new Timestamp(System.currentTimeMillis()));
    }

    public void addUser()        { System.out.println("Admin added a user."); }
    public void removeUser()     { System.out.println("Admin removed a user."); }
    public void defineRules()    { System.out.println("Admin defined rules."); }
    public void monitorReviews() { System.out.println("Admin monitoring reviews."); }
}