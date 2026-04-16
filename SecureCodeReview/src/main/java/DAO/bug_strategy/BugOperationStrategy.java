package main.java.DAO.bug_strategy;

import main.java.model.Bug;

public interface BugOperationStrategy {
    boolean execute(Bug bug);
}
