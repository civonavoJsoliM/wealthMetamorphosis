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

    public RealTimeStockPriceRefresher(HttpService httpService, JSONObject jsonObject) {
        this.httpService = httpService;
        this.jsonObject = jsonObject;
    }

    @Override
    public void run() {
        try {
            HttpResponse<String> response = httpService.getRealTimeStockPrice(stockSymbol);
            System.out.println(response.body());
            double currentPrice = getPriceFromJSONObject(response);
            System.out.println(currentPrice);
            if (label != null) {
                setNewPrice(currentPrice);
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

    private void setNewPrice(double currentPrice) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                label.setText((currentPrice) + "$");
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