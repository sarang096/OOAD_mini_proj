package main.java.service;

public class MinLengthValidationStrategy implements ApprovalStrategy {

    @Override
    public boolean validate(String title, String description) {
        return description != null && description.length() >= 10;
    }

    @Override
    public String getRuleName() {
        return "Description must be at least 10 characters";
    }
}