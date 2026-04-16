package main.java.view.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import main.java.security.AuthService;
import main.java.view.components.NavBar;

@Route("dashboard")
@PageTitle("Dashboard | BugTracker")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthService authService;

    public DashboardView() {
        this.authService = AuthService.getInstance();

        addClassName("dashboard-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authService.isLoggedIn()) {
            event.forwardTo("login");
            return;
        }

        String userRole = authService.getCurrentUserRole();
        NavBar navBar = new NavBar(authService, true);

        Component panel;
        System.out.println("Role: " + userRole);

        switch (userRole) {
            case "Administrator":
                panel = new AdminPanel(authService);
                break;
            case "Developer":
                panel = new DeveloperPanel(authService);
                break;
            case "Reviewer":
                panel = new ReviewerPanel(authService);
                break;
            case "Tester":
                panel = new TesterPanel(authService);
                break;
            case "ProjectManager":
                panel = new ManagerPanel(authService);
                break;
            default:
                UI.getCurrent().navigate("login");
                return;
        }

        add(navBar, panel);
    }
}