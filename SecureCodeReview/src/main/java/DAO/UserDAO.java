package main.java.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.User;

public class UserDAO {

    public User addUser(User user) {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getter_name());
            stmt.setString(2, user.getter_email());
            stmt.setString(3, user.getter_password());
            stmt.setString(4, user.getter_userrole().toString());
            stmt.executeUpdate();
            System.out.println("[DEBUG] User insertion successful.");
        } catch (SQLException e) {
            System.out.println("[ERROR] Error adding user: " + e.getMessage());
        }
        return user;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("[DEBUG] User found: " + rs.getString("name"));
                return new User(
                    rs.getInt("userId"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    User.UserRole.valueOf(rs.getString("role"))
                );
            } else {
                System.out.println("[DEBUG] No user found with email: " + email);
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Error retrieving user by email: " + e.getMessage());
        }
        return null;
    }

    public User getUserByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("userId"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    User.UserRole.fromString(rs.getString("role"))
                );
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Error retrieving user by name: " + e.getMessage());
        }
        return null;
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE userId = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("userId"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    User.UserRole.fromString(rs.getString("role"))
                );
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Error retrieving user by ID: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User(
                    rs.getInt("userId"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    User.UserRole.fromString(rs.getString("role"))
                );
                users.add(user);
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Error fetching all users: " + e.getMessage());
        }
        return users;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, password = ?, role = ? WHERE userId = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getter_name());
            stmt.setString(2, user.getter_email());
            stmt.setString(3, user.getter_password());
            stmt.setString(4, user.getter_userrole().toString());
            stmt.setInt(5, user.getter_id());
            int updated = stmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] Error updating user: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUserProjects(int userId, List<Integer> projectIds) {
        String deleteSQL = "DELETE FROM Project_Developers WHERE developer_id = ?";
        String insertSQL = "INSERT INTO Project_Developers (project_id, developer_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                    deleteStmt.setInt(1, userId);
                    deleteStmt.executeUpdate();
                }
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    for (Integer projectId : projectIds) {
                        insertStmt.setInt(1, projectId);
                        insertStmt.setInt(2, userId);
                        insertStmt.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE userId = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int deleted = stmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] Error deleting user: " + e.getMessage());
        }
        return false;
    }
}