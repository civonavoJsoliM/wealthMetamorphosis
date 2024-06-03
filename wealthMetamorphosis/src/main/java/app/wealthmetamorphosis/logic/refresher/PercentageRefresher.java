package app.wealthmetamorphosis.logic.refresher;

import javafx.scene.control.Label;

import java.text.DecimalFormat;

public class PercentageRefresher implements Runnable {
    private final double buyPrice;
    private final double currentPrice;
    private final Label percantageLabel;

    public PercentageRefresher(double buyPrice, double currentPrice, Label percantageLabel) {
        this.buyPrice = buyPrice;
        this.currentPrice = currentPrice;
        this.percantageLabel = percantageLabel;
    }

    @Override
    public void run() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        double difference = currentPrice - buyPrice;
        double percentage = 100 / (buyPrice / difference);
        percantageLabel.setText(decimalFormat.format(percentage) + " %");
        System.out.println("Refresh label");
    }
}
