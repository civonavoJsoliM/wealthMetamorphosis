package app.wealthmetamorphosis.logic.refresher;

import app.wealthmetamorphosis.logic.colorChanger.ColorChanger;
import app.wealthmetamorphosis.logic.colorChanger.GreenColorChanger;
import app.wealthmetamorphosis.logic.colorChanger.RedColorChanger;
import app.wealthmetamorphosis.logic.service.HttpService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javafx.util.Duration;
import org.json.JSONObject;

public class RealTimeStockPriceRefresher implements Runnable {
    private final HttpService httpService;
    private String stockSymbol;
    private Label label;

    public RealTimeStockPriceRefresher(HttpService httpService) {
        this.httpService = httpService;
    }

    @Override
    public void run() {
        try {
            HttpResponse<String> response = httpService.getRealTimeStockPrice(stockSymbol);
            double newCurrentPrice = getPriceFromJSONObject(response);
            //if (label != null) {
                setNewPrice(newCurrentPrice);
            //}
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
                if (!label.getText().isBlank()) {
                    changeCurrentPriceColor(newCurrentPrice, label);
                }
                label.setText((newCurrentPrice) + "$");
                changeTextFillBack();
            }
        });
    }

    private void changeTextFillBack() {
        PauseTransition transition = new PauseTransition(Duration.seconds(3));
        transition.setOnFinished(event -> label.setStyle("-fx-text-fill: white"));
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

    public void setLabel(Label label) {
        this.label = label;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
}