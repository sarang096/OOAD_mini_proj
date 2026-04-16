package main.java.model;

import main.java.model.ChangeRequest;

public interface ChangeObserver {
    void onStatusChanged(ChangeRequest cr);
}