package main.java.view.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import main.java.security.AuthService;
import main.java.view.auth.LoginView;
import main.java.view.auth.SignupView;

public class NavBar extends HorizontalLayout {
    
    private final AuthService authService;
    
    public NavBar() {
        this(null, false);
    }
    
    public NavBar(AuthService authService, boolean isLoggedIn) {
        this(authService, isLoggedIn, true);
    }
    
    public NavBar(AuthService authService, boolean isLoggedIn, boolean showAuthButtons) {
        this.authService = authService;
        
        addClassName("nav-bar");
        setWidthFull();
        setHeight("60px");
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
        
        // Logo and brand name on the left
        HorizontalLayout logoLayout = new HorizontalLayout();
        Icon bugIcon = VaadinIcon.BUG.create();
        bugIcon.setColor("#1676f3");
        bugIcon.setSize("24px");
        
        H3 brandName = new H3("BugTracker");
        brandName.getStyle().set("margin", "0");
        brandName.getStyle().set("font-weight", "600");
        brandName.getStyle().set("cursor", "pointer");
        brandName.addClickListener(e -> UI.getCurrent().navigate(""));
        
        logoLayout.add(bugIcon, brandName);
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.getStyle().set("cursor", "pointer");
        logoLayout.addClickListener(e -> UI.getCurrent().navigate(""));
        
        // Right side buttons
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        
        if (showAuthButtons) {
            if (isLoggedIn && authService != null) {
                // User info and logout button for logged-in users
                String username = authService.getCurrentUsername();
                String role = authService.getCurrentUserRole();
                
                Button userButton = new Button(username + " (" + role + ")");
                userButton.addThemeVariants();
                userButton.setIcon(VaadinIcon.USER.create());
                
                Button logoutButton = new Button("Logout", e -> {
                    authService.logout();
                    UI.getCurrent().navigate("");
                    UI.getCurrent().getPage().reload();
                });
                logoutButton.setIcon(VaadinIcon.SIGN_OUT.create());
                
                buttonsLayout.add(userButton, logoutButton);
            } else {
                // Login and signup buttons for guests
                Button loginButton = new Button("Login", e -> UI.getCurrent().navigate("login"));
                loginButton.addClassName("login-button");
                loginButton.getStyle()
                    .set("background-color", "#1676f3")
                    .set("color", "white")
                    .set("font-weight", "500")
                    .set("border-radius", "4px");
                
                Button signupButton = new Button("Sign Up", e -> UI.getCurrent().navigate("signup"));
                signupButton.addClassName("signup-button");
                signupButton.getStyle()
                    .set("border", "1px solid #1676f3")
                    .set("color", "#1676f3")
                    .set("font-weight", "500")
                    .set("border-radius", "4px");
                
                buttonsLayout.add(loginButton, signupButton);
            }
        }
        
        // Add components to the layout with space between
        add(logoLayout);
        add(buttonsLayout);
        
        // Push the buttons to the right
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        // Add some styling
        getStyle().set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");
        getStyle().set("background-color", "white");
    }
}




