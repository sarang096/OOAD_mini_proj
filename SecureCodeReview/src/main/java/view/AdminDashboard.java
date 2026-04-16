package main.java.view;
import main.java.model.factory.UserFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import main.java.DAO.UserDAO;
import main.java.model.ApprovalRule;
import main.java.model.ChangeRequest;
import main.java.model.User;
import main.java.security.AuthService;
import main.java.service.ApprovalRuleService;
import main.java.service.ChangeRequestService;

import java.util.List;

@Route("admin")
@PageTitle("Admin Dashboard")
public class AdminDashboard extends VerticalLayout {

    private final UserDAO              userDAO     = new UserDAO();
    private final ChangeRequestService crService   = new ChangeRequestService();
    private final ApprovalRuleService  ruleService = new ApprovalRuleService();
    private final AuthService          authService = AuthService.getInstance();

    private Grid<User>          userGrid    = new Grid<>(User.class, false);
    private Grid<ChangeRequest> requestGrid = new Grid<>(ChangeRequest.class, false);
    private Grid<ApprovalRule>  ruleGrid    = new Grid<>(ApprovalRule.class, false);

    public AdminDashboard() {
        User user = authService.getCurrentUser();
        if (user == null) { getUI().ifPresent(ui -> ui.navigate("")); return; }

        add(new H2(" Admin Dashboard — " + user.getter_name()));

        setupUserGrid();
        setupRequestGrid();
        setupRuleGrid();

        Button addUserBtn    = new Button("Add User",           e -> openAddUserDialog());
        Button removeUserBtn = new Button("Remove User",        e -> openRemoveUserDialog());
        Button addRuleBtn    = new Button("Add Approval Rule",  e -> openAddRuleDialog(user));
        Button deleteRuleBtn = new Button("Delete Rule",        e -> openDeleteRuleDialog());
        Button mergeBtn      = new Button("Merge Request",      e -> openMergeDialog());
        Button logoutBtn     = new Button("Logout", e -> {
            authService.logout();
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        add(new HorizontalLayout(addUserBtn, removeUserBtn, addRuleBtn,
                                 deleteRuleBtn, mergeBtn, logoutBtn));
        add(new H2("Users"),              userGrid);
        add(new H2("All Change Requests"), requestGrid);
        add(new H2("Approval Rules"),      ruleGrid);
    }

    private void setupUserGrid() {
        userGrid.addColumn(User::getter_id).setHeader("ID");
        userGrid.addColumn(User::getter_name).setHeader("Name");
        userGrid.addColumn(User::getter_email).setHeader("Email");
        userGrid.addColumn(u -> u.getter_userrole().name()).setHeader("Role");
        userGrid.setWidthFull();
        refreshUserGrid();
    }

    private void setupRequestGrid() {
        requestGrid.addColumn(ChangeRequest::getRequestId).setHeader("ID");
        requestGrid.addColumn(ChangeRequest::getTitle).setHeader("Title");
        requestGrid.addColumn(cr -> cr.getStatus().name()).setHeader("Status");
        requestGrid.addColumn(cr -> cr.getCreatedDate().toString()).setHeader("Date");
        requestGrid.setWidthFull();
        refreshRequestGrid();
    }

    private void setupRuleGrid() {
        ruleGrid.addColumn(ApprovalRule::getRuleId).setHeader("ID");
        ruleGrid.addColumn(ApprovalRule::getRuleDescription).setHeader("Rule");
        ruleGrid.setWidthFull();
        refreshRuleGrid();
    }

    private void refreshUserGrid()    { userGrid.setItems(userDAO.getAllUsers()); }
    private void refreshRequestGrid() { requestGrid.setItems(crService.getAllRequests()); }
    private void refreshRuleGrid()    { ruleGrid.setItems(ruleService.getAllRules()); }

    private void openAddUserDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New User");

        TextField nameField  = new TextField("Name");
        TextField emailField = new TextField("Email");
        TextField passField  = new TextField("Password");

        // ← Dropdown instead of text field — no more wrong role issue!
        Select<String> roleSelect = new Select<>();
        roleSelect.setLabel("Role");
        roleSelect.setItems("Developer", "Reviewer", "Administrator");
        roleSelect.setValue("Developer");

        Button save = new Button("Add", e -> {
                if (nameField.getValue().isBlank() || emailField.getValue().isBlank() || passField.getValue().isBlank()) {
                    Notification.show(" All fields are required!");
                    return;
    }
               String selectedRole = roleSelect.getValue();
               User existing = userDAO.getUserByEmail(emailField.getValue().trim());
               if (existing != null) {
                   Notification.show(" Email already exists!");
                   return;
              }
              User newUser = UserFactory.createSpecializedUser(
                 selectedRole,
                 0,
                 nameField.getValue().trim(),
                 emailField.getValue().trim(),
                 passField.getValue().trim()
);
userDAO.addUser(newUser);
            Notification.show(" User added as " + selectedRole + "!");
            refreshUserGrid();
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", e -> dialog.close());

        dialog.add(new VerticalLayout(nameField, emailField, passField, roleSelect,
                   new HorizontalLayout(save, cancel)));
        dialog.open();
    }

    private void openRemoveUserDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Remove User");
        TextField idField = new TextField("User ID");
        Button remove = new Button("Remove", e -> {
            userDAO.deleteUser(Integer.parseInt(idField.getValue().trim()));
            Notification.show(" User removed!");
            refreshUserGrid();
            dialog.close();
        });
        remove.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.add(new VerticalLayout(idField, new HorizontalLayout(remove, cancel)));
        dialog.open();
    }

    private void openAddRuleDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Approval Rule");
        TextField descField = new TextField("Rule Description");
        descField.setWidthFull();
        Button save = new Button("Add", e -> {
            ruleService.addRule(descField.getValue().trim(), user.getter_id());
            Notification.show(" Rule added!");
            refreshRuleGrid();
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.add(new VerticalLayout(descField, new HorizontalLayout(save, cancel)));
        dialog.open();
    }

    private void openDeleteRuleDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete Rule");
        TextField idField = new TextField("Rule ID");
        Button delete = new Button("Delete", e -> {
            ruleService.deleteRule(Integer.parseInt(idField.getValue().trim()));
            Notification.show("Rule deleted!");
            refreshRuleGrid();
            dialog.close();
        });
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.add(new VerticalLayout(idField, new HorizontalLayout(delete, cancel)));
        dialog.open();
    }

    private void openMergeDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Merge Request");
        TextField idField = new TextField("Request ID");
        Button merge = new Button("Merge", e -> {
            try {
                int id = Integer.parseInt(idField.getValue().trim());
                crService.markMerged(id);
                Notification.show(" Merged!");
                refreshRequestGrid();
                dialog.close();
           } catch (NumberFormatException ex) {
                Notification.show(" Invalid Request ID!");
             }
});
        merge.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.add(new VerticalLayout(idField, new HorizontalLayout(merge, cancel)));
        dialog.open();
    }
}