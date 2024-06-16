package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.data.*;
import app.wealthmetamorphosis.data.singleton.DBConnectionSingleton;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.db.DBInserter;
import app.wealthmetamorphosis.logic.db.DBUpdater;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class TradingService {
    private final Label priceLabel;
    private final Label stockLabel;
    private final TextField buySharesTextField;
    private final TextField sellSharesTextField;

    public TradingService(Label priceLabel, Label stockLabel, TextField buySharesTextField, TextField sellSharesTextField) {
        this.priceLabel = priceLabel;
        this.stockLabel = stockLabel;
        this.buySharesTextField = buySharesTextField;
        this.sellSharesTextField = sellSharesTextField;
    }

    /*public void placeOrder(OrderType orderType) {
        Order newOrder;
        double price = Double.parseDouble(priceLabel.getText().substring(0, priceLabel.getText().length() - 1));
        double shares;
        if (orderType.equals(OrderType.BUY)) {
            shares = Double.parseDouble(buySharesTextField.getText());
            newOrder = getNewOrder(buySharesTextField, orderType, price);
            setNewBalance(-(price * shares));
        } else {
            shares = Double.parseDouble(sellSharesTextField.getText());
            newOrder = getNewOrder(sellSharesTextField, orderType, price);
            setNewBalance(price * shares);
        }
        DBInserter inserter = new DBInserter(DBConnectionSingleton.getInstance());
        inserter.insertIntoOrders(newOrder);
        UserSingleton.getInstance().getOrders().add(newOrder);

        DBUpdater dbUpdater = new DBUpdater(DBConnectionSingleton.getInstance());
        dbUpdater.updateBalance();
    } */

    public void placeBuyOrder() {
        Order newOrder;
        double price = Double.parseDouble(priceLabel.getText().substring(0, priceLabel.getText().length() - 1));
        double shares = Double.parseDouble(buySharesTextField.getText());
        newOrder = getNewOrder(Double.parseDouble(buySharesTextField.getText()), OrderType.BUY, price);
        setNewBalance(-(price * shares));
        DBInserter inserter = new DBInserter();
        inserter.insertIntoOrders(newOrder);
        /*if (UserSingleton.getInstance().getOrders() == null) {
            UserSingleton.getInstance().setOrders(new ArrayList<>());
        } */
        UserSingleton.getInstance().getOrders().add(newOrder);

        DBUpdater dbUpdater = new DBUpdater(DBConnectionSingleton.getInstance());
        dbUpdater.updateBalance();
    }

    public void placeSellOrder(TextField sellSharesTextField) {
        Order newOrder;
        double price = Double.parseDouble(priceLabel.getText().substring(0, priceLabel.getText().length() - 1));
        double shares = Double.parseDouble(sellSharesTextField.getText());
        newOrder = getNewOrder(Double.parseDouble(sellSharesTextField.getText()), OrderType.SELL, price);
        setNewBalance(price * shares);
        DBInserter inserter = new DBInserter();
        inserter.insertIntoOrders(newOrder);

        /*if (UserSingleton.getInstance().getOrders() == null) {
            UserSingleton.getInstance().setOrders(new ArrayList<>());
        } */
        UserSingleton.getInstance().getOrders().add(newOrder);

        DBUpdater dbUpdater = new DBUpdater(DBConnectionSingleton.getInstance());
        dbUpdater.updateBalance();
    }

    public void placeSellOrder() {
        Order newOrder;
        double price = Double.parseDouble(priceLabel.getText().substring(0, priceLabel.getText().length() - 1));
        double shares = UserSingleton.getInstance().getOrders().stream()
                .filter(order -> order.getStockSymbol().equals(stockLabel.getText()))
                .mapToDouble(Order::getStockShares)
                .sum();
        newOrder = getNewOrder(shares, OrderType.SELL, price);
        setNewBalance(price * shares);
        DBInserter inserter = new DBInserter();
        inserter.insertIntoOrders(newOrder);

        /*if (UserSingleton.getInstance().getOrders() == null) {
            UserSingleton.getInstance().setOrders(new ArrayList<>());
        } */
        UserSingleton.getInstance().getOrders().add(newOrder);

        DBUpdater dbUpdater = new DBUpdater(DBConnectionSingleton.getInstance());
        dbUpdater.updateBalance();
    }

    private Order getNewOrder(double shares, OrderType orderType, double price) {
        return new Order(UUID.randomUUID().toString(), stockLabel.getText(), price,
                shares, orderType, LocalDateTime.now(), UserSingleton.getInstance().getUserId());
    }

    private void setNewBalance(double value) {
        UserSingleton.getInstance().setBalance(UserSingleton.getInstance().getBalance() + value);
    }
}
