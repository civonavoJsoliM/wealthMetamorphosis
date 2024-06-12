package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.data.*;
import app.wealthmetamorphosis.data.singleton.DBConnectionSingleton;
import app.wealthmetamorphosis.logic.Checker;
import app.wealthmetamorphosis.logic.db.DBInserter;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.LocalDateTime;
import java.util.UUID;

public class TradingService {
    private final Checker checker;
    private final User user;
    private final Label priceLabel;
    private final Label stockLabel;
    private final TextField buySharesTextField;
    private final TextField sellSharesTextField;

    public TradingService(Checker checker, User user, Label priceLabel, Label stockLabel, TextField buySharesTextField, TextField sellSharesTextField) {
        this.checker = checker;
        this.user = user;
        this.priceLabel = priceLabel;
        this.stockLabel = stockLabel;
        this.buySharesTextField = buySharesTextField;
        this.sellSharesTextField = sellSharesTextField;
    }

    public void placeOrder(OrderType orderType) {
        Order newOrder = orderType.equals(OrderType.BUY) ?
                new Order(UUID.randomUUID().toString(), stockLabel.getText(), Double.parseDouble(
                        priceLabel.getText().substring(0, priceLabel.getText().length() - 1)
                ),
                Double.parseDouble(buySharesTextField.getText()), orderType, LocalDateTime.now(), user.getUserId()) :

                new Order(UUID.randomUUID().toString(), stockLabel.getText(), Double.parseDouble(
                        priceLabel.getText().substring(0, priceLabel.getText().length() - 1)
                ),
                Double.parseDouble(sellSharesTextField.getText()), orderType, LocalDateTime.now(), user.getUserId());

        DBInserter inserter = new DBInserter(DBConnectionSingleton.getInstance());
        inserter.insertIntoOrders(newOrder);
        user.getOrders().add(newOrder);
    }
}
