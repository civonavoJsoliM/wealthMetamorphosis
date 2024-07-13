package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.Main;
import app.wealthmetamorphosis.data.User;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class AlertService {
    private final Label stockLabel;
    private final OwnedStockService ownedStockService;
    private final double price;

    public AlertService(Label stockLabel, OwnedStockService ownedStockService, double price) {
        this.stockLabel = stockLabel;
        this.ownedStockService = ownedStockService;
        this.price = price;
    }

    public Alert getAlert() {
        String stockSymbol = stockLabel.getText();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(
                Main.class.getResource("/app/wealthmetamorphosis/css/main-scene-layout.css")).toExternalForm());
        alert.setHeaderText("SELLING ALL '" + stockSymbol + "' SHARES!");

        double profitLoss = getProfitLoss(stockSymbol);

        HBox hBox = getValueLabel(profitLoss);
        setDialogPaneContent(alert, hBox);

        alert.setTitle("CAUTION");
        return alert;
    }

    private double getProfitLoss(String stockSymbol) {
        User user = UserSingleton.getInstance();
        double investedInStock = ownedStockService.getInvestedInStock(user, stockSymbol);
        double ownedShares = ownedStockService.getSharesFromCertainStock(user, stockSymbol);
        double amountToSell = price * ownedShares;
        double percentage = ownedStockService.getPercentage(investedInStock, price, ownedShares);
        return Math.round((amountToSell - (amountToSell / percentage)) * 100.0) / 100.0;
    }

    private void setDialogPaneContent(Alert alert, HBox hBox) {
        Label text = new Label("Are you sure that you want to sell all '" + stockLabel.getText() + "' shares?\n");
        text.setStyle("-fx-text-fill: white");
        VBox vBox = new VBox(text, hBox);
        vBox.setAlignment(Pos.CENTER_LEFT);
        alert.getDialogPane().setContent(vBox);
    }

    private HBox getValueLabel(double value) {
        Label textLabel = new Label("You are selling for a ");
        textLabel.setStyle("-fx-text-fill: white");
        String valueText = value < 0 ? "loss of: " + value + "$" : "profit of: " + value + "$";
        Label valueLabel = new Label(valueText);
        valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        return new HBox(textLabel, valueLabel);
    }
}
