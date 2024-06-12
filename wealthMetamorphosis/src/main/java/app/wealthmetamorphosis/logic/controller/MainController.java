package app.wealthmetamorphosis.logic.controller;

import app.wealthmetamorphosis.Main;
import app.wealthmetamorphosis.data.*;
import app.wealthmetamorphosis.data.singleton.DBConnectionSingleton;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.data.stock.Stock;
import app.wealthmetamorphosis.logic.*;
import app.wealthmetamorphosis.logic.db.DBReader;
import app.wealthmetamorphosis.logic.db.ResultSetToList;
import app.wealthmetamorphosis.logic.refresher.RealTimeChartRefresher;
import app.wealthmetamorphosis.logic.refresher.RealTimeStockPriceRefresher;
import app.wealthmetamorphosis.logic.service.HttpService;
import app.wealthmetamorphosis.logic.service.OrderService;
import app.wealthmetamorphosis.logic.service.TradingService;
import app.wealthmetamorphosis.logic.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainController {

    // stocks Nodes
    @FXML
    private VBox stocksVBox;
    @FXML
    private TextField searchTextField;
    @FXML
    private ScrollPane scrollPane;

    // chart Nodes
    @FXML
    private VBox chartsVBox;
    @FXML
    private Label placeholderLabel;

    // trading Nodes
    @FXML
    private Label stockLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private TextField buySharesTextField;
    @FXML
    private TextField sellSharesTextField;
    @FXML
    private VBox tradingVBox;
    @FXML
    private VBox sellVBox;
    @FXML
    private Label ownedSharesLabel;
    @FXML
    private Button profileButton;

    private Stage stage;
    private List<String> stockSymbols;
    private List<Stock> stocks;
    private int counter;
    private User user;
    private TradingService tradingService;
    private Checker checker;
    private HttpService httpService;
    private FileReader fileReader;
    private AreaChart<String, Number> chart;
    private ScheduledExecutorService stockPriceScheduler;
    private RealTimeStockPriceRefresher realTimeStockPriceRefresher;
    private ScheduledExecutorService chartSchedule;
    private RealTimeChartRefresher realTimeChartRefresher;
    private Label chartDataLabel;
    private double yMin;
    private double yMax;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;
    private Line line;
    private Button activeButton;
    private HBox timeIntervalsHBox;
    private List<HBox> stocksHBoxes;


    @FXML
    void initialize() {
        stocks = new ArrayList<>();
        counter = 0;
        fileReader = new FileReader();
        httpService = new HttpService(fileReader, counter);
        tradingService = new TradingService(checker, user, priceLabel, stockLabel, buySharesTextField, sellSharesTextField);
        checker = new Checker();

        // refactor ->
        ResultSetToList<Order> rstl = new OrderService();
        DBReader<Order> reader = new DBReader<>(rstl, DBConnectionSingleton.getInstance());
        List<Order> orders = reader.readFromDB("SELECT * FROM orders WHERE user_id = '7143a7c0-8d85-418e-a1d8-b2d98c9e1b25' " +
                "ORDER BY order_timeStamp");
        ResultSetToList<User> rstlUser = new UserService();
        DBReader<User> readerUser = new DBReader<>(rstlUser, DBConnectionSingleton.getInstance());
        List<User> users = readerUser.readFromDB("SELECT * FROM users");
        Optional<User> optionalUser = users.stream().filter(us -> us.getUserId().equals("7143a7c0-8d85-418e-a1d8-b2d98c9e1b25")).findFirst();
        optionalUser.ifPresent(value -> user = value);
        user.setOrders(orders);
        user.setBalance(1000);
        UserSingleton.setUser(user);
        setSharesOnSellOrdersToNegative();
        // <- refactor

        getChart();
        stocksHBoxes = new ArrayList<>();
        activeButton = new Button();

        stockSymbols = fileReader.readFromFile("/Users/ipoce/Desktop/wealthMetamorphosis/StockSymbols.txt");
        stockSymbols.forEach(stockSymbol -> {
            Button stockButton = getStockButton(stockSymbol);

            Circle ownedStockCircle = getOwnedStockCircle();

            HBox stockHBox = getStockHBox(ownedStockCircle, stockButton);
            stocksHBoxes.add(stockHBox);
        });
        stocksVBox.getChildren().addAll(stocksHBoxes);
        stocksVBox.setId("stocksVBox");

        setOwnedStockCircleVisible();
        initializeStockPriceRefresher();
        initializeChartRefresher();
        getChartDataLabel();
    }

    private Button getStockButton(String stockSymbol) {
        stocks.add(new Stock());
        stocks.getLast().setSymbol(stockSymbol);
        stocks.getLast().setButton(new Button(stockSymbol));
        Button stockButton = stocks.getLast().getButton();
        stockButton.setText(stockSymbol);
        stockButton.setId("stockButton");
        stockButton.setPrefWidth(stocksVBox.getPrefWidth());
        stockButton.setOnMouseClicked(event -> {
            Optional<Button> button = stocks.stream().map(Stock::getButton).filter(node -> node.isFocused() || node.isPressed()).findAny();
            if (button.isEmpty() || !activeButton.equals(stockButton)) {
                fillChartWithData(stockSymbol, stockButton);
            }
            setSellVBoxVisibility(stockSymbol);
            stylePressedOrFocusedStockButtons();
            styleNotPressedOrUnfocusedStockButtons();
        });
        stockButton.setOnMouseEntered(event -> {
            for (HBox hBox : stocksHBoxes) {
                Button button = (Button) hBox.getChildren().getLast();
                if (button.isHover() || button.getText().equals(stockLabel.getText())) {
                    hBox.setStyle("-fx-background-color: white");
                    hBox.getChildren().getFirst().setStyle("-fx-fill: #212324; -fx-stroke: #212324;");
                    hBox.getChildren().getLast().setStyle("-fx-text-fill: #212324");
                }
            }
        });
        stockButton.setOnMouseExited(event -> {
            for (HBox hBox : stocksHBoxes) {
                Button button = (Button) hBox.getChildren().getLast();
                if ((button.isHover() && !button.isPressed() && !button.isFocused()) || !button.getText().equals(stockLabel.getText())) {
                    hBox.setStyle("-fx-background-color: #212324");
                    hBox.getChildren().getFirst().setStyle("-fx-fill: white; -fx-stroke: white;");
                    hBox.getChildren().getLast().setStyle("-fx-text-fill: white");
                }
            }
        });
        return stockButton;
    }

    private void styleNotPressedOrUnfocusedStockButtons() {
        stocksHBoxes.stream().filter(hb -> hb.getChildren().getLast().isFocused() || hb.getChildren().getLast().isPressed()).forEach(hb -> {
            hb.setStyle("-fx-background-color: white");
            hb.getChildren().getFirst().setStyle("-fx-fill: #212324; -fx-stroke: #212324;");
            hb.getChildren().getLast().setStyle("-fx-text-fill: #212324");
            Button bt = (Button) hb.getChildren().getLast();
            System.out.println(bt.getText());
        });
    }

    private void stylePressedOrFocusedStockButtons() {
        stocksHBoxes.stream().filter(hb -> !hb.getChildren().getLast().isFocused() || !hb.getChildren().getLast().isPressed()).forEach(hb -> {
            hb.setStyle("-fx-background-color: #212324");
            hb.getChildren().getFirst().setStyle("-fx-fill: white; -fx-stroke: white;");
            hb.getChildren().getLast().setStyle("-fx-text-fill: white");
        });
    }

    private void setSellVBoxVisibility(String stockSymbol) {
        double ownedShares = getOwnedStockShares(stockSymbol);
        if (ownedShares > 0) {
            sellVBox.setVisible(true);
            ownedSharesLabel.setText(String.valueOf(ownedShares));
        } else {
            sellVBox.setVisible(false);
        }
    }

    private void fillChartWithData(String stockSymbol, Button stockButton) {
        activeButton = stockButton;
        arrangeChartWindow(stockSymbol);
        try {
            XYChart.Series<String, Number> series = getChartSeries(stockSymbol, "1min", "390");
            chart.getData().add(series);
            refreshStockPrice();
            refreshChart(stockSymbol, "1min", "390");
            set1DTimeIntervalButtonToActive();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void set1DTimeIntervalButtonToActive() {
        timeIntervalsHBox.getChildren().stream().map(node -> (Button) node).forEach(bt -> bt.getStylesheets().clear());
        Button timeIntervalButton = (Button) timeIntervalsHBox.getChildren().getFirst();
        addStyleSheetToButton(timeIntervalButton);
    }

    private void arrangeChartWindow(String stockSymbol) {
        tradingVBox.setVisible(true);
        stockLabel.setText(stockSymbol);
        removePlaceholderLabel();
        if (chartsVBox.getChildren().isEmpty()) {
            VBox vBoxChartWindow = getChartWindowVBox(chart);
            chartsVBox.getChildren().add(vBoxChartWindow);
        } else {
            chart.getData().clear();
        }
    }

    private Circle getOwnedStockCircle() {
        Circle ownedStockCircle = new Circle();
        ownedStockCircle.setId("ownedStockCircle");
        ownedStockCircle.setRadius(5);
        ownedStockCircle.setVisible(false);
        return ownedStockCircle;
    }

    private HBox getStockHBox(Circle circle, Button stockButton) {
        HBox.setMargin(circle, new Insets(0, 0, 0, 35));
        HBox.setMargin(stockButton, new Insets(0, 0, 0, -45));
        HBox stockHBox = new HBox();
        stockHBox.getChildren().addAll(circle, stockButton);
        stockHBox.setId("stockHBox");
        return stockHBox;
    }

    private void setOwnedStockCircleVisible() {
        Map<String, Double> map = UserSingleton.getInstance().getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStockSymbol, Collectors.summingDouble(Order::getStockShares)))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (String stockSymbol : map.keySet()) {
            for (HBox hBox : stocksHBoxes) {
                Button button = (Button) hBox.getChildren().getLast();
                if (button.getText().equals(stockSymbol)) {
                    Circle circle = (Circle) hBox.getChildren().getFirst();
                    circle.setVisible(true);
                }
            }
        }
    }

    private void initializeChartRefresher() {
        chartSchedule = Executors.newScheduledThreadPool(1);
        ObjectMapper objectMapper = new ObjectMapper();
        realTimeChartRefresher = new RealTimeChartRefresher(httpService, chart, objectMapper);
    }

    private void initializeStockPriceRefresher() {
        stockPriceScheduler = Executors.newScheduledThreadPool(1);
        JSONObject jsonObject = new JSONObject();
        realTimeStockPriceRefresher = new RealTimeStockPriceRefresher(httpService, jsonObject);
        realTimeStockPriceRefresher.setLabel(priceLabel);
    }

    private void getChartDataLabel() {
        chartDataLabel = new Label();
        chartDataLabel.setText("Date and price");
        chartDataLabel.setId("chartDataLabel");
        chartDataLabel.setVisible(false);
    }

    @FXML
    void onProfileClicked() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("profile-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ProfileController controller = fxmlLoader.getController();
        Stage profileStage = controller.getProfileStage();
        setUpProfileStage(profileStage, scene);
        profileStage.show();
    }

    private void setUpProfileStage(Stage profileStage, Scene scene) {
        profileStage.setScene(scene);
        profileStage.initOwner(stage);
        profileStage.initModality(Modality.WINDOW_MODAL);
        profileStage.initStyle(StageStyle.UTILITY);
    }

    @FXML
    void typingIntoSearchTextField() {
        String searchItem = searchTextField.getText();
        removeIrrelevantStocks();
        if (!searchItem.isBlank()) {
            for (HBox hbox : stocksHBoxes) {
                Button button = (Button) hbox.getChildren().getLast();
                if (button.getText().contains(searchItem.toUpperCase())) {
                    stocksVBox.getChildren().add(hbox);
                }
            }
        } else {
            stocksHBoxes.forEach(hBox -> stocksVBox.getChildren().add(hBox));
        }
    }

    private void removeIrrelevantStocks() {
        while (!stocksVBox.getChildren().isEmpty()) {
            stocksVBox.getChildren().remove(stocksVBox.getChildren().size() - 1);
        }
    }

    @FXML
    void onBuyClicked() {
        tradingService.placeOrder(OrderType.BUY);
        makeTrade();
    }

    @FXML
    void onSellButtonClicked() {
        tradingService.placeOrder(OrderType.SELL);
        makeTrade();
    }

    private void makeTrade() {
        setSharesOnSellOrdersToNegative();
        Optional<Stock> chosenStock = stocks.stream().filter(stk -> stk.getSymbol().equals(stockLabel.getText())).findFirst();
        double ownedStockShares = getOwnedStockShares(stockLabel.getText());
        chosenStock.ifPresent(value -> setSellVBoxAndStockDotVisibility(value, ownedStockShares));
    }

    private void setSharesOnSellOrdersToNegative() {
        UserSingleton.getInstance().getOrders().stream()
                .filter(order -> order.getOrderType().equals(OrderType.SELL) && order.getStockShares() > 0)
                .forEach(order -> order.setStockShares(order.getStockShares() * -1));
    }

    private double getOwnedStockShares(String stock) {
        return UserSingleton.getInstance().getOrders().stream()
                .filter(order -> order.getStockSymbol().equals(stock))
                .mapToDouble(Order::getStockShares)
                .sum();
    }

    private void setSellVBoxAndStockDotVisibility(Stock stock, double ownedShares) {
        for (Node vBoxChildren : stocksVBox.getChildren()) {
            HBox hBox = (HBox) vBoxChildren;
            for (Node hBoxChildren : hBox.getChildren()) {
                Circle circle = (Circle) hBox.getChildren().getFirst();
                if (hBoxChildren.equals(stock.getButton())) {
                    changeVisibility(ownedShares, circle);
                }
            }
        }
    }

    private void changeVisibility(double ownedShares, Circle circle) {
        if (ownedShares < 1) {
            sellVBox.setVisible(false);
            circle.setVisible(false);
        } else {
            ownedSharesLabel.setText(String.valueOf(ownedShares));
            sellVBox.setVisible(true);
            circle.setVisible(true);
        }
    }

    private XYChart.Series<String, Number> getChartSeries(String symbol, String interval, String outputSize) throws IOException, InterruptedException {
        Stock stock = getStock(symbol, interval, outputSize);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = stock.getValues().size() - 1; i >= 0; i--) {
            Number close = BigDecimal.valueOf(Double.parseDouble(stock.getValues().get(i).getClose()));
            String dateTime = stock.getValues().get(i).getDatetime();
            XYChart.Data<String, Number> data = new XYChart.Data<>(dateTime, close);
            series.getData().add(data);
        }
        return series;
    }

    private Stock getStock(String symbol, String interval, String outputSize) throws IOException, InterruptedException {
        HttpResponse<String> response = httpService.getStock(symbol, interval, outputSize);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response.body(), Stock.class);
    }

    private VBox getChartWindowVBox(AreaChart<String, Number> chart) {
        HBox hBox = getTimeIntervalsHBox(chart);
        line = getChartLine();
        Circle circle = getChartLineCircle();
        Pane pane = getPane(line, circle);
        StackPane stackPane = getStackPane(chart, pane);
        HBox chartDataHBox = new HBox(chartDataLabel);
        chartDataHBox.setId("chartDataHBox");
        VBox chartWindowVBox = new VBox();
        chartWindowVBox.setId("chartWindowVBox");
        chartWindowVBox.getChildren().addAll(hBox, chartDataHBox, stackPane);
        return chartWindowVBox;
    }

    private HBox getTimeIntervalsHBox(AreaChart<String, Number> chart) {
        timeIntervalsHBox = new HBox();
        timeIntervalsHBox.setId("timeIntervalsHBox");
        List<Button> timeIntervalButtons = new ArrayList<>();
        List<String> timeIntervalsData = fileReader.readFromFile("/Users/ipoce/Desktop/wealthMetamorphosis/TimeIntervals.txt");
        for (String timeInterval : timeIntervalsData) {
            Button timeIntervalButton = new Button();
            timeIntervalButton.setId("timeIntervalButton");
            timeIntervalButton.setText(timeInterval.split(" ")[0]);
            if (timeIntervalButton.getText().equals("1D")) {
                addStyleSheetToButton(timeIntervalButton);
            }
            timeIntervalButtons.add(timeIntervalButton);
            timeIntervalButton.setOnMouseClicked(event -> {
                //if (!timeIntervalButton.getStylesheets().contains(Objects.requireNonNull(
                //getClass().getResource("/app/wealthmetamorphosis/css/timeIntervalButton-whenPressed.css")).toExternalForm())) {
                if (!timeIntervalButton.getStylesheets().isEmpty()) {
                    addStyleSheetToButton(timeIntervalButton);
                }
                String outputSize = getOutputSize(timeInterval);
                try {
                    XYChart.Series<String, Number> series = getChartSeries(stockLabel.getText(), timeInterval.split(" ")[1], outputSize);
                    chart.getData().clear();
                    chart.getData().add(series);

                    // restart refresher
                    chartSchedule.shutdown();
                    chartSchedule = Executors.newScheduledThreadPool(1);
                    refreshChart(stockLabel.getText(), timeInterval.split(" ")[1], outputSize);

                    // clear stylesheets for not-pressed buttons
                    timeIntervalButtons.stream().filter(button -> !button.equals(timeIntervalButton)).forEach(button -> button.getStylesheets().clear());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            timeIntervalsHBox.getChildren().add(timeIntervalButton);
        }
        return timeIntervalsHBox;
    }

    private void addStyleSheetToButton(Button button) {
        button.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/app/wealthmetamorphosis/css/timeIntervalButton-whenPressed.css")).toExternalForm());
    }

    private String getOutputSize(String timeInterval) {
        DaysCalculator calculator = new DaysCalculator(fileReader);
        int days = calculator.getNumberOfTradingDaysFromBeginOfYearTillNow();
        return timeInterval.split(" ")[0].equals("YTD") ? String.valueOf(days) : timeInterval.split(" ")[2];
    }

    private StackPane getStackPane(AreaChart<String, Number> chart, Pane pane) {
        StackPane stackPane = new StackPane();
        stackPane.setId("stackPane");
        stackPane.getChildren().addAll(chart, pane);
        stackPane.setOnMouseEntered(event -> {
            chartDataLabel.setVisible(true);
            pane.setVisible(true);
            yMax = yAxis.getDisplayPosition(yAxis.getUpperBound());
            line.setStartY(yMax + yAxis.getBaselineOffset());
            yMin = yAxis.getDisplayPosition(yAxis.getLowerBound());
            line.setEndY(yMin + yAxis.getBaselineOffset());
            chart.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/app/wealthmetamorphosis/css/chart-hover.css")).toExternalForm());
        });
        stackPane.setOnMouseExited(event -> {
            chartDataLabel.setVisible(false);
            pane.setVisible(false);
            chart.getStylesheets().clear();
        });
        return stackPane;
    }

    private Pane getPane(Line line, Circle circle) {
        Pane pane = new Pane();
        pane.toFront();
        pane.getChildren().add(line);
        pane.getChildren().add(circle);
        pane.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double yPos;
                for (XYChart.Series<String, Number> series : chart.getData()) {
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        if ((int) chart.getXAxis().getDisplayPosition(data.getXValue()) == ((int) event.getX() - 47)) {

                            // set line coordinates
                            line.setStartX(event.getX());
                            line.setEndX(event.getX());

                            // set circle center coordinates
                            yPos = chart.getYAxis().getDisplayPosition(data.getYValue()) + 17;
                            circle.setCenterX(event.getX());
                            circle.setCenterY(yPos);

                            // set text
                            chartDataLabel.setText(data.getXValue() + " | " + data.getYValue() + "$");
                        }
                    }
                }
            }
        });
        return pane;
    }

    private Circle getChartLineCircle() {
        Circle circle = new Circle();
        circle.setId("chartLineCircle");
        circle.setRadius(5);
        return circle;
    }

    private Line getChartLine() {
        line = new Line();
        line.setId("chartLine");
        line.setStartX(0);
        line.setEndX(0);
        return line;
    }

    private void removePlaceholderLabel() {
        if (chartsVBox.getChildren().get(0).equals(placeholderLabel)) {
            chartsVBox.getChildren().remove(chartsVBox.getChildren().getFirst());
        }
    }

    private void refreshStockPrice() {
        realTimeStockPriceRefresher.setStockSymbol(stockLabel.getText());
        stockPriceScheduler.scheduleAtFixedRate(realTimeStockPriceRefresher, 0, 30, TimeUnit.SECONDS);
    }

    private void refreshChart(String button, String interval, String outputSize) {
        realTimeChartRefresher.setParameters(button, interval, outputSize);
        chartSchedule.scheduleAtFixedRate(realTimeChartRefresher, 0, 30, TimeUnit.SECONDS);
    }

    private void getChart() {
        getXAxis();
        getYAxis();
        chart = new AreaChart<>(xAxis, yAxis);
        chart.setId("chart");
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
    }

    private void getYAxis() {
        yAxis = new NumberAxis();
        yAxis.setId("yAxis");
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
    }

    private void getXAxis() {
        xAxis = new CategoryAxis();
        xAxis.setId("xAxis");
        xAxis.setAutoRanging(true);
        xAxis.setStartMargin(0);
        xAxis.setGapStartAndEnd(false);
    }

    @FXML
    void onMouseEnteredStocksScrollPane() {
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    @FXML
    void onMouseExitedStocksScrollPane() {
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}