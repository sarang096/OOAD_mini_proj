package main.java.model;

public class AdminObserver implements ChangeObserver {
    private String adminName;

    public AdminObserver(String adminName) {
        this.adminName = adminName;
    }

    @Override
    public void onStatusChanged(ChangeRequest cr) {
        System.out.println(" [Admin Notified: " + adminName + "] " +
            "Request #" + cr.getRequestId() +
            " '" + cr.getTitle() + "'" +
            " → status changed to: " + cr.getStatus());
    }
}