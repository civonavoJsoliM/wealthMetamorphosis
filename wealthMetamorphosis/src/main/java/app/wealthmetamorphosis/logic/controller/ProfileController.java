package app.wealthmetamorphosis.logic.controller;

import app.wealthmetamorphosis.data.*;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.data.stock.Stock;
import app.wealthmetamorphosis.logic.FileReader;
import app.wealthmetamorphosis.logic.refresher.PercentageRefresher;
import app.wealthmetamorphosis.logic.service.HttpService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    @FXML
    private ScrollPane stocksScrollPane;
    @FXML
    private ScrollPane ordersScrollPane;
    @FXML
    private Label availableFundsLabel;
    @FXML
    private Label portfolioWorthLabel;
    @FXML
    private VBox progressVBox;
    @FXML
    private HBox myStocksHBox;
    @FXML
    private VBox noStocksToDisplayVBox;
    @FXML
    private VBox noOrdersToDisplayVBox;

    private Stage profileStage;
    private int counter;
    private List<String> colors;
    private FileReader fileReader;
    private HttpService service;
    private List<ScheduledExecutorService> scheduledExecutorServices;
    private List<Label> profitLossLabels;

    @FXML
    void initialize() throws IOException {
        fileReader = new FileReader();
        service = new HttpService(fileReader, counter);
        counter = 0;
        scheduledExecutorServices = new ArrayList<>();
        colors = Files.readAllLines(Path.of("/Users/ipoce/Desktop/wealthMetamorphosis/Colors.txt"));

        boolean isUserOwningStocks = UserSingleton.getInstance().getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStockSymbol, Collectors.summingDouble(Order::getStockShares)))
                .entrySet().stream()
                .noneMatch(entry -> entry.getValue() > 0);
        if (UserSingleton.getInstance().getOrders().isEmpty()) {
            noStocksToDisplayVBox.toFront();
            noOrdersToDisplayVBox.toFront();
            portfolioWorthLabel.setText("0$");
        } else if (isUserOwningStocks) {
            noStocksToDisplayVBox.toFront();
            fillOrdersVBox();
            ordersScrollPane.toFront();
            portfolioWorthLabel.setText("0$");
        } else {
            progressVBox.toFront();
            ordersScrollPane.toFront();
            fillPieChartWithData();
            fillStocksVBoxWithData();
            fillOrdersVBox();
        }
        setProfileStage();
        setProfilePicture();

        userNameLabel.setText(UserSingleton.getInstance().getUserName());
        registeredLabel.setText(UserSingleton.getInstance().getRegistered().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        availableFundsLabel.setText(Math.round(UserSingleton.getInstance().getBalance()) + "$");
    }

    private void setProfileStage() {
        profileStage = new Stage();
        profileStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                System.out.println("Stage closed");
                scheduledExecutorServices.forEach(ScheduledExecutorService::shutdownNow);
            }
        });
        profileStage.setResizable(false);
    }

    private void setProfilePicture() {
        Image image = new Image(String.valueOf(Objects.requireNonNull(getClass().getResource("/app/wealthmetamorphosis/jpg/profile-picture.jpg"))));
        ImagePattern pattern = new ImagePattern(image);
        profilePicture.setFill(pattern);
    }

    private void fillStocksVBoxWithData() {
        UserSingleton.getInstance().getOrders().stream()
                .filter(order -> order.getOrderType().equals(OrderType.SELL) && order.getStockShares() > 0)
                .forEach(order -> order.setStockShares(order.getStockShares() * -1));

        Map<String, Double> portfolio = UserSingleton.getInstance().getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStockSymbol, Collectors.summingDouble(Order::getStockShares)))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        profitLossLabels = new ArrayList<>();
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
        getPercentageScheduler();
    }

    private HBox gethBox(Label stockLabel, Label sharesLabel, Label profitLossLabel) {
        HBox hBox = new HBox(stockLabel, sharesLabel, profitLossLabel);
        hBox.setAlignment(Pos.TOP_LEFT);
        HBox.setMargin(stockLabel, new Insets(0, 37.5, 0, 10));
        HBox.setMargin(sharesLabel, new Insets(0, 30, 0, 0));
        return hBox;
    }

    private Label getProfitLossLabel() {
        Label profitLossLabel = new Label();
        profitLossLabel.setId("stocksVBoxLabel");
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
        System.out.println("Label: " + colors.get(i));
        return stockLabel;
    }

    private void getPercentageScheduler() {
        PercentageRefresher refresher = new PercentageRefresher(profitLossLabels, service, portfolioWorthLabel, myStocksHBox);
        ScheduledExecutorService percentageScheduler = Executors.newScheduledThreadPool(1);
        percentageScheduler.scheduleAtFixedRate(refresher, 0, 30, TimeUnit.SECONDS);
        scheduledExecutorServices.add(percentageScheduler);
    }

    private void fillOrdersVBox() {
        for (Order order : UserSingleton.getInstance().getOrders()) {
            Label dateLabel = getDateLabel(order);
            Label stockLabel = getOrdersVBoxStockLabel(order);
            Label sharesLabel = getSharesLabel(order);
            Label buyPriceLabel = getBuyPriceLabel(order);
            Label orderTypeLabel = getOrderTypeLabel(order);
            HBox hBox = new HBox(dateLabel, stockLabel, sharesLabel, buyPriceLabel, orderTypeLabel);
            ordersVBox.getChildren().add(hBox);
        }
    }

    private Label getDateLabel(Order order) {
        Label dateLabel = new Label();
        dateLabel.setText(order.getOrderTimeStamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss")));
        dateLabel.setId("ordersVBoxLabel");
        dateLabel.setPadding(new Insets(0, 0, 0, 0));
        return dateLabel;
    }

    private Label getOrdersVBoxStockLabel(Order order) {
        Label stockLabel = new Label();
        stockLabel.setText(order.getStockSymbol());
        stockLabel.setId("ordersVBoxLabel");
        stockLabel.setPadding(new Insets(0, 0, 0, 45));
        stockLabel.setPrefWidth(100);
        return stockLabel;
    }

    private Label getSharesLabel(Order order) {
        Label sharesLabel = new Label();
        sharesLabel.setText(String.valueOf(order.getStockShares()));
        sharesLabel.setId("ordersVBoxLabel");
        sharesLabel.setPadding(new Insets(0, 0, 0, 80));
        sharesLabel.setPrefWidth(150);
        return sharesLabel;
    }

    private Label getBuyPriceLabel(Order order) {
        Label buyPriceLabel = new Label();
        buyPriceLabel.setText(order.getStockPrice() + "$");
        buyPriceLabel.setId("ordersVBoxLabel");
        buyPriceLabel.setPadding(new Insets(0, 0, 0, 90));
        buyPriceLabel.setPrefWidth(200);
        return buyPriceLabel;
    }

    private Label getOrderTypeLabel(Order order) {
        Label orderTypeLabel = new Label();
        orderTypeLabel.setText(order.getOrderType().name());
        orderTypeLabel.setId("ordersVBoxLabel");
        orderTypeLabel.setPadding(new Insets(0, 0, 0, 100));
        return orderTypeLabel;
    }

    private void fillPieChartWithData() {
        UserSingleton.getInstance().getOrders().stream()
                .filter(order -> order.getOrderType().equals(OrderType.SELL) && order.getStockShares() > 0)
                .forEach(order -> order.setStockShares(order.getStockShares() * -1));

        Map<String, Double> portfolio = UserSingleton.getInstance().getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStockSymbol, Collectors.summingDouble(Order::getStockShares)))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        int i = 0;
        for (Map.Entry<String, Double> entry : portfolio.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChartData.add(data);
            pieChart.getData().add(data);
            pieChart.setClockwise(true);
            pieChart.setStartAngle(90);
            data.getNode().setStyle("-fx-background-color: " + colors.get(i));
            System.out.println("Chart: " + colors.get(i));
            i++;
            if (i == colors.size()) {
                i = 0;
            }
        }
        pieChart.setId("pieChart");
    }

    @FXML
    void onMouseEnteredStocksScrollPane() {
        stocksScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        stocksScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    @FXML
    void onMouseExitedStocksScrollPane() {
        stocksScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        stocksScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    @FXML
    void onMouseEnteredOrdersScrollPane() {
        ordersScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        ordersScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    @FXML
    void onMouseExitedOrdersScrollPane() {
        ordersScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ordersScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    @FXML
    void onLogOutClicked() {
        UserSingleton.setUser(null);
        profileStage.close();
    }

    public Stage getProfileStage() {
        return profileStage;
    }
}