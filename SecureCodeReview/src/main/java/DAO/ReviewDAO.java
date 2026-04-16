package main.java.DAO;

import main.java.model.Review;
import main.java.model.enums.ReviewDecision;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public void save(Review review) {
        String sql = "INSERT INTO reviews (comments, decision, requestId, reviewerId) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, review.getComments());
            ps.setString(2, review.getDecision() != null ? review.getDecision().name() : null);
            ps.setInt(3, review.getRequestId());
            ps.setInt(4, review.getReviewerId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) review.setReviewId(keys.getInt(1));
            System.out.println("✅ Review saved.");
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public List<Review> findByRequestId(int requestId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE requestId = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapReview(rs));
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        return list;
    }

    public List<Review> findByReviewer(int reviewerId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE reviewerId = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapReview(rs));
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        return list;
    }

    private Review mapReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("reviewId"));
        r.setComments(rs.getString("comments"));
        String dec = rs.getString("decision");
        if (dec != null) r.setDecision(ReviewDecision.valueOf(dec));
        r.setRequestId(rs.getInt("requestId"));
        r.setReviewerId(rs.getInt("reviewerId"));
        return r;
    }
}