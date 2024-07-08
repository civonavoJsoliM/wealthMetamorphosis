package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.refresher.PercentageRefresher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProfileControllerService {
    private final OwnedStockService ownedStockService;
    private final HttpService service;
    private final Label portfolioWorthLabel;
    private final HBox myStocksHBox;
    private final List<ScheduledExecutorService> scheduledExecutorServices;

    public ProfileControllerService(OwnedStockService ownedStockService, HttpService service, Label portfolioWorthLabel, HBox myStocksHBox, List<ScheduledExecutorService> scheduledExecutorServices) {
        this.ownedStockService = ownedStockService;
        this.service = service;
        this.portfolioWorthLabel = portfolioWorthLabel;
        this.myStocksHBox = myStocksHBox;
        this.scheduledExecutorServices = scheduledExecutorServices;
    }

    public HBox gethBox(Label stockLabel, Label sharesLabel, Label profitLossLabel) {
        HBox hBox = new HBox(stockLabel, sharesLabel, profitLossLabel);
        hBox.setAlignment(Pos.TOP_LEFT);
        HBox.setMargin(stockLabel, new Insets(0, 0, 0, 17.5));
        HBox.setMargin(sharesLabel, new Insets(0, 33, 0, 30));
        return hBox;
    }

    public Label getProfitLossLabel() {
        Label profitLossLabel = new Label();
        profitLossLabel.setId("stocksVBoxLabel");
        return profitLossLabel;
    }

    public Label getSharesLabel(Map.Entry<String, Double> entry) {
        Label sharesLabel = new Label();
        sharesLabel.setText(String.valueOf(entry.getValue()));
        sharesLabel.setId("stocksVBoxLabel");
        return sharesLabel;
    }

    public Label getStocksVBoxStockLabel(Map.Entry<String, Double> entry, List<String> colors, int i) {
        Label stockLabel = new Label();
        stockLabel.setText(entry.getKey());
        stockLabel.setId("stocksVBoxLabel");
        stockLabel.setStyle("-fx-text-fill: " + colors.get(i));
        return stockLabel;
    }

    public void getPercentageScheduler(List<Label> profitLossLabels) {
        PercentageRefresher refresher = new PercentageRefresher(profitLossLabels, service, portfolioWorthLabel, myStocksHBox, ownedStockService);
        ScheduledExecutorService percentageScheduler = Executors.newScheduledThreadPool(1);
        percentageScheduler.scheduleAtFixedRate(refresher, 0, 30, TimeUnit.SECONDS);
        scheduledExecutorServices.add(percentageScheduler);
    }

    public void fillOrdersVBox(VBox ordersVBox) {
        for (Order order : UserSingleton.getInstance().getOrders()) {
            Label dateLabel = getDateLabel(order);
            Label stockLabel = getOrdersVBoxStockLabel(order);
            Label sharesLabel = getSharesLabel(order);
            Label buyPriceLabel = getBuyPriceLabel(order);
            Label orderTypeLabel = getOrderTypeLabel(order);
            HBox hBox = getHBox(dateLabel, stockLabel, sharesLabel, buyPriceLabel, orderTypeLabel);
            ordersVBox.getChildren().add(hBox);
        }
    }

    private HBox getHBox(Label dateLabel, Label stockLabel, Label sharesLabel, Label buyPriceLabel, Label orderTypeLabel) {
        HBox hBox = new HBox(dateLabel, stockLabel, sharesLabel, buyPriceLabel, orderTypeLabel);
        HBox.setMargin(dateLabel, new Insets(0, 0, 0, 10));
        HBox.setMargin(stockLabel, new Insets(0, 0, 0, 6));
        HBox.setMargin(sharesLabel, new Insets(0, 0, 0, 96));
        HBox.setMargin(buyPriceLabel, new Insets(0, 0, 0, 96));
        HBox.setMargin(orderTypeLabel, new Insets(0, 0, 0, 86));
        return hBox;
    }

    public Label getDateLabel(Order order) {
        Label dateLabel = new Label();
        dateLabel.setText(order.getOrderTimeStamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss")));
        dateLabel.setId("ordersVBoxLabel");
        dateLabel.setPrefWidth(170);
        return dateLabel;
    }

    public Label getOrdersVBoxStockLabel(Order order) {
        Label stockLabel = new Label();
        stockLabel.setText(order.getStockSymbol());
        stockLabel.setId("ordersVBoxLabel");
        stockLabel.setPrefWidth(60);
        return stockLabel;
    }

    public Label getSharesLabel(Order order) {
        Label sharesLabel = new Label();
        sharesLabel.setText(String.valueOf(order.getStockShares()));
        sharesLabel.setId("ordersVBoxLabel");
        sharesLabel.setPrefWidth(70);
        return sharesLabel;
    }

    public Label getBuyPriceLabel(Order order) {
        Label buyPriceLabel = new Label();
        buyPriceLabel.setText(order.getStockPrice() + "$");
        buyPriceLabel.setId("ordersVBoxLabel");
        buyPriceLabel.setPrefWidth(100);
        return buyPriceLabel;
    }

    public Label getOrderTypeLabel(Order order) {
        Label orderTypeLabel = new Label();
        orderTypeLabel.setText(order.getOrderType().name());
        orderTypeLabel.setId("ordersVBoxLabel");
        orderTypeLabel.setPrefWidth(40);
        return orderTypeLabel;
    }

    public void fillPieChartWithData(PieChart pieChart, List<String> colors) {
        Map<String, Double> portfolio = ownedStockService.getAllOwnedStockShares(UserSingleton.getInstance());

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        int i = 0;
        for (Map.Entry<String, Double> entry : portfolio.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChartData.add(data);
            pieChart.getData().add(data);
            pieChart.setClockwise(true);
            pieChart.setStartAngle(90);
            data.getNode().setStyle("-fx-background-color: " + colors.get(i));
            i++;
            if (i == colors.size()) {
                i = 0;
            }
        }
        pieChart.setId("pieChart");
    }

    public void fillStocksVBoxWithData(List<String> colors, VBox stocksVBox) {
        Map<String, Double> portfolio = ownedStockService.getAllOwnedStockShares(UserSingleton.getInstance());

        List<Label> profitLossLabels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Double> entry : portfolio.entrySet()) {
            Label stockLabel = getStocksVBoxStockLabel(entry, colors, i);
            Label sharesLabel = getSharesLabel(entry);
            Label profitLossLabel = getProfitLossLabel();
            profitLossLabels.add(profitLossLabel);
            HBox hBox = gethBox(stockLabel, sharesLabel, profitLossLabel);
            stocksVBox.getChildren().add(hBox);
            stocksVBox.setId("stocksVBox");
            i++;
            if (i == colors.size()) {
                i = 0;
            }
        }
        getPercentageScheduler(profitLossLabels);
    }
}