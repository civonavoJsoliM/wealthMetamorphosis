package app.wealthmetamorphosis.logic.refresher;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
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
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

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
        /*Map<String, Double> map = UserSingleton.getInstance().getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStockSymbol, Collectors.summingDouble(Order::getStockShares)))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); */
        Map<String, Double> map = ownedStockService.getOwnedStocks(UserSingleton.getInstance());

        double portfolioWorth = 0;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            OptionalDouble avgBuyPrice = UserSingleton.getInstance().getOrders().stream()
                    .filter(or -> or.getStockSymbol().equals(entry.getKey()) && or.getOrderType().equals(OrderType.BUY))
                    .mapToDouble(Order::getStockPrice)
                    .average();

            HttpResponse<String> response;
            try {
                response = service.getRealTimeStockPrice(entry.getKey());
                JSONObject jsonObject = new JSONObject(response.body());
                System.out.println(jsonObject);
                double currentStockPrice = Double.parseDouble(jsonObject.getString("price"));

                double difference = currentStockPrice - avgBuyPrice.getAsDouble();
                double percentage = 100 / (avgBuyPrice.getAsDouble() / difference);
                newPercentages.add(percentage);

                portfolioWorth += currentStockPrice * entry.getValue();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Platform.setImplicitExit(false);
        setLabelText(portfolioWorth, newPercentages);
    }

    private void setLabelText(double portfolioWorth, List<Double> newPercentages) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                PauseTransition transition = new PauseTransition(Duration.seconds(3));
                transition.setOnFinished(event -> {
                    portfolioWorthLabel.setText(Math.round(portfolioWorth) + "$");
                    DecimalFormat decimalFormat = new DecimalFormat("0,00");
                    for (int i = 0; i < profitLossLabels.size(); i++) {
                        if (!profitLossLabels.get(i).getText().isBlank()) {
                            System.out.println(Double.parseDouble(profitLossLabels.get(i).getText().substring(0, profitLossLabels.get(i).getText().length() - 2)));
                            if (Double.parseDouble(profitLossLabels.get(i).getText().substring(0, profitLossLabels.get(i).getText().length() - 2)) > Double.parseDouble(decimalFormat.format(newPercentages.get(i)))) {
                                profitLossLabels.get(i).setStyle("-fx-text-fill: #FF4D4D");
                            } else if (Double.parseDouble(profitLossLabels.get(i).getText().substring(0, profitLossLabels.get(i).getText().length() - 2)) < Double.parseDouble(decimalFormat.format(newPercentages.get(i)))) {
                                profitLossLabels.get(i).setStyle("-fx-text-fill: #AAFF00");
                            } else {
                                profitLossLabels.get(i).setStyle("-fx-text-fill: white");
                            }
                        }
                        profitLossLabels.get(i).setText(decimalFormat.format(newPercentages.get(i)) + " %");
                    }
                    myStocksHBox.toFront();
                    System.out.println("Done");
                    PauseTransition colorTransition = new PauseTransition(Duration.seconds(3));
                    colorTransition.setOnFinished(event1 -> {
                        for (int i = 0; i < profitLossLabels.size(); i++) {
                            profitLossLabels.get(i).setStyle("-fx-text-fill: white");
                        }
                    });
                    colorTransition.playFromStart();
                });
                transition.playFromStart();
                PauseTransition colorTransition = new PauseTransition(Duration.seconds(3));
                colorTransition.setOnFinished(event -> {
                    for (int i = 0; i < profitLossLabels.size(); i++) {
                        profitLossLabels.get(i).setStyle("-fx-text-fill: white");
                    }
                });
                colorTransition.playFromStart();
            }
        });
    }
}