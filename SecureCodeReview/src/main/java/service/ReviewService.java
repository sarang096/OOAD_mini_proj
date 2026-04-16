package main.java.service;

import main.java.DAO.ReviewDAO;
import main.java.DAO.ChangeRequestDAO;
import main.java.model.AdminObserver;
import main.java.model.ChangeRequest;
import main.java.model.Review;
import main.java.model.enums.ChangeStatus;
import main.java.model.enums.ReviewDecision;
import java.util.List;

public class ReviewService {

    private final ReviewDAO        reviewDAO = new ReviewDAO();
    private final ChangeRequestDAO crDAO     = new ChangeRequestDAO();

    public Review approveRequest(int requestId, int reviewerId, String comments) {
        Review review = new Review(comments, ReviewDecision.Approved, requestId, reviewerId);
        reviewDAO.save(review);

        // Observer Pattern — notify admin when approved
        ChangeRequest cr = crDAO.findById(requestId);
        if (cr != null) {
            cr.addObserver(new AdminObserver("Admin"));
            cr.updateStatus(ChangeStatus.Approved); // triggers notification
        }
        crDAO.updateStatus(requestId, ChangeStatus.Approved);

        System.out.println("✅ Request " + requestId + " APPROVED.");
        return review;
    }

    public Review rejectRequest(int requestId, int reviewerId, String comments) {
        Review review = new Review(comments, ReviewDecision.Rejected, requestId, reviewerId);
        reviewDAO.save(review);

        // Observer Pattern — notify admin when rejected
        ChangeRequest cr = crDAO.findById(requestId);
        if (cr != null) {
            cr.addObserver(new AdminObserver("Admin"));
            cr.updateStatus(ChangeStatus.Rejected); // triggers notification
        }
        crDAO.updateStatus(requestId, ChangeStatus.Rejected);

        System.out.println("✅ Request " + requestId + " REJECTED.");
        return review;
    }

    public void addComment(int requestId, int reviewerId, String comments) {
        Review review = new Review(comments, null, requestId, reviewerId);
        reviewDAO.save(review);

        // Observer Pattern — notify admin when comment added
        ChangeRequest cr = crDAO.findById(requestId);
        if (cr != null) {
            cr.addObserver(new AdminObserver("Admin"));
            cr.updateStatus(ChangeStatus.UnderReview); // triggers notification
        }
        crDAO.updateStatus(requestId, ChangeStatus.UnderReview);

        System.out.println("Comment added to request " + requestId);
    }

    public List<Review> getReviewsForRequest(int requestId)  { 
        return reviewDAO.findByRequestId(requestId); 
    }

    public List<Review> getReviewsByReviewer(int reviewerId) { 
        return reviewDAO.findByReviewer(reviewerId); 
    }
}