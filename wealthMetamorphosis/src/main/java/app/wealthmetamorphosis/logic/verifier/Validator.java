package app.wealthmetamorphosis.logic.service;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class Validator {
    public UnaryOperator<TextFormatter.Change> validateIfInputNumber() {
        return input -> {
            if (!input.getText().matches("\\d") || doesNumberStartWithZero(input)) {
                input.setText("");
            }
            return input;
        };
    }

    private boolean doesNumberStartWithZero(TextFormatter.Change input) {
        return !input.getText().isEmpty() && input.getControlNewText().startsWith("0");
    }
}