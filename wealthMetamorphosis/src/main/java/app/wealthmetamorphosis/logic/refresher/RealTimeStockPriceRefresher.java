package app.wealthmetamorphosis.logic.refresher;

import app.wealthmetamorphosis.logic.service.HttpService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.http.HttpResponse;

import javafx.util.Duration;
import org.json.JSONObject;

public class RealTimeStockPriceRefresher implements Runnable {
    private final HttpService httpService;
    private String stockSymbol;
    private Label label;
    private JSONObject jsonObject;
    private int i = 0;

    public RealTimeStockPriceRefresher(HttpService httpService, JSONObject jsonObject) {
        this.httpService = httpService;
        this.jsonObject = jsonObject;
    }

    @Override
    public void run() {

        try {
            HttpResponse<String> response = httpService.getRealTimeStockPrice(stockSymbol);
            System.out.println(response.body());
            double newCurrentPrice = getPriceFromJSONObject(response);
            System.out.println(newCurrentPrice);
            if (label != null) {
                setNewPrice(newCurrentPrice);
                System.out.println("Refresh price of: " + stockSymbol + ": " + ++i);
            }
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
                label.setStyle("-fx-text-fill: white");
                if (!label.getText().isBlank()) {
                    if (Double.parseDouble(label.getText().substring(0, label.getText().length() - 1)) < newCurrentPrice) {
                        label.setStyle("-fx-text-fill: #AAFF00");
                    }
                    if (Double.parseDouble(label.getText().substring(0, label.getText().length() - 1)) > newCurrentPrice) {
                        label.setStyle("-fx-text-fill: #FF4D4D");
                    }
                }
                label.setText((newCurrentPrice) + "$");
                PauseTransition transition = new PauseTransition(Duration.seconds(3));
                transition.setOnFinished(event -> label.setStyle("-fx-text-fill: white"));
                transition.playFromStart();
            }
        });
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
}