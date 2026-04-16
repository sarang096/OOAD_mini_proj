package main.java.service;

import main.java.DAO.UserDAO;
import main.java.model.ChangeRequest;
import main.java.model.Review;
import main.java.model.ApprovalRule;
import main.java.model.User;
import java.util.List;
import main.java.model.enums.ChangeStatus;
/**
 * Facade Pattern - provides a simplified interface
 * to the complex subsystem of services and DAOs
 */
public class CodeReviewFacade {

    private static CodeReviewFacade instance;

    private final ChangeRequestService crService;
    private final ReviewService        reviewService;
    private final ApprovalRuleService  ruleService;
    private final UserDAO              userDAO;

    private CodeReviewFacade() {
        this.crService     = new ChangeRequestService();
        this.reviewService = new ReviewService();
        this.ruleService   = new ApprovalRuleService();
        this.userDAO       = new UserDAO();
    }

    // Singleton + Facade combined
    public static CodeReviewFacade getInstance() {
        if (instance == null) instance = new CodeReviewFacade();
        return instance;
    }

    // ── Developer actions ──────────────────────────

    public ChangeRequest submitRequest(String title, 
                                       String description, 
                                       int developerId) {
        System.out.println("[Facade] Developer submitting request...");
        return crService.submitRequest(title, description, developerId);
    }

    public boolean resubmitRequest(int requestId, 
                                    String title, 
                                    String description) {
        System.out.println("[Facade] Developer resubmitting request...");
        return crService.resubmitRequest(requestId, title, description);
    }

    public List<ChangeRequest> getMyRequests(int developerId) {
        System.out.println("[Facade] Fetching developer requests...");
        return crService.getByDeveloper(developerId);
    }

    // ── Reviewer actions ───────────────────────────

    public Review approveRequest(int requestId, 
                                  int reviewerId, 
                                  String comments) {
        System.out.println("[Facade] Reviewer approving request...");
        return reviewService.approveRequest(requestId, reviewerId, comments);
    }

    public Review rejectRequest(int requestId, 
                                 int reviewerId, 
                                 String comments) {
        System.out.println("[Facade] Reviewer rejecting request...");
        return reviewService.rejectRequest(requestId, reviewerId, comments);
    }

    public void addComment(int requestId, 
                            int reviewerId, 
                            String comments) {
        System.out.println("[Facade] Reviewer adding comment...");
        reviewService.addComment(requestId, reviewerId, comments);
    }

    // ── Admin actions ──────────────────────────────

    public void addUser(User user) {
        System.out.println("[Facade] Admin adding user...");
        userDAO.addUser(user);
    }

    public void removeUser(int userId) {
        System.out.println("[Facade] Admin removing user...");
        userDAO.deleteUser(userId);
    }

    public ApprovalRule addRule(String description, int adminId) {
        System.out.println("[Facade] Admin adding rule...");
        return ruleService.addRule(description, adminId);
    }

    public void deleteRule(int ruleId) {
        System.out.println("[Facade] Admin deleting rule...");
        ruleService.deleteRule(ruleId);
    }

    public List<ChangeRequest> getAllRequests() {
        System.out.println("[Facade] Fetching all requests...");
        return crService.getAllRequests();
    }

    public List<User> getAllUsers() {
        System.out.println("[Facade] Fetching all users...");
        return userDAO.getAllUsers();
    }

    public List<ApprovalRule> getAllRules() {
        System.out.println("[Facade] Fetching all rules...");
        return ruleService.getAllRules();
    }

    public void mergeRequest(int requestId) {
        System.out.println("[Facade] Admin merging request...");
        crService.markMerged(requestId);
    }
    public List<ChangeRequest> getPendingRequests() {
         System.out.println("[Facade] Fetching pending requests...");
         var list = crService.getByStatus(ChangeStatus.Submitted);
         list.addAll(crService.getByStatus(ChangeStatus.UnderReview));
         return list;
    }
}