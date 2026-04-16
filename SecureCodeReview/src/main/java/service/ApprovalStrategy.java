package main.java.service;

/**
 * Open/Closed Principle —
 * Open for extension (add new rules)
 * Closed for modification (don't touch existing code)
 */
public interface ApprovalStrategy {
    boolean validate(String title, String description);
    String getRuleName();
}