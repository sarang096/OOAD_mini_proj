package main.java.model;

import java.sql.Timestamp;

public class Developer extends User {

    public Developer() {
        super(0, "", "", "", UserRole.Developer, new Timestamp(System.currentTimeMillis()));
    }

    public Developer(int id, String name, String email, String password) {
        super(id, name, email, password, UserRole.Developer, new Timestamp(System.currentTimeMillis()));
    }

    public void submitChange()   { System.out.println(getter_name() + " submitted a change."); }
    public void resubmitChange() { System.out.println(getter_name() + " resubmitted a change."); }
}