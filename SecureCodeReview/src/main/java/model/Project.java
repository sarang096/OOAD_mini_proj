package main.java.model;

import java.util.ArrayList;
import java.util.List;

public class Project {

    private int id;
    private String name;
    private String description;
    private List<String> bugs;
    private int managerId;
    private List<Integer> developerIds;

    // JSON column — stored as raw JSON string in DB
    private String bugsAsJsonString;

    public Project() {
        this.bugs = new ArrayList<>();
        this.developerIds = new ArrayList<>();
    }

    public Project(int id, String name, String description,
                   List<String> bugs, int managerId, List<Integer> developerIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.bugs = bugs != null ? bugs : new ArrayList<>();
        this.managerId = managerId;
        this.developerIds = developerIds != null ? developerIds : new ArrayList<>();
        // Build the JSON string from the bugs list for display purposes
        this.bugsAsJsonString = buildBugsJson(bugs);
    }

    private String buildBugsJson(List<String> bugs) {
        if (bugs == null || bugs.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bugs.size(); i++) {
            sb.append("{\"title\":\"").append(bugs.get(i)).append("\"}");
            if (i < bugs.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // Getters
    public int getter_id()                     { return id; }
    public String getter_name()                { return name; }
    public String getter_description()         { return description; }
    public List<String> getter_bugs()          { return bugs; }
    public int getter_managerId()              { return managerId; }
    public List<Integer> getter_developerIds() { return developerIds; }

    public String getBugsAsJsonString() {
        return bugsAsJsonString != null ? bugsAsJsonString : "[]";
    }

    // Setters
    public void setter_id(int id)                              { this.id = id; }
    public void setter_name(String name)                       { this.name = name; }
    public void setter_description(String description)         { this.description = description; }
    public void setter_managerId(int managerId)                { this.managerId = managerId; }
    public void setter_developerIds(List<Integer> developerIds){ this.developerIds = developerIds; }
    public void setter_bugs(List<String> bugs) {
        this.bugs = bugs != null ? bugs : new ArrayList<>();
        this.bugsAsJsonString = buildBugsJson(this.bugs);
    }
    public void setBugsAsJsonString(String json)               { this.bugsAsJsonString = json; }

    @Override
    public String toString() {
        return name;
    }
}
