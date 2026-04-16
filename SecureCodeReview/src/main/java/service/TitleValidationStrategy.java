package main.java.service;

public class TitleValidationStrategy implements ApprovalStrategy {

    @Override
    public boolean validate(String title, String description) {
        return title != null && !title.isBlank();
    }

    @Override
    public String getRuleName() {
        return "Title must not be empty";
    }
}