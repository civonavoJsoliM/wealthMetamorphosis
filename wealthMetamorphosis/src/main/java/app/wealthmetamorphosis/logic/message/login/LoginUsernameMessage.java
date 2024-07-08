package app.wealthmetamorphosis.logic.message.login;

import app.wealthmetamorphosis.logic.message.Message;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

public class LoginUsernameMessage implements Message {
    private final TextField textField;
    private final Label label;
    private final boolean isUsernameInDB;

    public LoginUsernameMessage(TextField textField, Label label, boolean isUsernameInDB) {
        this.textField = textField;
        this.label = label;
        this.isUsernameInDB = isUsernameInDB;
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
        outputs.put("Please enter valid username!", textField.getText().isBlank());
        outputs.put("Unknown username!", !isUsernameInDB);
        outputs.put("", isUsernameInDB);
        return outputs;
    }
}
