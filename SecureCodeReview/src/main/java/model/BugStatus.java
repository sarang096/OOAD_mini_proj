package main.java.model;

public enum BugStatus {
    reported,
    in_progress,
    fixed,
    verified,
    closed;

    public static BugStatus fromString(String value) {
        if (value == null) return reported;
        switch (value.toLowerCase()) {
            case "reported":    return reported;
            case "in_progress": return in_progress;
            case "fixed":       return fixed;
            case "verified":    return verified;
            case "closed":      return closed;
            default:            return reported;
        }
    }
}
