package app.wealthmetamorphosis.logic;

import app.wealthmetamorphosis.data.*;
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
    private final TextField sharesTextField;

    public TradingService(Checker checker, User user, Label priceLabel, Label stockLabel, TextField sharesTextField) {
        this.checker = checker;
        this.user = user;
        this.priceLabel = priceLabel;
        this.stockLabel = stockLabel;
        this.sharesTextField = sharesTextField;
    }

    public void placeOrder(OrderType orderType) {
        Order newOrder = new Order(UUID.randomUUID().toString(), stockLabel.getText(), Double.parseDouble(priceLabel.getText().substring(0, priceLabel.getText().length() - 1)),
                Double.parseDouble(sharesTextField.getText()), orderType, LocalDateTime.now(), user.getUserId());

        DBConnection dbConnection = new DBConnection("jdbc:mysql://localhost/wealthMetamorphosis", "root", "password");
        DBInserter inserter = new DBInserter(dbConnection);
        inserter.insertIntoOrders(newOrder);
        user.getOrders().add(newOrder);
    }
}
