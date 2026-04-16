package main.java.model;

import java.time.LocalDateTime;

public class Bug {

    private int id;
    private String title;
    private String description;
    private BugStatus bugstatus;
    private Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String assignedTo;
    private String reportedBy;
    private int projectId;

    public Bug() {}

    public Bug(int id, String title, String description,
               BugStatus bugstatus, Priority priority,
               LocalDateTime createdAt, LocalDateTime updatedAt,
               String assignedTo, String reportedBy, int projectId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.bugstatus = bugstatus;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.assignedTo = assignedTo;
        this.reportedBy = reportedBy;
        this.projectId = projectId;
    }

    // Getters
    public int getter_id()                  { return id; }
    public String getter_title()            { return title; }
    public String getter_description()      { return description; }
    public BugStatus getter_bugstatus()     { return bugstatus; }
    public Priority getter_priority()       { return priority; }
    public LocalDateTime getter_createdAt() { return createdAt; }
    public LocalDateTime getter_updatedAt() { return updatedAt; }
    public String getter_assignedTo()       { return assignedTo; }
    public String getter_reportedBy()       { return reportedBy; }
    public int getter_projectId()           { return projectId; }

    // Setters
    public void setter_id(int id)                          { this.id = id; }
    public void setter_title(String title)                 { this.title = title; }
    public void setter_description(String description)     { this.description = description; }
    public void setter_bugstatus(BugStatus bugstatus)      { this.bugstatus = bugstatus; }
    public void setter_priority(Priority priority)         { this.priority = priority; }
    public void setter_createdAt(LocalDateTime createdAt)  { this.createdAt = createdAt; }
    public void setter_updatedAt(LocalDateTime updatedAt)  { this.updatedAt = updatedAt; }
    public void setter_assignedTo(String assignedTo)       { this.assignedTo = assignedTo; }
    public void setter_reportedBy(String reportedBy)       { this.reportedBy = reportedBy; }
    public void setter_projectId(int projectId)            { this.projectId = projectId; }

    @Override
    public String toString() {
        return "Bug{id=" + id + ", title='" + title + "', status=" + bugstatus + "}";
    }
}
