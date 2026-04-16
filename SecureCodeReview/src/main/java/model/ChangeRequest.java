package main.java.model;

import main.java.model.enums.ChangeStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChangeRequest {

    private int requestId;
    private String title;
    private String description;
    private ChangeStatus status;
    private LocalDate createdDate;
    private int developerId;

    // Observer list
    private List<ChangeObserver> observers = new ArrayList<>();

    public ChangeRequest() {}

    public ChangeRequest(String title, String description, int developerId) {
        this.title = title;
        this.description = description;
        this.status = ChangeStatus.Submitted;
        this.createdDate = LocalDate.now();
        this.developerId = developerId;
    }

    public void submit() { this.status = ChangeStatus.Submitted; }

    // Updated — now notifies observers!
    public void updateStatus(ChangeStatus s) {
        this.status = s;
        notifyObservers();
    }

    // ── Observer methods ──────────────────────────
    public void addObserver(ChangeObserver o) {
        observers.add(o);
    }

    public void removeObserver(ChangeObserver o) {
        observers.remove(o);
    }

    private void notifyObservers() {
        for (ChangeObserver o : observers) {
            o.onStatusChanged(this);
        }
    }

    // ── Getters ───────────────────────────────────
    public int          getRequestId()   { return requestId; }
    public String       getTitle()       { return title; }
    public String       getDescription() { return description; }
    public ChangeStatus getStatus()      { return status; }
    public LocalDate    getCreatedDate() { return createdDate; }
    public int          getDeveloperId() { return developerId; }

    // ── Setters ───────────────────────────────────
    public void setRequestId(int id)        { this.requestId = id; }
    public void setTitle(String t)          { this.title = t; }
    public void setDescription(String d)    { this.description = d; }
    public void setStatus(ChangeStatus s)   { this.status = s; }
    public void setCreatedDate(LocalDate d) { this.createdDate = d; }
    public void setDeveloperId(int id)      { this.developerId = id; }

    @Override
    public String toString() {
        return "[" + requestId + "] " + title + " | " + status + " | " + createdDate;
    }
}