package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.Main;
import app.wealthmetamorphosis.data.stock.Stock;
import app.wealthmetamorphosis.logic.calculator.DaysCalculator;
import app.wealthmetamorphosis.logic.file.FileReader;
import app.wealthmetamorphosis.logic.refresher.ChartRefresher;
import app.wealthmetamorphosis.logic.refresher.StockPriceRefresher;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.EventHandler;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChartService {

    private AreaChart<String, Number> chart;
    private ScheduledExecutorService stockPriceScheduler;
    private ScheduledExecutorService chartScheduler;
    private final StockPriceRefresher stockPriceRefresher;
    private final FileReader fileReader;
    private final Label priceLabel;
    private final Label stockLabel;
    private Label chartDataLabel;
    private Line line;
    private NumberAxis yAxis;
    private CategoryAxis xAxis;
    private Button activeTimeIntervalButton;
    private HBox timeIntervalsHBox;
    private final HttpService httpService;
    private final VBox placeholderVBox;
    private final VBox chartsVBox;

    public ChartService(ScheduledExecutorService chartScheduler, StockPriceRefresher stockPriceRefresher, FileReader fileReader, Label priceLabel, Label stockLabel, HttpService httpService, VBox placeholderVBox, VBox chartsVBox, Label chartDataLabel) {
        this.chartScheduler = chartScheduler;
        this.stockPriceRefresher = stockPriceRefresher;
        this.fileReader = fileReader;
        this.priceLabel = priceLabel;
        this.stockLabel = stockLabel;
        this.httpService = httpService;
        this.placeholderVBox = placeholderVBox;
        this.chartsVBox = chartsVBox;
        this.chartDataLabel = chartDataLabel;
    }

    public void fillChartWithData(String stockSymbol) {
        arrangeChartWindow();
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

    private void arrangeChartWindow() {
        placeholderVBox.toBack();
        if (chartsVBox.getChildren().isEmpty()) {
            VBox vBoxChartWindow = getChartWindowVBox(chart);
            chartsVBox.getChildren().add(vBoxChartWindow);
        } else {
            chart.getData().clear();
        }
    }

    private VBox getChartWindowVBox(AreaChart<String, Number> chart) {
        HBox hBox = getTimeIntervalsHBox();
        getChartLine();
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

    private void refreshStockPrice() {
        if (stockPriceScheduler != null) {
            stockPriceScheduler.shutdown();
        }
        stockPriceScheduler = Executors.newScheduledThreadPool(1);
        stockPriceRefresher.setPriceLabel(priceLabel);
        stockPriceRefresher.setStockSymbol(stockLabel.getText());
        stockPriceScheduler.scheduleAtFixedRate(stockPriceRefresher, 0, 30, TimeUnit.SECONDS);
    }

    private void refreshChart(String stockSymbol, String interval, String outputSize) {
        if (chartScheduler != null) {
            chartScheduler.shutdown();
        }
        chartScheduler = (Executors.newScheduledThreadPool(1));
        ChartRefresher chartRefresher = new ChartRefresher(httpService, chart, new ObjectMapper());
        chartRefresher.setStockSymbol(stockSymbol);
        chartRefresher.setInterval(interval);
        chartRefresher.setOutputSize(outputSize);
        chartScheduler.scheduleAtFixedRate(chartRefresher, 0, 30, TimeUnit.SECONDS);
    }

    private void set1DTimeIntervalButtonToActive() {
        timeIntervalsHBox.getChildren().stream().map(node -> (Button) node).forEach(bt -> bt.getStylesheets().clear());
        Button timeIntervalButton = (Button) timeIntervalsHBox.getChildren().getFirst();
        addStyleSheetToButton(timeIntervalButton);
        activeTimeIntervalButton = timeIntervalButton;
    }

    private void addStyleSheetToButton(Button button) {
        button.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/app/wealthmetamorphosis/css/pressedButton.css")).toExternalForm());
    }

    public void getChartDataLabel() {
        chartDataLabel = new Label();
        chartDataLabel.setId("chartDataLabel");
        chartDataLabel.setVisible(false);
    }

    private HBox getTimeIntervalsHBox() {
        activeTimeIntervalButton = new Button();
        timeIntervalsHBox = new HBox();
        timeIntervalsHBox.setId("timeIntervalsHBox");
        List<Button> timeIntervalButtons = new ArrayList<>();
        List<String> timeIntervalsData = fileReader.readFromFile(
                Objects.requireNonNull(Main.class.getResource("/app/wealthMetamorphosis/files/TimeIntervals.txt")).getPath());
        for (String timeInterval : timeIntervalsData) {
            Button timeIntervalButton = getTimeIntervalButton(timeInterval, timeIntervalButtons);
            timeIntervalsHBox.getChildren().add(timeIntervalButton);
        }
        return timeIntervalsHBox;
    }

    private Button getTimeIntervalButton(String timeInterval, List<Button> timeIntervalButtons) {
        Button timeIntervalButton = new Button();
        timeIntervalButton.setId("timeIntervalButton");
        timeIntervalButton.setText(timeInterval.split(" ")[0]);
        timeIntervalButton.setOnMouseClicked(event -> {
            if (!activeTimeIntervalButton.equals(timeIntervalButton)) {
                activeTimeIntervalButton = timeIntervalButton;
                if (timeIntervalButton.getStylesheets().isEmpty()) {
                    addStyleSheetToButton(timeIntervalButton);
                }
                String outputSize = getOutputSize(timeInterval);
                try {
                    XYChart.Series<String, Number> series = getChartSeries(stockLabel.getText(), timeInterval.split(" ")[1], outputSize);
                    chart.getData().clear();
                    chart.getData().add(series);

                    chartScheduler.shutdown();
                    chartScheduler = Executors.newScheduledThreadPool(1);
                    refreshChart(stockLabel.getText(), timeInterval.split(" ")[1], outputSize);

                    timeIntervalButtons.stream().filter(bt -> !bt.equals(timeIntervalButton)).forEach(bt -> bt.getStylesheets().clear());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        timeIntervalButtons.add(timeIntervalButton);
        return timeIntervalButton;
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

            double yMax = yAxis.getDisplayPosition(yAxis.getUpperBound());
            line.setStartY(yMax + yAxis.getBaselineOffset());
            double yMin = yAxis.getDisplayPosition(yAxis.getLowerBound());
            line.setEndY(yMin + yAxis.getBaselineOffset());
            chart.getStylesheets().add(Objects.requireNonNull(getClass().getResource(
                    "/app/wealthmetamorphosis/css/chart-hover-layout.css")).toExternalForm());
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

                            line.setStartX(event.getX());
                            line.setEndX(event.getX());

                            yPos = chart.getYAxis().getDisplayPosition(data.getYValue()) + 17;
                            circle.setCenterX(event.getX());
                            circle.setCenterY(yPos);

                            chartDataLabel.setText(data.getXValue() + " | " + Math.round(data.getYValue().doubleValue() * 100.0) / 100.0 + "$");
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

    private void getChartLine() {
        line = new Line();
        line.setId("chartLine");
        line.setStartX(0);
        line.setEndX(0);
        //return line;
    }

    public void getChart() {
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
}