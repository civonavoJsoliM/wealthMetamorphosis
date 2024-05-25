package app.wealthmetamorphosis.logic.refresher;

import app.wealthmetamorphosis.logic.HttpService;
import javafx.application.Platform;
import javafx.scene.control.Label;
import java.io.IOException;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class RealTimeStockPriceRefresher implements Runnable {
    private final HttpService httpService;
    private String stockSymbol;
    private final Label priceLabel;
    private JSONObject jsonObject;

    public RealTimeStockPriceRefresher(HttpService httpService, Label priceLabel, JSONObject jsonObject) {
        this.httpService = httpService;
        this.priceLabel = priceLabel;
        this.jsonObject = jsonObject;
    }

    @Override
    public void run() {
        try {
            HttpResponse<String> response = httpService.getHttpResponse(stockSymbol);
            double currentPrice = getPriceFromJSONObject(response);
            setNewPrice(currentPrice);
            System.out.println(currentPrice);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private double getPriceFromJSONObject(HttpResponse<String> response) {
        jsonObject = new JSONObject(response.body());
        return Double.parseDouble(jsonObject.getString("price"));
    }

    private void setNewPrice(double currentPrice) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                priceLabel.setText(String.valueOf(currentPrice));
            }
        });
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
}
