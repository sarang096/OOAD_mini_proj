package main.java.service;

import main.java.DAO.ChangeRequestDAO;
import main.java.DAO.ApprovalRuleDAO;
import main.java.model.ApprovalRule;
import main.java.model.ChangeRequest;
import main.java.model.enums.ChangeStatus;
import java.util.ArrayList;
import java.util.List;

public class ChangeRequestService {

    private final ChangeRequestDAO crDAO   = new ChangeRequestDAO();
    private final ApprovalRuleDAO ruleDAO  = new ApprovalRuleDAO();

    // Open/Closed — add new strategies without changing this method!
    private final List<ApprovalStrategy> strategies = new ArrayList<>() {{
        add(new TitleValidationStrategy());
        add(new DescriptionValidationStrategy());
        add(new MinLengthValidationStrategy());
    }};

    public ChangeRequest submitRequest(String title, 
                                       String description, 
                                       int developerId) {
        // Run all strategies
        for (ApprovalStrategy strategy : strategies) {
            if (!strategy.validate(title, description)) {
                System.out.println("❌ Rule failed: " + strategy.getRuleName());
                return null;
            }
        }

        // Check DB rules too
        for (ApprovalRule rule : ruleDAO.getAll()) {
            if (!rule.validate(new ChangeRequest(title, description, developerId))) {
                System.out.println("❌ Rule violated: " + rule.getRuleDescription());
                return null;
            }
        }

        ChangeRequest cr = new ChangeRequest(title, description, developerId);
        crDAO.save(cr);
        return cr;
    }

    public boolean resubmitRequest(int requestId, String newTitle, String newDesc) {
        ChangeRequest cr = crDAO.findById(requestId);
        if (cr == null || cr.getStatus() != ChangeStatus.Rejected) return false;
        cr.setTitle(newTitle);
        cr.setDescription(newDesc);
        crDAO.updateStatus(requestId, ChangeStatus.Submitted);
        return true;
    }

    public void markMerged(int requestId) {
        crDAO.updateStatus(requestId, ChangeStatus.Merged);
    }

    public ChangeRequest          getById(int id)                { return crDAO.findById(id); }
    public List<ChangeRequest>    getByDeveloper(int devId)      { return crDAO.findByDeveloper(devId); }
    public List<ChangeRequest>    getByStatus(ChangeStatus s)    { return crDAO.findByStatus(s); }
    public List<ChangeRequest>    getAllRequests()                { return crDAO.getAll(); }
}