package app.wealthmetamorphosis;

import app.wealthmetamorphosis.data.Account;
import app.wealthmetamorphosis.data.Stock;
import app.wealthmetamorphosis.logic.*;
import app.wealthmetamorphosis.logic.refresher.RealTimeChartRefresher;
import app.wealthmetamorphosis.logic.refresher.RealTimeStockPriceRefresher;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainController {
    // main pane
    @FXML
    private BorderPane borderPane;

    // stocks Nodes
    @FXML
    private VBox stocksVBox;
    @FXML
    private TextField searchTextField;

    // chart Nodes
    @FXML
    private VBox chartsVBox;
    @FXML
    private Label nothingToDisplayLabel;

    // trading Nodes
    @FXML
    private Label stockLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private TextField amountTextField;
    @FXML
    private Button buyButton;
    @FXML
    private Button sellButton;

    private Stage stage;
    private List<String> stockSymbols;
    private int counter;
    private Account account;
    private TradingService tradingService;
    private Checker checker;
    private HttpService httpService;
    private FileReader fileReader;
    private AreaChart<String, Number> chart;
    ScheduledExecutorService stockPriceScheduler;
    RealTimeStockPriceRefresher realTimeStockPriceRefresher;
    ScheduledExecutorService chartSchedule;
    RealTimeChartRefresher realTimeChartRefresher;


    @FXML
    void initialize() {
        setUpChart();
        counter = 0;
        fileReader = new FileReader();
        httpService = new HttpService(fileReader, counter);
        account = new Account("Max", 1000, new HashMap<>());
        checker = new Checker();
        tradingService = new TradingService(checker, account, priceLabel, stockLabel, amountTextField);
        stockSymbols = fileReader.readFromFile("/Users/ipoce/Desktop/wealthMetamorphosis/StockSymbols.txt");
        addRelevantStocks(stockSymbols);

        // move to CSS file
        stocksVBox.setAlignment(Pos.CENTER);

        stockPriceScheduler = Executors.newScheduledThreadPool(1);
        JSONObject jsonObject = new JSONObject();
        realTimeStockPriceRefresher = new RealTimeStockPriceRefresher(httpService, priceLabel, jsonObject);
        chartSchedule = Executors.newScheduledThreadPool(1);
        ObjectMapper objectMapper = new ObjectMapper();
        realTimeChartRefresher = new RealTimeChartRefresher(httpService, chart, objectMapper);
    }

    @FXML
    void typingIntoSearchTextField() {
        String searchItem = searchTextField.getText();
        removeIrrelevantStocks();
        if (!searchItem.isBlank()) {
            List<String> relevantStockSymbols = getRelevantStockSymbols(searchItem);
            addRelevantStocks(relevantStockSymbols);
        } else {
            addRelevantStocks(stockSymbols);
        }
    }

    @FXML
    void onBuyClicked() {
        tradingService.buyOrder();
        System.out.println(account.getBalance());
        account.getPortfolio().forEach((str, dou) -> System.out.println(str + " " + dou));
    }

    @FXML
    void onSellClicked() {
        tradingService.sellOrder();
        System.out.println(account.getBalance());
        account.getPortfolio().forEach((str, dou) -> System.out.println(str + " " + dou));
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
        HttpResponse<String> response = httpService.getHttpResponse(symbol, interval, outputSize);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response.body(), Stock.class);
    }

    private VBox getChartWindowVBox(AreaChart<String, Number> chart) {
        // create HBox
        HBox hBox = getHBox(chart);

        // create Line
        Line line = getLine();

        // create Circle
        Circle circle = getCircle();

        // create Pane
        Pane pane = getPane(line, circle);

        // create StackPane
        StackPane stackPane = getStackPane(chart, pane);

        // create VBox
        VBox vBox = new VBox();
        vBox.getChildren().addAll(hBox, stackPane);

        // move to css file
        vBox.setStyle("-fx-background-color: transparent");
        vBox.setAlignment(Pos.TOP_CENTER);
        return vBox;
    }

    private HBox getHBox(AreaChart<String, Number> chart) {
        HBox hBox = new HBox();

        // move to css file
        hBox.setStyle("-fx-background-color: transparent");

        List<String> timeIntervalsData = fileReader.readFromFile("/Users/ipoce/Desktop/wealthMetamorphosis/TimeIntervals.txt");
        for (String timeInterval : timeIntervalsData) {
            Button tmIntButton = new Button();
            tmIntButton.setText(timeInterval.split(" ")[0]);
            tmIntButton.setOnMouseClicked(event -> {
                String outputSize = getOutputSize(timeInterval);
                try {
                    XYChart.Series<String, Number> series = getChartSeries(stockLabel.getText(), timeInterval.split(" ")[1], outputSize);
                    chart.getData().clear();
                    chart.getData().add(series);
                    chartSchedule.shutdown();

                    chartSchedule = Executors.newScheduledThreadPool(1);
                    refreshChart(stockLabel.getText(), timeInterval.split(" ")[1], outputSize);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            hBox.getChildren().add(tmIntButton);
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
                        }
                    }
                }
            }
        });
        return pane;
    }

    private Circle getCircle() {
        Circle circle = new Circle();
        circle.setFill(Paint.valueOf(String.valueOf(Color.BLACK)));
        circle.setRadius(2);
        return circle;
    }

    private Line getLine() {
        Line line = new Line();
        line.setStartX(0);
        line.setEndX(0);
        line.setStartY(17);
        line.setEndY(250);
        line.setStrokeWidth(1);
        return line;
    }

    private List<String> getRelevantStockSymbols(String searchItem) {
        return stockSymbols.stream().filter(symbol -> symbol.contains(searchItem.toUpperCase())).toList();
    }

    private void addRelevantStocks(List<String> stockSymbols) {
        /*stockPriceScheduler = Executors.newScheduledThreadPool(1);
        realTimeStockPriceRefresher = new RealTimeStockPriceRefresher(httpService, priceLabel);
        chartSchedule = Executors.newScheduledThreadPool(1);
        realTimeChartRefresher = new RealTimeChartRefresher(httpService, chart); */

        for (String stockSymbol : stockSymbols) {
            Button button = new Button();
            button.setText(stockSymbol);
            button.setPrefWidth(stocksVBox.getPrefWidth());
            button.setOnMouseClicked(event -> {
                stockLabel.setText(stockSymbol);
                removePlaceholderLabel();
                if (chartsVBox.getChildren().isEmpty()) {
                    VBox vBoxChartWindow = getChartWindowVBox(chart);
                    chartsVBox.getChildren().add(vBoxChartWindow);
                } else {
                    chart.getData().clear();
                }
                try {
                    XYChart.Series<String, Number> series = getChartSeries(button.getText(), "1min", "390");
                    chart.getData().add(series);
                    refreshStockPrice();
                    refreshChart(button.getText(), "1min", "390");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            stocksVBox.getChildren().add(button);
        }
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
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setAutoRanging(true);
        xAxis.setTickLabelGap(0);
        xAxis.setStartMargin(0);
        xAxis.setGapStartAndEnd(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        yAxis.setTickLabelGap(0);
        yAxis.setPrefWidth(30);
        chart = new AreaChart<>(xAxis, yAxis);
        yAxis.setForceZeroInRange(false);
        chart.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 2px;");
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
    }

    private void removeIrrelevantStocks() {
        while (stocksVBox.getChildren().size() != 1) {
            stocksVBox.getChildren().remove(stocksVBox.getChildren().size() - 1);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}