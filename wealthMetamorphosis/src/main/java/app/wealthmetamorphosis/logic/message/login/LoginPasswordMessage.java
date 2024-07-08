package app.wealthmetamorphosis.logic.message.login;

import app.wealthmetamorphosis.logic.message.Message;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

public class LoginPasswordMessage implements Message {
    private final TextField textField;
    private final Label label;
    private final boolean isPasswordCorrect;

    public LoginPasswordMessage(TextField textField, Label label, boolean isPasswordCorrect) {
        this.textField = textField;
        this.label = label;
        this.isPasswordCorrect = isPasswordCorrect;
    }

    @Override
    public void show() {
        Map<String, Boolean> outputs = getOutputs();

        for (Map.Entry<String, Boolean> entry : outputs.entrySet()) {
            if (entry.getValue()) {
                label.setText(entry.getKey());
                break;
            }
        }
    }

    private Map<String, Boolean> getOutputs() {
        Map<String, Boolean> outputs = new LinkedHashMap<>();
        outputs.put("Please enter valid password", textField.getText().isBlank());
        outputs.put("Incorrect password!", !isPasswordCorrect);
        outputs.put("", isPasswordCorrect);
        return outputs;
    }
}
