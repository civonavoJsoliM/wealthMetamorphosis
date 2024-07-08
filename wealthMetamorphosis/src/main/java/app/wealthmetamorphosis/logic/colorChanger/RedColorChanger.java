package app.wealthmetamorphosis.logic.colorChanger;

import javafx.scene.control.Label;

public class RedColorChanger implements ColorChanger {
    private final double oldPercentage;
    private final double newPercentage;
    private final Label firstLabel;

    public RedColorChanger(double oldPercentage, double newPercentage, Label label) {
        this.oldPercentage = oldPercentage;
        this.newPercentage = newPercentage;
        this.firstLabel = label;
    }

    @Override
    public void change() {

        if (newPercentage < oldPercentage) {
            firstLabel.setStyle("-fx-text-fill: #FF4D4D");
        }

    }
}
