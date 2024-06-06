package app.wealthmetamorphosis;

import app.wealthmetamorphosis.data.*;
import app.wealthmetamorphosis.logic.*;
import app.wealthmetamorphosis.logic.db.DBInserter;
import app.wealthmetamorphosis.logic.db.DBReader;
import app.wealthmetamorphosis.logic.db.ResultSetToList;
import app.wealthmetamorphosis.logic.refresher.RealTimeChartRefresher;
import app.wealthmetamorphosis.logic.refresher.RealTimeStockPriceRefresher;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.css.Stylesheet;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainController {
    // main pane
    @FXML
    private BorderPane borderPane;

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
    private Label nothingToDisplayLabel;

    // trading Nodes
    @FXML
    private Button profileButton;
    @FXML
    private Label stockLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private TextField sharesTextField;
    @FXML
    private Button buyButton;
    @FXML
    private Button sellButton;
    @FXML
    private VBox tradingVBox;

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
    private double height;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;
    private Line line;


    @FXML
    void initialize() {
        // testing
        tradingVBox.setVisible(false);

        stocks = new ArrayList<>();
        setUpChart();
        counter = 0;
        fileReader = new FileReader();
        httpService = new HttpService(fileReader, counter);

        DBConnection dbConnection = new DBConnection("jdbc:mysql://localhost/wealthMetamorphosis", "root", "password");
        ResultSetToList<Order> rstl = new OrderService();
        DBReader<Order> reader = new DBReader<>(rstl, dbConnection);
        List<Order> orders = reader.readFromDB("SELECT * FROM orders WHERE user_id = '7143a7c0-8d85-418e-a1d8-b2d98c9e1b25' " +
                "ORDER BY order_timeStamp");
        ResultSetToList<User> rstlUser = new UserService();
        DBReader<User> readerUser = new DBReader<>(rstlUser, dbConnection);
        List<User> users = readerUser.readFromDB("SELECT * FROM users");
        Optional<User> optionalUser = users.stream().filter(us -> us.getUserId().equals("7143a7c0-8d85-418e-a1d8-b2d98c9e1b25")).findFirst();
        optionalUser.ifPresent(value -> user = value);
        user.setOrders(orders);
        UserSingleton.setUser(user);

        DBInserter inserter = new DBInserter(dbConnection);
        checker = new Checker();
        tradingService = new TradingService(checker, user, priceLabel, stockLabel, sharesTextField);
        stockSymbols = fileReader.readFromFile("/Users/ipoce/Desktop/wealthMetamorphosis/StockSymbols.txt");
        stockSymbols.forEach(stockSymbol -> {
            stocks.add(new Stock());
            stocks.getLast().setSymbol(stockSymbol);
            stocks.getLast().setButton(new Button());
            stocks.getLast().getButton().setText(stockSymbol);
            stocks.getLast().getButton().setId("stockButton");
            stocks.getLast().getButton().setPrefWidth(stocksVBox.getPrefWidth());
            stocks.getLast().getButton().setOnMouseClicked(event -> {
                tradingVBox.setVisible(true);
                stockLabel.setText(stockSymbol);
                removePlaceholderLabel();
                if (chartsVBox.getChildren().isEmpty()) {
                    VBox vBoxChartWindow = getChartWindowVBox(chart);
                    chartsVBox.getChildren().add(vBoxChartWindow);
                } else {
                    chart.getData().clear();
                }
                try {
                    XYChart.Series<String, Number> series = getChartSeries(stockSymbol, "1min", "390");
                    chart.getData().add(series);
                    refreshStockPrice();
                    refreshChart(stockSymbol, "1min", "390");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        stocks.forEach(stock -> stocksVBox.getChildren().add(stock.getButton()));

        // move to CSS file
        stocksVBox.setAlignment(Pos.TOP_CENTER);

        stockPriceScheduler = Executors.newScheduledThreadPool(1);
        JSONObject jsonObject = new JSONObject();
        realTimeStockPriceRefresher = new RealTimeStockPriceRefresher(httpService, jsonObject);
        realTimeStockPriceRefresher.setLabel(priceLabel);
        chartSchedule = Executors.newScheduledThreadPool(1);
        ObjectMapper objectMapper = new ObjectMapper();
        realTimeChartRefresher = new RealTimeChartRefresher(httpService, chart, objectMapper);

        // Label to display Chart Data
        chartDataLabel = new Label();
        chartDataLabel.setText("Date and price");
        chartDataLabel.setId("chartDataLabel");
        chartDataLabel.setVisible(false);
    }

    @FXML
    void onProfileClicked() throws IOException {
        //Stage profileStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("profile-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ProfileController controller = fxmlLoader.getController();
        controller.getProfileStage().setScene(scene);
        //profileStage.setScene(scene);
        controller.getProfileStage().show();
    }

    @FXML
    void typingIntoSearchTextField() {
        String searchItem = searchTextField.getText();
        removeIrrelevantStocks();
        if (!searchItem.isBlank()) {
            stocks.stream().filter(stock -> stock.getSymbol().contains(searchItem.toUpperCase())).forEach(stock -> stocksVBox.getChildren().add(stock.getButton()));
        } else {
            stocks.forEach(stock -> stocksVBox.getChildren().add(stock.getButton()));
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
        Optional<Stock> stock = stocks.stream().filter(stk -> stk.getSymbol().equals(stockLabel.getText())).findFirst();

        // change
        stock.ifPresent(value -> value.getButton().setStyle("-fx-background-color: #89CFF0"));
    }

    private XYChart.Series<String, Number> getChartSeries(String symbol, String interval, String outputSize) throws IOException, InterruptedException {
        Stock stock = getStock(symbol, interval, outputSize);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = stock.getValues().size() - 1; i >= 0; i--) {
            Number close = BigDecimal.valueOf(Double.parseDouble(stock.getValues().get(i).getClose()));
            XYChart.Data<String, Number> data = new XYChart.Data<>(stock.getValues().get(i).getDatetime(), close);
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
        // create HBox
        HBox hBox = getHBox(chart);

        // create Line
        line = getLine(chart);

        // create Circle
        Circle circle = getCircle();

        // create Pane
        Pane pane = getPane(line, circle);

        // create StackPane
        StackPane stackPane = getStackPane(chart, pane);


        // create HBox to display Chart Data
        HBox chartDataHBox = new HBox(chartDataLabel);
        chartDataHBox.setAlignment(Pos.TOP_CENTER);

        // create VBox
        VBox vBox = new VBox();
        vBox.getChildren().addAll(hBox, chartDataHBox, stackPane);

        // move to css file
        vBox.setStyle("-fx-background-color: transparent");
        vBox.setAlignment(Pos.TOP_CENTER);
        return vBox;
    }

    private HBox getHBox(AreaChart<String, Number> chart) {
        HBox hBox = new HBox();

        // move to css file
        hBox.setStyle("-fx-background-color: transparent");

        List<Button> timeIntervalButtons = new ArrayList<>();
        List<String> timeIntervalsData = fileReader.readFromFile("/Users/ipoce/Desktop/wealthMetamorphosis/TimeIntervals.txt");
        for (String timeInterval : timeIntervalsData) {
            Button timeIntervalButton = new Button();
            timeIntervalButton.setText(timeInterval.split(" ")[0]);
            if (timeIntervalButton.getText().equals("1D")) {
                timeIntervalButton.getStylesheets().add(Objects.requireNonNull(getClass().getResource("timeIntervalButton-whenPressed.css")).toExternalForm());
            }
            timeIntervalButton.setId("timeIntervalButton");
            timeIntervalButtons.add(timeIntervalButton);
            timeIntervalButton.setOnMouseClicked(event -> {
                if (!timeIntervalButton.getStylesheets().contains(Objects.requireNonNull(getClass().getResource("timeIntervalButton-whenPressed.css")).toExternalForm())) {
                    timeIntervalButton.getStylesheets().add(Objects.requireNonNull(getClass().getResource("timeIntervalButton-whenPressed.css")).toExternalForm());
                }
                String outputSize = getOutputSize(timeInterval);
                try {
                    XYChart.Series<String, Number> series = getChartSeries(stockLabel.getText(), timeInterval.split(" ")[1], outputSize);
                    chart.getData().clear();
                    chart.getData().add(series);
                    chartSchedule.shutdown();

                    chartSchedule = Executors.newScheduledThreadPool(1);
                    refreshChart(stockLabel.getText(), timeInterval.split(" ")[1], outputSize);

                    timeIntervalButtons.stream().filter(button -> !button.equals(timeIntervalButton)).forEach(button -> button.getStylesheets().clear());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            hBox.getChildren().add(timeIntervalButton);
            hBox.setAlignment(Pos.CENTER);
        }
        return hBox;
    }

    private String getOutputSize(String timeInterval) {
        DaysCalculator calculator = new DaysCalculator(fileReader);
        int days = calculator.getNumberOfTradingDaysFromBeginOfYearTillNow();
        return timeInterval.split(" ")[0].equals("YTD") ? String.valueOf(days) : timeInterval.split(" ")[2];
    }

    private StackPane getStackPane(AreaChart<String, Number> chart, Pane pane) {
        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-background-color: transparent");
        stackPane.getChildren().add(chart);
        stackPane.getChildren().add(pane);
        stackPane.setOnMouseEntered(event -> {
            chartDataLabel.setVisible(true);
            pane.setVisible(true);
            yMax = yAxis.getDisplayPosition(yAxis.getUpperBound());
            line.setStartY(yMax + yAxis.getBaselineOffset());
            yMin = yAxis.getDisplayPosition(yAxis.getLowerBound());
            line.setEndY(yMin + yAxis.getBaselineOffset());
            chart.getStylesheets().add(Objects.requireNonNull(getClass().getResource("chart-hover.css")).toExternalForm());
        });
        stackPane.setOnMouseExited(event -> {
            chartDataLabel.setVisible(false);
            chart.getStylesheets().clear();
            pane.setVisible(false);
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

                            yPos = chart.getYAxis().getDisplayPosition(data.getYValue()) + 17;

                            // set circle center coordinates
                            circle.setCenterX(event.getX());
                            circle.setCenterY(yPos);
                            chartDataLabel.setText(data.getXValue() + " | " + data.getYValue() + "$");
                        }
                    }
                }
            }
        });
        return pane;
    }

    private Circle getCircle() {
        Circle circle = new Circle();
        circle.setFill(Paint.valueOf(String.valueOf(Color.WHITE)));
        circle.setRadius(5);
        return circle;
    }

    private Line getLine(AreaChart<String, Number> chart) {
        line = new Line();
        line.setFill(Paint.valueOf(String.valueOf(Color.WHITE)));
        line.setStartX(0);
        line.setEndX(0);
        line.setStrokeWidth(2);
        line.setStroke(Paint.valueOf(Color.WHITE.toString()));
        return line;
    }

    private void removePlaceholderLabel() {
        if (chartsVBox.getChildren().get(0).equals(nothingToDisplayLabel)) {
            chartsVBox.getChildren().remove(chartsVBox.getChildren().getFirst());
        }
    }

    private void refreshStockPrice() {
        realTimeStockPriceRefresher.setStockSymbol(stockLabel.getText());
        stockPriceScheduler.scheduleAtFixedRate(realTimeStockPriceRefresher, 0, 30, TimeUnit.SECONDS);
    }

    private void refreshChart(String button, String interval, String number) {
        realTimeChartRefresher.setParameters(button, interval, number);
        chartSchedule.scheduleAtFixedRate(realTimeChartRefresher, 0, 30, TimeUnit.SECONDS);
    }

    private void setUpChart() {
        xAxis = new CategoryAxis();
        xAxis.setAutoRanging(true);
        xAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickLabelGap(0);
        xAxis.setStartMargin(0);
        xAxis.setGapStartAndEnd(false);
        xAxis.setTickLabelFill(Paint.valueOf(Color.WHITE.toString()));

        yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        yAxis.setTickLabelFont(Font.font("Copperplate"));
        yAxis.setTickLabelGap(0);
        yAxis.setPrefWidth(30);
        yAxis.setForceZeroInRange(false);
        yAxis.setTickLabelFill(Paint.valueOf(Color.WHITE.toString()));

        chart = new AreaChart<>(xAxis, yAxis);
        chart.setId("chart");
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setPrefHeight(857);
    }

    @FXML
    void onMouseEntered() {
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    @FXML
    void onMouseExited() {
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}