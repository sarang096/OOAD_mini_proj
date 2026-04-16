package main.java.security;

import main.java.DAO.UserDAO;
import main.java.model.User;

public class AuthService {
    private static AuthService instance;
    private final UserDAO userDAO = new UserDAO();
    private User currentUser;

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    public User login(String email, String password) {
        User user = userDAO.getUserByEmail(email);
        if (user != null && user.getter_password().equals(password)) {
            this.currentUser = user;
            System.out.println("✅ Logged in as: " + user.getter_name() + " [" + user.getter_userrole() + "]");
            return user;
        }
        System.out.println("❌ Invalid credentials.");
        return null;
    }

    public void logout() {
        System.out.println("👋 " + (currentUser != null ? currentUser.getter_name() : "User") + " logged out.");
        this.currentUser = null;
    }

    public User getCurrentUser()         { return currentUser; }
    public boolean isLoggedIn()          { return currentUser != null; }
    public String getCurrentUsername()   { return currentUser != null ? currentUser.getter_name() : "Guest"; }
    public String getCurrentUserRole()   { return currentUser != null ? currentUser.getter_userrole().name() : ""; }
}