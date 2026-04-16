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
import main.java.security.AuthService;
import main.java.service.CodeReviewFacade;

import java.util.List;

@Route("developer")
@PageTitle("Developer Dashboard")
public class DeveloperDashboard extends VerticalLayout {

    private final CodeReviewFacade facade     = CodeReviewFacade.getInstance();
    private final AuthService      authService = AuthService.getInstance();
    private Grid<ChangeRequest> grid = new Grid<>(ChangeRequest.class, false);

    public DeveloperDashboard() {
        User user = authService.getCurrentUser();
        if (user == null) { getUI().ifPresent(ui -> ui.navigate("")); return; }

        add(new H2("🧑‍💻 Developer Dashboard — " + user.getter_name()));

        grid.addColumn(ChangeRequest::getRequestId).setHeader("ID");
        grid.addColumn(ChangeRequest::getTitle).setHeader("Title");
        grid.addColumn(ChangeRequest::getDescription).setHeader("Description");
        grid.addColumn(cr -> cr.getStatus().name()).setHeader("Status");
        grid.addColumn(cr -> cr.getCreatedDate().toString()).setHeader("Date");
        grid.setWidthFull();
        refreshGrid(user.getter_id());

        Button submitBtn   = new Button("Submit New Request", e -> openSubmitDialog(user));
        Button resubmitBtn = new Button("Resubmit Rejected",  e -> openResubmitDialog(user));
        Button logoutBtn   = new Button("Logout", e -> {
            authService.logout();
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        logoutBtn.getStyle().set("background-color", "#ff4444").set("color", "white");

        add(new HorizontalLayout(submitBtn, resubmitBtn, logoutBtn), grid);
    }

    private void refreshGrid(int devId) {
        List<ChangeRequest> list = facade.getMyRequests(devId);
        grid.setItems(list);
    }

    private void openSubmitDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Submit Change Request");

        TextField titleField = new TextField("Title");
        TextArea  descField  = new TextArea("Description");

        Button save = new Button("Submit", e -> {
            ChangeRequest cr = facade.submitRequest(
                titleField.getValue(), descField.getValue(), user.getter_id());
            if (cr != null) {
                Notification.show(" Submitted! ID: " + cr.getRequestId());
                refreshGrid(user.getter_id());
            } else {
                Notification.show(" Validation failed.");
            }
            dialog.close();
        });
        Button cancel = new Button("Cancel", e -> dialog.close());

        dialog.add(new VerticalLayout(titleField, descField,
                   new HorizontalLayout(save, cancel)));
        dialog.open();
    }

    private void openResubmitDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Resubmit Request");

        TextField idField    = new TextField("Request ID");
        TextField titleField = new TextField("New Title");
        TextArea  descField  = new TextArea("New Description");

        Button save = new Button("Resubmit", e -> {
            boolean ok = facade.resubmitRequest(
                Integer.parseInt(idField.getValue()),
                titleField.getValue(), descField.getValue());
            Notification.show(ok ? " Resubmitted!" 
                                 : " Failed — only Rejected requests can be resubmitted.");
            refreshGrid(user.getter_id());
            dialog.close();
        });
        Button cancel = new Button("Cancel", e -> dialog.close());

        dialog.add(new VerticalLayout(idField, titleField, descField,
                   new HorizontalLayout(save, cancel)));
        dialog.open();
    }
}