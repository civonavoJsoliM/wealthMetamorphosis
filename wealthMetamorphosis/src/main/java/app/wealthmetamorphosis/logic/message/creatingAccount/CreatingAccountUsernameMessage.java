package app.wealthmetamorphosis.logic.message.creatingAccount;

import app.wealthmetamorphosis.logic.message.Message;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

public class CreatingAccountUsernameMessage implements Message {
    private final TextField textField;
    private final Label label;
    private final boolean isAvailable;

    public CreatingAccountUsernameMessage(TextField textField, Label label, boolean isAvailable) {
        this.textField = textField;
        this.label = label;
        this.isAvailable = isAvailable;
    }

    @Override
    public void show() {
        Map<String, Boolean> map = getOutput();

        for (Map.Entry<String, Boolean> entry : map.entrySet()) {
            if (entry.getValue()) {
                label.setText(entry.getKey());
                break;
            }
        }
    }

    private Map<String, Boolean> getOutput() {
        Map<String, Boolean> outputs = new LinkedHashMap<>();
        outputs.put("Please enter valid username!", textField.getText().isBlank());
        outputs.put("Username is not available!", !isAvailable);
        outputs.put("", isAvailable);
        return outputs;
    }
}