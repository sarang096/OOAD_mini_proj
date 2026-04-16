package main.java.service;

import main.java.DAO.UserDAO;
import main.java.model.User;
import main.java.model.factory.UserFactory;

import java.util.List;

/**
 * Service layer for User operations.
 * Delegates to UserDAO for persistence.
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email);
    }

    public boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }

    /**
     * Updates the projects assigned to a user (by project names resolved to IDs).
     * AdminPanel passes a List<String> of project names for now — resolves via ProjectService.
     */
    public boolean updateUserProjects(int userId, List<String> projectNames) {
        // AdminPanel calls this with project names; resolve to IDs via ProjectService
        ProjectService projectService = new ProjectService();
        List<main.java.model.Project> allProjects = projectService.getAllProjects();
        List<Integer> projectIds = new java.util.ArrayList<>();
        for (String name : projectNames) {
            allProjects.stream()
                    .filter(p -> p.getter_name().equals(name))
                    .findFirst()
                    .ifPresent(p -> projectIds.add(p.getter_id()));
        }
        return userDAO.updateUserProjects(userId, projectIds);
    }

    public boolean deleteUserById(int id) {
        return userDAO.deleteUser(id);
    }

    public User addUser(User user) {
        return userDAO.addUser(user);
    }

    /**
     * Convenience method called by SignupView.
     * Creates a User via UserFactory then persists it.
     * Returns true on success.
     */
    public boolean addUser(String name, String email, String password, String role) {
        try {
            User user = UserFactory.createUser(name, email, password, role);
            userDAO.addUser(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
