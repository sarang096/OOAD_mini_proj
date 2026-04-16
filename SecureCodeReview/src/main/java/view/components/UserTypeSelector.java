package main.java.view.components;

import com.vaadin.flow.component.combobox.ComboBox;

/**
 * Reusable role selector dropdown for the signup form.
 */
public class UserTypeSelector extends ComboBox<String> {

    public UserTypeSelector() {
        super("Role");
        setItems("Developer", "Reviewer", "Tester", "ProjectManager", "Administrator");
        setPlaceholder("Select your role");
        setRequired(true);
        setWidthFull();
    }
}
