package main.java.DAO;

import main.java.model.ChangeRequest;
import main.java.model.enums.ChangeStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChangeRequestDAO implements IChangeRequestDAO {

    public void save(ChangeRequest cr) {
        String sql = "INSERT INTO change_requests (title, description, status, createdDate, developerId) "
                   + "VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cr.getTitle());
            ps.setString(2, cr.getDescription());
            ps.setString(3, cr.getStatus().name());
            ps.setDate(4, Date.valueOf(cr.getCreatedDate()));
            ps.setInt(5, cr.getDeveloperId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) cr.setRequestId(keys.getInt(1));
            System.out.println(" Change Request saved: " + cr.getTitle());
        } catch (SQLException e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    public void updateStatus(int requestId, ChangeStatus status) {
        String sql = "UPDATE change_requests SET status = ? WHERE requestId = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    public ChangeRequest findById(int requestId) {
        String sql = "SELECT * FROM change_requests WHERE requestId = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapCR(rs);
        } catch (SQLException e) {
            System.out.println(" Error: " + e.getMessage());
        }
        return null;
    }

    public List<ChangeRequest> findByDeveloper(int devId) {
        List<ChangeRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM change_requests WHERE developerId = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, devId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapCR(rs));
        } catch (SQLException e) {
            System.out.println(" Error: " + e.getMessage());
        }
        return list;
    }

    public List<ChangeRequest> findByStatus(ChangeStatus status) {
        List<ChangeRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM change_requests WHERE status = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapCR(rs));
        } catch (SQLException e) {
            System.out.println(" Error: " + e.getMessage());
        }
        return list;
    }

    public List<ChangeRequest> getAll() {
        List<ChangeRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM change_requests";
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapCR(rs));
        } catch (SQLException e) {
            System.out.println(" Error: " + e.getMessage());
        }
        return list;
    }

    private ChangeRequest mapCR(ResultSet rs) throws SQLException {
        ChangeRequest cr = new ChangeRequest();
        cr.setRequestId(rs.getInt("requestId"));
        cr.setTitle(rs.getString("title"));
        cr.setDescription(rs.getString("description"));
        cr.setStatus(ChangeStatus.valueOf(rs.getString("status")));
        cr.setCreatedDate(rs.getDate("createdDate").toLocalDate());
        cr.setDeveloperId(rs.getInt("developerId"));
        return cr;
    }
}