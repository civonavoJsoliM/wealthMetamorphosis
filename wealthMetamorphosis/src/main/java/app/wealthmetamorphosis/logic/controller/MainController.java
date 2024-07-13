package app.wealthmetamorphosis.logic.controller;

import app.wealthmetamorphosis.Main;
import app.wealthmetamorphosis.data.*;
import app.wealthmetamorphosis.data.parameters.StockPriceRefresherParameters;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.data.stock.Stock;
import app.wealthmetamorphosis.logic.colorChanger.ColorChanger;
import app.wealthmetamorphosis.logic.colorChanger.GreenColorChanger;
import app.wealthmetamorphosis.logic.colorChanger.RedColorChanger;
import app.wealthmetamorphosis.logic.file.FileReader;
import app.wealthmetamorphosis.logic.refresher.StockPriceRefresher;
import app.wealthmetamorphosis.logic.service.*;
import app.wealthmetamorphosis.logic.verifier.Checker;
import app.wealthmetamorphosis.logic.verifier.Validator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.UnaryOperator;

public class MainController {

    @FXML
    private VBox stocksVBox;
    @FXML
    private TextField searchTextField;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox chartsVBox;
    @FXML
    private VBox placeholderVBox;
    @FXML
    private Label stockLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private TextField buySharesTextField;
    @FXML
    private TextField sellSharesTextField;
    @FXML
    private VBox stockSymbolAndPriceVBox;
    @FXML
    private VBox sellVBox;
    @FXML
    private VBox buyVBox;
    @FXML
    private Label ownedSharesLabel;
    @FXML
    private Label invalidInputBuyLabel;
    @FXML
    private Label invalidInputSellLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private HBox balanceHBox;
    @FXML
    private Label totalCostLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label profitLossLabel;
    @FXML
    private Button buyButton;
    @FXML
    private Button sellButton;
    @FXML
    private Button sellAllButton;

    private Stage stage;
    private List<Stock> stocks;
    private TradingService tradingService;
    private Checker checker;
    private Validator validator;
    private HttpService httpService;
    private FileReader fileReader;
    private Button activeStockButton;
    private List<HBox> stocksHBoxes;
    private OwnedStockService ownedStockService;
    private ChartService chartService;
    private StockPriceRefresher stockPriceRefresher;


    @FXML
    void initialize() throws URISyntaxException {
        stocks = new ArrayList<>();
        ownedStockService = new OwnedStockService();
        int counter = 0;
        fileReader = new FileReader();
        httpService = new HttpService(fileReader, counter);
        tradingService = new TradingService(priceLabel, stockLabel, buySharesTextField, sellSharesTextField, ownedStockService);
        checker = new Checker(ownedStockService);
        validator = new Validator();

        initializeChart();

        stocksHBoxes = new ArrayList<>();

        URI path = Objects.requireNonNull(Main.class.getResource("/app/wealthMetamorphosis/files/StockSymbols.txt")).toURI();
        List<String> stockSymbols = fileReader.readFromFile(path);


        stockSymbols.forEach(stockSymbol -> {
            Button stockButton = getStockButton(stockSymbol);

            Circle ownedStockCircle = getOwnedStockCircle();

            HBox stockHBox = getStockHBox(ownedStockCircle, stockButton);
            stocksHBoxes.add(stockHBox);
        });
        stocksVBox.getChildren().addAll(stocksHBoxes);
        stocksVBox.setId("stocksVBox");

        chartService.getChartDataLabel();
    }

    private void initializeChart() {
        StockPriceRefresherParameters stockPriceRefresherParameters = new StockPriceRefresherParameters(httpService, ownedStockService,
                sellSharesTextField, buySharesTextField, profitLossLabel, totalCostLabel, totalLabel);

        stockPriceRefresher = new StockPriceRefresher(stockPriceRefresherParameters, buyButton, sellButton, sellAllButton);

        activeStockButton = new Button();
        Label chartDataLabel = new Label();

        chartService = new ChartService(stockPriceRefresher, fileReader, priceLabel, stockLabel, httpService,
                placeholderVBox, chartsVBox, chartDataLabel);

        chartService.getChart();
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


            if (!activeStockButton.equals(stockButton)) {
                disableTradingButtons();

                activeStockButton = stockButton;
                stockLabel.setText(stockSymbol);

                setStockSymbolAndPriceVBoxVisibility();

                priceLabel.setText(" ");

                chartService.fillChartWithData(stockSymbol);
                setTradingVBoxVisibility(stockSymbol);
                stylePressedOrFocusedStockButtons(stockButton);
                styleNotPressedOrUnfocusedStockButtons(stockButton);
                clearTextFieldsAndWarningLabels();

            }

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

    private void disableTradingButtons() {

        buyButton.setDisable(true);
        sellButton.setDisable(true);
        sellAllButton.setDisable(true);

    }

    private void setStockSymbolAndPriceVBoxVisibility() {
        if (!stockSymbolAndPriceVBox.isVisible()) {
            stockSymbolAndPriceVBox.setVisible(true);
        }
    }

    private void clearTextFieldsAndWarningLabels() {
        buySharesTextField.clear();
        sellSharesTextField.clear();
        invalidInputBuyLabel.setVisible(false);
        invalidInputSellLabel.setVisible(false);
        totalCostLabel.setText("0$");
        totalLabel.setText("0$");
        profitLossLabel.setText("");
    }

    private void stylePressedOrFocusedStockButtons(Button stockButton) {
        stocksHBoxes.stream().filter(hBox -> hBox.getChildren().getLast().equals(stockButton)).forEach(hBox -> {
            hBox.setStyle("-fx-background-color: white");
            hBox.getChildren().getFirst().setStyle("-fx-fill: #212324; -fx-stroke: #212324;");
            hBox.getChildren().getLast().setStyle("-fx-text-fill: #212324");
        });
    }

    private void styleNotPressedOrUnfocusedStockButtons(Button stockButton) {
        stocksHBoxes.stream().filter(hBox -> !hBox.getChildren().getLast().equals(stockButton)).forEach(hBox -> {
            hBox.setStyle("-fx-background-color: #212324");
            hBox.getChildren().getFirst().setStyle("-fx-fill: white; -fx-stroke: white;");
            hBox.getChildren().getLast().setStyle("-fx-text-fill: white");
        });
    }

    private void setTradingVBoxVisibility(String stockSymbol) {
        if (UserSingleton.getInstance() != null) {
            buyVBox.setVisible(true);
            setSellVBoxVisibility(stockSymbol);
        }
    }

    private void setSellVBoxVisibility(String stockSymbol) {
        double ownedShares = ownedStockService.getSharesFromCertainStock(UserSingleton.getInstance(), stockSymbol);
        if (ownedShares > 0) {
            sellVBox.setVisible(true);
            ownedSharesLabel.setText(String.valueOf(ownedShares));
        } else {
            sellVBox.setVisible(false);
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
        Map<String, Double> map = ownedStockService.getAllOwnedStockShares(UserSingleton.getInstance());

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

    @FXML
    void onProfileClicked() throws IOException {
        FXMLLoader fxmlLoader;
        if (UserSingleton.getInstance() == null) {
            fxmlLoader = new FXMLLoader(Main.class.getResource("/app/wealthmetamorphosis/view/login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            LoginController controller = fxmlLoader.getController();
            setUpLoginScene(controller, scene);
            if (UserSingleton.getInstance() != null) {
                setOwnedStockCircleVisible();
                setBalanceText();
                balanceHBox.setVisible(true);
                if (stocks.stream().map(Stock::getButton).anyMatch(button -> button.equals(activeStockButton))) {
                    buyVBox.setVisible(true);
                    setSellVBoxVisibility(activeStockButton.getText());
                }
                clearTextFieldsAndWarningLabels();
            }
        } else {
            fxmlLoader = new FXMLLoader(Main.class.getResource("/app/wealthmetamorphosis/view/profile-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            ProfileController controller = fxmlLoader.getController();
            Stage profileStage = controller.getProfileStage();
            setUpProfileStage(profileStage, scene);
            profileStage.showAndWait();
            if (UserSingleton.getInstance() == null) {
                setOwnedStockCircleNotVisible();
                buyVBox.setVisible(false);
                balanceHBox.setVisible(false);
                sellVBox.setVisible(false);
            }
        }
    }

    private void setBalanceText() {
        double balance = Math.round(UserSingleton.getInstance().getBalance() * 100.0) / 100.0;
        balanceLabel.setText(balance + "$");
    }

    private void setUpLoginScene(LoginController controller, Scene scene) {
        controller.getLoginStage().setScene(scene);
        controller.getLoginStage().initOwner(stage);
        controller.getLoginStage().initModality(Modality.WINDOW_MODAL);
        controller.getLoginStage().initStyle(StageStyle.UTILITY);
        controller.getLoginStage().showAndWait();
    }

    private void setOwnedStockCircleNotVisible() {
        for (HBox hBox : stocksHBoxes) {
            Circle circle = (Circle) hBox.getChildren().getFirst();
            circle.setVisible(false);
        }
    }


    private void setUpProfileStage(Stage profileStage, Scene scene) {
        profileStage.setScene(scene);
        profileStage.initOwner(stage);
        profileStage.initModality(Modality.WINDOW_MODAL);
        profileStage.initStyle(StageStyle.UNIFIED);
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
    void enteringSharesToBuy() {
        UnaryOperator<TextFormatter.Change> unaryOperator = validator.validateIfInputNumber();
        TextFormatter<String> formatter = new TextFormatter<>(unaryOperator);
        buySharesTextField.setTextFormatter(formatter);
    }

    @FXML
    void enteredSharesToBuy() {
        if (!buySharesTextField.getText().isBlank() && !priceLabel.getText().isBlank()) {
            double price = getPrice();
            double shares = Double.parseDouble(buySharesTextField.getText());

            double totalCost = Math.round(price * shares * 100.0) / 100.0;
            totalCostLabel.setText(totalCost + "$");
        } else {
            totalCostLabel.setText("0$");
        }
    }

    @FXML
    void onBuyButtonClicked() {
        double price = getPrice();
        if (checker.isNumberBiggerThenZero(buySharesTextField) &&
                checker.isBalanceEnough(price * Double.parseDouble(buySharesTextField.getText()))) {

            tradingService.placeOrder(OrderType.BUY);

            changeVisibilityOfStockDotAndSellVBox();

            setBalanceText();

            clearTextFieldsAndWarningLabels();

        } else {
            invalidInputBuyLabel.setVisible(true);
        }
    }

    private void changeVisibilityOfStockDotAndSellVBox() {
        Optional<Stock> chosenStock = stocks.stream().filter(stk -> stk.getSymbol().equals(stockLabel.getText())).findFirst();
        double ownedStockShares = ownedStockService.getSharesFromCertainStock(UserSingleton.getInstance(), stockLabel.getText());
        chosenStock.ifPresent(value -> setSellVBoxAndStockDotVisibility(value, ownedStockShares));
    }

    @FXML
    void enteringSharesToSell() {
        UnaryOperator<TextFormatter.Change> unaryOperator = validator.validateIfInputNumber();
        TextFormatter<String> formatter = new TextFormatter<>(unaryOperator);
        sellSharesTextField.setTextFormatter(formatter);
    }

    @FXML
    void enteredSharesToSell() {
        if (!sellSharesTextField.getText().isBlank() && !priceLabel.getText().isBlank()) {
            double price = getPrice();
            double total = Math.round(price * Double.parseDouble(sellSharesTextField.getText()) * 100.0) / 100.0;
            totalLabel.setText(total + "$");

            double value = getProfitLossValue();
            setProfitLossLabel(value);
        } else {
            clearTextFieldsAndWarningLabels();
        }
    }

    private double getProfitLossValue() {
        User user = UserSingleton.getInstance();
        String stockSymbol = stockLabel.getText();
        double price = getPrice();

        double ownedShares = ownedStockService.getSharesFromCertainStock(user, stockSymbol);
        double totalInvestedInStock = ownedStockService.getInvestedInStock(user, stockSymbol);
        double percentage = ownedStockService.getPercentage(totalInvestedInStock, price, ownedShares);
        double sharesToSell = Double.parseDouble(sellSharesTextField.getText());
        double amountToSell = price * sharesToSell;

        return Math.round((amountToSell - (amountToSell / percentage)) * 100.0) / 100.0;
    }

    private double getPrice() {
        return Double.parseDouble(priceLabel.getText().substring(0, priceLabel.getText().length() - 1));
    }

    private void setProfitLossLabel(double value) {
        ColorChanger redColorChanger = new RedColorChanger(0, value, profitLossLabel);
        ColorChanger greenColorChanger = new GreenColorChanger(0, value, profitLossLabel);
        List<ColorChanger> colorChangers = new ArrayList<>(List.of(redColorChanger, greenColorChanger));

        if (value != 0) {
            for (ColorChanger colorChanger : colorChangers) {
                colorChanger.change();
                profitLossLabel.setText(value + "$");
            }
        }
    }

    @FXML
    void onSellButtonClicked() {
        if (checker.isNumberBiggerThenZero(sellSharesTextField) &&
                checker.areEnoughStockSharesToBeSold(UserSingleton.getInstance(), stockLabel.getText(),
                        Double.parseDouble(sellSharesTextField.getText()))) {

            tradingService.placeOrder(OrderType.SELL);

            changeVisibilityOfStockDotAndSellVBox();

            setBalanceText();

            clearTextFieldsAndWarningLabels();
        } else {
            invalidInputSellLabel.setVisible(true);
        }
    }

    @FXML
    void onSellAllButtonClicked() {
        AlertService sellAllAlert = new AlertService(stockLabel, ownedStockService, getPrice());
        Alert alert = getAlert(sellAllAlert);
        ButtonType result = alert.getResult();
        if (result.equals(ButtonType.OK)) {

            tradingService.placeOrder(OrderType.SELL_ALL);
            changeVisibilityOfStockDotAndSellVBox();

            setBalanceText();

            clearTextFieldsAndWarningLabels();

        }
    }

    private Alert getAlert(AlertService sellAllAlert) {
        Alert alert = sellAllAlert.getAlert();
        stockPriceRefresher.setAlert(alert);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(stage);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
        return alert;
    }

    private void setSellVBoxAndStockDotVisibility(Stock stock, double ownedShares) {
        for (Node vBoxChildren : stocksVBox.getChildren()) {
            HBox hBox = (HBox) vBoxChildren;
            for (Node hBoxChildren : hBox.getChildren()) {
                Circle circle = (Circle) hBox.getChildren().getFirst();
                if (hBoxChildren.equals(stock.getButton())) {
                    changeVisibilityOfOwnedSharesCircle(ownedShares, circle);
                }
            }
        }
    }

    private void changeVisibilityOfOwnedSharesCircle(double ownedShares, Circle circle) {
        if (ownedShares <= 0) {
            sellVBox.setVisible(false);
            circle.setVisible(false);
        } else {
            ownedSharesLabel.setText(String.valueOf(ownedShares));
            sellVBox.setVisible(true);
            circle.setVisible(true);
        }
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