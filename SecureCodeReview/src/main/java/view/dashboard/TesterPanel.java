package main.java.view.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;

import main.java.DAO.BugDAO;
import main.java.DAO.ProjectDAO;
import main.java.DAO.UserDAO;
import main.java.model.Project;
import main.java.security.AuthService;
import main.java.service.ProjectService;
import main.java.service.BugService;
import main.java.model.Bug;
import main.java.model.BugStatus;
import main.java.model.Priority;
import main.java.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TesterPanel extends VerticalLayout {

    private final AuthService authService;
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final BugDAO bugDAO = new BugDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BugService bugService = new BugService();
    private List<Project> projects;
    private List<Bug> bug;
    private Grid<Bug> bugsGrid;

    public TesterPanel(AuthService authService) {
        this.authService = authService;

        addClassName("tester-panel");
        setSizeFull();

        // Welcome message
        H2 welcomeTitle = new H2("Tester Dashboard");
        welcomeTitle.getStyle().set("margin-top", "0");

        // Create tabs for different tester functions
        Tab reportBugTab = new Tab(VaadinIcon.PLUS.create(), new H3("Report Bug"));
        Tab myBugsTab = new Tab(VaadinIcon.BUG.create(), new H3("My Bugs"));

        Tabs tabs = new Tabs(reportBugTab, myBugsTab);
        tabs.setWidthFull();

        // Content for each tab
        Component reportBugContent = createReportBugContent();
        Component myBugsContent = createMyBugsContent();

        // Initially show report bug content
        add(welcomeTitle, tabs, reportBugContent);

        // Tab change listener
        tabs.addSelectedChangeListener(event -> {
            // Remove all content
            remove(getComponentAt(2));

            // Add appropriate content based on selected tab
            if (event.getSelectedTab().equals(reportBugTab)) {
                add(reportBugContent);
            } else if (event.getSelectedTab().equals(myBugsTab)) {
                add(myBugsContent);
            }
        });
    }

    private Component createReportBugContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        H3 formTitle = new H3("Report a New Bug");

        VerticalLayout form = new VerticalLayout();
        form.setMaxWidth("800px");
        form.setPadding(true);
        form.getStyle().set("background-color", "white");
        form.getStyle().set("border-radius", "8px");
        form.getStyle().set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");

        TextField titleField = new TextField("Bug Title");
        titleField.setWidthFull();
        titleField.setPlaceholder("Enter a descriptive title");
        titleField.setRequired(true);

        ComboBox<Project> projectField = new ComboBox<>("Project");
        projectField.setWidthFull();
        projectField.setRequired(true);
        List<Project> projects = new ProjectService().getAllProjects();
        projectField.setItems(projects);
        projectField.setItemLabelGenerator(Project::getter_name);

        ComboBox<Priority> priorityField = new ComboBox<>("Priority");
        priorityField.setItems(Priority.values());
        priorityField.setWidthFull();
        priorityField.setRequired(true);

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();
        descriptionField.setPlaceholder("Describe the bug in detail and include steps to reproduce it");
        descriptionField.setMinHeight("200px");
        descriptionField.setRequired(true);

        Button submitButton = new Button("Submit Bug Report", VaadinIcon.CHECK.create());
        submitButton.getStyle().set("margin-top", "20px");

        submitButton.addClickListener(event -> {
            if (titleField.isEmpty() || projectField.isEmpty() || priorityField.isEmpty()
                    || descriptionField.isEmpty()) {
                Notification.show("Please fill in all the fields.", 3000, Notification.Position.MIDDLE);
                return;
            }

            String currentUserId = VaadinSession.getCurrent().getAttribute("userId").toString();
            System.out.println(currentUserId);
            Bug newBug = new Bug(0, "", "", BugStatus.reported, Priority.Low, LocalDateTime.now(),
                    LocalDateTime.now(), null, null, 0);

            newBug.setter_title(titleField.getValue());
            newBug.setter_description(descriptionField.getValue());
            newBug.setter_bugstatus(BugStatus.reported);
            newBug.setter_priority(priorityField.getValue());
            newBug.setter_createdAt(LocalDateTime.now());
            newBug.setter_updatedAt(LocalDateTime.now());
            newBug.setter_assignedTo(null); // Unassigned initially
            newBug.setter_reportedBy(currentUserId); // Replace with actual current user retrieval
            newBug.setter_projectId(projectField.getValue().getter_id());

            new BugDAO().addBug(newBug);

            Notification.show("Bug reported successfully!", 3000, Notification.Position.BOTTOM_START);

            titleField.clear();
            projectField.clear();
            priorityField.clear();
            descriptionField.clear();
        });

        form.add(titleField, projectField, priorityField, descriptionField, submitButton);
        content.add(formTitle, form);
        content.setAlignItems(Alignment.CENTER);

        return content;
    }

    private Component createMyBugsContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        // Get current username from session
        String currentUser = (String) VaadinSession.getCurrent().getAttribute("email");
        if (currentUser == null) {
            content.add(new H3("User not logged in."));
            return content;
        }

        // Fetch tester's ID using UserDAO
        User currentTester = new UserDAO().getUserByEmail(currentUser);
        if (currentTester == null) {
            content.add(new H3("User not found in the database."));
            return content;
        }

        int testerId = currentTester.getter_id();

        // Fetch bugs reported by this tester
        List<Bug> bugs = new BugDAO().getBugByReportedId(String.valueOf(testerId));

        // Count summary stats
        long totalReported = bugs.size();
        long totalOpen = bugs.stream()
                .filter(b -> b.getter_bugstatus() == BugStatus.reported
                        || b.getter_bugstatus() == BugStatus.in_progress)
                .count();
        long totalResolved = bugs.stream()
                .filter(b -> b.getter_bugstatus() == BugStatus.fixed
                        || b.getter_bugstatus() == BugStatus.closed)
                .count();

        // Summary cards
        HorizontalLayout summaryLayout = new HorizontalLayout();
        summaryLayout.setWidthFull();

        VerticalLayout reportedCard = createSummaryCard("Reported", String.valueOf(totalReported),
                VaadinIcon.BUG.create(), "#1676f3");
        VerticalLayout openCard = createSummaryCard("Open", String.valueOf(totalOpen), VaadinIcon.HOURGLASS.create(),
                "orange");
        VerticalLayout resolvedCard = createSummaryCard("Resolved", String.valueOf(totalResolved),
                VaadinIcon.CHECK.create(), "green");

        summaryLayout.add(reportedCard, openCard, resolvedCard);
        summaryLayout.setFlexGrow(1, reportedCard, openCard, resolvedCard);

        // Bugs Grid
        bugsGrid = new Grid<>(Bug.class, false);
        bugsGrid.addColumn(Bug::getter_id).setHeader("ID");
        bugsGrid.addColumn(Bug::getter_title).setHeader("Title");
        bugsGrid.addColumn(b -> b.getter_bugstatus().name()).setHeader("Status");
        bugsGrid.addColumn(Bug::getter_priority).setHeader("Priority");
        // Fetch project name using lambda and ProjectDAO
        ProjectDAO projectDAO = new ProjectDAO();
        bugsGrid.addColumn(bug -> {
            Project project = projectDAO.getProjectById(bug.getter_projectId());
            return project != null ? project.getter_name() : "Unknown Project";
        }).setHeader("Project");
        bugsGrid.addColumn(b -> b.getter_createdAt().toLocalDate()).setHeader("Reported Date");

        bugsGrid.setItems(bugs);
        bugsGrid.setSizeFull();

        // Action buttons - Updated to match DeveloperPanel functionality
        bugsGrid.addComponentColumn(bug -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            actions.setPadding(false);
            actions.setJustifyContentMode(JustifyContentMode.CENTER);

            // View button
            Button viewButton = new Button(VaadinIcon.EYE.create());
            viewButton.getElement().setAttribute("aria-label", "View bug details");
            viewButton.addClickListener(e -> showBugDetailsDialog(bug));

            // Verify button (tick button) - only enabled for fixed bugs
            Button verifyButton = new Button(VaadinIcon.CHECK.create());
            verifyButton.getElement().setAttribute("aria-label", "Verify bug fix");
            
            // Only enable verify button for fixed bugs
            verifyButton.setEnabled(bug.getter_bugstatus() == BugStatus.fixed);

            verifyButton.addClickListener(e -> {
                if (bug.getter_bugstatus() == BugStatus.fixed) {
                    bug.setter_bugstatus(BugStatus.verified);
                    bug.setter_updatedAt(java.time.LocalDateTime.now());

                    if (bugService.updateBugStatus(bug.getter_id(), BugStatus.verified)) {
                        Notification.show("Bug verified successfully",
                                3000, Notification.Position.MIDDLE);
                        refreshBugsGrid(testerId);
                    } else {
                        Notification.show("Failed to update bug status",
                                3000, Notification.Position.MIDDLE);
                    }
                }
            });

            actions.add(viewButton, verifyButton);
            return actions;
        }).setHeader("Actions")
                .setWidth("150px")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);

        content.add(summaryLayout, bugsGrid);
        return content;
    }

    private void refreshBugsGrid(int testerId) {
        List<Bug> bugs = new BugDAO().getBugByReportedId(String.valueOf(testerId));
        bugsGrid.setItems(bugs);
    }

    private void showBugDetailsDialog(Bug bug) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Bug Details: #" + bug.getter_id());
        dialog.setWidth("600px");
        
        // Create the layout for bug details
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        
        // Title
        H3 title = new H3(bug.getter_title());
        title.getStyle().set("margin-top", "0");
        
        // Format dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        String createdDate = bug.getter_createdAt().format(formatter);
        String updatedDate = bug.getter_updatedAt().format(formatter);
        
        // Description
        Span description = new Span(bug.getter_description());
        description.getElement().getStyle().set("white-space", "pre-wrap");
        description.getElement().getStyle().set("font-family", "monospace");
        description.getElement().getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        description.getElement().getStyle().set("padding", "1em");
        description.getElement().getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        description.setWidthFull();
        
        // Create info grid with status and priority 
        Grid<KeyValuePair> infoGrid = new Grid<>();
        infoGrid.setAllRowsVisible(true);
        infoGrid.setWidth("100%");
        infoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        
        // Add columns to the info grid
        infoGrid.addColumn(KeyValuePair::getKey).setHeader("Field").setWidth("150px").setFlexGrow(0);
        infoGrid.addComponentColumn(pair -> {
            if ("Priority".equals(pair.getKey())) {
                Span prioritySpan = new Span(bug.getter_priority().name());
                prioritySpan.getStyle()
                    .set("color", getPriorityColor(bug.getter_priority()))
                    .set("font-weight", "bold");
                return prioritySpan;
            } else if ("Status".equals(pair.getKey())) {
                Span statusSpan = new Span(formatStatus(bug.getter_bugstatus()));
                statusSpan.getStyle()
                    .set("font-weight", "bold");
                return statusSpan;
            } else {
                return new Span(pair.getValue());
            }
        }).setHeader("Value").setFlexGrow(1);
        
        // Get additional information
        ProjectDAO projectDAO = new ProjectDAO();
        Project project = projectDAO.getProjectById(bug.getter_projectId());
        String projectName = project != null ? project.getter_name() : "Unknown Project";

        User reporter = null;
        try {
            reporter = userDAO.getUserById(Integer.parseInt(bug.getter_reportedBy()));
        } catch (NumberFormatException ex) {
            // Handle case where reportedBy is not a valid integer
        }
        String reporterName = reporter != null ? reporter.getter_name() : "Unknown User";
        
        // Get assignee information
        String assigneeName = "Unassigned";
        if (bug.getter_assignedTo() != null && !bug.getter_assignedTo().isEmpty()) {
            try {
                User assignee = userDAO.getUserById(Integer.parseInt(bug.getter_assignedTo()));
                if (assignee != null) {
                    assigneeName = assignee.getter_name();
                }
            } catch (NumberFormatException ex) {
                // Handle case where assignedTo is not a valid integer
            }
        }
        
        // Create data for the grid
        List<KeyValuePair> infoData = new ArrayList<>();
        infoData.add(new KeyValuePair("Status", formatStatus(bug.getter_bugstatus())));
        infoData.add(new KeyValuePair("Priority", bug.getter_priority().name()));
        infoData.add(new KeyValuePair("Project", projectName));
        infoData.add(new KeyValuePair("Reported By", reporterName));
        infoData.add(new KeyValuePair("Assigned To", assigneeName));
        infoData.add(new KeyValuePair("Created", createdDate));
        infoData.add(new KeyValuePair("Last Updated", updatedDate));
        
        infoGrid.setItems(infoData);
        
        // Action buttons based on status
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);
        
        Button closeDialogButton = new Button("Close", event -> dialog.close());
        closeDialogButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        Button verifyButton = new Button("Verify Fix", event -> {
            if (bug.getter_bugstatus() == BugStatus.fixed) {
                bug.setter_bugstatus(BugStatus.verified);
                bug.setter_updatedAt(java.time.LocalDateTime.now());
                
                if (bugService.updateBugStatus(bug.getter_id(), BugStatus.verified)) {
                    Notification.show("Bug verified successfully", 3000, Notification.Position.MIDDLE);
                    refreshBugsGrid(Integer.parseInt(bug.getter_reportedBy()));
                    dialog.close();
                } else {
                    Notification.show("Failed to update bug status", 3000, Notification.Position.MIDDLE);
                }
            }
        });
        verifyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        verifyButton.setEnabled(bug.getter_bugstatus() == BugStatus.fixed);
        
        if (bug.getter_bugstatus() == BugStatus.fixed) {
            actions.add(verifyButton, closeDialogButton);
        } else {
            actions.add(closeDialogButton);
        }
        
        // Add components to content
        content.add(title, description, new Hr(), infoGrid, actions);
        
        dialog.add(content);
        dialog.open();
    }

    // Helper class for key-value pairs
    private static class KeyValuePair {
        private final String key;
        private final String value;
        
        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() { return key; }
        public String getValue() { return value; }
    }

    // Helper methods for formatting
    private String formatStatus(BugStatus status) {
        if (status == null) return "";
        String text = status.name().replace('_', ' ');
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private String getPriorityColor(Priority priority) {
        if (priority == null) return "gray";
        
        switch(priority) {
            case Critical: return "var(--lumo-error-color)";
            case High: return "orange";
            case Medium: return "blue";
            case Low: return "green";
            default: return "gray";
        }
    }

    private VerticalLayout createSummaryCard(String title, String count, com.vaadin.flow.component.icon.Icon icon,
            String color) {
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

        H3 countLabel = new H3(count);
        countLabel.getStyle().set("margin", "10px 0");
        countLabel.getStyle().set("font-size", "2em");

        Span titleLabel = new Span(title);
        titleLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");

        card.add(icon, countLabel, titleLabel);

        return card;
    }
}