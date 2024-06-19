package app.wealthmetamorphosis.logic.refresher;

import app.wealthmetamorphosis.logic.service.HttpService;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class RealTimeStockPriceRefresher implements Runnable {
    private final HttpService httpService;
    private String stockSymbol;
    private Label label;
    private JSONObject jsonObject;
    private int i = 0;
    private double oldCurrentPrice;

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
                setNewPrice(newCurrentPrice, oldCurrentPrice);
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

    private void setNewPrice(double newCurrentPrice, double oldCurrentPrice) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                label.setText((newCurrentPrice) + "$");
                if (oldCurrentPrice != 0) {
                    if (oldCurrentPrice > newCurrentPrice) {
                        label.setStyle("-fx-background-color: red");
                    } else if (oldCurrentPrice < newCurrentPrice) {
                        label.setStyle("-fx-background-color: green");
                    } else {
                        label.setStyle("-fx-background-color: transparent");
                    }
                }
                setOldPrice(oldCurrentPrice, newCurrentPrice);
            }
        });
    }

    private void setOldPrice(double oldCurrentPrice, double newCurrentPrice) {
        oldCurrentPrice = newCurrentPrice;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
}