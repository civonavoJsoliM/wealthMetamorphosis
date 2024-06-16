package app.wealthmetamorphosis.logic.refresher;

import app.wealthmetamorphosis.data.stock.Stock;
import app.wealthmetamorphosis.logic.service.HttpService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;

public class RealTimeChartRefresher implements Runnable {
    private ObjectMapper objectMapper;
    private final HttpService httpService;
    private String stockSymbol;
    private String interval;
    private String outputSize;
    private final AreaChart<String, Number> chart;
    private int i = 0;

    public RealTimeChartRefresher(HttpService httpService, AreaChart<String, Number> chart, ObjectMapper objectMapper) {
        this.httpService = httpService;
        this.chart = chart;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        try {
            HttpResponse<String> response = httpService.getStock(stockSymbol, interval, outputSize);
            Stock stock = getStock(response);
            XYChart.Series<String, Number> series = getSeries(stock);
            setNewChartData(series);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private XYChart.Series<String, Number> getSeries(Stock stock) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = stock.getValues().size() - 1; i >= 0; i--) {
            Number close = BigDecimal.valueOf(Double.parseDouble(stock.getValues().get(i).getClose()));
            XYChart.Data<String, Number> data = new XYChart.Data<>(stock.getValues().get(i).getDatetime(), close);
            series.getData().add(data);
        }
        return series;
    }

    private Stock getStock(HttpResponse<String> response) throws JsonProcessingException {
        objectMapper = new ObjectMapper();
        return objectMapper.readValue(response.body(), Stock.class);
    }

    private void setNewChartData(XYChart.Series<String, Number> series) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                chart.getData().clear();
                chart.getData().add(series);
                System.out.println("Refresh chart of: " + stockSymbol);
            }
        });
    }

    public void setParameters(String stockSymbol, String interval, String outputSize) {
        this.stockSymbol = stockSymbol;
        this.interval = interval;
        this.outputSize = outputSize;
    }
}