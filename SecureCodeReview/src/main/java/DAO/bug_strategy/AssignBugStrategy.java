package main.java.DAO.bug_strategy;

import main.java.DAO.bug_repository.IBugRepository;
import main.java.model.Bug;
import main.java.model.BugStatus;

public class AssignBugStrategy implements BugOperationStrategy {
    private final IBugRepository bugRepository;
    private final String developerUsername;

    public AssignBugStrategy(IBugRepository bugRepository, String developerUsername) {
        this.bugRepository = bugRepository;
        this.developerUsername = developerUsername;
    }

    @Override
    public boolean execute(Bug bug) {
        bug.setter_assignedTo(developerUsername);
        bug.setter_bugstatus(BugStatus.in_progress);
        return bugRepository.updateBug(bug);
    }
}
