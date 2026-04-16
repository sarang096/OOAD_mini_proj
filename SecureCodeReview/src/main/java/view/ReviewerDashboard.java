package main.java.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import main.java.model.ChangeRequest;
import main.java.model.User;
import main.java.model.enums.ChangeStatus;
import main.java.security.AuthService;
import main.java.service.CodeReviewFacade;

@Route("reviewer")
@PageTitle("Reviewer Dashboard")
public class ReviewerDashboard extends VerticalLayout {

    private final CodeReviewFacade facade      = CodeReviewFacade.getInstance();
    private final AuthService      authService = AuthService.getInstance();
    private Grid<ChangeRequest> grid = new Grid<>(ChangeRequest.class, false);

    public ReviewerDashboard() {
        User user = authService.getCurrentUser();
        if (user == null) { getUI().ifPresent(ui -> ui.navigate("")); return; }

        add(new H2(" Reviewer Dashboard — " + user.getter_name()));

        grid.addColumn(ChangeRequest::getRequestId).setHeader("ID");
        grid.addColumn(ChangeRequest::getTitle).setHeader("Title");
        grid.addColumn(ChangeRequest::getDescription).setHeader("Description");
        grid.addColumn(cr -> cr.getStatus().name()).setHeader("Status");
        grid.setWidthFull();
        refreshGrid();

        Button approveBtn = new Button("Approve Request", e -> openReviewDialog(user, true));
        Button rejectBtn  = new Button("Reject Request",  e -> openReviewDialog(user, false));
        Button commentBtn = new Button("Add Comment",     e -> openCommentDialog(user));
        Button logoutBtn  = new Button("Logout", e -> {
            authService.logout();
            getUI().ifPresent(ui -> ui.navigate(""));
        });

        logoutBtn.getStyle().set("background-color", "#ff4444").set("color", "white");
        rejectBtn.getStyle().set("background-color", "#ff8800").set("color", "white");
        approveBtn.getStyle().set("background-color", "#00aa44").set("color", "white");

        add(new HorizontalLayout(approveBtn, rejectBtn, commentBtn, logoutBtn), grid);
    }

    private void refreshGrid() {
        var list = facade.getPendingRequests();
        grid.setItems(list);
    }

    private void openReviewDialog(User user, boolean isApprove) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isApprove ? "Approve Request" : "Reject Request");

        TextField idField      = new TextField("Request ID");
        TextArea  commentField = new TextArea("Comments");

        Button confirm = new Button("Confirm", e -> {
            // Edge case 1 — empty comment
            if (commentField.getValue().isBlank()) {
                Notification.show(" Please add a comment!");
                return;
            }
            // Edge case 2 — invalid request ID
            try {
                int id = Integer.parseInt(idField.getValue().trim());
                String comments = commentField.getValue();
                if (isApprove) facade.approveRequest(id, user.getter_id(), comments);
                else           facade.rejectRequest(id, user.getter_id(), comments);
                Notification.show(isApprove ? " Approved!" : " Rejected!");
                refreshGrid();
                dialog.close();
            } catch (NumberFormatException ex) {
                Notification.show(" Invalid Request ID!");
            }
        });
        Button cancel = new Button("Cancel", e -> dialog.close());

        dialog.add(new VerticalLayout(idField, commentField,
                   new HorizontalLayout(confirm, cancel)));
        dialog.open();
    }

    private void openCommentDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Comment");

        TextField idField      = new TextField("Request ID");
        TextArea  commentField = new TextArea("Comment");

        Button save = new Button("Save", e -> {
            // Edge case 1 — empty comment
            if (commentField.getValue().isBlank()) {
                Notification.show(" Comment cannot be empty!");
                return;
            }
            // Edge case 2 — invalid request ID
            try {
                facade.addComment(
                    Integer.parseInt(idField.getValue().trim()),
                    user.getter_id(), commentField.getValue());
                Notification.show(" Comment added!");
                refreshGrid();
                dialog.close();
            } catch (NumberFormatException ex) {
                Notification.show("Invalid Request ID!");
            }
        });
        Button cancel = new Button("Cancel", e -> dialog.close());

        dialog.add(new VerticalLayout(idField, commentField,
                   new HorizontalLayout(save, cancel)));
        dialog.open();
    }
}