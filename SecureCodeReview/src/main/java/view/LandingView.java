package main.java.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import main.java.model.User;
import main.java.security.AuthService;

@Route("")
@PageTitle("Login")
public class LandingView extends VerticalLayout {

    private final AuthService authService = AuthService.getInstance();

    public LandingView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title    = new H1("🔐 Secure Code Review System");
        H3 subtitle = new H3("Please login to continue");

        TextField     emailField = new TextField("Email");
        PasswordField passField  = new PasswordField("Password");
        emailField.setWidth("300px");
        passField.setWidth("300px");

        Button loginBtn = new Button("Login", e -> {
            String email = emailField.getValue().trim();
            String pass  = passField.getValue().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Notification.show("❌ Please enter email and password.");
                return;
            }

            User user = authService.login(email, pass);
            if (user == null) {
                Notification.show("❌ Invalid credentials.");
                return;
            }

            String roleName = user.getter_userrole().name();
            if (roleName.equals("Developer")) {
                getUI().ifPresent(ui -> ui.navigate("developer"));
            } else if (roleName.equals("Reviewer")) {
                getUI().ifPresent(ui -> ui.navigate("reviewer"));
            } else if (roleName.equals("Administrator")) {
                getUI().ifPresent(ui -> ui.navigate("admin"));
            } else {
                Notification.show("❌ No dashboard for this role.");
            }
        });
        loginBtn.getStyle().set("background-color", "#1a73e8").set("color", "white");

        add(title, subtitle, emailField, passField, loginBtn);
    }
}