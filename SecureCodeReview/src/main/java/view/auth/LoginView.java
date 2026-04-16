package main.java.view.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import main.java.security.AuthService;
import main.java.model.User;
@Route("login")
@PageTitle("Login | BugTracker")
public class LoginView extends VerticalLayout {

    public LoginView() {
        // Main layout setup
        addClassName("login-view");
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
        container.addClassName("login-container");
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
        
        H2 title = new H2("BugTracker Login");
        title.getStyle().set("margin", "0");
        
        titleLayout.add(bugIcon, title);
        titleLayout.getStyle().set("margin-bottom", "1.5rem");

        // Form fields
        TextField email = new TextField("Email");
        email.setRequired(true);
        email.setPlaceholder("Enter your email");
        email.setWidthFull();
        email.getStyle().set("margin-bottom", "1rem");

        PasswordField password = new PasswordField("Password");
        password.setRequired(true);
        password.setPlaceholder("Enter your password");
        password.setWidthFull();
        password.getStyle().set("margin-bottom", "1.5rem");

        Button loginButton = new Button("Login", event -> {
            if (email.isEmpty() || password.isEmpty()) {
                Notification notification = Notification.show("Please fill in all required fields");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
            	AuthService authService = AuthService.getInstance();
            	User loggedInUser = authService.login(email.getValue(), password.getValue());
            	System.out.println("Authenticated: " + (loggedInUser != null));
            	if (loggedInUser != null) {
            	    Notification.show("Login successful");
            	    UI.getCurrent().navigate("dashboard");
            	} else {
            	    Notification notification = Notification.show("Invalid email or password");
            	    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            	}
            }
        });

        loginButton.addClassName("login-button");
        loginButton.getStyle()
                .set("width", "100%")
                .set("margin-top", "1rem")
                .set("background-color", "#1676f3")
                .set("color", "white")
                .set("font-weight", "bold");

        // Signup link section
        HorizontalLayout signupLayout = new HorizontalLayout();
        signupLayout.setWidthFull();
        signupLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        signupLayout.setAlignItems(Alignment.CENTER);
        signupLayout.getStyle().set("margin-top", "1.5rem");
        
        Paragraph signupText = new Paragraph("Don't have an account?");
        signupText.getStyle().set("margin", "0 8px 0 0");
        
        RouterLink signupLink = new RouterLink("Sign up", SignupView.class);
        signupLink.getStyle().set("color", "#1676f3");
        
        signupLayout.add(signupText, signupLink);

        // Add all components to container
        container.add(titleLayout, email, password, loginButton, signupLayout);
        
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




