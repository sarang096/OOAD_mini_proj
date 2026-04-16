package main.java.view.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import main.java.service.UserService;
import main.java.view.components.UserTypeSelector;

@Route("signup")
@PageTitle("Sign Up | BugTracker")
public class SignupView extends VerticalLayout {

    private final UserService userService = new UserService(); // Ideally injected

    public SignupView() {
        // Main layout setup
        addClassName("signup-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        // Add navigation bar
        add(createNavBar());
        
        // Content wrapper for centering
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSizeFull();
        contentWrapper.setAlignItems(Alignment.CENTER);
        contentWrapper.setJustifyContentMode(JustifyContentMode.CENTER);
        
        // Container for styling
        Div container = new Div();
        container.addClassName("signup-container");
        container.getStyle()
                .set("width", "100%")
                .set("max-width", "400px")
                .set("padding", "2rem")
                .set("border-radius", "8px")
                .set("background-color", "white")
                .set("box-shadow", "0px 4px 10px rgba(0, 0, 0, 0.1)");

        // Title with icon
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidthFull();
        titleLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        titleLayout.setAlignItems(Alignment.CENTER);
        
        Icon bugIcon = VaadinIcon.BUG.create();
        bugIcon.setColor("#1676f3");
        bugIcon.setSize("24px");
        
        H2 title = new H2("Create an Account");
        title.getStyle().set("margin", "0");
        
        titleLayout.add(bugIcon, title);
        titleLayout.getStyle().set("margin-bottom", "1.5rem");

        // Input fields
        TextField fullName = new TextField("Full Name");
        fullName.setRequired(true);
        fullName.setPlaceholder("Enter your full name");
        fullName.setWidthFull();
        fullName.getStyle().set("margin-bottom", "1rem");

        TextField username = new TextField("Username");
        username.setRequired(true);
        username.setPlaceholder("Choose a username");
        username.setWidthFull();
        username.getStyle().set("margin-bottom", "1rem");

        EmailField email = new EmailField("Email");
        email.setRequired(true);
        email.setPlaceholder("Enter your email");
        email.setWidthFull();
        email.getStyle().set("margin-bottom", "1rem");

        PasswordField password = new PasswordField("Password");
        password.setRequired(true);
        password.setPlaceholder("Create a password");
        password.setWidthFull();
        password.getStyle().set("margin-bottom", "1rem");

        PasswordField confirmPassword = new PasswordField("Confirm Password");
        confirmPassword.setRequired(true);
        confirmPassword.setPlaceholder("Confirm your password");
        confirmPassword.setWidthFull();
        confirmPassword.getStyle().set("margin-bottom", "1rem");

        // User role dropdown
        UserTypeSelector userTypeSelector = new UserTypeSelector();
        userTypeSelector.getStyle().set("margin-bottom", "1.5rem");
        userTypeSelector.setWidthFull();

        // Sign up button
        Button signupButton = new Button("Sign Up", event -> {
            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() || userTypeSelector.getValue() == null) {

                Notification.show("Please fill in all required fields", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (!password.getValue().equals(confirmPassword.getValue())) {
                Notification.show("Passwords do not match", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            String usernameValue = username.getValue();
            String emailValue = email.getValue();
            String passwordValue = password.getValue();
            String roleValue = userTypeSelector.getValue();

            boolean success = userService.addUser(usernameValue, emailValue, passwordValue, roleValue);
            System.out.println("Values of user: " + usernameValue + ", " + emailValue + ", " + passwordValue + ", " + roleValue);

            if (success) {
                Notification.show("User registered successfully!", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate("login");
            } else {
                Notification.show("Failed to register user. Please check the role and try again.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        signupButton.addClassName("signup-button");
        signupButton.getStyle()
                .set("width", "100%")
                .set("margin-top", "1rem")
                .set("background-color", "#1676f3")
                .set("color", "white")
                .set("font-weight", "bold");

        // Login link section
        HorizontalLayout loginLayout = new HorizontalLayout();
        loginLayout.setWidthFull();
        loginLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        loginLayout.setAlignItems(Alignment.CENTER);
        loginLayout.getStyle().set("margin-top", "1.5rem");
        
        Paragraph loginText = new Paragraph("Already have an account?");
        loginText.getStyle().set("margin", "0 8px 0 0");
        
        RouterLink loginLink = new RouterLink("Log in", LoginView.class);
        loginLink.getStyle().set("color", "#1676f3");
        
        loginLayout.add(loginText, loginLink);

        // Add all components to container
        container.add(
            titleLayout,
            fullName,
            username,
            email,
            password,
            confirmPassword,
            userTypeSelector,
            signupButton,
            loginLayout
        );
        
        // Add container to content wrapper
        contentWrapper.add(container);
        
        // Add content wrapper to main layout
        add(contentWrapper);
    }
    
    private HorizontalLayout createNavBar() {
        HorizontalLayout navBar = new HorizontalLayout();
        navBar.addClassName("nav-bar");
        navBar.setWidthFull();
        navBar.setHeight("60px");
        navBar.setPadding(true);
        navBar.setAlignItems(Alignment.CENTER);
        navBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        // Logo and brand name
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setAlignItems(Alignment.CENTER);
        
        Icon bugIcon = VaadinIcon.BUG.create();
        bugIcon.setColor("#1676f3");
        bugIcon.setSize("24px");
        
        H3 brandName = new H3("BugTracker");
        brandName.getStyle().set("margin", "0");
        brandName.getStyle().set("font-weight", "600");
        brandName.getStyle().set("cursor", "pointer");
        brandName.addClickListener(e -> UI.getCurrent().navigate(""));
        
        logoLayout.add(bugIcon, brandName);
        logoLayout.getStyle().set("cursor", "pointer");
        logoLayout.addClickListener(e -> UI.getCurrent().navigate(""));
        
        // Empty layout for right side (to maintain justifyContentMode)
        HorizontalLayout rightSide = new HorizontalLayout();
        
        navBar.add(logoLayout, rightSide);
        navBar.getStyle()
            .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
            .set("background-color", "white");
        
        return navBar;
    }
}


