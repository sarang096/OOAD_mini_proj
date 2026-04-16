package main.java.service;

import main.java.DAO.BugDAO;
import main.java.model.Bug;
import main.java.model.BugStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Bug operations.
 * Delegates to BugDAO for persistence.
 */
public class BugService {

    private final BugDAO bugDAO;

    public BugService() {
        this.bugDAO = new BugDAO();
    }

    public List<Bug> getAllBugs() {
        return bugDAO.getAllBugs();
    }

    public Bug getBugById(int id) {
        return bugDAO.getBugById(id);
    }

    public List<Bug> getBugsByProjectId(int projectId) {
        return bugDAO.getAllBugs().stream()
                .filter(b -> b.getter_projectId() == projectId)
                .collect(Collectors.toList());
    }

    public boolean updateBugStatus(int bugId, BugStatus newStatus) {
        Bug bug = bugDAO.getBugById(bugId);
        if (bug == null) return false;
        bug.setter_bugstatus(newStatus);
        bug.setter_updatedAt(java.time.LocalDateTime.now());
        return bugDAO.updateBug(bug);
    }

    public boolean assignBugToUser(int bugId, int userId) {
        return bugDAO.assignBugToUser(bugId, userId);
    }

    public List<Bug> getUnassignedBugsForProject(int projectId) {
        return bugDAO.getUnassignedBugsForProject(projectId);
    }

    public int getAssignedBugCount(int userId) {
        return bugDAO.getAssignedBugCount(userId);
    }

    public int getCompletedBugCount(int userId) {
        return bugDAO.getCompletedBugCount(userId);
    }

    public int getActiveBugCount(int projectId) {
        return bugDAO.getActiveBugCount(projectId);
    }

    public int getActiveProjectBugCount(int projectId) {
        return bugDAO.getActiveProjectBugCount(projectId);
    }

    public int addBug(Bug bug) {
        return bugDAO.addBug(bug);
    }

    public boolean deleteBug(int id) {
        return bugDAO.deleteBug(id);
    }
}
