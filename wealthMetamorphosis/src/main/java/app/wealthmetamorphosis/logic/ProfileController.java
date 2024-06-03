package app.wealthmetamorphosis.logic;

import app.wealthmetamorphosis.Main;
import app.wealthmetamorphosis.data.*;
import app.wealthmetamorphosis.logic.refresher.PercentageRefresher;
import app.wealthmetamorphosis.logic.refresher.RealTimeStockPriceRefresher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.JSONObject;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ProfileController {

    @FXML
    private VBox stocksVBox;
    @FXML
    private Label registeredLabel;
    @FXML
    private Label userNameLabel;
    @FXML
    private VBox ordersVBox;
    @FXML
    private PieChart pieChart;
    @FXML
    private Circle profilePicture;

    private Stage profileStage;
    private int counter;
    private List<String> colors;
    private FileReader fileReader;
    private HttpService service;
    private RealTimeStockPriceRefresher realTimeStockPriceRefresher;
    private PercentageRefresher percentageRefresher;
    private DecimalFormat decimalFormat;
    private List<ScheduledExecutorService> scheduledExecutorServices;

    @FXML
    void initialize() throws IOException, InterruptedException {
        profileStage = new Stage();
        profileStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                System.out.println("Stage closed");
                scheduledExecutorServices.forEach(ScheduledExecutorService::shutdownNow);
            }
        });
        decimalFormat = new DecimalFormat("0.00");
        fileReader = new FileReader();
        service = new HttpService(fileReader, counter);
        counter = 0;
        scheduledExecutorServices = new ArrayList<>();
        colors = Files.readAllLines(Path.of("/Users/ipoce/Desktop/wealthMetamorphosis/Colors.txt"));

        fillPieChartWithData();
        fillStocksVBoxWithData();
        fillOrdersVBox();
        setProfilePicture();

        userNameLabel.setText(UserSingleton.getInstance().getUserName());
        registeredLabel.setText(UserSingleton.getInstance().getRegistered().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }

    private void setProfilePicture() {
        Image image = new Image(String.valueOf(Objects.requireNonNull(Main.class.getResource("profile-picture.jpg"))));
        ImagePattern pattern = new ImagePattern(image);
        profilePicture.setFill(pattern);
    }

    private void fillStocksVBoxWithData() throws IOException, InterruptedException {
        UserSingleton.getInstance().getOrders().stream()
                .filter(order -> order.getOrderType().equals(OrderType.SELL))
                .forEach(order -> order.setStockShares(order.getStockShares() * -1));

        Map<String, Double> portfolio = UserSingleton.getInstance().getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStockSymbol, Collectors.summingDouble(Order::getStockShares)));

        int i = 0;
        for (Map.Entry<String, Double> entry : portfolio.entrySet()) {
            Label stockLabel = getStocksVBoxStockLabel(entry, colors, i);
            Label sharesLabel = getSharesLabel(entry);
            Label profitLossLabel = getProfitLossLabel(entry);
            HBox hBox = gethBox(stockLabel, sharesLabel, profitLossLabel);
            stocksVBox.getChildren().add(hBox);
            stocksVBox.setId("stocksVBox");
            i++;
            if (i == 49) {
                i = 0;
            }
        }
    }

    private HBox gethBox(Label stockLabel, Label sharesLabel, Label profitLossLabel) {
        HBox hBox = new HBox(stockLabel, sharesLabel, profitLossLabel);
        hBox.setAlignment(Pos.TOP_LEFT);
        HBox.setMargin(stockLabel, new Insets(0, 37.5, 0, 10));
        HBox.setMargin(sharesLabel, new Insets(0, 30, 0, 0));
        return hBox;
    }

    private Label getProfitLossLabel(Map.Entry<String, Double> entry) throws IOException, InterruptedException {
        Label profitLossLabel = new Label();
        profitLossLabel.setId("stocksVBoxLabel");
        double profitLossPercentage = getPercentage(entry.getKey(), profitLossLabel);
        profitLossLabel.setText(decimalFormat.format(profitLossPercentage) + " %");
        return profitLossLabel;
    }

    private Label getSharesLabel(Map.Entry<String, Double> entry) {
        Label sharesLabel = new Label();
        sharesLabel.setText(String.valueOf(entry.getValue()));
        sharesLabel.setId("stocksVBoxLabel");
        return sharesLabel;
    }

    private Label getStocksVBoxStockLabel(Map.Entry<String, Double> entry, List<String> colors, int i) {
        Label stockLabel = new Label();
        stockLabel.setText(entry.getKey());
        stockLabel.setId("stocksVBoxLabel");
        stockLabel.setStyle("-fx-text-fill: " + colors.get(i));
        return stockLabel;
    }

    private double getPercentage(String stockSymbol, Label profitLossLabel) throws IOException, InterruptedException {
        OptionalDouble avgBuyPriceOptional = UserSingleton.getInstance().getOrders().stream()
                .filter(order -> order.getStockSymbol().equals(stockSymbol) && order.getOrderType().equals(OrderType.BUY))
                .mapToDouble(Order::getStockPrice)
                .average();

        JSONObject jsonObject = getJsonObject(stockSymbol);
        ScheduledExecutorService realTimeStockPriceScheduler = getRealTimeStockPriceScheduler(stockSymbol, jsonObject);
        double currentStockPrice = Double.parseDouble(jsonObject.getString("price"));
        double avgBuyPrice = 0;
        if (avgBuyPriceOptional.isPresent()) {
            ScheduledExecutorService percentageScheduler = getPercentageScheduler(profitLossLabel, avgBuyPriceOptional, currentStockPrice);
            avgBuyPrice = getAvgBuyPrice(currentStockPrice, avgBuyPriceOptional);
            scheduledExecutorServices.addAll(List.of(realTimeStockPriceScheduler, percentageScheduler));
        }
        return avgBuyPrice;
    }

    private double getAvgBuyPrice(double currentStockPrice, OptionalDouble avgBuyPriceOptional) {
        double difference = currentStockPrice - avgBuyPriceOptional.getAsDouble();
        return 100 / (avgBuyPriceOptional.getAsDouble() / difference);
    }

    private ScheduledExecutorService getPercentageScheduler(Label profitLossLabel, OptionalDouble avgBuyPriceOptional, double currentStockPrice) {
        percentageRefresher = new PercentageRefresher(avgBuyPriceOptional.getAsDouble(), currentStockPrice, profitLossLabel);
        ScheduledExecutorService percentageScheduler = Executors.newScheduledThreadPool(1);
        percentageScheduler.scheduleAtFixedRate(this.percentageRefresher, 1, 30, TimeUnit.SECONDS);
        return percentageScheduler;
    }

    private ScheduledExecutorService getRealTimeStockPriceScheduler(String stockSymbol, JSONObject jsonObject) {
        realTimeStockPriceRefresher = new RealTimeStockPriceRefresher(service, jsonObject);
        realTimeStockPriceRefresher.setStockSymbol(stockSymbol);
        ScheduledExecutorService realTimeStockPriceScheduler = Executors.newScheduledThreadPool(1);
        realTimeStockPriceScheduler.scheduleAtFixedRate(realTimeStockPriceRefresher, 0, 30, TimeUnit.SECONDS);
        return realTimeStockPriceScheduler;
    }

    private JSONObject getJsonObject(String stockSymbol) throws IOException, InterruptedException {
        HttpResponse<String> response = service.getRealTimeStockPrice(stockSymbol);
        return new JSONObject(response.body());
    }

    private void fillOrdersVBox() {
        for (Order order : UserSingleton.getInstance().getOrders()) {
            Label orderIdLabel = getOrderIdLabel(order);
            Label dateLabel = getDateLabel(order);
            Label stockLabel = getOrdersVBoxStockLabel(order);
            Label buyPriceLabel = getBuyPriceLabel(order);
            Label orderTypeLabel = getOrderTypeLabel(order);
            HBox hBox = new HBox(orderIdLabel, dateLabel, stockLabel, buyPriceLabel, orderTypeLabel);
            ordersVBox.getChildren().add(hBox);
        }
    }

    private Label getOrderIdLabel(Order order) {
        String orderIdSubString = order.getOrderId().substring(order.getOrderId().length() - 4);
        orderIdSubString = "..." + orderIdSubString;
        Label orderIdLabel = new Label();
        orderIdLabel.setText(orderIdSubString);
        orderIdLabel.setId("ordersVBoxLabel");
        orderIdLabel.setPadding(new Insets(0, 0, 0, 20));
        return orderIdLabel;
    }

    private Label getDateLabel(Order order) {
        Label dateLabel = new Label();
        dateLabel.setText(order.getOrderTimeStamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh.mm.ss")));
        dateLabel.setId("ordersVBoxLabel");
        dateLabel.setPadding(new Insets(0, 0, 0, 49));
        return dateLabel;
    }

    private Label getOrdersVBoxStockLabel(Order order) {
        Label stockLabel = new Label();
        stockLabel.setText(order.getStockSymbol());
        stockLabel.setId("ordersVBoxLabel");
        stockLabel.setPadding(new Insets(0, 0, 0, 32));
        stockLabel.setPrefWidth(100);
        return stockLabel;
    }

    private Label getBuyPriceLabel(Order order) {
        Label buyPriceLabel = new Label();
        buyPriceLabel.setText(String.valueOf(order.getStockPrice()));
        buyPriceLabel.setId("ordersVBoxLabel");
        buyPriceLabel.setPadding(new Insets(0, 0, 0, 90));
        buyPriceLabel.setPrefWidth(200);
        return buyPriceLabel;
    }

    private Label getOrderTypeLabel(Order order) {
        Label orderTypeLabel = new Label();
        orderTypeLabel.setText(order.getOrderType().name());
        orderTypeLabel.setId("ordersVBoxLabel");
        orderTypeLabel.setPadding(new Insets(0, 0, 0, 120));
        return orderTypeLabel;
    }

    private void fillPieChartWithData() {
        UserSingleton.getInstance().getOrders().stream()
                .filter(order -> order.getOrderType().equals(OrderType.SELL))
                .forEach(order -> order.setStockShares(order.getStockShares() * -1));

        Map<String, Double> portfolio = UserSingleton.getInstance().getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStockSymbol, Collectors.summingDouble(Order::getStockShares)));
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        int i = 0;
        for (Map.Entry<String, Double> entry : portfolio.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChartData.add(data);
            pieChart.getData().add(data);
            data.getNode().setStyle("-fx-background-color: " + colors.get(i));
            i++;
            if (i == 49) {
                i = 0;
            }
        }
        pieChart.setId("pieChart");
    }

    public Stage getProfileStage() {
        return profileStage;
    }
}