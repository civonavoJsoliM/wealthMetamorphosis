package app.wealthmetamorphosis.logic.service;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class Validator {
    public UnaryOperator<TextFormatter.Change> validateIfInputNumber() {
        return input -> {
            Pattern pattern = Pattern.compile("\\d|\\.");
            if (!pattern.matcher(input.getText()).matches()) {
                input.setText("");
            }
            return input;
        };
    }

    public UnaryOperator<TextFormatter.Change> validateIfInputLetter() {
        return input -> {
            Pattern pattern = Pattern.compile("\\w");
            if (!pattern.matcher(input.getText()).matches()) {
                input.setText("");
            }
            return input;
        };
    }
}
