package main.java.service;

import main.java.DAO.ReportDAO;
import main.java.model.Report;

import java.util.List;

/**
 * Service layer for Report operations.
 * Delegates to ReportDAO for persistence.
 */
public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public boolean addReport(Report report) {
        try {
            reportDAO.addReport(report);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Report getReportById(int id) {
        return reportDAO.getReportById(id);
    }

    public List<Report> getAllReports() {
        return reportDAO.getAllReports();
    }

    public boolean deleteReport(int id) {
        return reportDAO.deleteReport(id);
    }
}
