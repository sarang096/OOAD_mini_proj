package main.java.service;

import main.java.DAO.ApprovalRuleDAO;
import main.java.model.ApprovalRule;
import java.util.List;

public class ApprovalRuleService {
    private final ApprovalRuleDAO ruleDAO = new ApprovalRuleDAO();

    public ApprovalRule addRule(String description, int adminId) {
        ApprovalRule rule = new ApprovalRule(description, adminId);
        ruleDAO.save(rule);
        return rule;
    }

    public void deleteRule(int ruleId) {
        ruleDAO.delete(ruleId);
    }

    public List<ApprovalRule> getAllRules() {
        return ruleDAO.getAll();
    }
}