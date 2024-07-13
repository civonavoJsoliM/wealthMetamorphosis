package app.wealthmetamorphosis.logic.verifier;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class Validator {
    public UnaryOperator<TextFormatter.Change> validateIfInputNumber() {
        return input -> {
            if (!input.getText().matches("\\d") || input.getRangeEnd() > 4 || doesNumberStartWithZero(input)) {
                input.setText("");
            }
            return input;
        };
    }

    private boolean doesNumberStartWithZero(TextFormatter.Change input) {
        return !input.getText().isEmpty() && input.getControlNewText().startsWith("0");
    }

    public UnaryOperator<TextFormatter.Change> validateInput() {
        return input -> {
            if (input.getText().matches("\\s") || input.getRangeEnd() > 254) {
                input.setText("");
            }
            return input;
        };
    }
}