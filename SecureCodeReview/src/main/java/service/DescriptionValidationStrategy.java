package main.java.service;

public class DescriptionValidationStrategy implements ApprovalStrategy {

    @Override
    public boolean validate(String title, String description) {
        return description != null && !description.isBlank();
    }

    @Override
    public String getRuleName() {
        return "Description must not be empty";
    }
}