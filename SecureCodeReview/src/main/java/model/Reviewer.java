package main.java.model;

import java.sql.Timestamp;

public class Reviewer extends User {

    public Reviewer() {
        super(0, "", "", "", 
              User.UserRole.valueOf("Reviewer"), 
              new Timestamp(System.currentTimeMillis()));
    }

    public Reviewer(int id, String name, String email, String password) {
        super(id, name, email, password, 
              User.UserRole.valueOf("Reviewer"), 
              new Timestamp(System.currentTimeMillis()));
    }

    public void reviewChange()  { System.out.println(getter_name() + " is reviewing."); }
    public void approveChange() { System.out.println(getter_name() + " approved."); }
    public void rejectChange()  { System.out.println(getter_name() + " rejected."); }
    public void addComment()    { System.out.println(getter_name() + " added a comment."); }
}