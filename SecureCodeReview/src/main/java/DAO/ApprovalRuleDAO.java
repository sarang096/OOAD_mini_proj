package main.java.DAO;

import main.java.model.ApprovalRule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApprovalRuleDAO {

    public void save(ApprovalRule rule) {
        String sql = "INSERT INTO approval_rules (ruleDescription, adminId) VALUES (?,?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rule.getRuleDescription());
            ps.setInt(2, rule.getAdminId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) rule.setRuleId(keys.getInt(1));
            System.out.println("✅ Rule saved.");
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void delete(int ruleId) {
        String sql = "DELETE FROM approval_rules WHERE ruleId = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ruleId);
            ps.executeUpdate();
            System.out.println("✅ Rule deleted.");
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public List<ApprovalRule> getAll() {
        List<ApprovalRule> list = new ArrayList<>();
        String sql = "SELECT * FROM approval_rules";
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ApprovalRule ar = new ApprovalRule();
                ar.setRuleId(rs.getInt("ruleId"));
                ar.setRuleDescription(rs.getString("ruleDescription"));
                ar.setAdminId(rs.getInt("adminId"));
                list.add(ar);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        return list;
    }
}