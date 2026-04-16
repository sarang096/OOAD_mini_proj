package main.java.DAO.project_repository;

import main.java.model.Project;
import java.util.List;

public interface IProjectRepository {
    Project addProject(Project project);
    Project getProjectById(int id);
    List<Project> getProjectsByManagerId(int managerId);
    List<Project> getProjectsByDeveloperId(int developerId);
    List<Project> getAllProjects();
    boolean updateProject(Project project);
    boolean deleteProject(int id);
    boolean assignDeveloperToProject(int projectId, int developerId);
    boolean removeDeveloperFromProject(int projectId, int developerId);
}
