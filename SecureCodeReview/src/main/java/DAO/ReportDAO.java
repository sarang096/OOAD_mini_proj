package main.java.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.java.model.Report;

public class ReportDAO {
    // Strategy pattern for different export formats (OCP implementation)
    private ReportExportStrategy exportStrategy;

    public ReportDAO() {
        // Default constructor
    }

    // Constructor with export strategy
    public ReportDAO(ReportExportStrategy exportStrategy) {
        this.exportStrategy = exportStrategy;
    }

    // Setter for export strategy
    public void setExportStrategy(ReportExportStrategy exportStrategy) {
        this.exportStrategy = exportStrategy;
    }

    // Add a new report
    public void addReport(Report report) {
        String sql = "INSERT INTO reports (generated_by, project, bugs_summaries, generated_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, report.getter_generatedBy());
            stmt.setString(2, report.getter_project());
            stmt.setString(3, String.join(", ", report.getter_bugs_summaries())); // Convert List to CSV String
            stmt.setTimestamp(4, Timestamp.valueOf(report.getter_generatedDate()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get a report by ID
    public Report getReportById(int id) {
        String sql = "SELECT * FROM reports WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                List<String> bugsSummaries = Arrays.asList(rs.getString("bugs_summaries").split(", "));

                return new Report(
                        rs.getInt("id"),
                        rs.getString("generated_by"),
                        rs.getString("project"),
                        bugsSummaries,
                        rs.getTimestamp("generated_date").toLocalDateTime());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all reports
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                List<String> bugsSummaries = Arrays.asList(rs.getString("bugs_summaries").split(", "));

                reports.add(new Report(
                        rs.getInt("id"),
                        rs.getString("generated_by"),
                        rs.getString("project"),
                        bugsSummaries,
                        rs.getTimestamp("generated_date").toLocalDateTime()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    // Delete a report by ID
    public boolean deleteReport(int id) {
        String sql = "DELETE FROM reports WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0; // Returns true if a row was deleted
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Export a report (using the strategy pattern - OCP implementation)
    public String exportReport(Report report) {
        if (exportStrategy == null) {
            // Default export as text if no strategy is set
            return report.generateFormattedReport();
        }
        return exportStrategy.export(report);
    }

    // Strategy interface for report export (OCP implementation)
    public interface ReportExportStrategy {
        String export(Report report);
    }

    // Concrete strategies for different export formats (OCP implementation)
    public static class TextExportStrategy implements ReportExportStrategy {
        @Override
        public String export(Report report) {
            return report.generateFormattedReport();
        }
    }

    public static class JSONExportStrategy implements ReportExportStrategy {
        @Override
        public String export(Report report) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"id\": ").append(report.getter_id()).append(",\n");
            json.append("  \"generatedBy\": \"").append(report.getter_generatedBy()).append("\",\n");
            json.append("  \"project\": \"").append(report.getter_project()).append("\",\n");
            json.append("  \"generatedDate\": \"").append(report.getter_generatedDate()).append("\",\n");
            json.append("  \"bugs\": [\n");

            List<String> bugs = report.getter_bugs_summaries();
            for (int i = 0; i < bugs.size(); i++) {
                json.append("    \"").append(bugs.get(i)).append("\"");
                if (i < bugs.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("  ]\n");
            json.append("}");
            return json.toString();
        }
    }

    public static class CSVExportStrategy implements ReportExportStrategy {
        @Override
        public String export(Report report) {
            StringBuilder csv = new StringBuilder();
            csv.append("id,generatedBy,project,generatedDate\n");
            csv.append(report.getter_id()).append(",");
            csv.append("\"").append(report.getter_generatedBy()).append("\",");
            csv.append("\"").append(report.getter_project()).append("\",");
            csv.append("\"").append(report.getter_generatedDate()).append("\"\n\n");

            csv.append("Bug Summaries:\n");
            List<String> bugs = report.getter_bugs_summaries();
            for (int i = 0; i < bugs.size(); i++) {
                csv.append(i + 1).append(",\"").append(bugs.get(i)).append("\"\n");
            }

            return csv.toString();
        }
    }
}