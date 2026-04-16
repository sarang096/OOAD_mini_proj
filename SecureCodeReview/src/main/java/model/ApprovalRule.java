package main.java.model;

public class ApprovalRule {
    private int ruleId;
    private String ruleDescription;
    private int adminId;

    public ApprovalRule() {}

    public ApprovalRule(String ruleDescription, int adminId) {
        this.ruleDescription = ruleDescription;
        this.adminId = adminId;
    }

    public boolean validate(ChangeRequest cr) {
        return cr.getTitle() != null && !cr.getTitle().isBlank()
            && cr.getDescription() != null && !cr.getDescription().isBlank();
    }

    public int    getRuleId()          { return ruleId; }
    public String getRuleDescription() { return ruleDescription; }
    public int    getAdminId()         { return adminId; }

    public void setRuleId(int id)            { this.ruleId = id; }
    public void setRuleDescription(String d) { this.ruleDescription = d; }
    public void setAdminId(int id)           { this.adminId = id; }

    @Override
    public String toString() {
        return "[Rule " + ruleId + "] " + ruleDescription;
    }
}