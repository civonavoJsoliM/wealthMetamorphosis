package app.wealthmetamorphosis.logic.message.creatingAccount;

import app.wealthmetamorphosis.logic.message.Message;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

public class CreatingAccountConfirmedPasswordMessage implements Message {
    private final TextField textField;
    private final Label label;
    private final boolean doPasswordsMatch;

    public CreatingAccountConfirmedPasswordMessage(TextField textField, Label label, boolean doPasswordsMatch) {
        this.textField = textField;
        this.label = label;
        this.doPasswordsMatch = doPasswordsMatch;
    }

    @Override
    public void show() {
        Map<String, Boolean> outputs = getOutput();

        for (Map.Entry<String, Boolean> entry : outputs.entrySet()) {
            if (entry.getValue()) {
                label.setText(entry.getKey());
                break;
            }
        }
    }

    private Map<String, Boolean> getOutput() {
        Map<String, Boolean> outputs = new LinkedHashMap<>();
        outputs.put("Please enter valid password!", textField.getText().isBlank());
        outputs.put("Passwords don't match!", !doPasswordsMatch);
        outputs.put("", doPasswordsMatch);
        return outputs;
    }
}
