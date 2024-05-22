package app.wealthmetamorphosis;

import app.wealthmetamorphosis.logic.FileReader;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class MainController {
    // main pane
    @FXML
    private BorderPane borderPane;

    // stocks Nodes
    @FXML
    private VBox stocksVBox;
    @FXML
    private TextField searchTextField;

    private Stage stage;
    private List<String> stockSymbols;
    private int counter = 0;

    @FXML
    void initialize() {
        FileReader fileReader = new FileReader();
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

    private List<String> getRelevantStockSymbols(String searchItem) {
        return stockSymbols.stream()
                .filter(symbol -> symbol.contains(searchItem.toUpperCase()))
                .toList();
    }

    private void addRelevantStocks(List<String> stockSymbols) {
        for (String stockSymbol : stockSymbols) {
            Button button = new Button();
            button.setText(stockSymbol);
            button.setPrefWidth(stocksVBox.getPrefWidth());
            /* button.setOnMouseClicked(event -> {
                chartsVBox.getChildren().remove(chartsVBox.getChildren().getFirst());
                if (chartsVBox.getChildren().isEmpty()) {
                    VBox vBoxChartWindow = getVBoxChartWindow(button);
                    chartsVBox.getChildren().add(vBoxChartWindow);
                } else {
                    chartsVBox.getChildren().remove(chartsVBox.getChildren().size() - 1);
                    VBox vBoxChartWindow = getVBoxChartWindow(button);
                    chartsVBox.getChildren().add(vBoxChartWindow);
                }
            }); */
            stocksVBox.getChildren().add(button);
        }

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