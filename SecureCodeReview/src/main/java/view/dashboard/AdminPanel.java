package main.java.view.dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import main.java.security.AuthService;
import main.java.service.ProjectService;
import main.java.service.UserService;
import main.java.model.Bug;
import main.java.model.Project;
import main.java.model.User;
import main.java.DAO.UserDAO; 
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AdminPanel extends VerticalLayout {

    //private final ProjectService projectService = new ProjectService();
    private final AuthService authService;
    //private final UserService userService = new UserService();
    private final UserDAO userDAO = new UserDAO(); 
    private final User user = new User(); // Assuming this is the logged-in user
    private final ProjectService projectService;
    private final UserService userService;
    private Grid<User> userGrid;

    public AdminPanel(AuthService authService) {

        this.userService = new UserService();
        this.projectService = new ProjectService();
        this.userGrid = new Grid<>(User.class, false);
        
        setupUserGrid();

        this.authService = authService;

        setSizeFull();
        addClassName("admin-panel");

        H2 title = new H2("Admin Dashboard");

        Tab usersTab = new Tab(VaadinIcon.USERS.create(), new H3("Users"));
        Tab projectsTab = new Tab(VaadinIcon.FOLDER.create(), new H3("Projects"));
        Tab settingsTab = new Tab(VaadinIcon.COG.create(), new H3("Settings"));

        Tabs tabs = new Tabs(usersTab, projectsTab, settingsTab);
        tabs.setWidthFull();

        Component usersContent = createUsersTab();
        //Component projectsContent = createProjectsTab();
        Component projectsContent = createProjectsContent();
        Component settingsContent = createSettingsTab();

        add(title, tabs, usersContent);

        tabs.addSelectedChangeListener(event -> {
            remove(getComponentAt(2));
            if (event.getSelectedTab().equals(usersTab)) {
                add(usersContent);
            } else if (event.getSelectedTab().equals(projectsTab)) {
                add(projectsContent);
            } else {
                add(settingsContent);
            }
        });
    }

    private void setupUserGrid() {
        userGrid.addColumn(User::getter_name).setHeader("Name").setAutoWidth(true);
        userGrid.addColumn(User::getter_email).setHeader("Email").setAutoWidth(true);
        userGrid.addColumn(User::getter_userrole).setHeader("Role").setAutoWidth(true);
        userGrid.addColumn(User::getter_createdDate).setHeader("Created Date");
        userGrid.setSizeFull();

        userGrid.addComponentColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button edit = new Button(VaadinIcon.EDIT.create());
            Button delete = new Button(VaadinIcon.TRASH.create());
            
            edit.addClickListener(e -> openEditUserDialog(user));
            // In setupUserGrid() method, update the delete button click listener:
            delete.addClickListener(e -> {
                if (user.getter_userrole() == User.UserRole.Administrator) {
                    showNotification("Administrators cannot be deleted");
                    return;
                }

                if (user.getter_userrole() != User.UserRole.Developer && 
                    user.getter_userrole() != User.UserRole.ProjectManager) {
                    showNotification("Only Developers and Project Managers can be deleted");
                    return;
                }

                boolean confirmed = showDeleteConfirmation(user);
                if (confirmed) {
                    boolean deleted = userService.deleteUserById(user.getter_id());
                    if (deleted) {
                        refreshUserGrid();
                        showNotification("User deleted successfully");
                    } else {
                        showNotification("Failed to delete user");
                    }
                }
            });
            
            actions.add(edit, delete);
            return actions;
        }).setHeader("Actions").setFlexGrow(0).setWidth("130px");

        refreshUserGrid();
    }

    private void refreshUserGrid() {
        userGrid.setItems(userService.getAllUsers());
    }


    private Component createUsersTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        HorizontalLayout toolbar = new HorizontalLayout();
        // UserService userService = new UserService(); // Assuming this uses UserDAO internally

        // // --- Toolbar with Add, Export, and Role Filter ---
        // HorizontalLayout toolbar = new HorizontalLayout();
        // toolbar.setPadding(true);
        // toolbar.setWidthFull();

        // Button addUser = new Button("Add User", VaadinIcon.PLUS.create());
        // Button export = new Button("Export", VaadinIcon.DOWNLOAD.create());

        // ComboBox<String> roleFilter = new ComboBox<>();
        // roleFilter.setItems("All", "Administrator", "Developer", "Tester", "ProjectManager");
        // roleFilter.setValue("All");
        // roleFilter.setPlaceholder("Filter by Role");

        // toolbar.add(addUser, export, roleFilter);

        // // --- Grid Setup ---
        // Grid<User> userGrid = new Grid<>(User.class, false); // false means don't auto-generate columns
        // userGrid.addColumn(User::getter_name).setHeader("Name").setAutoWidth(true);
        // userGrid.addColumn(User::getter_email).setHeader("Email").setAutoWidth(true);
        // userGrid.addColumn(User::getter_userrole).setHeader("Role").setAutoWidth(true);
        // userGrid.addColumn(User::getter_createdDate).setHeader("Created Date");

        // userGrid.setSizeFull();

        // // Replace the existing action column configuration with:
        // userGrid.addComponentColumn(user -> {
        //     HorizontalLayout actions = new HorizontalLayout();
        //     Button edit = new Button(VaadinIcon.EDIT.create());
        //     Button delete = new Button(VaadinIcon.TRASH.create());
            
        //     // Style the buttons
        //     actions.setSpacing(true);
        //     actions.setPadding(true);
        //     actions.setWidth("120px");
            
        //     edit.getStyle().set("margin-right", "8px");
        //     edit.setMinWidth("40px");
        //     delete.setMinWidth("40px");
            
        //     // Add click listeners
        //     edit.addClickListener(e -> openEditUserDialog(user));
        //     delete.addClickListener(e -> {
        //         boolean confirmed = showDeleteConfirmation(user);
        //         if (confirmed) {
        //             boolean deleted = userService.deleteUserById(user.getter_id());
        //             if (deleted) {
        //                 userGrid.setItems(userService.getAllUsers());
        //                 showNotification("User deleted successfully");
        //             }
        //         }
        //     });
            
        //     actions.add(edit, delete);
        //     return actions;
        // }).setHeader("Actions")
        //   .setFlexGrow(0)
        //   .setAutoWidth(true)
        //   .setWidth("150px");

        // // --- Populate Grid ---
        // List<User> allUsers = userService.getAllUsers();
        // userGrid.setItems(allUsers);

        // // --- Role Filter Logic ---
        // roleFilter.addValueChangeListener(event -> {
        //     String selectedRole = event.getValue();
        //     if ("All".equals(selectedRole)) {
        //         userGrid.setItems(allUsers);
        //     } else {
        //         List<User> filtered = allUsers.stream()
        //             .filter(user -> selectedRole.equalsIgnoreCase(user.getRole()))
        //             .toList();
        //         userGrid.setItems(filtered);
        //     }
        // });

        layout.add(toolbar, userGrid);
        layout.setFlexGrow(1, userGrid); // Make grid take all available space
        return layout;
    }

    private void openEditUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle("Edit User: " + user.getter_name());

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        // Basic user information
        TextField nameField = new TextField("Name");
        nameField.setValue(user.getter_name());
        
        TextField emailField = new TextField("Email");
        emailField.setValue(user.getter_email());
        emailField.setReadOnly(true); // Email cannot be changed

        // Project assignment section (only for Developers and Project Managers)
        MultiSelectComboBox<String> projectsCombo = new MultiSelectComboBox<>("Assigned Projects");

        if (user.getter_userrole() == User.UserRole.Developer || 
            user.getter_userrole() == User.UserRole.ProjectManager) {
            
            List<Project> allProjects = projectService.getAllProjects();
            
            // Get projects where user is already assigned
            List<String> assignedProjects = new ArrayList<>();
            if (user.getter_userrole() == User.UserRole.Developer) {
                assignedProjects = allProjects.stream()
                    .filter(p -> p.getter_developerIds().contains(user.getter_id()))
                    .map(Project::getter_name)
                    .collect(Collectors.toList());
            } else { // ProjectManager
                assignedProjects = allProjects.stream()
                    .filter(p -> p.getter_managerId() == user.getter_id())
                    .map(Project::getter_name)
                    .collect(Collectors.toList());
            }
            
            projectsCombo.setItems(allProjects.stream()
                .map(Project::getter_name)
                .collect(Collectors.toList()));
            projectsCombo.setValue(new HashSet<>(assignedProjects));
            
            dialogLayout.add(projectsCombo);
        }

        // Buttons
        Button saveButton = new Button("Save", e -> {
            user.setter_name(nameField.getValue());
            userService.updateUser(user);

            if (user.getter_userrole() == User.UserRole.Developer) {
                Set<String> selectedProjects = projectsCombo.getSelectedItems();
                boolean projectsUpdated = userService.updateUserProjects(
                    user.getter_id(), 
                    new ArrayList<>(selectedProjects)
                );
                
                if (!projectsUpdated) {
                    showNotification("Error updating project assignments");
                    return;
                }
            }

            dialog.close();
            refreshUserGrid();
            showNotification("User updated successfully");
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        
        dialogLayout.add(nameField, emailField, buttons);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private boolean showDeleteConfirmation(User user) {
        // First check if user is an admin
        if (user.getter_userrole() == User.UserRole.Administrator) {
            Notification.show("Administrators cannot be deleted", 
                3000, Notification.Position.TOP_CENTER);
            return false;
        }

        // Only allow deleting Developers and Project Managers
        if (user.getter_userrole() != User.UserRole.Developer && 
            user.getter_userrole() != User.UserRole.ProjectManager) {
            Notification.show("Only Developers and Project Managers can be deleted", 
                3000, Notification.Position.TOP_CENTER);
            return false;
        }

        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");
        
        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3("Are you sure you want to delete user: " + user.getter_name() + "?"));
        
        AtomicBoolean result = new AtomicBoolean(false);
        
        Button confirmButton = new Button("Delete", e -> {
            result.set(true);
            confirmDialog.close();
        });
        confirmButton.getStyle().set("color", "var(--lumo-error-color)");
        
        Button cancelButton = new Button("Cancel", e -> confirmDialog.close());
        
        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        buttons.setWidthFull();
        
        layout.add(buttons);
        confirmDialog.add(layout);
        confirmDialog.open();
        
        // Wait for dialog to close
        return result.get();
    }

    private void showNotification(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER);
    }


    private VerticalLayout createProjectsContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);

        HorizontalLayout actionButtons = new HorizontalLayout();
        Button addProjectButton = new Button("Add Project", VaadinIcon.PLUS.create());
        Button exportButton = new Button("Export", VaadinIcon.DOWNLOAD.create());
        actionButtons.add(addProjectButton, exportButton);

        System.out.println("[DEBUG] Setting up projects grid...");

        Grid<Project> projectsGrid = new Grid<>(Project.class, false); // false means don't auto-generate columns
        projectsGrid.addColumn(Project::getter_id).setHeader("ID").setAutoWidth(false).setFlexGrow(0).setWidth("100px");
        projectsGrid.addColumn(Project::getter_name).setHeader("Name").setAutoWidth(true).setFlexGrow(0).setWidth("300px");
        projectsGrid.addColumn(Project::getter_description).setHeader("Description").setAutoWidth(true).setFlexGrow(1);
        
        // Manager column
        projectsGrid.addColumn(project -> {
            User manager = userDAO.getUserById(project.getter_managerId());
            System.out.println("[DEBUG] Manager for project " + project.getter_id() + ": " + manager);
            return manager != null ? manager.getter_name() : "";
        }).setHeader("Manager").setAutoWidth(true).setFlexGrow(0);

        // Bug count column
        projectsGrid.addColumn(project -> {
            System.out.println("[DEBUG] Counting bugs for project: " + project.getter_id());
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Bug> bugs = objectMapper.readValue(project.getBugsAsJsonString(), 
                    new TypeReference<List<Bug>>() {});
                int count = bugs.size();
                System.out.println("[DEBUG] Bug count: " + count);
                return count;
            } catch (Exception e) {
                System.out.println("[ERROR] Failed to parse bugs: " + e.getMessage());
                return 0;
            }
        }).setHeader("Bug Count").setFlexGrow(0).setAutoWidth(true);

        // Add the actions column
        projectsGrid.addComponentColumn(project -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addClickListener(click -> openEditProjectDialog(project));
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addClickListener(click -> {
                boolean deleted = projectService.deleteProject(project.getter_id());
                if (deleted) {
                    projectsGrid.setItems(projectService.getAllProjects());
                }
            });
            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setFlexGrow(0).setWidth("130px");

        // Load and set items with debug logging
        System.out.println("[DEBUG] Fetching all projects...");
        List<Project> projects = projectService.getAllProjects();
        System.out.println("[DEBUG] Found " + projects.size() + " projects");
        
        // Debug print each project
        projects.forEach(project -> {
            System.out.println("[DEBUG] Project: " + project.getter_id() + 
                ", Name: " + project.getter_name() + 
                ", Manager ID: " + project.getter_managerId());
        });

        projectsGrid.setItems(projects);
        System.out.println("[DEBUG] Set items in grid");

        // Add refresh functionality to ensure data is loaded
        addProjectButton.addClickListener(e -> {
            System.out.println("[DEBUG] Refreshing projects grid...");
            List<Project> updatedProjects = projectService.getAllProjects();
            System.out.println("[DEBUG] Found " + updatedProjects.size() + " projects after refresh");
            projectsGrid.setItems(updatedProjects);
        });

        content.add(actionButtons, projectsGrid);
        content.setFlexGrow(1, projectsGrid); // Make grid take all available space
        return content;
    }

    private void openEditProjectDialog(Project project) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
    
        TextField nameField = new TextField("Project Name");
        nameField.setValue(project.getter_name());
    
        TextArea descField = new TextArea("Description");
        descField.setValue(project.getter_description());
    
        ComboBox<String> managerCombo = new ComboBox<>("Project Manager");
        // Fetch managers from database
        List<String> managers = userService.getAllUsers().stream()
                .filter(u -> u.getter_userrole() == User.UserRole.ProjectManager)
                .map(User::getter_name)
                .collect(Collectors.toList());
        managerCombo.setItems(managers);
        managerCombo.setValue(Optional.ofNullable(project.getter_managerId())
                .map(id -> userDAO.getUserById(id))
                .map(User::getter_name)
                .orElse(null));
    
        MultiSelectComboBox<String> devCombo = new MultiSelectComboBox<>("Assign Developers");
        // Fetch developers from database
        List<String> developers = userService.getAllUsers().stream()
                .filter(u -> u.getter_userrole() == User.UserRole.Developer)
                .map(User::getter_name)
                .collect(Collectors.toList());
        devCombo.setItems(developers);
        List<String> selectedDevNames = project.getter_developerIds().stream()
                .map(id -> userDAO.getUserById(id))
                .filter(Objects::nonNull)
                .map(User::getter_name)
                .collect(Collectors.toList());
        devCombo.setValue(new HashSet<>(selectedDevNames));
    
        Button save = new Button("Save", e -> {
            project.setter_name(nameField.getValue());
            project.setter_description(descField.getValue());
            
            // Convert manager name to ID before setting
            String selectedManagerName = managerCombo.getValue();
            List<User> allUsers = userService.getAllUsers();
            Optional<User> selectedManager = allUsers.stream()
                    .filter(u -> u.getter_name().equals(selectedManagerName))
                    .findFirst();
            selectedManager.ifPresent(manager -> project.setter_managerId(manager.getter_id()));
    
            // Convert developer names to IDs before setting
            List<Integer> developerIds = new ArrayList<>();
            Set<String> selectedDevNames1 = devCombo.getSelectedItems();
            selectedDevNames1.forEach(devName -> {
                allUsers.stream()
                        .filter(u -> u.getter_name().equals(devName))
                        .findFirst()
                        .ifPresent(dev -> developerIds.add(dev.getter_id()));
            });
            project.setter_developerIds(developerIds);
    
            // Save the project to database
            projectService.updateProject(project);
            dialog.close();
        });
    
        Button cancel = new Button("Cancel", e -> dialog.close());
    
        FormLayout form = new FormLayout(nameField, descField, managerCombo, devCombo);
        HorizontalLayout buttons = new HorizontalLayout(save, cancel);
    
        dialog.add(form, buttons);
        dialog.open();
    }

    private Component createSettingsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        H3 general = new H3("General Settings");
        H3 security = new H3("Security Settings");
        H3 notifications = new H3("Notification Settings");

        Button save = new Button("Save Settings");
        save.getStyle().set("margin-top", "20px");

        layout.add(general, security, notifications, save);
        return layout;
    }

    // === Sample Data Classes & Methods ===

    // public static class User {
    //     private String username, email, role;
    //     private LocalDate created;

    //     public User(String username, String email, String role, LocalDate created) {
    //         this.username = username;
    //         this.email = email;
    //         this.role = role;
    //         this.created = created;
    //     }

    //     public String getUsername() { return username; }
    //     public String getEmail() { return email; }
    //     public String getRole() { return role; }
    //     public LocalDate getCreated() { return created; }
    // }

    // public static class Project {
    //     private String name, description, manager;
    //     private List<String> developers;
    //     private int bugs;

    //     public Project(String name, String desc, String manager, List<String> devs, int bugs) {
    //         this.name = name;
    //         this.description = desc;
    //         this.manager = manager;
    //         this.developers = devs;
    //         this.bugs = bugs;
    //     }

    //     public String getName() { return name; }
    //     public String getDescription() { return description; }
    //     public String getManager() { return manager; }
    //     public List<String> getDevelopers() { return developers; }
    //     public int getBugs() { return bugs; }

    //     public void setName(String name) { this.name = name; }
    //     public void setDescription(String description) { this.description = description; }
    //     public void setManager(String manager) { this.manager = manager; }
    //     public void setDevelopers(List<String> developers) { this.developers = developers; }
    // }

    // private List<User> getSampleUsers() {
    //     return List.of(
    //             new User("admin", "admin@example.com", "Administrator", LocalDate.now().minusDays(30)),
    //             new User("dev1", "dev1@example.com", "Developer", LocalDate.now().minusDays(25)),
    //             new User("dev2", "dev2@example.com", "Developer", LocalDate.now().minusDays(22)),
    //             new User("tester1", "tester1@example.com", "Tester", LocalDate.now().minusDays(20)),
    //             new User("manager1", "manager1@example.com", "Manager", LocalDate.now().minusDays(15)),
    //             new User("manager2", "manager2@example.com", "Manager", LocalDate.now().minusDays(10))
    //     );
    // }

    // private List<Project> getSampleProjects() {
    //     return List.of(
    //             new Project("Website Redesign", "Redesigning company site", "manager1", List.of("dev1"), 12),
    //             new Project("Mobile App", "Customer mobile application", "manager1", List.of("dev1", "dev2"), 8),
    //             new Project("API Integration", "Integrating 3rd party API", "manager2", List.of("dev2"), 5)
    //     );
    // }
}
