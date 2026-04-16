package main.java.view.dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.VaadinSession;
import main.java.model.ChangeRequest;
import main.java.model.Review;
import main.java.model.enums.ChangeStatus;
import main.java.security.AuthService;
import main.java.service.ChangeRequestService;
import main.java.service.ReviewService;

import java.util.List;

public class ReviewerPanel extends VerticalLayout {

    private final AuthService authService;
    private final ReviewService reviewService;
    private final ChangeRequestService crService;
    private final Grid<ChangeRequest> pendingGrid;
    private final Grid<Review> myReviewsGrid;

    public ReviewerPanel(AuthService authService) {
        this.authService = authService;
        this.reviewService = new ReviewService();
        this.crService = new ChangeRequestService();
        this.pendingGrid = new Grid<>(ChangeRequest.class, false);
        this.myReviewsGrid = new Grid<>(Review.class, false);

        addClassName("reviewer-panel");
        setSizeFull();

        H2 welcomeTitle = new H2("Reviewer Dashboard");
        welcomeTitle.getStyle().set("margin-top", "0");

        Tab pendingTab   = new Tab(VaadinIcon.CLOCK.create(),    new H3("Pending Requests"));
        Tab myReviewsTab = new Tab(VaadinIcon.CHECK.create(),    new H3("My Reviews"));
        Tab statsTab     = new Tab(VaadinIcon.CHART.create(),    new H3("Stats"));

        Tabs tabs = new Tabs(pendingTab, myReviewsTab, statsTab);
        tabs.setWidthFull();

        VerticalLayout pendingContent   = createPendingContent();
        VerticalLayout myReviewsContent = createMyReviewsContent();
        VerticalLayout statsContent     = createStatsContent();

        add(welcomeTitle, tabs, pendingContent);

        tabs.addSelectedChangeListener(event -> {
            remove(getComponentAt(2));
            if (event.getSelectedTab().equals(pendingTab)) {
                add(pendingContent);
            } else if (event.getSelectedTab().equals(myReviewsTab)) {
                add(myReviewsContent);
            } else if (event.getSelectedTab().equals(statsTab)) {
                add(statsContent);
            }
        });
    }

    private VerticalLayout createPendingContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        Integer reviewerId = getCurrentUserId();
        if (reviewerId == null) {
            content.add(new H3("You need to be logged in to view requests"));
            return content;
        }

        // Summary cards
        List<ChangeRequest> submitted   = crService.getByStatus(ChangeStatus.Submitted);
        List<ChangeRequest> underReview = crService.getByStatus(ChangeStatus.UnderReview);

        HorizontalLayout summaryLayout = new HorizontalLayout();
        summaryLayout.setWidthFull();

        VerticalLayout submittedCard   = createSummaryCard("Submitted",
                String.valueOf(submitted.size()),
                VaadinIcon.INBOX.create(), "blue");
        VerticalLayout underReviewCard = createSummaryCard("Under Review",
                String.valueOf(underReview.size()),
                VaadinIcon.HOURGLASS.create(), "orange");
        VerticalLayout approvedCard    = createSummaryCard("Approved",
                String.valueOf(crService.getByStatus(ChangeStatus.Approved).size()),
                VaadinIcon.CHECK.create(), "green");

        summaryLayout.add(submittedCard, underReviewCard, approvedCard);
        summaryLayout.setFlexGrow(1, submittedCard, underReviewCard, approvedCard);

        // Grid
        pendingGrid.addColumn(ChangeRequest::getRequestId).setHeader("ID").setAutoWidth(true);
        pendingGrid.addColumn(ChangeRequest::getTitle).setHeader("Title").setFlexGrow(1);
        pendingGrid.addColumn(ChangeRequest::getDescription).setHeader("Description").setFlexGrow(2);
        pendingGrid.addColumn(cr -> cr.getStatus().name()).setHeader("Status").setAutoWidth(true);
        pendingGrid.addColumn(cr -> cr.getCreatedDate().toString()).setHeader("Date").setAutoWidth(true);
        pendingGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        List<ChangeRequest> allPending = submitted;
        allPending.addAll(underReview);
        pendingGrid.setItems(allPending);
        pendingGrid.setSizeFull();

        // Action column
        pendingGrid.addComponentColumn(cr -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button approveBtn = new Button(VaadinIcon.CHECK.create());
            approveBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            approveBtn.getElement().setAttribute("aria-label", "Approve");
            approveBtn.addClickListener(e -> openReviewDialog(cr, reviewerId, true));

            Button rejectBtn = new Button(VaadinIcon.CLOSE.create());
            rejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            rejectBtn.getElement().setAttribute("aria-label", "Reject");
            rejectBtn.addClickListener(e -> openReviewDialog(cr, reviewerId, false));

            Button commentBtn = new Button(VaadinIcon.COMMENT.create());
            commentBtn.addClickListener(e -> openCommentDialog(cr, reviewerId));

            actions.add(approveBtn, rejectBtn, commentBtn);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        content.add(summaryLayout, pendingGrid);
        return content;
    }

    private VerticalLayout createMyReviewsContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        Integer reviewerId = getCurrentUserId();
        if (reviewerId == null) {
            content.add(new H3("You need to be logged in."));
            return content;
        }

        myReviewsGrid.addColumn(Review::getReviewId).setHeader("Review ID").setAutoWidth(true);
        myReviewsGrid.addColumn(Review::getRequestId).setHeader("Request ID").setAutoWidth(true);
        myReviewsGrid.addColumn(Review::getComments).setHeader("Comments").setFlexGrow(1);
        myReviewsGrid.addColumn(r -> r.getDecision() != null ? r.getDecision().name() : "Comment only")
                .setHeader("Decision").setAutoWidth(true);
        myReviewsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        List<Review> myReviews = reviewService.getReviewsByReviewer(reviewerId);
        myReviewsGrid.setItems(myReviews);
        myReviewsGrid.setSizeFull();

        content.add(new H3("My Reviews"), myReviewsGrid);
        return content;
    }

    private VerticalLayout createStatsContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        long totalSubmitted   = crService.getByStatus(ChangeStatus.Submitted).size();
        long totalApproved    = crService.getByStatus(ChangeStatus.Approved).size();
        long totalRejected    = crService.getByStatus(ChangeStatus.Rejected).size();
        long totalMerged      = crService.getByStatus(ChangeStatus.Merged).size();

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();

        statsLayout.add(
            createSummaryCard("Submitted",   String.valueOf(totalSubmitted),
                    VaadinIcon.INBOX.create(), "blue"),
            createSummaryCard("Approved",    String.valueOf(totalApproved),
                    VaadinIcon.CHECK.create(), "green"),
            createSummaryCard("Rejected",    String.valueOf(totalRejected),
                    VaadinIcon.CLOSE.create(), "red"),
            createSummaryCard("Merged", String.valueOf(totalMerged),
                    VaadinIcon.ARROW_FORWARD.create(), "purple")
        );

        content.add(new H3("Overall Statistics"), statsLayout);
        return content;
    }

    private void openReviewDialog(ChangeRequest cr, int reviewerId, boolean isApprove) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isApprove ? "Approve Request #" + cr.getRequestId()
                                        : "Reject Request #" + cr.getRequestId());

        TextArea commentField = new TextArea("Comments");
        commentField.setWidthFull();
        commentField.setMinHeight("100px");

        Button confirm = new Button("Confirm", e -> {
            String comments = commentField.getValue();
            if (isApprove) reviewService.approveRequest(cr.getRequestId(), reviewerId, comments);
            else           reviewService.rejectRequest(cr.getRequestId(), reviewerId, comments);
            Notification.show(isApprove ? "✅ Approved!" : "✅ Rejected!", 3000, Position.MIDDLE);
            refreshPendingGrid();
            dialog.close();
        });
        confirm.addThemeVariants(isApprove ? ButtonVariant.LUMO_SUCCESS : ButtonVariant.LUMO_ERROR);

        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(new VerticalLayout(commentField, new HorizontalLayout(confirm, cancel)));
        dialog.open();
    }

    private void openCommentDialog(ChangeRequest cr, int reviewerId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Comment to Request #" + cr.getRequestId());

        TextArea commentField = new TextArea("Comment");
        commentField.setWidthFull();
        commentField.setMinHeight("100px");

        Button save = new Button("Save", e -> {
            reviewService.addComment(cr.getRequestId(), reviewerId, commentField.getValue());
            Notification.show("✅ Comment added!", 3000, Position.MIDDLE);
            refreshPendingGrid();
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(new VerticalLayout(commentField, new HorizontalLayout(save, cancel)));
        dialog.open();
    }

    private void refreshPendingGrid() {
        List<ChangeRequest> list = crService.getByStatus(ChangeStatus.Submitted);
        list.addAll(crService.getByStatus(ChangeStatus.UnderReview));
        pendingGrid.setItems(list);
    }

    private Integer getCurrentUserId() {
        Object userIdObj = VaadinSession.getCurrent().getAttribute("userId");
        if (userIdObj != null) {
            try { return Integer.parseInt(userIdObj.toString()); }
            catch (NumberFormatException e) { return null; }
        }
        // fallback to AuthService
        if (authService.getCurrentUser() != null) {
            return authService.getCurrentUser().getter_id();
        }
        return null;
    }

    private VerticalLayout createSummaryCard(String title, String count,
            com.vaadin.flow.component.icon.Icon icon, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(Alignment.CENTER);
        card.getStyle()
            .set("background-color", "white")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
            .set("padding", "20px")
            .set("margin", "10px");

        icon.setSize("40px");
        icon.setColor(color);

        H3 countLabel = new H3(count);
        countLabel.getStyle().set("margin", "10px 0").set("font-size", "2em");

        Span titleLabel = new Span(title);
        titleLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");

        card.add(icon, countLabel, titleLabel);
        return card;
    }
}