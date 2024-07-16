package app.wealthmetamorphosis.logic.refresher;

import app.wealthmetamorphosis.data.User;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.colorChanger.ColorChanger;
import app.wealthmetamorphosis.logic.colorChanger.GreenColorChanger;
import app.wealthmetamorphosis.logic.colorChanger.RedColorChanger;
import app.wealthmetamorphosis.logic.service.HttpService;
import app.wealthmetamorphosis.logic.service.OwnedStockService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.json.JSONObject;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

public class PercentageRefresher implements Runnable {
    private final List<Label> profitLossLabels;
    private final HttpService service;
    private final Label portfolioWorthLabel;
    private final HBox myStocksHBox;
    private final OwnedStockService ownedStockService;

    public PercentageRefresher(List<Label> profitLossLabels, HttpService service, Label portfolioWorthLabel, HBox myStocksHBox, OwnedStockService ownedStockService) {
        this.profitLossLabels = profitLossLabels;
        this.service = service;
        this.portfolioWorthLabel = portfolioWorthLabel;
        this.myStocksHBox = myStocksHBox;
        this.ownedStockService = ownedStockService;
    }

    @Override
    public void run() {
        List<Double> newPercentages = new ArrayList<>();
        Map<String, Double> map = ownedStockService.getAllOwnedStockShares(UserSingleton.getInstance());
        double portfolioWorth = 0;

        for (Map.Entry<String, Double> entry : map.entrySet()) {

            try {
                double currentStockPrice = getCurrentStockPrice(entry);
                User user = UserSingleton.getInstance();
                String stockSymbol = entry.getKey();

                double percentage = getPercentage(user, stockSymbol, currentStockPrice);
                newPercentages.add(percentage);

                portfolioWorth += currentStockPrice * entry.getValue();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Platform.setImplicitExit(false);
        setLabelText(portfolioWorth, newPercentages);
    }

    private double getPercentage(User user, String stockSymbol, double currentStockPrice) {
        double investedInStock = ownedStockService.getInvestedInStock(user, stockSymbol);
        double ownedShares = ownedStockService.getSharesFromCertainStock(user, stockSymbol);
        double percentage = ((100 / investedInStock) * (currentStockPrice * ownedShares)) - 100;
        percentage = Math.round(percentage * 100.0) / 100.0;
        return percentage;
    }

    private double getCurrentStockPrice(Map.Entry<String, Double> entry) throws IOException, InterruptedException {
        HttpResponse<String> response = service.getRealTimeStockPrice(entry.getKey());
        while (response.statusCode() != 200) {
            response = service.getRealTimeStockPrice(entry.getKey());
        }
        JSONObject jsonObject = new JSONObject(response.body());
        return Double.parseDouble(jsonObject.getString("price"));
    }

    private void setLabelText(double portfolioWorth, List<Double> newPercentages) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                PauseTransition transition = new PauseTransition(Duration.seconds(3));
                transition.setOnFinished(event -> {

                    double worth = Math.round(portfolioWorth * 100.0) / 100.0;
                    portfolioWorthLabel.setText(worth + "$");

                    for (int i = 0; i < profitLossLabels.size(); i++) {
                        if (!profitLossLabels.get(i).getText().isBlank()) {
                            changePercentageColor(i, newPercentages.get(i));
                        }
                        profitLossLabels.get(i).setText(newPercentages.get(i) + "%");
                    }
                    myStocksHBox.toFront();
                    changeTextFillBack();

                });
                transition.playFromStart();
            }
        });
    }

    private void changeTextFillBack() {
        PauseTransition colorTransition = new PauseTransition(Duration.seconds(3));
        colorTransition.setOnFinished(evt -> {
            for (Label profitLossLabel : profitLossLabels) {
                profitLossLabel.setStyle("-fx-text-fill: white");
            }
        });
        colorTransition.playFromStart();
    }

    private void changePercentageColor(int i, Double newPercentages) {
        String oldPercentageString = profitLossLabels.get(i).getText().substring(0, profitLossLabels.get(i).getText().length() - 1).replace(",", ".");
        double oldPercentage = Double.parseDouble(oldPercentageString);
        double newPercentage = newPercentages;
        Label profitLossLabel = profitLossLabels.get(i);

        ColorChanger greenColorChanger = new GreenColorChanger(oldPercentage, newPercentage, profitLossLabel);
        ColorChanger redColorChanger = new RedColorChanger(oldPercentage, newPercentage, profitLossLabel);
        List<ColorChanger> colorChangers = new ArrayList<>(List.of(greenColorChanger, redColorChanger));

        if (oldPercentage != newPercentage) {
            for (ColorChanger colorChanger : colorChangers) {
                colorChanger.change();
            }
        }
    }
}