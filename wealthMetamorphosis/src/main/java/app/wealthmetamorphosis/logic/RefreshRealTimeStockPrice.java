package app.wealthmetamorphosis.logic;

import javafx.application.Platform;
import javafx.scene.control.Label;
import java.io.IOException;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class RefreshRealTimeStockPrice implements Runnable {
    private final HttpService httpService;
    private String stockSymbol;
    private final Label priceLabel;

    public RefreshRealTimeStockPrice(HttpService httpService, Label priceLabel) {
        this.httpService = httpService;
        this.priceLabel = priceLabel;
    }

    @Override
    public void run() {
        try {
            HttpResponse<String> response = httpService.getHttpResponse(stockSymbol);
            double currentPrice = getPriceFromJSONObject(response);
            setNewPrice(currentPrice);
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
                priceLabel.setText(String.valueOf(currentPrice));
            }
        });
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
}
