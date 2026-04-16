package main.java.service;

import main.java.DAO.ProjectDAO;
import main.java.model.Project;

import java.util.List;

/**
 * Service layer for Project operations.
 * Delegates to ProjectDAO for persistence.
 */
public class ProjectService {

    private final ProjectDAO projectDAO;

    public ProjectService() {
        this.projectDAO = new ProjectDAO();
    }

    public List<Project> getAllProjects() {
        return projectDAO.getAllProjects();
    }

    public Project getProjectById(int id) {
        return projectDAO.getProjectById(id);
    }

    public List<Project> getProjectsByManagerId(int managerId) {
        return projectDAO.getProjectsByManagerId(managerId);
    }

    public List<Project> getProjectsByDeveloperId(int developerId) {
        return projectDAO.getProjectsByDeveloperId(developerId);
    }

    public Project addProject(Project project) {
        return projectDAO.addProject(project);
    }

    public boolean updateProject(Project project) {
        return projectDAO.updateProject(project);
    }

    public boolean deleteProject(int id) {
        return projectDAO.deleteProject(id);
    }

    public boolean assignDeveloperToProject(int projectId, int developerId) {
        return projectDAO.assignDeveloperToProject(projectId, developerId);
    }

    public boolean removeDeveloperFromProject(int projectId, int developerId) {
        return projectDAO.removeDeveloperFromProject(projectId, developerId);
    }
}
