package app.wealthmetamorphosis;

import app.wealthmetamorphosis.data.Account;
import app.wealthmetamorphosis.data.Stock;
import app.wealthmetamorphosis.logic.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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


    @FXML
    void initialize() {
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

    private VBox getChartVBoxWindow(AreaChart<String, Number> chart) {
        HBox hBox = new HBox();
        List<String> timeIntervalsData = fileReader.readFromFile("/Users/ipoce/Desktop/wealthMetamorphosis/TimeIntervals.txt");
        for (String timeInterval : timeIntervalsData) {
            Button tmIntButton = new Button();
            tmIntButton.setText(timeInterval.split(" ")[0]);
            tmIntButton.setOnMouseClicked(event -> {
                DaysCalculator calculator = new DaysCalculator(fileReader);
                int days = calculator.getNumberOfTradingDaysFromBeginOfYearTillNow();
                chart.getData().clear();
                String outputSize = timeInterval.split(" ")[0].equals("YTD") ? String.valueOf(days) : timeInterval.split(" ")[2];
                XYChart.Series<String, Number> series;
                try {
                    series = getChartSeries(stockLabel.getText(), timeInterval.split(" ")[1], outputSize);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                chart.getData().add(series);
            });
            hBox.getChildren().add(tmIntButton);
        }
        VBox vBox = new VBox();
        vBox.getChildren().addAll(hBox, chart);
        return vBox;
    }

    private List<String> getRelevantStockSymbols(String searchItem) {
        return stockSymbols.stream()
                .filter(symbol -> symbol.contains(searchItem.toUpperCase()))
                .toList();
    }

    private void addRelevantStocks(List<String> stockSymbols) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        RefreshRealTimeStockPrice refreshRealTimeStockPrice = new RefreshRealTimeStockPrice(httpService, priceLabel);
        for (String stockSymbol : stockSymbols) {
            Button button = new Button();
            button.setText(stockSymbol);
            button.setPrefWidth(stocksVBox.getPrefWidth());
            button.setOnMouseClicked(event -> {
                stockLabel.setText(stockSymbol);
                if (chartsVBox.getChildren().get(0).equals(nothingToDisplayLabel)) {
                    chartsVBox.getChildren().remove(chartsVBox.getChildren().getFirst());
                }
                if (chartsVBox.getChildren().isEmpty()) {
                    setUpChart();
                    VBox vBoxChartWindow = getChartVBoxWindow(chart);
                    chartsVBox.getChildren().add(vBoxChartWindow);
                } else {
                    chart.getData().clear();
                }
                XYChart.Series<String, Number> series;
                try {
                    series = getChartSeries(button.getText(), "1min", "390");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                chart.getData().add(series);
                refreshRealTimeStockPrice.setStockSymbol(stockLabel.getText());
                scheduler.scheduleAtFixedRate(refreshRealTimeStockPrice, 0, 30, TimeUnit.SECONDS);
            });
            stocksVBox.getChildren().add(button);
        }
    }

    private void setUpChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setAutoRanging(true);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        chart = new AreaChart<>(xAxis, yAxis);
        yAxis.setForceZeroInRange(false);
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