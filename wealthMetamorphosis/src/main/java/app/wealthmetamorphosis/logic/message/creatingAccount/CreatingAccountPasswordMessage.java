package app.wealthmetamorphosis.logic.message.creatingAccount;

import app.wealthmetamorphosis.logic.message.Message;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CreatingAccountPasswordMessage implements Message {
    private final TextField textField;
    private final Label label;

    public CreatingAccountPasswordMessage(TextField textField, Label label) {
        this.textField = textField;
        this.label = label;
    }

    @Override
    public void show() {

        if (textField.getText().isBlank()) {
            label.setText("Please enter valid password!");
        } else {
            label.setText("");
        }

    }
}
