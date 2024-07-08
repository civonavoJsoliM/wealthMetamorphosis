package app.wealthmetamorphosis.logic.refresher;

import app.wealthmetamorphosis.data.parameters.StockPriceRefresherParameters;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.colorChanger.ColorChanger;
import app.wealthmetamorphosis.logic.colorChanger.GreenColorChanger;
import app.wealthmetamorphosis.logic.colorChanger.RedColorChanger;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javafx.util.Duration;
import org.json.JSONObject;

public class StockPriceRefresher implements Runnable {
    private final StockPriceRefresherParameters sprp;
    private Label priceLabel;
    private String stockSymbol;

    public StockPriceRefresher(StockPriceRefresherParameters sprp) {
        this.sprp = sprp;
    }

    @Override
    public void run() {
        try {
            HttpResponse<String> response = sprp.httpService().getRealTimeStockPrice(stockSymbol);
            double newCurrentPrice = getPriceFromJSONObject(response);
            setNewPrice(newCurrentPrice);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private double getPriceFromJSONObject(HttpResponse<String> response) {
        JSONObject jsonObject = new JSONObject(response.body());
        return Double.parseDouble(jsonObject.getString("price"));
    }

    private void setNewPrice(double newCurrentPrice) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!priceLabel.getText().matches("\\s")) {
                    changeCurrentPriceColor(newCurrentPrice, priceLabel);

                }
                priceLabel.setText(newCurrentPrice + "$");
                changeTextFillBack();

                if (!sprp.sellSharesTextField().getText().isBlank()) {
                    setTotalLabelText(newCurrentPrice);

                    setProfitLossValue(newCurrentPrice);
                }
                if (!sprp.buySharesTextField().getText().isBlank()) {
                    setTotalCostLabelText(newCurrentPrice);
                }
            }
        });
    }

    private void setTotalLabelText(double newCurrentPrice) {
        double total = Math.round(newCurrentPrice * Double.parseDouble(sprp.sellSharesTextField().getText()) * 100.0) / 100.0;
        sprp.totalLabel().setText(total + "$");
    }

    private void setTotalCostLabelText(double newCurrentPrice) {
        double totalCost = Math.round(newCurrentPrice * Double.parseDouble(sprp.buySharesTextField().getText()) * 100.0) / 100.0;
        sprp.totalCostLabel().setText(totalCost + "$");
    }

    private void setProfitLossValue(double newPrice) {
        double ownedShares = sprp.ownedStockService().getSharesFromCertainStock(UserSingleton.getInstance(), stockSymbol);
        double totalInvestedInStock = sprp.ownedStockService().getInvestedInStock(UserSingleton.getInstance(), stockSymbol);
        double percentage = sprp.ownedStockService().getPercentage(totalInvestedInStock, newPrice, ownedShares);
        double sharesToSell = Double.parseDouble(sprp.sellSharesTextField().getText());
        double amountToSell = newPrice * sharesToSell;
        double profitLoss = Math.round((amountToSell - (amountToSell / percentage)) * 100.0) / 100.0;

        setProfitLossLabel(profitLoss);
    }


    private void setProfitLossLabel(double value) {
        ColorChanger redColorChanger = new RedColorChanger(0, value, sprp.profitLossLabel());
        ColorChanger greenColorChanger = new GreenColorChanger(0, value, sprp.profitLossLabel());
        List<ColorChanger> colorChangers = new ArrayList<>(List.of(redColorChanger, greenColorChanger));

        if (value != 0) {
            for (ColorChanger colorChanger : colorChangers) {
                colorChanger.change();
                sprp.profitLossLabel().setText(value + "$");
            }
        }
    }

    private void changeTextFillBack() {
        PauseTransition transition = new PauseTransition(Duration.seconds(3));
        transition.setOnFinished(event -> priceLabel.setStyle("-fx-text-fill: white"));
        transition.playFromStart();
    }

    private void changeCurrentPriceColor(double newCurrentPrice, Label label) {
        double oldCurrentPrice = Double.parseDouble(label.getText().substring(0, label.getText().length() - 1));

        ColorChanger greenColorChanger = new GreenColorChanger(oldCurrentPrice, newCurrentPrice, label);
        ColorChanger redColorChanger = new RedColorChanger(oldCurrentPrice, newCurrentPrice, label);
        List<ColorChanger> colorChangers = new ArrayList<>(List.of(greenColorChanger, redColorChanger));

        for (ColorChanger colorChanger : colorChangers) {
            colorChanger.change();
        }
    }

    public void setPriceLabel(Label priceLabel) {
        this.priceLabel = priceLabel;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
}