package main.java.view.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;
import com.fasterxml.jackson.core.type.TypeReference;

import main.java.DAO.ProjectDAO;
import main.java.security.AuthService;
import main.java.service.BugService;
import main.java.service.UserService;
import main.java.service.ProjectService;
import main.java.model.Bug;
import main.java.model.BugStatus;
import main.java.model.Project;
import main.java.model.User;
import main.java.model.Report;
import main.java.service.ReportService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ManagerPanel extends VerticalLayout {

    private TextField nameField;
    private TextArea descriptionField;
    private TextArea bugsField;
    private Button saveButton;
    private Dialog projectDialog;
    private Project currentEditingProject = null; // 🆕 Used for Edit vs Add decision
    private Dialog viewDialog = new Dialog(); // 🆕 Dialog for viewing project details
    private final UserService userService = new UserService();
    private final ProjectService projectService = new ProjectService();
    private final BugService bugService = new BugService();

    private final AuthService authService;

    private final ProjectDAO projectDAO = new ProjectDAO();
    private Grid<Project> projectsGrid;

    private List<Project> projects;

    public static class TeamMember {
        private String name;
        private String role;
        private String project;
        private int assignedTasks;
        private int completedTasks;

        public TeamMember(String name, String role, String project, int assignedTasks, int completedTasks) {
            this.name = name;
            this.role = role;
            this.project = project;
            this.assignedTasks = assignedTasks;
            this.completedTasks = completedTasks;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }

        public String getProject() {
            return project;
        }

        public int getAssignedTasks() {
            return assignedTasks;
        }

        public int getCompletedTasks() {
            return completedTasks;
        }
    }

    public ManagerPanel(AuthService authService) {
        this.authService = authService;

        addClassName("manager-panel");
        setSizeFull();

        // Welcome message
        H2 welcomeTitle = new H2("Project Manager Dashboard");
        welcomeTitle.getStyle().set("margin-top", "0");

        // Create tabs for different manager functions
        Tab projectsTab = new Tab(VaadinIcon.FOLDER.create(), new H3("Projects"));
        Tab teamTab = new Tab(VaadinIcon.USERS.create(), new H3("Team"));
        Tab reportsTab = new Tab(VaadinIcon.CHART.create(), new H3("Reports"));

        Tabs tabs = new Tabs(projectsTab, teamTab, reportsTab);
        tabs.setWidthFull();

        // Content for each tab
        VerticalLayout projectsContent = createProjectsContent();
        VerticalLayout teamContent = createTeamContent();
        VerticalLayout reportsContent = createReportsContent();

        // Initially show projects content
        add(welcomeTitle, tabs, projectsContent);

        // Tab change listener
        tabs.addSelectedChangeListener(event -> {
            // Remove all content
            remove(getComponentAt(2));

            // Add appropriate content based on selected tab
            if (event.getSelectedTab().equals(projectsTab)) {
                add(projectsContent);
            } else if (event.getSelectedTab().equals(teamTab)) {
                add(teamContent);
            } else if (event.getSelectedTab().equals(reportsTab)) {
                add(reportsContent);
            }
        });
    }

    public VerticalLayout createProjectsContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        HorizontalLayout summaryLayout = new HorizontalLayout();
        summaryLayout.setWidthFull();

        int currentManagerId = VaadinSession.getCurrent().getAttribute("userId") != null
                ? Integer.parseInt(VaadinSession.getCurrent().getAttribute("userId").toString())
                : 0;

        System.out.println("[DEBUG] Current manager ID: " + currentManagerId);

        // Filter projects by manager ID
        projects = projectDAO.getAllProjects().stream()
                .filter(project -> project.getter_managerId() == currentManagerId)
                .collect(Collectors.toList());

        System.out.println("[DEBUG] Found " + projects.size() + " projects for manager " + currentManagerId);

        H3 totalProjectCountLabel = new H3();
        VerticalLayout totalProjects = createSummaryCard("Total Projects", String.valueOf(projects.size()),
                VaadinIcon.FOLDER.create(), "#1676f3", totalProjectCountLabel);

        // Count active and completed based on bugs
        long activeCount = projects.stream().filter(p -> bugService.getActiveProjectBugCount(p.getter_id()) > 0)
                .count();
        long completedCount = projects.stream().filter(p -> bugService.getActiveProjectBugCount(p.getter_id()) == 0)
                .count();

        H3 activeProjectCountLabel = new H3();
        VerticalLayout activeProjects = createSummaryCard("Active", String.valueOf(activeCount),
                VaadinIcon.PLAY.create(), "green", activeProjectCountLabel);

        H3 completedProjectCountLabel = new H3();
        VerticalLayout completedProjects = createSummaryCard("Completed", String.valueOf(completedCount),
                VaadinIcon.CHECK.create(), "gray", completedProjectCountLabel);

        summaryLayout.add(totalProjects, activeProjects, completedProjects);
        summaryLayout.setFlexGrow(1, totalProjects, activeProjects, completedProjects);

        projectsGrid = new Grid<>(Project.class);
        projectsGrid.removeAllColumns();
        projectsGrid.addColumn(Project::getter_id).setHeader("ID").setAutoWidth(false).setFlexGrow(0).setWidth("100px");
        projectsGrid.addColumn(Project::getter_name).setHeader("Name").setAutoWidth(true).setFlexGrow(0)
                .setWidth("300px");
        projectsGrid.addColumn(Project::getter_description).setHeader("Description").setAutoWidth(true).setFlexGrow(1);
        // projectsGrid.addColumn(project ->
        // project.getter_bugs().size()).setHeader("Bug
        // Count").setFlexGrow(0).setAutoWidth(true);
        projectsGrid.addColumn(project -> {
            System.out.println("Project ID: " + project.getter_id() + ", Bugs JSON: " + project.getBugsAsJsonString());
            int count = 0;
            ObjectMapper objectMapper = new ObjectMapper();
            String bugsJson = project.getBugsAsJsonString();

            try {
                // Check if the bugs JSON is null or empty
                if (bugsJson != null && !bugsJson.trim().isEmpty()) {
                    // Parse JSON string into a List of Bugs
                    List<Bug> listOfBugs = objectMapper.readValue(
                            bugsJson, new TypeReference<List<Bug>>() {
                            });
                    System.out.println("Parsed Bugs: " + listOfBugs);
                    System.out.println("Parsed Bugs Count: " + listOfBugs.size());
                    count = listOfBugs.size();
                } else {
                    System.out.println("No bugs data available for project ID: " + project.getter_id());
                }
            } catch (Exception e) {
                System.out.println("Failed to parse bugs: " + e.getMessage());
                e.printStackTrace();
            }

            return count;
        }).setHeader("Bug Count").setFlexGrow(0).setAutoWidth(true).setWidth("150px");

        projectsGrid.addColumn(project -> {
            System.out.println("[DEBUG] Counting active bugs for project: " + project.getter_id());
            try {
                return bugService.getActiveBugCount(project.getter_id());
            } catch (Exception e) {
                System.out.println("[ERROR] Failed to get bug count: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }).setHeader("Active Bugs").setFlexGrow(0).setAutoWidth(true).setWidth("150px");

        projectsGrid.setItems(projects);
        projectsGrid.setSizeFull();

        // Project form dialog
        Dialog projectDialog = new Dialog();
        projectDialog.setCloseOnOutsideClick(true);
        projectDialog.setCloseOnEsc(true);
        projectDialog.setWidth("500px");

        TextField nameField = new TextField("Project Name");
        nameField.setRequired(true);
        nameField.setWidthFull();
        TextArea descriptionField = new TextArea("Description");
        descriptionField.setRequired(true);
        descriptionField.setWidthFull();
        TextArea bugsField = new TextArea("Initial Bugs (comma-separated)");
        bugsField.setPlaceholder("e.g. Login error, UI glitch");
        bugsField.setWidthFull();
        bugsField.setVisible(false); // Initially hidden

        Button saveButton = new Button("Save");
        Button updateButton = new Button("Update");

        saveButton.setVisible(true);
        updateButton.setVisible(false);

        saveButton.addClickListener(event -> {
            System.out.println("Save button clicked, NameField: " + nameField.getValue() + " DescriptionField: "
                    + descriptionField.getValue());
            if (!nameField.isEmpty() && !descriptionField.isEmpty()) {
                Project newProject = new Project();
                int currentUserId = VaadinSession.getCurrent().getAttribute("userId").toString().equals("null") ? 0
                        : Integer.parseInt(VaadinSession.getCurrent().getAttribute("userId").toString());
                newProject.setter_name(nameField.getValue());
                newProject.setter_description(descriptionField.getValue());
                // newProject.setter_bugs(
                // Arrays.stream(bugsField.getValue().split(","))
                // .map(String::trim).collect(Collectors.toList())
                // );
                newProject.setter_bugs(new ArrayList<>());
                newProject.setter_managerId(currentUserId);
                newProject.setter_developerIds(new ArrayList<>());
                System.out.println(
                        "Current User ID: " + currentUserId + " Project Manager ID: " + newProject.getter_managerId());

                projectDAO.addProject(newProject);
                refreshProjectGrid(totalProjectCountLabel);
                clearForm(nameField, descriptionField, bugsField);
                projectDialog.close();
                bugsField.setVisible(false); // Keep hidden for next open
            }
        });

        updateButton.addClickListener(event -> {
            if (currentEditingProject != null && !nameField.isEmpty() && !descriptionField.isEmpty()) {
                currentEditingProject.setter_name(nameField.getValue());
                currentEditingProject.setter_description(descriptionField.getValue());
                currentEditingProject.setter_bugs(
                        Arrays.stream(bugsField.getValue().split(","))
                                .map(String::trim).collect(Collectors.toList()));

                boolean success = projectDAO.updateProject(currentEditingProject);
                if (success) {
                    Notification.show("Project updated successfully", 3000, Notification.Position.MIDDLE);
                    refreshProjectGrid(totalProjectCountLabel);
                    clearForm(nameField, descriptionField, bugsField);
                    projectDialog.close();
                    currentEditingProject = null;
                    bugsField.setVisible(false); // Hide again after edit
                } else {
                    Notification.show("Update failed", 3000, Notification.Position.MIDDLE);
                }
            }
        });

        VerticalLayout formLayout = new VerticalLayout(nameField, descriptionField, bugsField,
                new HorizontalLayout(saveButton, updateButton));
        formLayout.setWidthFull(); // Make layout take full width
        formLayout.setPadding(true);
        formLayout.setSpacing(true);
        projectDialog.add(formLayout);

        Button addProjectButton = new Button("Add Project", VaadinIcon.PLUS.create());
        addProjectButton.getStyle().set("margin-bottom", "20px");

        addProjectButton.addClickListener(event -> {
            clearForm(nameField, descriptionField, bugsField);
            saveButton.setVisible(true);
            updateButton.setVisible(false);
            currentEditingProject = null;
            projectDialog.open();
        });

        projectsGrid.addComponentColumn(project -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button viewButton = new Button(VaadinIcon.EYE.create());
            Button editButton = new Button(VaadinIcon.EDIT.create());

            viewButton.addClickListener(e -> {
                viewDialog.removeAll(); // Clear previous content

                ObjectMapper objectMapper = new ObjectMapper();

                H4 idHeader = new H4("ID");
                Span idLabel = new Span(String.valueOf(project.getter_id()));

                H4 nameHeader = new H4("Name");
                Span nameLabel = new Span(project.getter_name());

                H4 descriptionHeader = new H4("Description");
                Span descriptionLabel = new Span(project.getter_description());

                H4 bugsHeader = new H4("Bugs");
                UnorderedList bugList = new UnorderedList();

                try {
                    List<Bug> listOfBugs = objectMapper.readValue(
                            project.getBugsAsJsonString(), new TypeReference<List<Bug>>() {
                            });

                    for (Bug bug : listOfBugs) {
                        bugList.add(new ListItem(bug.getter_title())); // assuming Bug has getTitle()
                    }

                } catch (Exception ex) {
                    bugList.add(new ListItem("Failed to parse bugs"));
                    ex.printStackTrace();
                }

                VerticalLayout detailsLayout = new VerticalLayout(
                        idHeader, idLabel,
                        nameHeader, nameLabel,
                        descriptionHeader, descriptionLabel,
                        bugsHeader, bugList);
                detailsLayout.setSpacing(true);
                detailsLayout.setPadding(true);
                detailsLayout.setWidthFull();
                detailsLayout.setWidth("500px");

                Button closeButton = new Button("Close", closeEvent -> viewDialog.close());
                closeButton.getStyle().set("margin-top", "10px");

                viewDialog.add(detailsLayout, closeButton);
                viewDialog.open();
            });

            editButton.addClickListener(e -> {
                nameField.setValue(project.getter_name());
                descriptionField.setValue(project.getter_description());
                bugsField.setValue(String.join(",", project.getter_bugs()));
                currentEditingProject = project;

                saveButton.setVisible(false);
                updateButton.setVisible(true);

                projectDialog.open();
            });

            // Delete button
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.getElement().setProperty("title", "Delete");
            deleteButton.getStyle().set("color", "red");

            deleteButton.addClickListener(e -> {
                ConfirmDialog confirmDialog = new ConfirmDialog("Confirm Delete",
                        "Are you sure you want to delete this project?",
                        "Delete", confirmEvent -> {
                            boolean deleted = projectDAO.deleteProject(project.getter_id());
                            if (deleted) {
                                Notification.show("Project deleted", 3000, Notification.Position.TOP_CENTER);
                                projects = projectDAO.getAllProjects();
                                projectsGrid.setItems(projects);
                                totalProjectCountLabel.setText(String.valueOf(projects.size()));
                            } else {
                                Notification.show("Failed to delete project", 3000, Notification.Position.TOP_CENTER);
                            }
                        },
                        "Cancel", cancelEvent -> {
                        });
                confirmDialog.open();
            });

            actions.add(viewButton, editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

        content.add(summaryLayout, addProjectButton, projectsGrid);
        return content;
    }

    // Helpers
    private void clearForm(TextField name, TextArea desc, TextArea bugs) {
        name.clear();
        desc.clear();
        bugs.clear();
    }

    private void refreshProjectGrid(H3 totalLabel) {
        projects = projectDAO.getAllProjects();
        projectsGrid.setItems(projects);
        totalLabel.setText(String.valueOf(projects.size()));
    }

    public VerticalLayout createSummaryCard(String title, String count, com.vaadin.flow.component.icon.Icon icon,
            String color, H3 countLabel) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("summary-card");
        card.setAlignItems(Alignment.CENTER);
        card.getStyle().set("background-color", "white");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");
        card.getStyle().set("padding", "20px");
        card.getStyle().set("margin", "10px");

        icon.setSize("40px");
        icon.setColor(color);
        System.out.println("title " + title + " count " + count);
        // H3 countLabel = new H3(count);
        countLabel.setText(count); // set initial count
        countLabel.getStyle().set("margin", "10px 0");
        countLabel.getStyle().set("font-size", "2em");

        Span titleLabel = new Span(title);
        titleLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");

        System.out.println("countLabel text: " + countLabel.getText());
        card.add(icon, countLabel, titleLabel);

        return card;
    }

    public VerticalLayout createTeamContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        H3 teamTitle = new H3("Team Members");

        // Get current manager's ID from session
        int currentManagerId = VaadinSession.getCurrent().getAttribute("userId") != null
                ? Integer.parseInt(VaadinSession.getCurrent().getAttribute("userId").toString())
                : 0;

        System.out.println("[DEBUG] Creating team content for manager ID: " + currentManagerId);

        // Team members grid
        Grid<User> teamGrid = new Grid<>(User.class, false);
        teamGrid.addColumn(User::getter_name).setHeader("Name");
        teamGrid.addColumn(User::getter_email).setHeader("Email");
        teamGrid.addColumn(User::getter_userrole).setHeader("Role");

        // Get projects for current manager
        List<Project> managerProjects = projectService.getAllProjects().stream()
                .filter(p -> p.getter_managerId() == currentManagerId)
                .collect(Collectors.toList());

        System.out.println("[DEBUG] Found " + managerProjects.size() + " projects for manager");

        // Get all developers assigned to manager's projects (modified part)
        Set<Integer> uniqueDeveloperIds = new HashSet<>();
        Set<User> teamMembers = new HashSet<>();
        for (Project project : managerProjects) {
            List<Integer> developerIds = project.getter_developerIds();
            for (Integer devId : developerIds) {
                // Only process each developer ID once
                if (uniqueDeveloperIds.add(devId)) { // returns true if the id was not already in the set
                    User developer = userService.getUserById(devId);
                    if (developer != null) {
                        teamMembers.add(developer);
                    }
                }
            }
        }
        // Add columns for assigned and completed bugs
        teamGrid.addColumn(user -> {
            return bugService.getAssignedBugCount(user.getter_id());
        }).setHeader("Assigned Bugs");

        teamGrid.addColumn(user -> {
            return bugService.getCompletedBugCount(user.getter_id());
        }).setHeader("Completed Bugs");

        // Actions column
        teamGrid.addComponentColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();

            // Assign Bug Button
            Button assignButton = new Button(VaadinIcon.TASKS.create());
            assignButton.addClickListener(e -> openAssignBugDialog(user));

            // Message Button (placeholder for future feature)
            Button messageButton = new Button(VaadinIcon.ENVELOPE.create());
            messageButton.addClickListener(e -> Notification.show("Messaging feature coming soon!",
                    3000, Notification.Position.TOP_CENTER));

            actions.add(assignButton, messageButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

        teamGrid.setItems(teamMembers);
        teamGrid.setSizeFull();

        content.add(teamTitle, teamGrid);
        return content;
    }

    private void openAssignBugDialog(User developer) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Assign Bug to " + developer.getter_name());

        // Get current manager's ID
        int currentManagerId = VaadinSession.getCurrent().getAttribute("userId") != null
                ? Integer.parseInt(VaadinSession.getCurrent().getAttribute("userId").toString())
                : 0;

        // First, get all projects where this developer is assigned
        List<Project> developerProjects = projectService.getAllProjects().stream()
                .filter(p -> p.getter_managerId() == currentManagerId
                        && p.getter_developerIds().contains(developer.getter_id()))
                .collect(Collectors.toList());

        // Project selection combo box
        ComboBox<Project> projectCombo = new ComboBox<>("Select Project");
        projectCombo.setItems(developerProjects);
        projectCombo.setItemLabelGenerator(Project::getter_name);

        // Get unassigned bugs from projects where this developer is assigned
        // List<Bug> availableBugs = bugService.getUnassignedBugs();

        // Bug selection combo box
        // ComboBox<Bug> bugCombo = new ComboBox<>("Select Bug");
        // bugCombo.setItems(availableBugs);
        // bugCombo.setItemLabelGenerator(Bug::getter_title);

        // Bug selection combo box (initially empty)
        ComboBox<Bug> bugCombo = new ComboBox<>("Select Bug");
        bugCombo.setEnabled(false); // Disable until project is selected

        // Update bug combo box when project is selected
        projectCombo.addValueChangeListener(event -> {
            Project selectedProject = event.getValue();
            if (selectedProject != null) {
                List<Bug> unassignedBugs = bugService.getUnassignedBugsForProject(selectedProject.getter_id());
                bugCombo.setItems(unassignedBugs);
                bugCombo.setEnabled(true);
            } else {
                bugCombo.setItems(new ArrayList<>());
                bugCombo.setEnabled(false);
            }
        });

        bugCombo.setItemLabelGenerator(Bug::getter_title);

        Button assignButton = new Button("Assign", e -> {
            Project selectedProject = projectCombo.getValue();
            Bug selectedBug = bugCombo.getValue();
            if (selectedProject != null && selectedBug != null) {
                boolean success = bugService.assignBugToUser(selectedBug.getter_id(), developer.getter_id());
                if (success) {
                    Notification.show("Bug assigned successfully",
                            3000, Notification.Position.TOP_CENTER);
                    dialog.close();
                } else {
                    Notification.show("Failed to assign bug",
                            3000, Notification.Position.TOP_CENTER);
                }
            } else {
                Notification.show("Please select both project and bug",
                        3000, Notification.Position.TOP_CENTER);
            }
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        VerticalLayout dialogLayout = new VerticalLayout(
                projectCombo,
                bugCombo,
                new HorizontalLayout(assignButton, cancelButton));
        dialog.add(dialogLayout);

        dialog.open();
    }

    public VerticalLayout createReportsContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        H3 reportsTitle = new H3("Reports & Analytics");

        // Updated Report types - only Bug Reports and Create Project Report
        HorizontalLayout reportTypes = new HorizontalLayout();
        reportTypes.setWidthFull();

        VerticalLayout bugReport = createReportCard("Bug Reports", "View bug statistics and trends");
        VerticalLayout createProjectReport = createReportCard("Create Project Report",
                "Generate a detailed project status report");

        // Add click listener for the Create Project Report button
        Button createReportButton = (Button) createProjectReport.getComponentAt(2); // Get the button component
        createReportButton.setText("Generate Report");
        createReportButton.addClickListener(e -> openCreateReportDialog());

        // Add this code to the createReportCard method for the "Bug Reports" card
        Button viewButton = (Button) bugReport.getComponentAt(2);
        viewButton.addClickListener(e -> openViewReportsDialog());

        reportTypes.add(bugReport, createProjectReport);
        reportTypes.setFlexGrow(1, bugReport, createProjectReport);

        // Export buttons
        HorizontalLayout exportButtons = new HorizontalLayout();
        Button pdfButton = new Button("Export as PDF", VaadinIcon.DOWNLOAD.create());
        Button csvButton = new Button("Export as CSV", VaadinIcon.DOWNLOAD.create());
        exportButtons.add(pdfButton, csvButton);

        // Add components to content layout - removed chart placeholder
        content.add(reportsTitle, reportTypes, exportButtons);
        return content;
    }

    // Add a method to handle the creation of project reports
    private void openCreateReportDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create Project Report");

        // Get current manager's ID
        int currentManagerId = VaadinSession.getCurrent().getAttribute("userId") != null
                ? Integer.parseInt(VaadinSession.getCurrent().getAttribute("userId").toString())
                : 0;

        // Get projects for current manager
        List<Project> managerProjects = projectService.getAllProjects().stream()
                .filter(p -> p.getter_managerId() == currentManagerId)
                .collect(Collectors.toList());

        // Project selection combo box
        ComboBox<Project> projectCombo = new ComboBox<>("Select Project");
        projectCombo.setItems(managerProjects);
        projectCombo.setItemLabelGenerator(Project::getter_name);
        projectCombo.setWidthFull();

        // Report details
        TextArea reportNotes = new TextArea("Additional Notes");
        reportNotes.setPlaceholder("Add any additional information for this report...");
        reportNotes.setWidthFull();
        reportNotes.setHeight("150px");

        // Preview section - will show basic bug stats when a project is selected
        VerticalLayout previewLayout = new VerticalLayout();
        previewLayout.setVisible(false);
        previewLayout.getStyle().set("border", "1px solid #e0e0e0");
        previewLayout.getStyle().set("border-radius", "4px");
        previewLayout.getStyle().set("padding", "16px");
        previewLayout.getStyle().set("background-color", "#f9f9f9");

        H4 previewTitle = new H4("Report Preview");
        Span bugCountSpan = new Span();
        Span openBugsSpan = new Span();
        Span inProgressSpan = new Span();
        Span resolvedSpan = new Span();

        previewLayout.add(previewTitle, bugCountSpan, openBugsSpan, inProgressSpan, resolvedSpan);

        // Update preview when project is selected
        projectCombo.addValueChangeListener(event -> {
            Project selectedProject = event.getValue();
            if (selectedProject != null) {
                // Get bugs for the selected project
                List<Bug> projectBugs = bugService.getBugsByProjectId(selectedProject.getter_id());

                // Count bugs by status
                long totalBugs = projectBugs.size();
                long openBugs = projectBugs.stream()
                        .filter(b -> b.getter_bugstatus() == BugStatus.reported)
                        .count();
                long inProgressBugs = projectBugs.stream()
                        .filter(b -> b.getter_bugstatus() == BugStatus.in_progress)
                        .count();
                long resolvedBugs = projectBugs.stream()
                        .filter(b -> b.getter_bugstatus() ==BugStatus.fixed ||
                                b.getter_bugstatus() ==BugStatus.verified ||
                                b.getter_bugstatus() ==BugStatus.closed)
                        .count();

                // Update preview
                bugCountSpan.setText("Total Bugs: " + totalBugs);
                openBugsSpan.setText("Open Bugs: " + openBugs);
                inProgressSpan.setText("In Progress: " + inProgressBugs);
                resolvedSpan.setText("Resolved: " + resolvedBugs);

                previewLayout.setVisible(true);
            } else {
                previewLayout.setVisible(false);
            }
        });

        // Buttons
        Button generateButton = new Button("Generate Report", e -> {
            Project selectedProject = projectCombo.getValue();
            if (selectedProject != null) {
                int userId = currentManagerId;
                String generatedBy = userService.getUserById(userId).getter_name();
                String projectName = selectedProject.getter_name();

                // Get bugs for summary
                List<Bug> projectBugs = bugService.getBugsByProjectId(selectedProject.getter_id());
                List<String> bugSummaries = new ArrayList<>();

                for (Bug bug : projectBugs) {
                    bugSummaries.add(bug.getter_id() + ": " + bug.getter_title() + " - " + bug.getter_bugstatus());
                }

                if (bugSummaries.isEmpty()) {
                    bugSummaries.add("No bugs found for this project");
                }

                // Create Report object
                Report report = new Report(0, generatedBy, projectName, bugSummaries, LocalDateTime.now());

                // Save report
                ReportService reportService = new ReportService();
                boolean success = reportService.addReport(report);

                if (success) {
                    Notification.show("Report generated successfully", 3000, Notification.Position.TOP_CENTER);
                    dialog.close();
                } else {
                    Notification.show("Failed to generate report", 3000, Notification.Position.TOP_CENTER);
                }
            } else {
                Notification.show("Please select a project", 3000, Notification.Position.TOP_CENTER);
            }
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        HorizontalLayout buttonLayout = new HorizontalLayout(generateButton, cancelButton);

        VerticalLayout dialogLayout = new VerticalLayout(
                projectCombo,
                reportNotes,
                previewLayout,
                buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        dialog.add(dialogLayout);
        dialog.setWidth("600px");
        dialog.open();
    }

    public VerticalLayout createReportCard(String title, String description) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("report-card");
        card.setAlignItems(Alignment.CENTER);
        card.getStyle().set("background-color", "white");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");
        card.getStyle().set("padding", "20px");
        card.getStyle().set("margin", "10px");
        card.getStyle().set("cursor", "pointer");

        H3 titleLabel = new H3(title);
        titleLabel.getStyle().set("margin-top", "0");

        Span descriptionLabel = new Span(description);
        descriptionLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        descriptionLabel.getStyle().set("text-align", "center");

        Button viewButton = new Button("View Report");
        viewButton.getStyle().set("margin-top", "15px");

        card.add(titleLabel, descriptionLabel, viewButton);

        return card;
    }

    // Add this method to ManagerPanel class
    private void openViewReportsDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("View Reports");

        // Create grid for reports
        Grid<Report> reportsGrid = new Grid<>(Report.class, false);
        reportsGrid.addColumn(Report::getter_id).setHeader("ID");
        reportsGrid.addColumn(Report::getter_project).setHeader("Project");
        reportsGrid.addColumn(Report::getter_generatedBy).setHeader("Generated By");
        reportsGrid.addColumn(r -> r.getter_generatedDate().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).setHeader("Date");

        // Fetch reports
        ReportService reportService = new ReportService();
        List<Report> reports = reportService.getAllReports();
        reportsGrid.setItems(reports);

        // Add view details button
        reportsGrid.addComponentColumn(report -> {
            Button viewDetailsButton = new Button("View Details");
            viewDetailsButton.addClickListener(event -> openReportDetailsDialog(report));
            return viewDetailsButton;
        }).setHeader("Actions");

        Button closeButton = new Button("Close", e -> dialog.close());

        VerticalLayout dialogLayout = new VerticalLayout(reportsGrid, closeButton);
        dialogLayout.setSizeFull();

        dialog.add(dialogLayout);
        dialog.setWidth("800px");
        dialog.setHeight("600px");
        dialog.open();
    }

    private void openReportDetailsDialog(Report report) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Report Details");

        H3 projectTitle = new H3("Project: " + report.getter_project());
        H4 generatedInfo = new H4("Generated by " + report.getter_generatedBy() +
                " on " + report.getter_generatedDate().format(
                        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));

        H4 bugsTitle = new H4("Bug Summaries");
        UnorderedList bugsList = new UnorderedList();

        for (String bugSummary : report.getter_bugs_summaries()) {
            bugsList.add(new ListItem(bugSummary));
        }

        Button closeButton = new Button("Close", e -> dialog.close());

        VerticalLayout dialogLayout = new VerticalLayout(
                projectTitle, generatedInfo, bugsTitle, bugsList, closeButton);

        dialog.add(dialogLayout);
        dialog.setWidth("600px");
        dialog.open();
    }
}
