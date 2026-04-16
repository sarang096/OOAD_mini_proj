package main.java.model;

import main.java.model.enums.ReviewDecision;

public class Review {
    private int reviewId;
    private String comments;
    private ReviewDecision decision;
    private int requestId;
    private int reviewerId;

    public Review() {}

    public Review(String comments, ReviewDecision decision, int requestId, int reviewerId) {
        this.comments   = comments;
        this.decision   = decision;
        this.requestId  = requestId;
        this.reviewerId = reviewerId;
    }

    public int            getReviewId()   { return reviewId; }
    public String         getComments()   { return comments; }
    public ReviewDecision getDecision()   { return decision; }
    public int            getRequestId()  { return requestId; }
    public int            getReviewerId() { return reviewerId; }

    public void setReviewId(int id)           { this.reviewId = id; }
    public void setComments(String c)         { this.comments = c; }
    public void setDecision(ReviewDecision d) { this.decision = d; }
    public void setRequestId(int id)          { this.requestId = id; }
    public void setReviewerId(int id)         { this.reviewerId = id; }

    @Override
    public String toString() {
        return "[Review " + reviewId + "] " + decision + " | " + comments;
    }
}