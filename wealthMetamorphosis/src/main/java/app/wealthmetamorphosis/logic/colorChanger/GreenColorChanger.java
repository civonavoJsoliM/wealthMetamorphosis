package app.wealthmetamorphosis.logic.colorChanger;

import javafx.scene.control.Label;

public class GreenColorChanger implements ColorChanger {
    private final double oldPercentage;
    private final double newPercentage;
    private final Label label;

    public GreenColorChanger(double oldPercentage, double newPercentage, Label label) {
        this.oldPercentage = oldPercentage;
        this.newPercentage = newPercentage;
        this.label = label;
    }

    @Override
    public void change() {

        if (newPercentage > oldPercentage) {
            label.setStyle("-fx-text-fill: #AAFF00");
        }

    }
}