package app.wealthmetamorphosis.logic.refresher;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.data.UserSingleton;
import app.wealthmetamorphosis.logic.HttpService;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.json.JSONObject;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
public class PercentageRefresher implements Runnable {
    private final List<Label> profitLossLabels;
    private final HttpService service;

    public PercentageRefresher(List<Label> profitLossLabels, HttpService service) {
        this.profitLossLabels = profitLossLabels;
        this.service = service;
    }

    @Override
    public void run() {
        List<Double> percentages = new ArrayList<>();
        for (Order order : UserSingleton.getInstance().getOrders()) {
            OptionalDouble avgBuyPriceOptional = UserSingleton.getInstance().getOrders().stream()
                    .filter(or -> or.getStockSymbol().equals(order.getStockSymbol()) && order.getOrderType().equals(OrderType.BUY))
                    .mapToDouble(Order::getStockPrice)
                    .average();

            HttpResponse<String> response;
            try {
                response = service.getRealTimeStockPrice(order.getStockSymbol());
                //response.statusCode() == 200;
                JSONObject jsonObject = new JSONObject(response.body());
                System.out.println(jsonObject);
                double currentStockPrice = Double.parseDouble(jsonObject.getString("price"));

                double difference = currentStockPrice - avgBuyPriceOptional.getAsDouble();
                double percentage = 100 / (avgBuyPriceOptional.getAsDouble() / difference);
                percentages.add(percentage);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Platform.setImplicitExit(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                for (int i = 0; i < profitLossLabels.size(); i++) {
                    profitLossLabels.get(i).setText(decimalFormat.format(percentages.get(i)) + " %");
                }
                System.out.println("Done");
            }
        });
    }
}